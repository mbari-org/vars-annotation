package org.mbari.vars.ui.javafx.mlstage;


import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.paint.Color;
import org.mbari.vars.core.util.Requirements;
import org.mbari.vars.services.impl.ml.MegalodonService;

import org.mbari.vars.ui.UIToolBox;
import org.mbari.vars.ui.javafx.ImageArchiveServiceDecorator;
import org.mbari.vars.ui.services.FrameCaptureService;
import org.mbari.vars.ui.services.MLAnalysisService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Optional;
import javafx.embed.swing.SwingFXUtils;

public class MachineLearningStageController {

    private final UIToolBox toolBox;
    private static final Logger log = LoggerFactory.getLogger(MachineLearningStageController.class);
    private MachineLearningStage machineLearningStage;


    public MachineLearningStageController(UIToolBox toolBox) {
        this.toolBox = toolBox;
        this.machineLearningStage = new MachineLearningStage(toolBox);
        toolBox.getEventBus()
                .toObserverable()
                .ofType(ApplyMLToVideoCmd.class)
                .forEach(e -> this.analyzeAsync());
    }

    public void analyzeAsync() {
        new Thread(() -> {
            try {
                analyze2();
            } catch (Exception e) {
                log.atWarn()
                        .setCause(e)
                        .log("ML analysis failed.");
            }
        }
        ).start();
    }

    public void analyze2() throws IOException {
        var mlRemoteUrlOpt = MLSettingsPaneController.getRemoteUrl();
        Requirements.validate(mlRemoteUrlOpt.isPresent(), "The URL for the machine learning web service was not set");
        var mlService = new MegalodonService(mlRemoteUrlOpt.get());
        var framegrabRecord = MLAnalysisService.analyzeCurrentElapsedTime(toolBox, mlService);
        var fxImage = SwingFXUtils.toFXImage(framegrabRecord.imageData().getBufferedImage(), null);
        Platform.runLater(() -> {
            machineLearningStage.setImage(fxImage);
            // Don't add localizaitons unti the image has been set! Otherwise they will be cropped out of existence!
            var locView = framegrabRecord.localizations()
                    .stream()
                    .map(v -> MLUtil.toLocalization(v, machineLearningStage.getImagePaneController()))
                    .flatMap(Optional::stream)
                    .toList();
            log.atDebug().log("Created " + locView.size() + " Localization UI objects");
            machineLearningStage.setLocalizations(locView);
            machineLearningStage.show();
        });
    }

//    public void analyze() throws IOException {
//        log.atDebug().log("Starting ML analysis");
//        var mlRemoteUrl = MLSettingsPaneController.getRemoteUrl();
//
//        if (mlRemoteUrl.isPresent()) {
//            log.atInfo().log("Starting ML analysis using service at " + mlRemoteUrl.get());
//            var media = toolBox.getData().getMedia();
//            var mediaPlayer = toolBox.getMediaPlayer();
//            if (mediaPlayer != null && media != null) {
//
//                // Frame grab
//                var imageFile = ImageArchiveServiceDecorator.buildLocalImageFile(media, ".jpg");
////                log.atInfo().log("Target: " + imageFile);
//                var opt = FrameCaptureService.capture(imageFile, media, mediaPlayer);
//                if (opt.isPresent()) {
//                    var framegrab = opt.get();
//                    if (framegrab.isComplete()) {
////                        log.atInfo().log("Got an image");
//                        // TODO handle image
//                        var service = new MegalodonService(mlRemoteUrl.get());
//                        var bufferedImage = (BufferedImage) framegrab.getImage().get();
//                        var localizations = service.predict(bufferedImage);
//                        log.atDebug().log("Found " + localizations.size() + " localizations");
//                        var fxImage = SwingFXUtils.toFXImage(bufferedImage, null);
//
//                        Platform.runLater(() -> {
//                            machineLearningStage.setImage(fxImage);
//                            // Don't add localizaitons unti the image has been set! Otherwise they will be cropped out of existence!
//                            var locView = localizations.stream()
//                                    .map(v -> MLUtil.toLocalization(v, machineLearningStage.getImagePaneController()))
//                                    .flatMap(Optional::stream)
//                                    .toList();
//                            log.atDebug().log("Created " + locView.size() + " Localization UI objects");
//                            machineLearningStage.setLocalizations(locView);
//                            machineLearningStage.show();
//                        });
//
//
//                    }
//
//                } else {
//                    // TODO handle failure to capture image
//                }
//                Files.delete(imageFile.toPath());
//
//                // TODO framecapture (but don't save)
//                // TODO send image to Ml API
//                // TODO convert API response to Localizations
//                // TODO handle actions from Stage (cancel, save localizations, save localization and image)
//            }
//        }
//        else {
//            // TODO show dialog to indiacte URL has not been set.
//        }
//    }






}
