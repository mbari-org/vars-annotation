package org.mbari.vars.annotation.ui.mediaplayers;

import javafx.application.Platform;
import org.mbari.vars.vampiresquid.sdk.r1.models.Media;
import org.mbari.vcr4j.VideoError;
import org.mbari.vcr4j.VideoState;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author Brian Schlining
 * @since 2017-12-28T12:24:00
 */
public interface MediaControlsFactory {

    /**
     * Should return the settings paen that can be displayed in a dialog
     * @return
     */
    SettingsPane getSettingsPane();

    /**
     *
     * @param media The media we want to open
     * @return true if this media can be opened by this factory. false if it can not
     */
    boolean canOpen(Media media);

    /**V
     * Open a media. Should not be called directly. Use safeOpen instead.
     * @param media
     * @return
     */
    CompletableFuture<MediaControls<? extends VideoState, ? extends VideoError>> open(Media media);

//    default MediaControls<? extends VideoState, ? extends VideoError> safeOpen(Media media) {
//        var ref = new AtomicReference<MediaControls<? extends VideoState, ? extends VideoError>>();
//        if (canOpen(media)) {
//            try {
//                return open(media).get(10, TimeUnit.SECONDS);
//            } catch (Exception e) {
//                throw new RuntimeException("Failed to open " + media + " using " + this, e);
//            }
//        }
//        else {
//            throw new RuntimeException(getClass().getName() + " is unable to open media");
//        }
//    }

    default MediaControls<? extends VideoState, ? extends VideoError> safeOpen(Media media) {
        if (!canOpen(media)) {
            throw new RuntimeException(getClass().getName() + " is unable to open media");
        }

        try {
            if (Platform.isFxApplicationThread()) {
                // Already on FX thread → no wrapping needed
                return open(media).get(10, TimeUnit.SECONDS);
            }

            CompletableFuture<MediaControls<? extends VideoState, ? extends VideoError>> future =
                    new CompletableFuture<>();

            Platform.runLater(() -> {
                try {
                    future.complete(open(media).get());
                } catch (Throwable t) {
                    future.completeExceptionally(t);
                }
            });

            return future.get(10, TimeUnit.SECONDS);

        } catch (Exception e) {
            throw new RuntimeException("Failed to open " + media + " using " + this, e);
        }
    }

}
