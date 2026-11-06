package org.mbari.vars.annotation.ui.javafx.mlstage;


import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

import org.mbari.vars.annotation.etc.jdk.Loggers;
import org.mbari.vars.annotation.services.ml.JdkPythiaService;
import org.mbari.vars.annotation.ui.UIToolBox;
import org.mbari.vars.annotation.ui.commands.BulkCreateAnnotations;
import org.mbari.vars.annotation.ui.commands.FramegrabUploadCmd;
import org.mbari.vars.annotation.ui.messages.ShowNonfatalErrorAlert;
import org.mbari.vars.annotation.ui.services.MLAnalysisService;
import org.mbari.vars.annotation.ui.services.MLImageInference;
import org.mbari.vars.annotation.util.Requirements;

import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;

public class MachineLearningStageController {

    private final UIToolBox toolBox;
    private static final Loggers log = new Loggers(MachineLearningStageController.class);
    private MachineLearningStage machineLearningStage;
    private ObjectProperty<MLImageInference> inference = new SimpleObjectProperty<>();


    /**
     * Incremented for each analysis request and whenever the stage is closed. A result
     * whose generation is no longer current is discarded, so a slow ML response can't
     * re-open the window after the user has cancelled, or overwrite a newer request.
     */
    private final AtomicLong generation = new AtomicLong();

    public MachineLearningStageController(UIToolBox toolBox) {
        this.toolBox = toolBox;
        init();
    }

    private void init() {
        this.machineLearningStage = new MachineLearningStage(toolBox);
        toolBox.getEventBus()
                .toObserverable()
                .ofType(ApplyMLToVideoCmd.class)
                .forEach(e -> this.analyzeAsync());

        machineLearningStage.getCancelButton()
                .setOnAction(evt -> done());

        machineLearningStage.getSaveAnnotationsButton()
                .setOnAction(evt -> saveAnnotations());

        machineLearningStage.getSaveAllButton()
                .setOnAction(event -> saveAll());

        // Covers the Cancel button and closing the window from its title bar
        machineLearningStage.setOnHidden(evt -> {
            generation.incrementAndGet();
            inference.set(null);
            machineLearningStage.setLocalizations(Collections.emptyList());
            machineLearningStage.setImage(null);
        });
    }

    private void done() {
        machineLearningStage.hide();
    }

    private void saveAnnotations() {
        var mlInference = inference.get();
        if (mlInference != null) {
            final var observer = toolBox.getData().getUser().getUsername();
            final var group = toolBox.getData().getGroup();
            final var activity = toolBox.getData().getActivity();
            final var imageData = mlInference.imageData();
            final var locs = machineLearningStage.getLocalizations();
            final var annos = locs.stream()
                    .flatMap(loc -> MLUtil.toAnnotation(observer,
                            group,
                            activity,
                            imageData.getVideoIndex(),
                            imageData.getVideoReferenceUuid(),
                            loc).stream())
                    .toList();
            toolBox.getEventBus().send(new BulkCreateAnnotations(annos));
        }
        done();
    }

    private void saveAll() {
        var mlInference = inference.get();
        if (mlInference != null) {
            var imageData = mlInference.imageData();
            toolBox.getEventBus().send(new FramegrabUploadCmd(imageData));
            saveAnnotations();
        }
    }


    public void analyzeAsync() {
        runOnFxThread(() -> {
            // Show the window right away so the user gets feedback while the
            // frame capture and the remote ML request are running
            var requestId = generation.incrementAndGet();
            inference.set(null);
            machineLearningStage.showLoading();
            Thread.ofVirtual().start(() -> analyze(requestId));
        });
    }

    private void analyze(long requestId) {
        try {
            var mlRemoteUrlOpt = MLSettingsPaneController.getRemoteUrl();
            Requirements.validate(mlRemoteUrlOpt.isPresent(), "The URL for the machine learning web service was not set");
            var mlService = new JdkPythiaService(mlRemoteUrlOpt.get());
            log.atDebug().log("[START] ML analysis");
            var mlImageInference = MLAnalysisService.analyzeCurrentElapsedTime(toolBox, mlService);
            log.atDebug().log("[END] ML analysis");
            // Convert off the FX thread; large frames are expensive to copy
            var fxImage = SwingFXUtils.toFXImage(mlImageInference.imageData().getBufferedImage(), null);
            Platform.runLater(() -> display(requestId, mlImageInference, fxImage));
        }
        catch (Exception e) {
            log.atWarn().withCause(e).log("ML analysis failed.");
            Platform.runLater(() -> {
                if (generation.get() == requestId) {
                    machineLearningStage.hide();
                }
            });
            if (generation.get() == requestId) {
                var i18n = toolBox.getI18nBundle();
                var msg = ShowNonfatalErrorAlert.from("ml.controller.analyze.error", e, i18n);
                toolBox.getEventBus().send(msg);
            }
        }
    }

    private void display(long requestId, MLImageInference mlImageInference, Image fxImage) {
        if (generation.get() != requestId) {
            log.atDebug().log("Discarding stale ML result");
            return;
        }
        try {
            inference.set(mlImageInference);
            machineLearningStage.setImage(fxImage);
            // Don't add localizations until the image has been set! Otherwise they will be cropped out of existence!
            var locView = mlImageInference.localizations()
                    .stream()
                    .map(v -> MLUtil.toLocalization(v, machineLearningStage.getImagePaneController()))
                    .flatMap(Optional::stream)
                    .toList();
            log.atDebug().log("Created " + locView.size() + " Localization UI objects");
            machineLearningStage.setLocalizations(locView);
            machineLearningStage.showResults();
        }
        catch (Exception e) {
            // Never leave the window open in a half-rendered state
            log.atWarn().withCause(e).log("Failed to display ML results.");
            machineLearningStage.hide();
            var i18n = toolBox.getI18nBundle();
            toolBox.getEventBus().send(ShowNonfatalErrorAlert.from("ml.controller.analyze.error", e, i18n));
        }
    }

    private static void runOnFxThread(Runnable runnable) {
        if (Platform.isFxApplicationThread()) {
            runnable.run();
        }
        else {
            Platform.runLater(runnable);
        }
    }

}
