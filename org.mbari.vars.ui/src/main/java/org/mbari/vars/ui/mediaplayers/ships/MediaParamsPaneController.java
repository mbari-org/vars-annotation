package org.mbari.vars.ui.mediaplayers.ships;

import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXTextField;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.function.UnaryOperator;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.layout.GridPane;
import org.mbari.vars.ui.Initializer;
import org.mbari.vars.ui.util.FXMLUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MediaParamsPaneController {

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private GridPane root;

    @FXML
    private JFXComboBox<String> cameraIdComboBox;

    @FXML
    private JFXTextField sequenceNumberTextField;

    private final Logger log = LoggerFactory.getLogger(getClass());

    @FXML
    void initialize() {

        UnaryOperator<TextFormatter.Change> filter = change -> {
            String text = change.getText();
            if (text.matches("[0-9]*")) {
                return change;
            }
            return null;
        };

        TextFormatter<String> textFormatter1 = new TextFormatter<>(filter);
        sequenceNumberTextField.setTextFormatter(textFormatter1);

        cameraIdComboBox.setEditable(true);
    }

    public void refresh() {
        Initializer.getToolBox()
                .getServices()
                .getMediaService()
                .findAllCameraIds()
                .thenAccept(cameraIds -> {
                    Platform.runLater(() -> {
                        sequenceNumberTextField.setText(null);
                        final ObservableList<String> items = FXCollections.observableArrayList(cameraIds);
                        cameraIdComboBox.setItems(items);
                        updateUiWithDefaults();
                    });
                });
    }

    public GridPane getRoot() {
        return root;
    }

    public ComboBox<String> getCameraIdComboBox() {
        return cameraIdComboBox;
    }

    public TextField getSequenceNumberTextField() {
        return sequenceNumberTextField;
    }

    public static MediaParamsPaneController newInstance() {
        ResourceBundle i18n = Initializer.getToolBox().getI18nBundle();
        return FXMLUtils.newInstance(MediaParamsPaneController.class,
                "/fxml/MediaParamsPane.fxml",
                i18n);
    }

    private void updateUiWithDefaults() {
        try {
            String cameraid = Initializer.getToolBox()
                    .getConfig()
                    .getString("app.defaults.cameraid");
            cameraIdComboBox.getSelectionModel().select(cameraid);
        }
        catch (Exception e) {
            log.info("No default cameraId was found in the configuration file." +
                    " (app.defaults.cameraid");
        }
    }
}
