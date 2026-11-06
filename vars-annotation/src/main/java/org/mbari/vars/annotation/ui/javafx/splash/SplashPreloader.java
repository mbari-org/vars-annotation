package org.mbari.vars.annotation.ui.javafx.splash;

import javafx.application.Preloader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * Splash screen shown while the application starts up.
 *
 * <p>JavaFX shows this before it calls {@code App.init()}, where the slow startup work
 * happens (loading configuration, connecting to services, and building the UI), and
 * hides it just before {@code App.start()}. It is registered with the
 * {@code javafx.preloader} system property in {@code App.main}.</p>
 *
 * <p>This runs before the application's toolbox and stylesheets exist, so it is
 * self-contained and styled inline.</p>
 */
public class SplashPreloader extends Preloader {

    /** Matches -fx-background in annotation.css */
    private static final String BACKGROUND = "#2b3940";
    /** Matches -default-text in annotation.css */
    private static final String TEXT = "#B3A9A3";

    private Stage stage;
    private Label statusLabel;

    @Override
    public void start(Stage stage) {
        this.stage = stage;

        var imageView = new ImageView();
        var imageUrl = getClass().getResource("/images/splash/vars-splash.png");
        if (imageUrl != null) {
            imageView.setImage(new Image(imageUrl.toExternalForm()));
        }
        imageView.setFitWidth(200);
        imageView.setPreserveRatio(true);
        imageView.setSmooth(true);

        var titleLabel = new Label("VARS Annotation");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: " + TEXT + ";");

        var progressBar = new ProgressBar(ProgressBar.INDETERMINATE_PROGRESS);
        progressBar.setPrefWidth(260);

        statusLabel = new Label(" ");
        statusLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: " + TEXT + ";");

        var root = new VBox(16, imageView, titleLabel, progressBar, statusLabel);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(30, 40, 30, 40));
        root.setStyle("-fx-background-color: " + BACKGROUND + ";");

        var scene = new Scene(root);
        scene.setFill(Color.web(BACKGROUND));

        stage.initStyle(StageStyle.UNDECORATED);
        stage.setTitle("VARS Annotation");
        stage.setScene(scene);
        stage.centerOnScreen();
        stage.show();
    }

    @Override
    public void handleApplicationNotification(PreloaderNotification info) {
        if (info instanceof SplashMessage(String message) && statusLabel != null) {
            statusLabel.setText(message);
        }
    }

    @Override
    public void handleStateChangeNotification(StateChangeNotification info) {
        // App.start() shows the main window, or an alert, so get out of the way first
        if (info.getType() == StateChangeNotification.Type.BEFORE_START) {
            hide();
        }
    }

    @Override
    public boolean handleErrorNotification(ErrorNotification info) {
        // Hide so the splash doesn't sit on top of whatever error handling follows,
        // and return false so JavaFX's default error handling still runs
        hide();
        return false;
    }

    private void hide() {
        if (stage != null) {
            stage.hide();
        }
    }
}
