package org.mbari.m3.vars.annotation.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Functional utils.
 * @author Brian Schlining
 * @since 2017-09-26T15:30:00
 */
public class FnUtils {

    /**
     * Attempt to emulate Scala collections distinct by function
     * Usage:
     *   <pre>
     *       persons.stream().filter(distinctBy(p -> p.getName());
     *   </pre>
     * @param keyExtractor
     * @param <T>
     * @return
     */
    public static <T> Predicate<T> distinctBy(Function<? super T, ?> keyExtractor) {
        Map<Object,Boolean> seen = new ConcurrentHashMap<>();
        return t -> seen.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
    }
}
