package org.mbari.m3.vars.annotation.services;

import io.reactivex.Observable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Supplier;

/**
 * Wrapper around a Supplier (intended to be making remote service request),
 * that will attempt to retry a request if an exception occurs.
 *
 * @author Brian Schlining
 * @since 2019-04-24T13:54:00
 */
public class RequestWithRetry<T> implements Supplier<Observable<T>> {

    private final Supplier<T> supplier;
    private final int retries;

    public RequestWithRetry(Supplier<T> supplier) {
        this(supplier, 0);
    }

    /**
     *
     * @param supplier Our request function. If a null is returned it is treated
     *                 as a failed attempt.
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
        return Observable.defer(() -> Observable.just(execute(retries)));
    }

    private T execute(int remainingRetries) {
        try {
            T v =  supplier.get();
            if (v == null) {
                throw new NullPointerException("Supplier in RequestWithRetry return null.  This is not allowed");
            }
            return v;
        } catch (Exception e) {
            Logger log = LoggerFactory.getLogger(getClass());
            if (remainingRetries == 0) {
                int attempts = retries + 1;
                String msg = "Execution failed after " + attempts + " attempts. Terminating Request";
                log.warn(msg, e);
                throw new RuntimeException(msg, e);
            }
            else {
                int attempts = retries - remainingRetries + 1;
                int allowedAttempts = retries + 1;
                log.warn("Execution failed. Retrying (" + attempts +
                        " of " + allowedAttempts + ")", e);
                return execute(remainingRetries - 1);
            }
        }
    }


}
