package org.mbari.vars.annotation.test.etc.jdk;


import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mbari.vars.annotation.etc.jdk.Strings.*;

/**
 * @author Brian Schlining
 * @since 2017-06-29T09:32:00
 */
public class StringsTest {

    @Test
    public void testContainsCharsInOrder() {
        assertTrue(containsOrderedChars("abc", "abc"));
        assertTrue(containsOrderedChars("abc", "fooafoobfooc"));
        assertFalse(containsOrderedChars("abc", "defg"));
        assertFalse(containsOrderedChars("abc", "abbbbd"));
        assertFalse(containsOrderedChars("abc", "bc"));
    }

    @Test
    public void testIsBlank() {
        assertTrue(isBlank(" "));
        assertFalse(isBlank("Abc"));
        assertFalse(isBlank("Abc ed"));
    }
}
