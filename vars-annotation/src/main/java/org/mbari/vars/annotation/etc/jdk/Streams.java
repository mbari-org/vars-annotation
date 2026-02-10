package org.mbari.vars.annotation.etc.jdk;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class Streams {

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

    /**
     * Transform an Iterator to a Stream
     * @param sourceIterator
     * @param <T>
     * @return A sequential stream
     */
    public static <T> Stream<T> toStream(Iterator<T> sourceIterator) {
        return toStream(sourceIterator, false);
    }

    /**
     * Transform an Iterator to a Stream
     * @param sourceIterator
     * @param parallel true to create a parallel stream, false for sequential stream
     * @param <T>
     * @return A stream
     */
    public static <T> Stream<T> toStream(Iterator<T> sourceIterator, boolean parallel) {
        Iterable<T> iterable = () -> sourceIterator;
        return StreamSupport.stream(iterable.spliterator(), parallel);
    }

    /**
     * Transform an Enumeration to a Stream
     * @param e
     * @param <T>
     * @return A sequential stream
     */
    public static <T> Stream<T> toStream(Enumeration<T> e) {
        return StreamSupport.stream(
                Spliterators.spliteratorUnknownSize(
                        new Iterator<T>() {
                            public T next() {
                                return e.nextElement();
                            }

                            public boolean hasNext() {
                                return e.hasMoreElements();
                            }
                        },
                        Spliterator.ORDERED), false);
    }

}