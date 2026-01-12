package mbarix4j.util;

/**
 * @author Brian Schlining
 * @since Sep 27, 2010
 */
public class Tuple2<A, B> {

    private final A a;
    private final B b;

    /**
     * Constructs ...
     *
     * @param a
     * @param b
     */
    public Tuple2(A a, B b) {
        this.a = a;
        this.b = b;
    }

    /**
     * @return
     */
    public A getA() {
        return a;
    }

    /**
     * @return
     */
    public B getB() {
        return b;
    }
}
