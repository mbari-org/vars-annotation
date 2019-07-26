package org.mbari.vars.core.util;

/**
 * @author Brian Schlining
 * @since 2017-06-29T08:56:00
 */
public class StringUtils {

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
}
