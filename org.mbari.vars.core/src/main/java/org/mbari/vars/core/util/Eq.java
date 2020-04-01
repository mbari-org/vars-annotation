package org.mbari.vars.core.util;

@FunctionalInterface
public interface Eq<A> {

    boolean isEqual(A a, A b);

}
