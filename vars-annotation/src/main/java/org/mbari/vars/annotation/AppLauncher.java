package org.mbari.vars.annotation;

/**
 * Launcher class that does not extend {@link javafx.application.Application}.
 * This avoids the "JavaFX runtime components are missing" error that occurs
 * when the main class extends Application and JavaFX is on the classpath
 * (not the module path).
 */
public class AppLauncher {
    public static void main(String[] args) throws Exception {
        App.main(args);
    }
}
