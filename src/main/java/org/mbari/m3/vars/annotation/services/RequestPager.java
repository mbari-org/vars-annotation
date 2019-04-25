package org.mbari.m3.vars.annotation.services;

import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;
import org.mbari.vcr4j.util.Preconditions;

import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.function.Function;
import java.util.function.Supplier;

/**
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
        private final Subject<B> observable = PublishSubject.create();
        private final ExecutorService executor;
        private final Deque<RequestWithRetry<B>> queue;


        public Runner(List<RequestWithRetry<B>> requests, int numberSimultaneous) {
            queue = new LinkedBlockingDeque<>(requests);
            executor = Executors.newFixedThreadPool(numberSimultaneous);
        }

        @Override
        public void run() {
            if (!hasRun) {
                hasRun = true;
                // TODO Run queue


            }

        }

        public Subject<B> getObservable() {
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

    public Observable<B> apply(int totalCount, int pageSize) {
        List<RequestWithRetry<B>> requests = buildPageRequests(totalCount, pageSize);

    }

    private List<RequestWithRetry<B>> buildPageRequests(int totalCount, int pageSize) {
        int n = (int) Math.ceil(totalCount / pageSize);
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

    private Observable<B> runPageRequests(List<RequestWithRetry<B>> requests) {
        Subject<B> subject = PublishSubject.create();



    }






}
