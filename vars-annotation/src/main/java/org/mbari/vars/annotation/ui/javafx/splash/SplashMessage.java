package org.mbari.vars.annotation.ui.javafx.splash;

import javafx.application.Preloader;

/**
 * A status message to display on the splash screen. Send it from
 * {@link javafx.application.Application#init()} using
 * {@code notifyPreloader(new SplashMessage("..."))}.
 *
 * @param message The text to show under the progress bar
 */
public record SplashMessage(String message) implements Preloader.PreloaderNotification {
}
