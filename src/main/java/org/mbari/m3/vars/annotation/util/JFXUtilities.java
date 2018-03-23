package org.mbari.m3.vars.annotation.util;

import javafx.application.Platform;

/**
 * @author Brian Schlining
 * @since 2018-03-23T13:26:00
 */
public class JFXUtilities {
    public static void runOnFXThread(Runnable r) {
        if (Platform.isFxApplicationThread()) {
            r.run();
        }
        else {
            Platform.runLater(r);
        }
    }
}
