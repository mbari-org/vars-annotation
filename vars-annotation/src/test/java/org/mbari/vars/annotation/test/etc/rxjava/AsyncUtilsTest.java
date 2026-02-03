package org.mbari.vars.annotation.test.etc.rxjava;

import io.reactivex.rxjava3.core.Observable;
import org.junit.jupiter.api.Test;
import org.mbari.vars.annotation.etc.rxjava.AsyncUtils;


import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Brian Schlining
 * @since 2018-10-16T10:12:00
 */
public class AsyncUtilsTest {

    int multipier = 20;

    @Test
    public void observeAllTest01() {
        Collection<Integer> items = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            items.add(i);
        }

        System.out.println("Processing " + items.size() + " items");

        Observable<?> observable = AsyncUtils.observeAll(items, i ->
                CompletableFuture.supplyAsync(() -> {
                    try {
                        System.out.println("Future: Processing " + i);
                        Thread.sleep(i * multipier);
                        return "Processed " + i;
                    } catch (Exception e) {
                        return "";
                    }
                }));

        AtomicInteger n = new AtomicInteger(0);

        observable.subscribe(s -> {
                        n.addAndGet(1);
                        System.out.println("Observable: " + s);
                    },
                e -> fail("An error occurred: " + e.getCause()),
                () -> System.out.println("Completed " + n.get()));

        try {
            int timeout = items.stream().mapToInt(i -> i * multipier).sum();
            Thread.sleep(timeout);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        assertEquals(items.size(), n.get());

    }
}
