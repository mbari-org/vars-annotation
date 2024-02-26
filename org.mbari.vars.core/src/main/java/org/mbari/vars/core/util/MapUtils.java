package org.mbari.vars.core.util;

import java.util.Map;

public class MapUtils {

    /**
     * Create a map from a list of key-value pairs. This is a convenience method to
     * create a map from a list of arguments. The arguments must be in pairs. The
     * first argument in the pair is the key, the second is the value. If the key or
     * value is null, it is ignored.
     *
     * @param args
     * @return
     */
    public static Map<String, Object> of(Object... args) {
        if (args.length % 2 != 0) {
            throw new IllegalArgumentException("You must provide an even number of arguments");
        }
        var map = new java.util.HashMap<String, Object>();
        for (int i = 0; i < args.length; i += 2) {
            var key = args[i];
            var value = args[i + 1];
            if (key != null && value != null) {
                map.put(key.toString(), value);
            }
        }
        return map;
    }
}
