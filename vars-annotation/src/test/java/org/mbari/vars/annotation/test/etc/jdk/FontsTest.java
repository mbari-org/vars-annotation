package org.mbari.vars.annotation.test.etc.jdk;

import org.junit.jupiter.api.Test;
import org.mbari.vars.annotation.etc.jdk.Fonts;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class FontsTest {

    @Test
    public void candidatesArePlatformSpecificAndEndWithFallback() {
        var mac = Fonts.monospacedCandidates("Mac OS X");
        assertEquals("SF Mono", mac.getFirst());
        assertEquals(Fonts.FALLBACK_MONOSPACED, mac.getLast());

        var win = Fonts.monospacedCandidates("Windows 11");
        assertEquals("Consolas", win.getFirst());
        assertEquals(Fonts.FALLBACK_MONOSPACED, win.getLast());

        var linux = Fonts.monospacedCandidates("Linux");
        assertEquals("DejaVu Sans Mono", linux.getFirst());
        assertEquals(Fonts.FALLBACK_MONOSPACED, linux.getLast());

        assertEquals(Fonts.FALLBACK_MONOSPACED, Fonts.monospacedCandidates(null).getLast());
    }

    @Test
    public void firstAvailableUsesPreferenceOrderIgnoringCase() {
        var candidates = Fonts.monospacedCandidates("Mac OS X");
        assertEquals("SF Mono", Fonts.firstAvailable(candidates, List.of("Menlo", "sf mono", "Monaco")));
        assertEquals("Menlo", Fonts.firstAvailable(candidates, List.of("Menlo", "Monaco")));
        assertEquals("Monaco", Fonts.firstAvailable(candidates, List.of("Times", "Monaco")));
    }

    @Test
    public void fallsBackWhenNothingPreferredIsInstalled() {
        assertEquals(Fonts.FALLBACK_MONOSPACED,
                Fonts.firstAvailable(Fonts.monospacedCandidates("Windows 11"), List.of("Times", "Arial")));
        assertEquals(Fonts.FALLBACK_MONOSPACED, Fonts.firstAvailable(List.of("Nope"), List.of()));
    }
}
