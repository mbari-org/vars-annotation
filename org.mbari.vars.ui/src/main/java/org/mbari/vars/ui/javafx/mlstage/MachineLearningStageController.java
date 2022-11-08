package org.mbari.vars.ui.javafx.mlstage;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.transformation.FilteredList;
import org.mbari.vars.services.model.Framegrab;
import org.mbari.vars.services.model.Media;
import org.mbari.vars.ui.UIToolBox;
import org.mbari.vars.ui.javafx.ImageArchiveServiceDecorator;
import org.mbari.vars.ui.services.FrameCaptureService;

import java.io.File;
import java.util.Optional;

public class MachineLearningStageController {

    private final UIToolBox toolBox;

    public MachineLearningStageController(UIToolBox toolBox) {
        this.toolBox = toolBox;
    }

    public void analyze() {
        var media = toolBox.getData().getMedia();
        var mediaPlayer = toolBox.getMediaPlayer();
        if (mediaPlayer != null && media != null) {

            // Frame grab
            var imageFile = ImageArchiveServiceDecorator.buildLocalImageFile(media, ".png");
            var opt = FrameCaptureService.capture(imageFile, media, mediaPlayer);
            if (opt.isPresent()) {
                // TODO handle image
            }
            else {
                // TODO handle failure to capture image
            }

            // TODO framecapture (but don't save)
            // TODO send image to Ml API
            // TODO convert API response to Localizations
            // TODO handle actions from Stage (cancel, save localizations, save localization and image)
        }
    }


}
