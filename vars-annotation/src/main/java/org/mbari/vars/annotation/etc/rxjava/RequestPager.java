package org.mbari.vars.annotation.etc.rxjava;


import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.subjects.PublishSubject;
import io.reactivex.rxjava3.subjects.Subject;
import org.mbari.vcr4j.util.Preconditions;
import org.mbari.vars.annotation.etc.jdk.Loggers;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * This class automates paing requests for you. The usage is:
 * <pre>
 *  // A function that accepts a page (limit, offset) and fetches data using those
 *  Function<RequestPager.Page, List<Integer> fn = (page) -> //some data list
 *
 *  // Instantiate pager with function, number of retries on a fn fail, and number of fetch threads
 *  RequestPager<List<Integer>> pager = new RequestPager<>(fn, 2, 2)
 *
 *  int count = 1000; // The expected number of returns
 *  int pageSize = 50; // The number of items requested per page
 *  RequestPager.Runner<List<Annotation>> runner = pager.build(count, pageSize);
 *
 *  // Subscribe to the observable to handle page returns
 *  Observable<List<Integer>> observable = runner.getObservable();
 *  observable.subscribeOn(Schedulers.io())
 *      .subscribe(xs -> System.out.println("Got a page of " + xs.size()),
 *          e -> System.err.println("Got an error"),
 *          () -> System.out.println("All done"));
 *
 *   // Start the fetch
 *   runner.run();
 *
 * </pre>
 *
 * @author Brian Schlining
 * @since 2019-04-24T14:28:00
 */
public class RequestPager<B> {

    public static class Page {
        private final long limit;
        private final long offset;

        public Page(long limit, long offset) {
            this.limit = limit;
            this.offset = offset;
        }

        public long getLimit() {
            return limit;
        }

        public long getOffset() {
            return offset;
        }
    }

    public static class Runner<B> implements Runnable {
        private boolean hasRun = false;
        private final Subject<B> observable;
        private final ExecutorService executor;
        private final int numberSimultaneous;
        private final BlockingQueue<RequestWithRetry<B>> queue;
        private final int expectedCount;
        private final AtomicInteger completedCount = new AtomicInteger(0);

        public Runner(List<RequestWithRetry<B>> requests,
                      int numberSimultaneous) {
            Preconditions.checkArgument(numberSimultaneous > 0, "Number of threads is less than 1");
            Subject<B> obs = PublishSubject.create();
            this.observable = obs.toSerialized();
            queue = new LinkedBlockingQueue<>(requests);
            expectedCount = requests.size();
            this.numberSimultaneous = numberSimultaneous;
            this.executor = Executors.newFixedThreadPool(numberSimultaneous);
        }

        @Override
        public void run() {
            if (!hasRun) {
                hasRun = true;
                int n = Math.min(numberSimultaneous, queue.size());
                for (int i = 0; i < n; i++) {
                    next();
                }
            }
        }

        private void execute(RequestWithRetry<B> request) {
            Runnable runnable = () -> request.get()
                    .subscribe(observable::onNext,
                            this::doError,
                            this::doCompleted);
            executor.execute(runnable);
        }

        private void doError(Throwable e) {
            List<Runnable> rs = executor.shutdownNow();
            int unfinished = rs.size() + queue.size();
            new Loggers(getClass()).atError()
                    .log("Page requests failed. " + unfinished +
                            " pages were not fetched");
            observable.onError(e);
        }

        private void doCompleted() {
            int n = completedCount.incrementAndGet();
            if (n == expectedCount) {
                observable.onComplete();
            }
            else {
                if (!queue.isEmpty()) {
                    next();
                }
            }
        }

        private void next() {
            if (!queue.isEmpty()) {
                try {
                    RequestWithRetry<B> request = queue.poll(100, TimeUnit.MILLISECONDS);
                    execute(request);
                }
                catch (InterruptedException e) {
                    doError(e);
                }
            }
        }

        public Observable<B> getObservable() {
            return observable;
        }
    }


    private final Function<Page, B> function;
    private final int retries;
    private final int threadCount;

    public RequestPager(Function<Page, B> function) {
        this(function, 0);
    }

    public RequestPager(Function<Page, B> function, int retries) {
        this(function, retries, 1);
    }

    public RequestPager(Function<Page, B> function, int retries, int threadCount) {
        Preconditions.checkArgument(threadCount > 0, "The min");
        this.function = function;
        this.retries = retries;
        this.threadCount = threadCount;
    }

    public Runner<B> build(int totalCount, int pageSize) {
        List<RequestWithRetry<B>> requests = buildPageRequests(totalCount, pageSize);
        return new Runner<>(requests, threadCount);
    }

    private List<RequestWithRetry<B>> buildPageRequests(int totalCount, int pageSize) {
        int n = (int) Math.ceil(totalCount / (double) pageSize);
        long limit = pageSize;
        List<RequestWithRetry<B>> requests = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            long offset = i * pageSize;
            Supplier<B> supplier = () -> function.apply(new Page(limit, offset));
            RequestWithRetry<B> request = new RequestWithRetry<>(supplier, retries);
            requests.add(request);
        }
        return requests;
    }

}
