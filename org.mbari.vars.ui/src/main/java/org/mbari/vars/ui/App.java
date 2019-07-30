package org.mbari.vars.ui;


import com.google.common.collect.Lists;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Alert;

import javafx.scene.control.Label;
import javafx.stage.Stage;
import org.mbari.vars.ui.commands.CommandManager;
import org.mbari.vars.core.util.ActiveAppBeacon;
import org.mbari.vars.core.util.ActiveAppPinger;
import org.mbari.vars.javafx.util.JFXUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

/**
 * Hello world!
 *
 */
public class App extends Application {

    private UIToolBox toolBox;

    private static final Collection<Integer> BEACON_PORTS = Lists.newArrayList(4002, 4121, 5097, 6238, 6609,
            7407, 8169, 9069, 9669, 16569);
    private static final String BEACON_MESSAGE = "VARS Annotation (M3)";
    private static ActiveAppBeacon activeAppBeacon;
    private static Logger log;
    private AppController appController;
    private CommandManager commandManager;

    private static final String WIDTH_KEY = "stage-width";
    private static final String HEIGHT_KEY = "stage-height";

    public static void main(String[] args) {
        System.getProperties().setProperty("user.timezone", "UTC");
        log = LoggerFactory.getLogger(App.class);
        //Log uncaught Exceptions
        Thread.setDefaultUncaughtExceptionHandler((thread, ex) -> {
            log.error("Exception in thread [" + thread.getName() + "]", ex);
        });
        launch(args);
    }


    @Override
    public void init() throws Exception {
        super.init();
        toolBox = Initializer.getToolBox();
        appController = new AppController(toolBox);
        commandManager = new CommandManager();
    }

    @Override
    public void start(final Stage primaryStage) throws Exception {

        if (ActiveAppPinger.pingAll(BEACON_PORTS, BEACON_MESSAGE)) {
            // Must show stage before showing alert
            primaryStage.setScene(new Scene(new Label("VARS ...")));
            primaryStage.show();
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.initOwner(primaryStage);
            // TODO alert params should be in i18n prop file
            alert.setTitle("VARS Information");
            alert.setHeaderText("VARS is already running");
            alert.setContentText("An instance of VARS is already running. Exiting ...");
            alert.getDialogPane().getStylesheets().addAll(toolBox.getStylesheets());
            alert.showAndWait();
            Platform.exit();
            System.exit(0);
        }
        else if (Initializer.getSettingsDirectory() == null) {
            // Must show stage before showing alert
            primaryStage.setScene(new Scene(new Label("VARS ...")));
            primaryStage.show();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.initOwner(primaryStage);
            // TODO alert params should be in i18n prop file
            alert.setTitle("VARS Error");
            alert.setHeaderText("Unable to create a settings directory ");
            alert.setContentText("VARS failed to create a directory for writing temporary information.");
            alert.getDialogPane().getStylesheets().addAll(toolBox.getStylesheets());
            alert.showAndWait();
            Platform.exit();
            System.exit(-1);
        }
        else {
            activeAppBeacon = new ActiveAppBeacon(BEACON_PORTS, BEACON_MESSAGE);
        }

        primaryStage.setScene(appController.getScene());
        toolBox.primaryStageProperty().set(primaryStage);
        final Class clazz = getClass();

        // Load size from local prefs
        JFXUtilities.loadStageSize(primaryStage, clazz);
        primaryStage.setOnCloseRequest(e -> {
            // Save size on exit
            JFXUtilities.saveStageSize(primaryStage, clazz);
            Platform.exit();
            System.exit(0);
        });
        primaryStage.show();
    }

}
