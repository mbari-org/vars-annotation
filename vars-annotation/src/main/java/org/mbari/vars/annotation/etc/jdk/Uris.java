package org.mbari.vars.annotation.etc.jdk;

import java.nio.charset.StandardCharsets;

public class Uris {

    public static String encodeURIComponent(String s) {

        return java.net
                .URLEncoder
                .encode(s, StandardCharsets.UTF_8)
                .replace("+", "%20")
                .replace("*", "%2A")
                .replace("%7E", "~");
    }

}
