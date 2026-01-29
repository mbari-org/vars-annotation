package org.mbari.vars.annotation.etc.jdk;

import java.net.URL;
import java.util.Optional;
import java.util.Random;
import java.util.stream.IntStream;

/**
 * @author Brian Schlining
 * @since 2017-06-29T08:56:00
 */
public class Strings {

    private static final String chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final Random random = new Random();

    public static boolean isBlank(final CharSequence cs) {
        int strLen;
        if (cs == null || (strLen = cs.length()) == 0) {
            return true;
        }
        for (int i = 0; i < strLen; i++) {
            if (!Character.isWhitespace(cs.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    public static boolean containsOrderedChars(final String chars, final String text) {
        if (chars == null || text == null ) {
            return false;
        }
        int idx = text.indexOf(chars.charAt(0));
        if (idx >= 0 && chars.length() == 1) {
            return true;
        }
        else if (idx >= 0) {
            return containsOrderedChars(chars.substring(1), text.substring(idx + 1));
        }
        else {
            return false;
        }

    }

    public static Optional<URL> asUrl(final String s) {
        try {
            return Optional.of(new URL(s));
        }
        catch (Exception e) {
            return Optional.empty();
        }
    }

    public static String random(int length) {
        var xs = IntStream.range(0, length)
                .mapToObj(i -> chars.charAt(random.nextInt(chars.length())))
                .collect(StringBuilder::new, StringBuilder::append, StringBuilder::append);
        return xs.toString();
    }
}
