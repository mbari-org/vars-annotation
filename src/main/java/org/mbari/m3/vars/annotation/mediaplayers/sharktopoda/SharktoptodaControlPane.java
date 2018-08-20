package org.mbari.m3.vars.annotation.mediaplayers.sharktopoda;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXSlider;
import de.jensd.fx.glyphs.GlyphsFactory;
import de.jensd.fx.glyphs.materialicons.MaterialIcon;
import de.jensd.fx.glyphs.materialicons.utils.MaterialIconFactory;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringBinding;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import org.mbari.m3.vars.annotation.UIToolBox;
import org.mbari.m3.vars.annotation.mediaplayers.MediaPlayer;
import org.mbari.m3.vars.annotation.model.Media;
import org.mbari.vcr4j.VideoError;
import org.mbari.vcr4j.VideoIndex;
import org.mbari.vcr4j.VideoState;
import org.mbari.vcr4j.sharktopoda.SharktopodaVideoIO;
import org.mbari.vcr4j.sharktopoda.commands.SharkCommands;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * @author Brian Schlining
 * @since 2017-08-14T16:48:00
 */
public class SharktoptodaControlPane extends Pane {

    private final UIToolBox toolBox;
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofLocalizedTime(FormatStyle.FULL)
            .withZone(ZoneId.of("UTC"));
    JFXSlider speedSlider;
    JFXSlider scrubber;
    Button rewindButton;
    Button fastForwardButton;
    Button playButton;
    Button frameAdvanceButton;
    Label elapsedTimeLabel = new Label("00:00");
    Label durationLabel = new Label("00:00");
    Label recordedTimestampLabel = new Label("--:--:--");
    private GlyphsFactory glyphsFactory = MaterialIconFactory.get();
    private Color color = Color.LIGHTGRAY;
    Text speedUpIcon = glyphsFactory.createIcon(MaterialIcon.ADD, "20px");
    Text speedDownIcon = glyphsFactory.createIcon(MaterialIcon.REMOVE, "20px");
    private volatile MediaPlayer<? extends VideoState, ? extends VideoError> mediaPlayer;
    private final List<Disposable> disposables = new ArrayList<>();
    private Text playIcon = glyphsFactory.createIcon(MaterialIcon.PLAY_ARROW, "50px");
    private Text pauseIcon = glyphsFactory.createIcon(MaterialIcon.PAUSE, "50px");
    private volatile VideoState videoState;
    //private final Observer

    public SharktoptodaControlPane(UIToolBox toolBox) {
        this.toolBox = toolBox;
        setPrefSize(440, 80);
        speedDownIcon.setFill(color);
        speedUpIcon.setFill(color);
        elapsedTimeLabel.setTextFill(color);
        durationLabel.setTextFill(color);
        recordedTimestampLabel.setTextFill(color);
        recordedTimestampLabel.getStyleClass().add(".monospace");
        elapsedTimeLabel.getStyleClass().add("monospace");
        getStylesheets().addAll(toolBox.getStylesheets());

        doLayout();

        getChildren().addAll(
//                speedDownIcon,
//                speedUpIcon,
                getRewindButton(),
                getPlayButton(),
                getFastForwardButton(),
                getFrameAdvanceButton(),
                elapsedTimeLabel,
                durationLabel,
                recordedTimestampLabel,
                getSpeedSlider(),
                getScrubber());
    }

    private void doLayout() {
//        speedDownIcon.relocate(5, 25);
//        getSpeedSlider().relocate(25, 19);
//        speedUpIcon.relocate(90, 25);
        getSpeedSlider().relocate(5, 19);
        getRewindButton().relocate(145, 8);
        getPlayButton().relocate(195, 0);
        getFastForwardButton().relocate(260, 8);
        getFrameAdvanceButton().relocate(310, 8);
        recordedTimestampLabel.relocate(365, 19);
        elapsedTimeLabel.relocate(9, 47);
        getScrubber().relocate(55, 47);
        durationLabel.relocate(395, 47);
    }

    protected JFXSlider getSpeedSlider() {
        if (speedSlider == null) {
            // We'll use 4 as the max shuttle rate for now
            double v = 4.0 * 1000;

            speedSlider = new JFXSlider(0, v, 2000);
            speedSlider.setPrefWidth(130);
            speedSlider.setIndicatorPosition(JFXSlider.IndicatorPosition.RIGHT);
            String tooltip = toolBox.getI18nBundle().getString("mediaplayer.sharktopoda.speedslider.tooltip");
            speedSlider.setTooltip(new Tooltip(tooltip));
            StringBinding binding = Bindings.createStringBinding(() ->
                    String.format("%3.2fx", speedSlider.getValue() / 1000D),
                    speedSlider.valueProperty());
            speedSlider.setValueFactory(p -> binding);
        }
        return speedSlider;
    }

    protected Button getFrameAdvanceButton() {
        if (frameAdvanceButton == null) {
            Text icon = glyphsFactory.createIcon(MaterialIcon.KEYBOARD_ARROW_RIGHT, "30px");
            icon.setFill(color);
            frameAdvanceButton = new JFXButton();
            frameAdvanceButton.setGraphic(icon);
            frameAdvanceButton.setPrefSize(30, 30);
            frameAdvanceButton.setOnAction(e -> {
                mediaPlayer.stop();
                mediaPlayer.getVideoIO()
                        .send(SharkCommands.FRAMEADVANCE);
            });
        }
        return frameAdvanceButton;
    }

    protected Button getFastForwardButton() {
        if (fastForwardButton == null) {
            Text icon = glyphsFactory.createIcon(MaterialIcon.FAST_FORWARD, "30px");
            icon.setFill(color);
            fastForwardButton = new JFXButton();
            fastForwardButton.setGraphic(icon);
            fastForwardButton.setPrefSize(30, 30);
            fastForwardButton.setOnAction(e -> {
                if (mediaPlayer != null) {
                    double speed = getSpeedSlider().getValue() / 1000D / SharktopodaVideoIO.MAX_SHUTTLE_RATE;
                    mediaPlayer.shuttle(speed);
                }
            });
        }
        return fastForwardButton;
    }

    protected Button getRewindButton() {
        if (rewindButton == null) {
            Text icon = glyphsFactory.createIcon(MaterialIcon.FAST_REWIND, "30px");
            icon.setFill(color);
            rewindButton = new JFXButton();
            rewindButton.setGraphic(icon);
            rewindButton.setPrefSize(30, 30);
            rewindButton.setOnAction(e -> {
                if (mediaPlayer != null) {
                    double speed = getSpeedSlider().getValue() / 1000D / SharktopodaVideoIO.MAX_SHUTTLE_RATE;
                    mediaPlayer.shuttle(-speed);
                }
            });
        }
        return rewindButton;
    }

    protected Button getPlayButton() {
        if (playButton == null) {
            playIcon.setFill(color);
            pauseIcon.setFill(color);
            playButton = new JFXButton();
            playButton.setGraphic(playIcon);
            playButton.setPrefSize(30, 30);
            playButton.setOnAction(e -> {
                if (mediaPlayer != null) {
                    if (videoState == null || videoState.isStopped()) {
                        mediaPlayer.play();
                    }
                    else {
                        mediaPlayer.stop();
                    }
                }
            });
        }
        return playButton;
    }

    public JFXSlider getScrubber() {
        if (scrubber == null) {
            // The scrubber represents the position into the video in Millisecs
            scrubber = new JFXSlider(0, 1000, 0);
            scrubber.setPrefWidth(325);
            StringBinding binding = Bindings.createStringBinding(() ->
                    formatSeconds(Math.round(scrubber.getValue() / 1000D)),
                    scrubber.valueProperty());
            scrubber.setValueFactory(p -> binding);
            scrubber.valueProperty().addListener(observable -> {
                if (scrubber.isValueChanging()) {
                    long millis = Math.round(scrubber.getValue());
                    mediaPlayer.seek(Duration.ofMillis(millis));
                }
//                else {
//                    String s = formatSeconds(Math.round(scrubber.getValue() / 1000D));
//                    scrubber.setValue();
//                }
            });
//            scrubber.setOnMouseReleased(v -> {
//                if (mediaPlayer != null) {
//                    long millis = Math.round(scrubber.getValue());
//                    mediaPlayer.seek(Duration.ofMillis(millis));
//                }
//            });
        }
        return scrubber;
    }

    public void setMediaPlayer(MediaPlayer<? extends VideoState, ? extends VideoError> mediaPlayer) {
        getScrubber().setValue(0);
        this.mediaPlayer = mediaPlayer;

        if (mediaPlayer == null) {
            getScrubber().setDisable(true);
        }
        else {
            getScrubber().setDisable(false);
            Duration duration = mediaPlayer.getMedia().getDuration();
            if (duration != null) {
                long durationMillis = duration.toMillis();
                Platform.runLater(() -> {
                    getScrubber().setMax(durationMillis);
                    durationLabel.setText(formatSeconds(duration.getSeconds()));
                });
                mediaPlayer.getVideoIO()
                        .getIndexObservable()
                        .subscribe(new Observer<VideoIndex>() {
                            @Override
                            public void onSubscribe(Disposable disposable) {
                                disposables.add(disposable);
                            }

                            @Override
                            public void onNext(VideoIndex videoIndex) {
                                videoIndex.getElapsedTime()
                                        .ifPresent(d -> {
                                            Platform.runLater(() -> {
                                                getScrubber().setValue(d.toMillis());
                                                elapsedTimeLabel.setText(formatSeconds(d.getSeconds()));

                                                // Set recorded date
                                                Optional<Instant> time = calculateRecordedTimestamp(mediaPlayer.getMedia(), videoIndex);
                                                if (time.isPresent()) {
                                                    recordedTimestampLabel.setText(timeFormatter.format(time.get()));
                                                }
                                                else {
                                                    recordedTimestampLabel.setText("--:--:--");
                                                }

                                            });
                                        });

                            }

                            @Override
                            public void onError(Throwable throwable) { }

                            @Override
                            public void onComplete() { }
                        });

                mediaPlayer.getVideoIO()
                        .getStateObservable()
                        .subscribe(new Observer<VideoState>() {
                            @Override
                            public void onSubscribe(Disposable disposable) {
                                disposables.add(disposable);
                            }

                            @Override
                            public void onNext(VideoState videoState) {
                                updateState(videoState);
                            }

                            @Override
                            public void onError(Throwable throwable) {

                            }

                            @Override
                            public void onComplete() {

                            }
                        });
            }
        }
    }

    private String formatSeconds(long seconds) {
        return String.format("%02d:%02d", (seconds % 3600) / 60, (seconds % 60));
    }

    private void updateState(VideoState videoState) {
        this.videoState = videoState;
        Text icon = videoState.isStopped() ? playIcon : pauseIcon;
        Platform.runLater(() -> getPlayButton().setGraphic(icon));
    }

    private Optional<Instant> calculateRecordedTimestamp(Media media, VideoIndex videoIndex) {
        if (media == null
                || media.getStartTimestamp() == null
                || videoIndex == null
                || !videoIndex.getElapsedTime().isPresent()) {
            return Optional.empty();
        }
        else {
            Instant startTimestamp = media.getStartTimestamp();
            Duration elapsedTime = videoIndex.getElapsedTime().get();
            Instant recordedTimestamp = startTimestamp.plus(elapsedTime);
            return Optional.of(recordedTimestamp);
        }
    }

}
