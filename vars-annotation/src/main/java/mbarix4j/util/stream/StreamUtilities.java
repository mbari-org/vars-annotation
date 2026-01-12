package mbarix4j.util.stream;

import java.util.Enumeration;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class StreamUtilities {

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