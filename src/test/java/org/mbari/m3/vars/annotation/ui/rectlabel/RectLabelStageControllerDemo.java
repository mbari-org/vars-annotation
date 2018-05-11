package org.mbari.m3.vars.annotation.ui.rectlabel;

import javafx.application.Application;
import javafx.stage.Stage;
import org.mbari.m3.vars.annotation.AppDemo;
import org.mbari.m3.vars.annotation.Initializer;
import org.mbari.m3.vars.annotation.UIToolBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Brian Schlining
 * @since 2018-05-07T10:08:00
 */
public class RectLabelStageControllerDemo extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        Logger log = LoggerFactory.getLogger(AppDemo.class);
        UIToolBox toolBox = Initializer.getToolBox();
        toolBox.getEventBus()
                .toObserverable()
                .subscribe(e -> log.debug(e.toString()));
        RectLabelStageController controller = new RectLabelStageController(toolBox);
        controller.show();
        toolBox.getServices()
                .getMediaService()
                .findByVideoSequenceName("Doc Ricketts 0953")
                .thenAccept(ms -> {
                    int idx = 5;
                    System.out.println("Using Media: " + ms.get(idx));
                    toolBox.getData().setMedia(ms.get(idx));
                    controller.setMedia(ms.get(idx));
                });
    }
}
