package org.mbari.vars.core.util;

import java.nio.charset.StandardCharsets;

public class UriUtils {

    public static String encodeURIComponent(String s) {

        return java.net
                .URLEncoder
                .encode(s, StandardCharsets.UTF_8)
                .replace("+", "%20")
                .replace("*", "%2A")
                .replace("%7E", "~");
    }

}
