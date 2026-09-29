package org.mbari.vars.annotation.etc.jdk;

import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Platform specific font lookup.
 * <p>
 * JavaFX CSS only honors the first family in a <code>-fx-font-family</code> list (there is no
 * fallback), so a font that isn't installed silently becomes the proportional system font. This
 * class picks the first font from a per-platform preference list that is actually installed, and
 * falls back to the cross platform logical font <code>Monospaced</code>.
 * <p>
 * Font lookup needs the JavaFX toolkit to be running, so call it from the FX thread (or after the
 * toolkit has started).
 */
public final class Fonts {

    /** Logical font that JavaFX/Java provide on every platform. */
    public static final String FALLBACK_MONOSPACED = "Monospaced";

    private static volatile String monospacedFamily;

    private Fonts() {
        // no instantiation
    }

    /**
     * @param osName The value of the <code>os.name</code> system property
     * @return Preferred monospaced font families for the platform, best first. Always ends with
     *         {@link #FALLBACK_MONOSPACED}.
     */
    public static List<String> monospacedCandidates(String osName) {
        var os = osName == null ? "" : osName.toLowerCase(Locale.ROOT);
        List<String> preferred;
        if (os.contains("mac") || os.contains("darwin")) {
            preferred = List.of("SF Mono", "Menlo", "Monaco");
        }
        else if (os.contains("win")) {
            preferred = List.of("Consolas", "Cascadia Mono", "Courier New");
        }
        else {
            preferred = List.of("DejaVu Sans Mono", "Liberation Mono", "Ubuntu Mono", "Noto Sans Mono");
        }
        return java.util.stream.Stream.concat(preferred.stream(), java.util.stream.Stream.of(FALLBACK_MONOSPACED))
                .toList();
    }

    /**
     * @param candidates Font families in order of preference
     * @param available The font families that are installed. Compared ignoring case.
     * @return The first candidate that is available, or {@link #FALLBACK_MONOSPACED} if none are
     */
    public static String firstAvailable(List<String> candidates, Collection<String> available) {
        Set<String> installed = available.stream()
                .map(s -> s.toLowerCase(Locale.ROOT))
                .collect(Collectors.toSet());
        return candidates.stream()
                .filter(c -> installed.contains(c.toLowerCase(Locale.ROOT)))
                .findFirst()
                .orElse(FALLBACK_MONOSPACED);
    }

    /**
     * @return The name of the best available monospaced font family on this machine. "SF Mono" on a
     *         Mac (if installed, otherwise Menlo/Monaco), "Consolas" on Windows, DejaVu Sans Mono
     *         (or similar) on Linux, or "Monospaced" if none of those can be found.
     */
    public static String monospacedFamily() {
        var family = monospacedFamily;
        if (family == null) {
            try {
                family = firstAvailable(monospacedCandidates(System.getProperty("os.name")), Font.getFamilies());
                monospacedFamily = family; // Only cache a real answer
            }
            catch (RuntimeException e) {
                // e.g. the toolkit isn't up yet. Use the cross platform font, but don't remember it.
                family = FALLBACK_MONOSPACED;
            }
        }
        return family;
    }

    public static Font monospaced(double size) {
        return Font.font(monospacedFamily(), size);
    }

    public static Font monospaced(FontWeight weight, double size) {
        return Font.font(monospacedFamily(), weight, size);
    }
}
