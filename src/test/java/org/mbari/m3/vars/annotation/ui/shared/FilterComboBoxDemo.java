package org.mbari.m3.vars.annotation.ui.shared;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import org.mbari.m3.vars.annotation.Initializer;

/**
 * @author Brian Schlining
 * @since 2017-08-17T11:09:00
 */
public class FilterComboBoxDemo extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {

        StringConverter<String> s = new StringConverter<String>() {
            @Override
            public String toString(String object) {
                return object;
            }

            @Override
            public String fromString(String string) {
                return string;
            }
        };

        FilterComboBox<String> comboBox = new FilterComboBox<>(true);
        comboBox.setConverter(s);
        Initializer.getToolBox()
                .getServices()
                .getConceptService()
                .findAllNames()
                .thenAccept(names -> {
                    Platform.runLater(() -> {
                        comboBox.setInitialItems(names);
                    });
                });

        Scene scene = new Scene(comboBox);
        primaryStage.setScene(scene);
        primaryStage.setOnCloseRequest(e -> {
            Platform.exit();
            System.exit(0);
        });
        primaryStage.show();

    }
}
