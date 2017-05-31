package org.mbari.m3.vars.annotation.ui.mediadialog;

import com.jfoenix.controls.JFXSlider;
import javafx.application.Platform;
import javafx.beans.binding.StringBinding;
import javafx.collections.FXCollections;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.layout.*;
import org.mbari.m3.vars.annotation.model.Media;
import org.mbari.m3.vars.annotation.services.MediaService;
import org.mbari.m3.vars.annotation.ui.shared.DateTimePickerController;

import javax.inject.Inject;
import java.time.Instant;
import java.util.ResourceBundle;


/**
 * @author Brian Schlining
 * @since 2017-05-30T15:01:00
 */
public class VideoBrowserPaneController {

    private final BorderPane root;
    private final MediaService mediaService;
    private DateTimePickerController fromDateController;
    private DateTimePickerController toDateController;
    private ListView<String> cameraIdListView;
    private ListView<String> videoSequenceListView;
    private ListView<String> videoListView;
    private ListView<Media> mediaListView;
    private StackPane centerPane;
    private Pane topPane;
    private JFXSlider timeSlider;
    private Label fromLabel = new Label();
    private Label toLabel = new Label();
    private final ResourceBundle uiBundle;


    @Inject
    public VideoBrowserPaneController(MediaService mediaService, ResourceBundle uiBundle) {
        this.uiBundle = uiBundle;
        this.mediaService = mediaService;
        fromLabel.setText(uiBundle.getString("mediadialog.fromlabel"));
        toLabel.setText(uiBundle.getString("mediadialog.tolabel"));
        root = new BorderPane(getCenterPane());
        root.setTop(getTopPane());
    }

    public StackPane getCenterPane() {
        if (centerPane == null) {
            HBox boxCenter = new HBox(getCameraIdListView(),
                    getVideoSequenceListView(),
                    getVideoListView(),
                    getMediaListView());
            centerPane = new StackPane(boxCenter);
        }
        return centerPane;
    }

    public Pane getTopPane() {
        if (topPane == null) {
            HBox hBox = new HBox(fromLabel,
                    getFromDateController().getRoot(),
                    toLabel,
                    getToDateController().getRoot());
            topPane = new VBox(hBox, getTimeSlider());
        }
        return topPane;

    }

    public DateTimePickerController getFromDateController() {
        if (fromDateController == null) {
            fromDateController = new DateTimePickerController();
        }
        return fromDateController;
    }

    public DateTimePickerController getToDateController() {
        if (toDateController == null) {
            toDateController = new DateTimePickerController();
        }
        return toDateController;
    }

    public ListView getCameraIdListView() {
        if (cameraIdListView == null) {
            cameraIdListView = new ListView<>();
            cameraIdListView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

            // Populate the cameraIds
            mediaService.findAllCameraIds()
                    .thenAccept(cameras -> {
                        Platform.runLater(() -> {
                            cameraIdListView.setItems(FXCollections.observableArrayList(cameras));
                        });
                    });

            // When a camera_id is selected set the next list to all the videosequence names
            // for that camera_id.
            cameraIdListView.getSelectionModel()
                    .selectedItemProperty()
                    .addListener(((observable, oldValue, newValue) -> {
                        mediaService.findVideoSequenceNamesByCameraId(newValue)
                                .thenAccept(vs -> Platform.runLater(() -> {
                                    getVideoSequenceListView().setItems(FXCollections.observableArrayList(vs));
                                }));
                    }));


        }
        return cameraIdListView;
    }

    public ListView<String> getVideoSequenceListView() {
        if (videoSequenceListView == null) {
            videoSequenceListView = new ListView<>();

            // When a video sequence is selected set the next list to all the video names
            // available in the video sequence
            videoSequenceListView.getSelectionModel()
                    .selectedItemProperty()
                    .addListener((obs, oldValue, newValue) -> {
                        mediaService.findVideoNamesByVideoSequenceName(newValue)
                                .thenAccept(vs -> Platform.runLater(() -> {
                                    getVideoListView().setItems(FXCollections.observableArrayList(vs));
                                }));
                    });
        }
        return videoSequenceListView;
    }

    public ListView<String> getVideoListView() {
        if (videoListView == null) {
            videoListView = new ListView<>();
            videoListView.getSelectionModel()
                    .selectedItemProperty()
                    .addListener((obs, oldValue, newValue) -> {
                        mediaService.findByVideoName(newValue)
                                .thenAccept(vs -> Platform.runLater(() -> {
                                    getMediaListView().setItems(FXCollections.observableArrayList(vs));
                                }));
                    });
        }
        return videoListView;
    }

    public ListView<Media> getMediaListView() {
        if (mediaListView == null) {
            mediaListView = new ListView<>();
            mediaListView.setCellFactory(lv -> new MediaCell());
        }
        return mediaListView;
    }

    public BorderPane getRoot() {
        return root;
    }

    public JFXSlider getTimeSlider() {
        if (timeSlider == null) {
            timeSlider = new JFXSlider();
            timeSlider.setIndicatorPosition(JFXSlider.IndicatorPosition.RIGHT);
            timeSlider.setValueFactory(slider ->
                new StringBinding() {
                    @Override
                    protected String computeValue() {
                        Instant timestamp  = getFromDateController().getTimestamp();
                        return timestamp.toString();
                    }
                });

        }
        return timeSlider;
    }

    class MediaCell extends ListCell<Media> {
        private final Label label = new Label();
        @Override
        protected void updateItem(Media item, boolean empty) {
            super.updateItem(item, empty);

            if (item == null) {
                label.setText("");
            }
            else {
                label.setText(item.getUri().toString());
            }
            setGraphic(label);
        }
    }
}
