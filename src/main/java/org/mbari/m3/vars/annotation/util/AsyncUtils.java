package org.mbari.m3.vars.annotation.util;

import io.reactivex.Observable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * @author Brian Schlining
 * @since 2017-06-27T09:31:00
 */
public class AsyncUtils {

    private static final Logger log = LoggerFactory.getLogger(AsyncUtils.class);

    public static <T> Optional<T> await(CompletableFuture<T> f, Duration timeout) {
        Optional<T> r;
        try {
            r = Optional.ofNullable(f.get(timeout.toMillis(), TimeUnit.MILLISECONDS));
        }
        catch (Exception e) {
            log.info("An exception was thrown when waiting for a future to complete", e);
            r = Optional.empty();
        }
        return r;
    }

    /**
     * Convert a CompletableFuture to an rx Observable. This observable will
     * emit exactly one item.
     *
     * @param future The future to convert
     * @param <T> The return type of the future
     * @return An rx java Observable
     */
    public static <T> Observable<T> observe(CompletableFuture<T> future) {
        return Observable.create(subscriber -> {
            future.whenComplete((value, exception) -> {
                if (exception != null) {
                    subscriber.onError(exception);
                }
                else {
                    subscriber.onNext(value);
                    subscriber.onComplete();
                }
            });
        });
    }

}
