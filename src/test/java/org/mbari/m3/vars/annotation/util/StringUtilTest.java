package org.mbari.m3.vars.annotation.util;

import org.junit.Test;
import static org.junit.Assert.*;
import static org.mbari.m3.vars.annotation.util.StringUtils.containsOrderedChars;

/**
 * @author Brian Schlining
 * @since 2017-06-29T09:32:00
 */
public class StringUtilTest {

    @Test
    public void testContainsCharsInOrder() {
        assertTrue(containsOrderedChars("abc", "abc"));
        assertTrue(containsOrderedChars("abc", "fooafoobfooc"));
        assertFalse(containsOrderedChars("abc", "defg"));
        assertFalse(containsOrderedChars("abc", "abbbbd"));
        assertFalse(containsOrderedChars("abc", "bc"));
    }
}
