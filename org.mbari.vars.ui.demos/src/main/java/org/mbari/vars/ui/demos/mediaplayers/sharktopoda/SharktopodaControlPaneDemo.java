package org.mbari.vars.ui.demos.mediaplayers.sharktopoda;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import org.mbari.vars.ui.Initializer;
import org.mbari.vars.ui.mediaplayers.sharktopoda.SharktoptodaControlPane;

/**
 * @author Brian Schlining
 * @since 2017-08-14T16:55:00
 */
public class SharktopodaControlPaneDemo extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        Pane pane = new SharktoptodaControlPane(Initializer.getToolBox());
        Scene scene = new Scene(pane);
        primaryStage.setScene(scene);
        primaryStage.setOnCloseRequest(e -> {
            Platform.exit();
            System.exit(0);
        });
        primaryStage.show();
    }
}
