package org.mbari.vars.core.util;

public class Requirements {

    public static void checkNotNull(Object obj) {
        if (obj == null) throw new RuntimeException("null is not allowed");
    }

    public static void checkNotNull(Object obj, String msg) {
        if (obj == null) throw new RuntimeException(msg);
    }

    public static void validate(boolean ok, String msg) {
        if (!ok) throw new RuntimeException(msg);
    }
}
