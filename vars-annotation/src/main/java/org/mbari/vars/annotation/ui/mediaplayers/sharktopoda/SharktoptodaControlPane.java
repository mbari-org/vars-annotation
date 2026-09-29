package org.mbari.vars.annotation.ui.mediaplayers.sharktopoda;

import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringBinding;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import org.mbari.vars.annotation.etc.jdk.Fonts;
import org.mbari.vars.annotation.etc.jdk.Loggers;
import org.mbari.vars.annotation.ui.UIToolBox;
import org.mbari.vars.annotation.ui.javafx.controls.JFXSlider;
import org.mbari.vars.annotation.ui.mediaplayers.MediaPlayer;
import org.mbari.vars.annotation.ui.javafx.Icons;
import org.mbari.vars.vampiresquid.sdk.r1.models.Media;
import org.mbari.vcr4j.VideoError;
import org.mbari.vcr4j.VideoIndex;
import org.mbari.vcr4j.VideoState;
import org.mbari.vcr4j.commands.RemoteCommands;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * @author Brian Schlining
 * @since 2017-08-14T16:48:00
 */
public class SharktoptodaControlPane extends Pane {
    private static final Loggers log = new Loggers(SharktoptodaControlPane.class);

    // The size of this control is fixed
    private static final double WIDTH = 440;
    private static final double HEIGHT = 80;
    private static final double TIME_FONT_SIZE = 13;

    private final UIToolBox toolBox;
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss.SSS'Z'")
            .withZone(ZoneId.of("UTC"));
//    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofLocalizedTime(FormatStyle.FULL)
//            .withZone(ZoneId.of("UTC"));
    JFXSlider speedSlider;
    JFXSlider scrubber;
    Button rewindButton;
    Button fastForwardButton;
    Button playButton;
    Button frameAdvanceButton;
    Label elapsedTimeLabel = new Label("00:00:00");
    Label durationLabel = new Label("00:00:00");
    Label recordedTimestampLabel = new Label("--:--:--");
    private Color color = Color.LIGHTGRAY;
//    Text speedUpIcon = glyphsFactory.createIcon(MaterialIcon.ADD, "20px");
//    Text speedDownIcon = glyphsFactory.createIcon(MaterialIcon.REMOVE, "20px");
    private Text speedUpIcon = Icons.ADD.size(20);
    private Text speedDownIcon = Icons.REMOVE.size(20);
    private volatile MediaPlayer<? extends VideoState, ? extends VideoError> mediaPlayer;
    private final List<Disposable> disposables = new ArrayList<>();
//    private Text playIcon = glyphsFactory.createIcon(MaterialIcon.PLAY_ARROW, "50px");
//    private Text pauseIcon = glyphsFactory.createIcon(MaterialIcon.PAUSE, "50px");
    private Text playIcon = Icons.PLAY_ARROW.size(44);
    private Text pauseIcon = Icons.PAUSE.size(44);
    private volatile VideoState videoState;
    //private final Observer

    public SharktoptodaControlPane(UIToolBox toolBox) {
        this.toolBox = toolBox;
        setMinSize(WIDTH, HEIGHT);
        setPrefSize(WIDTH, HEIGHT);
        setMaxSize(WIDTH, HEIGHT);
        speedDownIcon.setFill(color);
        speedUpIcon.setFill(color);
        elapsedTimeLabel.setTextFill(color);
        durationLabel.setTextFill(color);
        recordedTimestampLabel.setTextFill(color);
        // Monospace so the digits line up and the labels don't jitter as the time changes. We look up
        // a good platform font (SF Mono, Consolas, ...) instead of using CSS, which can't fall back.
        var timeFont = Fonts.monospaced(TIME_FONT_SIZE);
        recordedTimestampLabel.setFont(timeFont);
        elapsedTimeLabel.setFont(timeFont);
        durationLabel.setFont(timeFont);
        getStylesheets().addAll(toolBox.getStylesheets());

        doLayout();
    }

    /**
     * Layout (the overall size is fixed):
     * <pre>
     *  [speed slider]     [rewind] [play] [ffwd] [step]          [recorded time]
     *  [elapsed] [------------------ scrubber -------------------] [duration]
     * </pre>
     */
    private void doLayout() {
        double sideWidth = 116;   // width of the speed slider / recorded time columns
        double timeWidth = 70;    // width of the elapsed / duration labels (8 monospaced chars plus label padding)

        // --- Top row: speed | transport controls | recorded time
        Slider speed = getSpeedSlider();
        speed.setMinWidth(sideWidth);
        speed.setPrefWidth(sideWidth);
        speed.setMaxWidth(sideWidth);

        sizeButton(getRewindButton(), 34, 34);
        sizeButton(getPlayButton(), 48, 46);
        sizeButton(getFastForwardButton(), 34, 34);
        sizeButton(getFrameAdvanceButton(), 34, 34);
        HBox transport = new HBox(4,
                getRewindButton(),
                getPlayButton(),
                getFastForwardButton(),
                getFrameAdvanceButton());
        transport.setAlignment(Pos.CENTER);

        recordedTimestampLabel.setMinWidth(sideWidth);
        recordedTimestampLabel.setPrefWidth(sideWidth);
        recordedTimestampLabel.setAlignment(Pos.CENTER_RIGHT);

        GridPane topRow = new GridPane();
        ColumnConstraints left = new ColumnConstraints(sideWidth, sideWidth, sideWidth);
        left.setHalignment(HPos.LEFT);
        ColumnConstraints center = new ColumnConstraints();
        center.setHgrow(Priority.ALWAYS);
        center.setHalignment(HPos.CENTER);
        ColumnConstraints right = new ColumnConstraints(sideWidth, sideWidth, sideWidth);
        right.setHalignment(HPos.RIGHT);
        topRow.getColumnConstraints().addAll(left, center, right);
        topRow.setAlignment(Pos.CENTER_LEFT);
        topRow.add(speed, 0, 0);
        topRow.add(transport, 1, 0);
        topRow.add(recordedTimestampLabel, 2, 0);
        GridPane.setValignment(speed, javafx.geometry.VPos.CENTER);
        GridPane.setValignment(transport, javafx.geometry.VPos.CENTER);
        GridPane.setValignment(recordedTimestampLabel, javafx.geometry.VPos.CENTER);

        // --- Bottom row: elapsed | scrubber | duration
        elapsedTimeLabel.setMinWidth(timeWidth);
        elapsedTimeLabel.setPrefWidth(timeWidth);
        elapsedTimeLabel.setAlignment(Pos.CENTER_RIGHT);
        durationLabel.setMinWidth(timeWidth);
        durationLabel.setPrefWidth(timeWidth);
        durationLabel.setAlignment(Pos.CENTER_LEFT);
        Slider scrubber = getScrubber();
        scrubber.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(scrubber, Priority.ALWAYS);
        HBox bottomRow = new HBox(8, elapsedTimeLabel, scrubber, durationLabel);
        bottomRow.setAlignment(Pos.CENTER);

        VBox content = new VBox(2, topRow, bottomRow);
        content.setPadding(new Insets(4, 12, 6, 12));
        content.setPrefSize(WIDTH, HEIGHT);
        content.setMinSize(WIDTH, HEIGHT);
        content.setMaxSize(WIDTH, HEIGHT);
        VBox.setVgrow(topRow, Priority.ALWAYS);
        getChildren().add(content);
    }

    private static void sizeButton(Button button, double width, double height) {
        button.setMinSize(width, height);
        button.setPrefSize(width, height);
        button.setMaxSize(width, height);
    }

    protected Slider getSpeedSlider() {
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
//            Text icon = glyphsFactory.createIcon(MaterialIcon.KEYBOARD_ARROW_RIGHT, "30px");
            Text icon = Icons.KEYBOARD_ARROW_RIGHT.standardSize();
            icon.setFill(color);
            frameAdvanceButton = new Button();
            frameAdvanceButton.setGraphic(icon);
            
            frameAdvanceButton.setOnAction(e -> {
                mediaPlayer.stop();
                mediaPlayer.getVideoIO()
                        .send(RemoteCommands.FRAMEADVANCE);
            });
        }
        return frameAdvanceButton;
    }

    protected Button getFastForwardButton() {
        if (fastForwardButton == null) {
//            Text icon = glyphsFactory.createIcon(MaterialIcon.FAST_FORWARD, "30px");
            Text icon = Icons.FAST_FORWARD.standardSize();
            icon.setFill(color);
            fastForwardButton = new Button();
            fastForwardButton.setGraphic(icon);
            
            fastForwardButton.setOnAction(e -> {
                if (mediaPlayer != null) {
                    double speed = getSpeedSlider().getValue() / 1000D / Constants.MAX_SHUTTLE_RATE;
                    mediaPlayer.shuttle(speed);
                }
            });
        }
        return fastForwardButton;
    }

    protected Button getRewindButton() {
        if (rewindButton == null) {
//            Text icon = glyphsFactory.createIcon(MaterialIcon.FAST_REWIND, "30px");
            Text icon = Icons.FAST_REWIND.standardSize();
            icon.setFill(color);
            rewindButton = new Button();
            rewindButton.setGraphic(icon);
            
            rewindButton.setOnAction(e -> {
                if (mediaPlayer != null) {
                    double speed = getSpeedSlider().getValue() / 1000D / Constants.MAX_SHUTTLE_RATE;
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
            playButton = new Button();
            playButton.setGraphic(playIcon);
            
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

    public Slider getScrubber() {
        if (scrubber == null) {
            // The scrubber represents the position into the video in Millisecs
            scrubber = new JFXSlider(0, 1000, 0);
            scrubber.setPrefWidth(200);

            // TODO THis is useful with JFoenix. Can I recreate in JavaFX?
            StringBinding binding = Bindings.createStringBinding(() ->
                    formatSeconds(Math.round(scrubber.getValue() / 1000D)),
                    scrubber.valueProperty());
            scrubber.setValueFactory(p -> binding);
            scrubber.valueProperty().addListener(observable -> {
                if (scrubber.isValueChanging()) {
                    long millis = Math.round(scrubber.getValue());
                    mediaPlayer.seek(Duration.ofMillis(millis));
                }
            });
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
            // TODO when a video has no duration, disable the scrubber and notify the user (tooltip?)
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
//                                log.info(new VideoIndexAsString(videoIndex).toString());
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
        return String.format("%02d:%02d:%02d", seconds / 3600, (seconds % 3600) / 60, seconds % 60);
//        return String.format("%02d:%02d", (seconds % 3600) / 60, (seconds % 60));
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
                || videoIndex.getElapsedTime().isEmpty()) {
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
