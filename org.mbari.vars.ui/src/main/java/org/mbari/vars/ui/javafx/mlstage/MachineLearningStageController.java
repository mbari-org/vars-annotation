package org.mbari.vars.ui.javafx.mlstage;


import javafx.application.Platform;
import org.mbari.vars.core.util.Requirements;
import org.mbari.vars.services.impl.ml.JdkMegalodonService;

import org.mbari.vars.services.impl.ml.OkHttpMegalodonService;
import org.mbari.vars.ui.UIToolBox;
import org.mbari.vars.ui.services.MLAnalysisService;
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
                .setOnAction(evt -> {
            machineLearningStage.setLocalizations(Collections.emptyList());
            machineLearningStage.hide();
        });

        machineLearningStage.getSaveAnnotationsButton()
                .setOnAction(evt -> {

                });

        machineLearningStage.getSaveAllButton()
                .setOnAction(event -> {

                });


    }

    private void done() {
        machineLearningStage.setLocalizations(Collections.emptyList());
        machineLearningStage.hide();
    }

    private void saveAnnotations() {
        var locs = machineLearningStage.getLocalizations();
        done();
        // TODO save annotations via cmd

    }

    private void saveAll() {
        var locs = machineLearningStage.getLocalizations();
        done();
        // TODO save framegrab then save annotations
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
        var mlService = new OkHttpMegalodonService(mlRemoteUrlOpt.get());
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


}
