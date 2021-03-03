package org.mbari.vars.ui.javafx.mediadialog;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTextArea;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import org.mbari.vars.services.model.Media;
import org.mbari.vars.ui.Initializer;
import org.mbari.vars.ui.util.FXMLUtils;

public class MediaDescriptionEditorPaneController {

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private GridPane root;

    @FXML
    private Label videoSequenceLabel;

    @FXML
    private Label videoLabel;

    @FXML
    private Label videoReferenceLabel;

    @FXML
    private JFXButton saveButton;

    @FXML
    private JFXTextArea videoSequenceTextArea;

    @FXML
    private JFXTextArea videoTextArea;

    @FXML
    private JFXTextArea videoReferenceTextArea;

    ObjectProperty<Media> media = new SimpleObjectProperty<>();


    @FXML
    void initialize() {
        mediaProperty().addListener((obs, oldValue, newValue) -> {
            updateView(newValue);
        });

        videoReferenceTextArea.textProperty().addListener(change -> checkDisable());
        videoTextArea.textProperty().addListener(change -> checkDisable());
        videoSequenceTextArea.textProperty().addListener(change -> checkDisable());

        updateView(null);
    }

    private void checkDisable() {
        var disable = media.get() == null
                || (videoReferenceTextArea.getText().equals(media.get().getDescription())
                && videoTextArea.getText().equals(media.get().getVideoDescription())
                && videoSequenceTextArea.getText().equals(media.get().getVideoSequenceDescription()));
        saveButton.setDisable(disable);
    }

    private void updateView(Media media) {
        saveButton.setDisable(true);
        if (media == null) {
            resetView();
        }
        else {
            videoSequenceTextArea.setText(media.getVideoSequenceDescription());
            videoTextArea.setText(media.getVideoDescription());
            videoReferenceTextArea.setText(media.getDescription());
        }
    }

    private void resetView() {
        videoSequenceTextArea.setText(null);
        videoTextArea.setText(null);
        videoReferenceTextArea.setText(null);
    }

    public GridPane getRoot() {
        return root;
    }

    public JFXButton getSaveButton() {
        return saveButton;
    }

    public Media getMedia() {
        return media.get();
    }

    public ObjectProperty<Media> mediaProperty() {
        return media;
    }

    public void setMedia(Media media) {
        this.media.set(media);
    }

    public static MediaDescriptionEditorPaneController newInstance() {
        ResourceBundle i18n = Initializer.getToolBox().getI18nBundle();
        return FXMLUtils.newInstance(MediaDescriptionEditorPaneController.class,
                "/fxml/MediaDescriptionEditorPane.fxml",
                i18n);
    }
}
