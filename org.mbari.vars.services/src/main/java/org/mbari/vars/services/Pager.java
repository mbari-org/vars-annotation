package org.mbari.vars.services;

import io.reactivex.Observable;

import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Class that hides the gory details of doing a pager request. The usage is:
 * <pre>
 *     var fn = (limit, offset) -> remoteCall(limit, offset);
 *     var pager = new Pager(fn, numberOfItemsToFetch, pageSize)
 *     var observable = pager.getObservable();
 *     observable.onNext(i -> System.out.println(i))
 *     pager.run();
 * </pre>
 * @param <T>
 */
public class Pager<T> implements Runnable {

    private final RequestPager.Runner<T> runner;

    /**
     *
     * @param fetchFn Takes limit, offset as args and returns a value or collection
     *                of values from a page
     * @param limit The maximum number of objects to retrieve
     * @param pageSize The number of objects to retrieve per page request
     */
    public Pager(BiFunction<Long, Long, T> fetchFn, Long limit, Long pageSize) {

        Function<RequestPager.Page, T> fn = (page) -> {
            try {
                return fetchFn.apply(page.getLimit(), page.getOffset());
            }
            catch (Exception e) {
                Long start = page.getOffset();
                Long end = start + page.getLimit();
                throw new RuntimeException("Page request from " + start +
                        " + to " + end + " failed");
            }
        };
        RequestPager<T> pager = new RequestPager<>(fn, 2, 2);
        runner = pager.build(limit.intValue(), pageSize.intValue());
    }

    public void run() {
        runner.run();
    }

    public Observable<T> getObservable() {
        return runner.getObservable();
    }


}
