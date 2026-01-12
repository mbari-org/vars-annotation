package org.mbari.vars.core.util;

public class Preconditions {

    public static void checkNotNull(Object obj) {
        if (obj == null) throw new IllegalArgumentException("null is not allowed");
    }

    public static void checkNotNull(Object obj, String msg) {
        if (obj == null) throw new IllegalArgumentException(msg);
    }

    public static void checkArgument(boolean ok, String msg) {
        if (!ok) throw new IllegalArgumentException(msg);
    }
}
