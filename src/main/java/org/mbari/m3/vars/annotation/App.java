package org.mbari.m3.vars.annotation;

import com.google.common.collect.Lists;
import com.typesafe.config.ConfigFactory;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.control.Alert;

import javafx.stage.Stage;
import org.mbari.m3.vars.annotation.util.ActiveAppBeacon;
import org.mbari.m3.vars.annotation.util.ActiveAppPinger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Hello world!
 *
 */
public class App extends Application {

    private UIToolBox toolBox;

    public static final Collection<Integer> BEACON_PORTS = Lists.newArrayList(4002, 4121, 5097, 6238, 6609,
            7407, 8169, 9069, 9669, 16569);
    public static final String BEACON_MESSAGE = "VARS Annotation (M3)";
    private static ActiveAppBeacon activeAppBeacon;
    private static Path settingsDirectory;
    private static Logger log;
    private AppController appController;

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
        toolBox = new UIToolBox(new AppState(Constants.getSettingsDirectory()),
                new EventBus(),
                ResourceBundle.getBundle("i18n", Locale.getDefault()),
                ConfigFactory.load());
        appController = new AppController(toolBox);

        // TODO initialize appState properties
    }

    @Override
    public void start(final Stage primaryStage) throws Exception {

        if (ActiveAppPinger.pingAll(BEACON_PORTS, BEACON_MESSAGE)) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("VARS Information");
            alert.setHeaderText("VARS is already running");
            alert.setContentText("An instance of VARS is already running. Exiting ...");
            alert.showAndWait();
            Platform.exit();
            System.exit(0);
        }
        else if (Constants.getSettingsDirectory() == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("VARS Error");
            alert.setHeaderText("Unable to create a settings directory ");
            alert.setContentText("VARS failed to create a directory for writing temporary information.");
            alert.showAndWait();
            Platform.exit();
            System.exit(-1);
        }
        else {
            activeAppBeacon = new ActiveAppBeacon(BEACON_PORTS, BEACON_MESSAGE);
        }

        primaryStage.setScene(appController.getScene());
        primaryStage.show();
    }



}