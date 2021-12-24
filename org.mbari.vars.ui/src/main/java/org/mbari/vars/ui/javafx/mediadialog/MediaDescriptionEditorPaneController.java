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
import org.mbari.vars.ui.UIToolBox;
import org.mbari.vars.ui.events.MediaChangedEvent;
import org.mbari.vars.ui.util.FXMLUtils;

/**
 * UI component for editing the 3 description fields associated with
 * a Media object.
 */
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

    private final ObjectProperty<Media> media = new SimpleObjectProperty<>();

    private UIToolBox toolBox;

    @FXML
    void initialize() {
        media.addListener((obs, oldValue, newValue) -> {
            updateView(newValue);
        });

        videoReferenceTextArea.textProperty().addListener(change -> checkDisable());
        videoTextArea.textProperty().addListener(change -> checkDisable());
        videoSequenceTextArea.textProperty().addListener(change -> checkDisable());

        saveButton.setOnAction(e -> updateMedia());
        updateView(null);
    }

    private void setToolBox(UIToolBox toolBox) {
        this.toolBox = toolBox;
    }

    private void checkDisable() {
        var disable = media.get() == null
                || (videoReferenceTextArea.getText().equals(media.get().getDescription())
                    && videoTextArea.getText().equals(media.get().getVideoDescription())
                    && videoSequenceTextArea.getText().equals(media.get().getVideoSequenceDescription()));
        saveButton.setDisable(disable);
    }

    public void setMedia(Media m) {
        media.set(m);
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

    private void updateMedia() {
        var m = media.get();
        if (m != null) {
            m.setVideoSequenceDescription(videoSequenceTextArea.getText());
            m.setVideoDescription(videoTextArea.getText());
            m.setDescription(videoReferenceTextArea.getText());
            toolBox.getServices()
                    .getMediaService()
                    .update(m)
                    .thenAccept(this::setMedia);
        }
    }


    public static MediaDescriptionEditorPaneController newInstance() {
        var toolbox = Initializer.getToolBox();
        ResourceBundle i18n = toolbox.getI18nBundle();
        var controller = FXMLUtils.newInstance(MediaDescriptionEditorPaneController.class,
                "/fxml/MediaDescriptionEditorPane.fxml",
                i18n);
        controller.setToolBox(toolbox);
        return controller;
    }
}
