package org.mbari.vars.ui.mediaplayers.vcr;


import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;


import io.reactivex.rxjava3.disposables.Disposable;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Text;
import org.mbari.vars.ui.javafx.Icons;
import org.mbari.vcr4j.VideoController;
import org.mbari.vcr4j.VideoError;
import org.mbari.vcr4j.VideoState;
//import org.mbari.vcr4j.rs422.commands.RS422VideoCommands;

/**
 * @author Brian Schlining
 * @since 2018-03-21T16:23:00
 */
public class VcrControlPaneController {

    private ObjectProperty<VideoController<? extends VideoState, ? extends VideoError>> videoController =
            new SimpleObjectProperty<>();
    private Disposable indexDisposable;

    @FXML
    private ResourceBundle resources;

    @FXML
    private AnchorPane root;

    @FXML
    private URL location;

    @FXML
    private Label timecodeLabel;

    @FXML
    private Button shuttleFwdButton;

    @FXML
    private Button playButton;

    @FXML
    private Button shuttleReverseButton;

    @FXML
    private Button gotoButton;

    @FXML
    private Button fastforwardButton;

    @FXML
    private Button stopButton;

    @FXML
    private Button rewindButton;

    @FXML
    private Button ejectButton;

    @FXML
    private Slider speedSlider;


    private Disposable stateDisposable;
    private VideoState lastState;

    @FXML
    void initialize() {

        videoController.addListener((obs, oldv, newv) -> {
            if (indexDisposable != null) {
                indexDisposable.dispose();
                indexDisposable = null;
            }

            if (stateDisposable != null) {
                stateDisposable.dispose();
                stateDisposable = null;
            }

            boolean disable = true;
            if (videoController != null) {
                indexDisposable = newv.getIndexObservable()
                        .subscribe(vi ->  vi.getTimecode()
                                .ifPresent(tc -> Platform.runLater(() ->
                                        timecodeLabel.setText(tc.toString()))));

                stateDisposable = newv.getStateObservable()
                        .subscribe(state -> lastState = state);

                disable = false;
            }
            shuttleReverseButton.setDisable(disable);
            shuttleFwdButton.setDisable(disable);
            playButton.setDisable(disable);
            rewindButton.setDisable(disable);
            fastforwardButton.setDisable(disable);
            stopButton.setDisable(disable);
            //gotoButton.setDisable(disable); // TODO uncomment when implemented
        });

        int size = 35;
        final Text shuttleFwdIcon = Icons.ARROW_FORWARD.size(size);
        shuttleFwdButton.setGraphic(shuttleFwdIcon);
        shuttleFwdButton.setOnAction(e -> doShuttleForward());
        shuttleFwdButton.setDisable(true);

        final Text shuttleReverseIcon = Icons.ARROW_BACK.size(size);
        shuttleReverseButton.setGraphic(shuttleReverseIcon);
        shuttleReverseButton.setOnAction(e -> doShuttleReverse());
        shuttleReverseButton.setDisable(true);

        final Text playIcon = Icons.PLAY_ARROW.size(size);
        playButton.setGraphic(playIcon);
        playButton.setOnAction(e -> doPlay());
        playButton.setDisable(true);

        final Text gotoIcon = Icons.SEARCH.size(size);
        gotoButton.setGraphic(gotoIcon);
        gotoButton.setOnAction(e -> doSeek());
        gotoButton.setDisable(true); // TODO Disable until we implement this

        final Text fwdIcon = Icons.FAST_FORWARD.size(size);
        fastforwardButton.setGraphic(fwdIcon);
        fastforwardButton.setOnAction(e -> doFastForward());
        fastforwardButton.setDisable(true);

        final Text rewindIcon = Icons.FAST_REWIND.size(size);
        rewindButton.setGraphic(rewindIcon);
        rewindButton.setOnAction(e -> doRewind());
        rewindButton.setDisable(true);

        final Text stopIcon = Icons.STOP.size(size);
        stopButton.setGraphic(stopIcon);
        stopButton.setOnAction(e -> doStop());
        stopButton.setDisable(true);

        final Text ejectIcon = Icons.EJECT.size(size);
        ejectButton.setGraphic(ejectIcon);
        ejectButton.setOnAction(e -> doEject());
        ejectButton.setDisable(true);
        setVideoController(null);

        speedSlider.valueChangingProperty().addListener((obs, oldv, newv) -> {
            VideoController<? extends VideoState, ? extends VideoError> vc = this.videoController.get();
            VideoState state = lastState;
            if (!newv && state != null && vc != null) {
                boolean shuttling = state.isShuttling();
                if (shuttling) {
                    boolean isReverse = state.isReverseDirection();
                    double rate = asShuttleRate(speedSlider.getValue(), isReverse);
                    vc.shuttle(rate);
                }
            }
        });

    }

    private double asShuttleRate(Double sliderValue, boolean isReverse) {
        int direction = isReverse ? -1 : 1;
        return sliderValue / 255D * direction;
    }

    private void doShuttleForward() {
        Optional.ofNullable(videoController.get())
                .ifPresent(vc -> {
                    double rate = asShuttleRate(speedSlider.getValue(), false);
                    vc.shuttle(rate);
                });
    }

    private void doShuttleReverse() {
        Optional.ofNullable(videoController.get())
                .ifPresent(vc -> {
                    double rate = asShuttleRate(speedSlider.getValue(), true);
                    vc.shuttle(rate);
                });
    }

    private void doPlay() {
        Optional.ofNullable(videoController.get())
                .ifPresent(VideoController::play);
    }

    private void doSeek() {
        // TODO show timecode selection dialog
    }

    private void doFastForward() {
        Optional.ofNullable(videoController.get())
                .ifPresent(VideoController::fastForward);
    }

    private void doRewind() {
        Optional.ofNullable(videoController.get())
                .ifPresent(VideoController::rewind);
    }

    private void doStop() {
        Optional.ofNullable(videoController.get())
                .ifPresent(VideoController::stop);
    }

    private void doEject() {
//        Optional.ofNullable(videoController.get())
//                .ifPresent(vc -> vc.send(RS422VideoCommands.EJECT));
    }

    public VideoController<? extends VideoState, ? extends VideoError> getVideoController() {
        return videoController.get();
    }

    public ObjectProperty<VideoController<? extends VideoState, ? extends VideoError>> videoControllerProperty() {
        return videoController;
    }

    public void setVideoController(VideoController<? extends VideoState, ? extends VideoError> videoController) {
        this.videoController.set(videoController);
    }

    public AnchorPane getRoot() {
        return root;
    }

    public static VcrControlPaneController newInstance() {
        FXMLLoader loader = new FXMLLoader(VcrControlPaneController.class
                .getResource("/fxml/VcrControlPane.fxml"));

        try {
            loader.load();
            return loader.getController();
        }
        catch (Exception e) {
            throw new RuntimeException("Failed to load VcrControlPane from FXML", e);
        }
    }
}

