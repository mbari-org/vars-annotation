package org.mbari.vars.annotation.ui.services;

import org.mbari.vars.annotation.services.MachineLearningService;
import org.mbari.vars.annotation.util.Requirements;
import org.mbari.vars.vampiresquid.sdk.r1.models.Media;
import org.mbari.vars.annotation.ui.UIToolBox;
import org.mbari.vars.annotation.ui.javafx.ImageArchiveServiceDecorator;
import org.mbari.vars.annotation.ui.mediaplayers.MediaPlayer;
import org.mbari.vcr4j.VideoError;
import org.mbari.vcr4j.VideoState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class MLAnalysisService {

    private static final Logger log = LoggerFactory.getLogger(MLAnalysisService.class);

    public static MLImageInference analyzeCurrentElapsedTime(UIToolBox toolBox, MachineLearningService mlService) throws IOException {
        var media = toolBox.getData().getMedia();
        var mediaPlayer = toolBox.getMediaPlayer();
        Requirements.checkNotNull(media, "No Media is currently open");
        Requirements.checkNotNull(mediaPlayer, "No media player is available to use for framecapture");
        var framegrabRecord = capturePng(media, mediaPlayer);
        log.atInfo().log("Captured png of " + framegrabRecord.imageData().getPngBytes().length + " bytes at " + framegrabRecord.imageData().getVideoIndex());
        log.atInfo().log("Converted jpeg of " + framegrabRecord.imageData().getJpegBytes().length + " bytes at " + framegrabRecord.imageData().getVideoIndex());
        var localizations = mlService.predict(framegrabRecord.imageData().getJpegBytes());
        return framegrabRecord.copy(localizations);
    }

    private static MLImageInference capturePng(Media media,
                                               MediaPlayer<? extends VideoState, ? extends VideoError> mediaPlayer) {
        var pngFile = ImageArchiveServiceDecorator.buildLocalImageFile(media, ".png");
        var opt = FrameCaptureService.capture(pngFile, media, mediaPlayer);
        if (opt.isPresent()) {
            var imageData = opt.get();
            return new MLImageInference(imageData);
        }
        else {
            throw new RuntimeException("Failed to capture image from " + media.getUri() +
                    "and save it to " + pngFile);
        }
    }

}
