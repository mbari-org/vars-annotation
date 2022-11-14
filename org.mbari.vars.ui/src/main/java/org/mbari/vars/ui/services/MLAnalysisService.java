package org.mbari.vars.ui.services;

import org.mbari.vars.core.util.Requirements;
import org.mbari.vars.services.MachineLearningService;
import org.mbari.vars.services.model.Media;
import org.mbari.vars.ui.UIToolBox;
import org.mbari.vars.ui.javafx.ImageArchiveServiceDecorator;
import org.mbari.vars.ui.mediaplayers.MediaPlayer;
import org.mbari.vcr4j.VideoError;
import org.mbari.vcr4j.VideoState;

import java.io.IOException;

public class MLAnalysisService {

    public static MLImageInference analyzeCurrentElapsedTime(UIToolBox toolBox, MachineLearningService mlService) throws IOException {
        var media = toolBox.getData().getMedia();
        var mediaPlayer = toolBox.getMediaPlayer();
        Requirements.checkNotNull(media, "No Media is currently open");
        Requirements.checkNotNull(mediaPlayer, "No media player is available to use for framecapture");
        var framegrabRecord = capturePng(media, mediaPlayer);
        var localizations = mlService.predict(framegrabRecord.imageData().getJpegBytes());
        return framegrabRecord.copy(localizations);
    }

    private static MLImageInference capturePng(Media media,
                                               MediaPlayer<? extends VideoState, ? extends VideoError> mediaPlayer) {
        var pngFile = ImageArchiveServiceDecorator.buildLocalImageFile(media, "png");
        var opt = FrameCaptureService.capture(pngFile, media, mediaPlayer);
        if (opt.isPresent()) {
            var imageData = opt.get();
            return new MLImageInference(pngFile.toPath(), imageData);
        }
        else {
            throw new RuntimeException("Failed to capture image from " + media.getUri() +
                    "and save it to " + pngFile);
        }
    }

}
