package org.mbari.m3.vars.annotation.mediaplayers.ships;

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
import org.mbari.m3.vars.annotation.Initializer;
import org.mbari.m3.vars.annotation.util.FXMLUtils;

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
        return FXMLUtils.newInstance(MediaParamsPaneController.class, "/fxml/MediaParamsPane.fxml");
    }
}
