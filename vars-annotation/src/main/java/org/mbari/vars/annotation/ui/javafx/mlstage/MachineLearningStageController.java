package org.mbari.vars.annotation.ui.javafx.mlstage;


import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

import org.mbari.vars.annotation.services.ml.JdkMegalodonService;
import org.mbari.vars.annotation.ui.UIToolBox;
import org.mbari.vars.annotation.ui.commands.BulkCreateAnnotations;
import org.mbari.vars.annotation.ui.commands.FramegrabUploadCmd;
import org.mbari.vars.annotation.ui.messages.ShowNonfatalErrorAlert;
import org.mbari.vars.annotation.ui.services.MLAnalysisService;
import org.mbari.vars.annotation.ui.services.MLImageInference;
import org.mbari.vars.annotation.util.Requirements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collections;
import java.util.Optional;

import javafx.embed.swing.SwingFXUtils;

public class MachineLearningStageController {

    private final UIToolBox toolBox;
    private static final Logger log = LoggerFactory.getLogger(MachineLearningStageController.class);
    private MachineLearningStage machineLearningStage;
    private ObjectProperty<MLImageInference> inference = new SimpleObjectProperty<>();


    // TODO use Executor instead fo single thread
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

        inference.addListener((obs, oldv, newv) -> {
            if (newv != null) {
                var fxImage = SwingFXUtils.toFXImage(newv.imageData().getBufferedImage(), null);
                Platform.runLater(() -> {
                    machineLearningStage.setImage(fxImage);
                    // Don't add localizaitons unti the image has been set! Otherwise they will be cropped out of existence!
                    var locView = newv.localizations()
                            .stream()
                            .map(v -> MLUtil.toLocalization(v, machineLearningStage.getImagePaneController()))
                            .flatMap(Optional::stream)
                            .toList();
                    log.atDebug().log("Created " + locView.size() + " Localization UI objects");
                    machineLearningStage.setLocalizations(locView);
                    machineLearningStage.show();

                });
            } else {
                machineLearningStage.setLocalizations(Collections.emptyList());
                machineLearningStage.setImage(null);
            }
        });

    }

    private void done() {
        machineLearningStage.hide();
        inference.set(null);
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
        Thread.ofVirtual().start(() -> {
            try {
                analyze();
            } catch (Exception e) {
                log.atWarn()
                        .setCause(e)
                        .log("ML analysis failed.");
            }
        });
    }

    public void analyze() throws IOException {
        var mlRemoteUrlOpt = MLSettingsPaneController.getRemoteUrl();
        Requirements.validate(mlRemoteUrlOpt.isPresent(), "The URL for the machine learning web service was not set");
//        var mlService = new OkHttpMegalodonService(mlRemoteUrlOpt.get());
        var mlService = new JdkMegalodonService(mlRemoteUrlOpt.get());
        try {
            log.atDebug().log("[START] ML analysis");
            var mlImageInference = MLAnalysisService.analyzeCurrentElapsedTime(toolBox, mlService);
            log.atDebug().log("[END] ML analysis");
            Platform.runLater(() -> inference.set(mlImageInference));
        }
        catch (Exception e) {
            var i18n = toolBox.getI18nBundle();
            var msg = ShowNonfatalErrorAlert.from("ml.controller.analyze.error", e, i18n);
            toolBox.getEventBus().send(msg);
        }
    }


}
