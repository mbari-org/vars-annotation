package org.mbari.m3.vars.annotation.services;

import io.reactivex.Observable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Supplier;

/**
 * Wrapper around a Supplier (intended to be making remote service request),
 * that will attempt to retry a request if an exception/failure occurs.
 *
 * @author Brian Schlining
 * @since 2019-04-24T13:54:00
 */
public class RequestWithRetry<T> implements Supplier<Observable<T>> {

    final Supplier<T> supplier;
    final int retries;
    private Logger log  = LoggerFactory.getLogger(getClass());

    public RequestWithRetry(Supplier<T> supplier) {
        this(supplier, 0);
    }

    /**
     *
     * @param supplier Our request function
     * @param retries The number of retries to attempt.
     */
    public RequestWithRetry(Supplier<T> supplier, int retries) {
        this.supplier = supplier;
        this.retries = retries;
    }

    /**
     * Execute the request; this simply calls supplier.get() to trigger the
     * request. The supplier can deal with timeouts itself.
     * @return An observable that will emit the contents of the supplier.
     *  If the supplier fails and the number of retries is met, then the
     *  observable will complete with the last error thrown by the supplier.
     */
    public Observable<T> get() {
        return Observable.defer(() -> Observable.just(execute()));
    }

    private T execute() {
        T v = null;
        boolean keepGoing = true;
        int attempt = 0;
        while (keepGoing) {
            try {
                v = supplier.get();
                keepGoing = false;
            } catch (Exception e) {
                keepGoing = attempt < retries;
                if (!keepGoing) {
                    throw new RuntimeException(e);
                }
                else {
                    attempt++;
                }
            }
        }

        if (v == null) {
            throw new NullPointerException("Request returned null. This is not allowed");
        }

        return v;
    }

}
