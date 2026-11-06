package org.mbari.vars.annotation;

import java.io.IOException;
import java.util.Optional;
import java.util.Properties;

/**
 * The application's version, read from {@code version.properties}, which the build
 * generates from the most recent git tag (see {@code generateVersionInfo} in
 * build.gradle.kts).
 */
public final class AppVersion {

    private static final Properties PROPERTIES = load();

    private AppVersion() {
        // No instantiation
    }

    /**
     * @return The version, e.g. "2.6.1" for a tagged release or "2.6.1-11-g64533ef8" for a
     *      build after that tag ("-dirty" is added for uncommitted changes). "unknown" if the
     *      version file is missing, e.g. when running from an IDE that skipped the Gradle build.
     */
    public static String version() {
        return PROPERTIES.getProperty("version", "unknown");
    }

    /**
     * @return The copyright year (the year of the commit that was built)
     */
    public static Optional<String> copyrightYear() {
        return Optional.ofNullable(PROPERTIES.getProperty("copyright.year"));
    }

    private static Properties load() {
        var properties = new Properties();
        // Same package as this class, so this also works inside the jlink merged module
        try (var in = AppVersion.class.getResourceAsStream("version.properties")) {
            if (in != null) {
                properties.load(in);
            }
        }
        catch (IOException e) {
            // Fall back to the defaults in the accessors
        }
        return properties;
    }
}
