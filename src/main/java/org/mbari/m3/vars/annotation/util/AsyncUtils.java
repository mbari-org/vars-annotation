package org.mbari.m3.vars.annotation.util;

import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

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

    /**
     * For each item in a collection, apply a function that returns a future.
     * As the futures complete, the output will be published to observer returned
     * by this function.
     *
     * @param items The items to process
     * @param fn The function to apply to each item in `items`
     * @param <T> The type of the item
     * @param <R> The type returned when `fn` completes
     * @return An observable that will
     */
    public static <T, R> Observable<R> observeAll(Collection<T> items,
                                                  Function<T, CompletableFuture<R>> fn) {

        PublishSubject<R> subject = PublishSubject.create();

        CompletableFuture[] futures = items.stream()
                .map(fn)                            // Apply function: item -> future
                .map(AsyncUtils::observe)           // Convert future to observable
                .map(obs -> obs.subscribe(subject::onNext, subject::onError)) // Pipe results to subject
                .toArray(CompletableFuture[]::new); // Convert to array for `allOf` consumption

        // Complete the observer when all futures are finished or an error occurs
        CompletableFuture.allOf(futures).whenComplete((v, ex) -> {
            if (ex != null) {
                subject.onError(ex);
            }
            else {
                subject.onComplete();
            }
        });

        return subject;

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
