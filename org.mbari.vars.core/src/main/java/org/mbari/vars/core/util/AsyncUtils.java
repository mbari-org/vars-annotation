package org.mbari.vars.core.util;

import io.reactivex.Observable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

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
        return Observable.fromFuture(future);
    }

    /**
     * For each item in a collection, apply a function that returns a future.
     * As the futures complete, the output will be published to observer returned
     * by this function. Note that the returned observable will not emit
     * results until subscribed to (i.e a cold observerable). So even if the
     * future completes before you subscribe to it you will be able to get
     * results
     *
     * @param items The items to process
     * @param fn The function to apply to each item in `items`
     * @param <T> The type of the item
     * @param <R> The type returned when `fn` completes
     * @return An observable that will
     */
    public static <T, R> Observable<R> observeAll(Collection<T> items,
                                                  Function<T, CompletableFuture<R>> fn) {

        // Apply the function to each item
        List<CompletableFuture<R>> futures = items.stream()
                .map(fn)
                .collect(Collectors.toList());

        // Return a cold observable!
        return Observable.defer(() -> {
            List<Observable<R>> observables = futures.stream()
                    .map(AsyncUtils::observe)
                    .collect(Collectors.toList());
            return Observable.concat(observables);
        });

    }

    /**
     * Apply a function that converts an item in a collection to a future. Return
     * a future that completes when all futures on the items complete.
     * @param items The items to process
     * @param fn The function to apply to each item
     * @param <T> The type of the items
     * @return A future that completes when all items futures have completed.
     */
    public static <T> CompletableFuture<Void> completeAll(Collection<T> items,
                                                          Function<T, CompletableFuture> fn) {
        CompletableFuture[] futures = items.stream()
                .map(fn)
                .toArray(CompletableFuture[]::new);

        return CompletableFuture.allOf(futures);
    }

    /**
     * Apply a function that converts an item in a collection to a future that
     * returns a value when completed. This method will collect all the
     * results and return them as a collection when all futures are completed
     * @param items
     * @param fn
     * @param <T>
     * @param <R>
     * @return
     */
    public static <T, R> CompletableFuture<Collection<R>> collectAll(Collection<T> items,
                                                                   Function<T, CompletableFuture<R>> fn) {
        CopyOnWriteArrayList<R> returnValues = new CopyOnWriteArrayList<>();

        CompletableFuture[] futures = items.stream()
                .map(fn)
                .map(r -> r.thenAccept(returnValues::add))
                .toArray(CompletableFuture[]::new);

        return CompletableFuture.allOf(futures)
                .thenApply(v -> returnValues);

    }


}
