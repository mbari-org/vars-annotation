package org.mbari.vars.ui.javafx.mediadialog;

import com.jfoenix.controls.JFXSlider;
import javafx.application.Platform;
import javafx.beans.binding.StringBinding;
import javafx.collections.FXCollections;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import org.mbari.vars.ui.Initializer;
import org.mbari.vars.services.model.Media;
import org.mbari.vars.services.AnnotationService;
import org.mbari.vars.services.MediaService;
import org.mbari.vars.ui.javafx.shared.DateTimePickerController;
import org.mbari.vars.ui.util.FXMLUtils;
import org.mbari.vars.ui.util.JFXUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.Optional;
import java.util.ResourceBundle;


/**
 * @author Brian Schlining
 * @since 2017-05-30T15:01:00
 */
public class VideoBrowserPaneController {

    private final BorderPane root;
    private final AnnotationService annotationService;
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
    private MediaPaneController mediaPaneController;
    private Logger log = LoggerFactory.getLogger(getClass());


    public VideoBrowserPaneController(AnnotationService annotationService,
            MediaService mediaService,
            ResourceBundle uiBundle) {
        this.uiBundle = uiBundle;
        this.annotationService = annotationService;
        this.mediaService = mediaService;
        fromLabel.setText(uiBundle.getString("mediadialog.fromlabel"));
        toLabel.setText(uiBundle.getString("mediadialog.tolabel"));
        root = new BorderPane(getCenterPane());
        root.setTop(getTopPane());
        root.setRight(getMediaPaneController().getRoot());


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
                                    if (vs.size() == 1) {
                                        getVideoSequenceListView().getSelectionModel().select(0);
                                    }
                                }));
                    }));


        }
        return cameraIdListView;
    }

    public ListView<String> getVideoSequenceListView() {
        if (videoSequenceListView == null) {
            videoSequenceListView = new ListView<>();
            videoSequenceListView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

            // When a video sequence is selected set the next list to all the video names
            // available in the video sequence
            videoSequenceListView.getSelectionModel()
                    .selectedItemProperty()
                    .addListener((obs, oldValue, newValue) -> {
                        mediaService.findVideoNamesByVideoSequenceName(newValue)
                                .thenAccept(vs -> Platform.runLater(() -> {
                                    getVideoListView().setItems(FXCollections.observableArrayList(vs));
                                    if (vs.size() == 1) {
                                        getVideoListView().getSelectionModel().select(0);
                                    }
                                }));
                    });

            videoSequenceListView.getSelectionModel()
                    .selectedItemProperty()
                    .addListener(obs ->  getVideoListView().setItems(FXCollections.emptyObservableList()));
        }
        return videoSequenceListView;
    }

    public ListView<String> getVideoListView() {
        if (videoListView == null) {
            videoListView = new ListView<>();
            videoListView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

            // When a video is selected
            videoListView.getSelectionModel()
                    .selectedItemProperty()
                    .addListener((obs, oldValue, newValue) -> {
                        if (newValue != null) {
                            mediaService.findByVideoName(newValue)
                                    .thenAccept(vs -> JFXUtilities.runOnFXThread(() -> {
                                        getMediaListView().setItems(FXCollections.observableArrayList(vs));
                                        if (vs.size() == 1) {
                                            getMediaListView().getSelectionModel().select(0);
                                        }
                                    }));
                        }
                        else {
                            JFXUtilities.runOnFXThread(() -> getMediaListView().setItems(FXCollections.emptyObservableList()));
                        }
                    });

            videoListView.getSelectionModel()
                    .selectedItemProperty()
                    .addListener(obs -> getMediaListView().setItems(FXCollections.emptyObservableList()));
        }
        return videoListView;
    }

    public ListView<Media> getMediaListView() {
        if (mediaListView == null) {
            mediaListView = new ListView<>();
            mediaListView.getSelectionModel()
                    .setSelectionMode(SelectionMode.SINGLE);
            mediaListView.setCellFactory(lv -> new MediaCell());

            mediaListView.getSelectionModel()
                    .selectedItemProperty()
                    .addListener((obs, oldValue, newValue) -> {
                        getMediaPaneController().setMedia(newValue, annotationService);
                    });

            mediaListView.getSelectionModel()
                    .selectedItemProperty()
                    .addListener(obs -> getMediaPaneController().setMedia(null, annotationService));
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

    public MediaPaneController getMediaPaneController() {
        if (mediaPaneController == null) {
            ResourceBundle i18n = Initializer.getToolBox().getI18nBundle();
            mediaPaneController = FXMLUtils.newInstance(MediaPaneController.class,
                    "/fxml/MediaPane.fxml",
                    i18n);
        }
        return mediaPaneController;
    }

    class MediaCell extends ListCell<Media> {
        private final Label label = new Label();
        @Override
        protected void updateItem(Media item, boolean empty) {
            super.updateItem(item, empty);

            if (item == null) {
                label.setText("");
                setTooltip(null);
            }
            else {
                String uri = item.getUri().toString();
                label.setText(uri);
                setTooltip(new Tooltip(uri));
            }
            setGraphic(label);

        }
    }

    public Optional<Media> getSelectedMedia() {
        return Optional.ofNullable(getMediaPaneController().getMedia());
    }


}
