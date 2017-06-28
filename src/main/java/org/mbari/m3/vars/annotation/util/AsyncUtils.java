package org.mbari.m3.vars.annotation.util;

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
}
