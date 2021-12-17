package org.mbari.vars.ui.mediaplayers;

import org.mbari.vars.services.model.Media;
import org.mbari.vcr4j.VideoError;
import org.mbari.vcr4j.VideoState;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

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

    default MediaControls<? extends VideoState, ? extends VideoError> safeOpen(Media media) {
        if (canOpen(media)) {
            try {
                return open(media).get(10, TimeUnit.SECONDS);
            } catch (Exception e) {
                throw new RuntimeException("Failed to open " + media + " using " + this);
            }
        }
        else {
            throw new RuntimeException(getClass().getName() + " is unable to open media");
        }
    }

}
