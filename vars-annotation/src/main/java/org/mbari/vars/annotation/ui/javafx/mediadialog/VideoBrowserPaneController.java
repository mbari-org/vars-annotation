package org.mbari.vars.annotation.ui.javafx.mediadialog;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import org.mbari.vars.annotation.ui.Initializer;
import org.mbari.vars.vampiresquid.sdk.r1.models.Media;
import org.mbari.vars.annotation.ui.UIToolBox;
import org.mbari.vars.annotation.ui.javafx.shared.DateTimePickerController;
import org.mbari.vars.annotation.ui.messages.ReloadServicesMsg;
import org.mbari.vars.annotation.ui.util.FXMLUtils;
import org.mbari.vars.annotation.ui.util.JFXUtilities;
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
//    private final AnnotationService annotationService;
//    private final MediaService mediaService;
    private final UIToolBox toolBox;
    private DateTimePickerController fromDateController;
    private DateTimePickerController toDateController;
    private ListView<String> cameraIdListView;
    private ListView<String> videoSequenceListView;
    private ListView<String> videoListView;
    private ListView<Media> mediaListView;
    private StackPane centerPane;
    private Pane topPane;
    private Slider timeSlider;
    private Label fromLabel = new Label();
    private Label toLabel = new Label();
    private final ResourceBundle uiBundle;
    private MediaPaneController mediaPaneController;
    private Logger log = LoggerFactory.getLogger(getClass());


    public VideoBrowserPaneController(UIToolBox toolBox, ResourceBundle uiBundle) {
        this.toolBox = toolBox;
        this.uiBundle = uiBundle;
        fromLabel.setText(uiBundle.getString("mediadialog.fromlabel"));
        toLabel.setText(uiBundle.getString("mediadialog.tolabel"));
        root = new BorderPane(getCenterPane());
        // root.setTop(getTopPane()); // Not used yet
        root.setRight(getMediaPaneController().getRoot());
        toolBox.getEventBus()
                .toObserverable()
                .ofType(ReloadServicesMsg.class)
                .subscribe(msg -> loadCameraIds());
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

    private void loadCameraIds() {
        // Populate the cameraIds
        toolBox.getServices()
                .mediaService()
                .findAllCameraIds()
                .thenAccept(cameras -> {
                    Platform.runLater(() -> {
                        if (cameraIdListView != null) {
                            cameraIdListView.setItems(FXCollections.observableArrayList(cameras));
                        }
                    });
                });
    }

    public ListView getCameraIdListView() {
        if (cameraIdListView == null) {
            cameraIdListView = new ListView<>();
            cameraIdListView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

            // Populate the cameraIds
            loadCameraIds();

            // When a camera_id is selected set the next list to all the videosequence names
            // for that camera_id.
            cameraIdListView.getSelectionModel()
                    .selectedItemProperty()
                    .addListener(((observable, oldValue, newValue) -> {
                        toolBox.getServices()
                                .mediaService()
                                .findVideoSequenceNamesByCameraId(newValue)
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
                        toolBox.getServices()
                                .mediaService()
                                .findVideoNamesByVideoSequenceName(newValue)
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
                            toolBox.getServices()
                                    .mediaService()
                                    .findByVideoName(newValue)
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
                        getMediaPaneController().setMedia(newValue, toolBox.getServices().annotationService());
                    });

            mediaListView.getSelectionModel()
                    .selectedItemProperty()
                    .addListener(obs -> getMediaPaneController().setMedia(null, toolBox.getServices().annotationService()));
        }
        return mediaListView;
    }

    public BorderPane getRoot() {
        return root;
    }

    public Slider getTimeSlider() {
        if (timeSlider == null) {
            timeSlider = new Slider();
            // TODO this was for JFXSLider, can we don something like this in JavafX?
//            timeSlider.setIndicatorPosition(JFXSlider.IndicatorPosition.RIGHT);
//            timeSlider.setValueFactory(slider ->
//                new StringBinding() {
//                    @Override
//                    protected String computeValue() {
//                        Instant timestamp  = getFromDateController().getTimestamp();
//                        return timestamp.toString();
//                    }
//                });

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
