package org.mbari.m3.vars.annotation.mediaplayers.sharktopoda;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXSlider;
import de.jensd.fx.glyphs.GlyphsFactory;
import de.jensd.fx.glyphs.materialicons.MaterialIcon;
import de.jensd.fx.glyphs.materialicons.utils.MaterialIconFactory;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableStringValue;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.util.Callback;
import org.mbari.m3.vars.annotation.UIToolBox;
import org.mbari.vcr4j.sharktopoda.SharktopodaVideoIO;


/**
 * @author Brian Schlining
 * @since 2017-08-14T16:48:00
 */
public class SharktoptodaControlPane extends Pane {

    JFXSlider speedSlider;
    JFXSlider scrubber;
    Button rewindButton;
    Button fastForwardButton;
    Button playButton;
    Label elapsedTimeLabel = new Label("00:00");
    Label durationLabel = new Label("00:00");
    private GlyphsFactory glyphsFactory = MaterialIconFactory.get();
    private Color color = Color.LIGHTGRAY;
    Text speedUpIcon = glyphsFactory.createIcon(MaterialIcon.ADD, "20px");
    Text speedDownIcon = glyphsFactory.createIcon(MaterialIcon.REMOVE, "20px");

    public SharktoptodaControlPane(UIToolBox toolBox) {
        setStyle("-fx-background-color: #263238;");
        setPrefSize(440, 80);
        speedDownIcon.setFill(color);
        speedUpIcon.setFill(color);
        elapsedTimeLabel.setTextFill(color);
        durationLabel.setTextFill(color);

        doLayout();

        getChildren().addAll(speedDownIcon,
                speedUpIcon,
                getRewindButton(),
                getPlayButton(),
                getFastForwardButton(),
                elapsedTimeLabel,
                durationLabel,
                getSpeedSlider(),
                getScrubber());
    }

    private void doLayout() {
        speedDownIcon.relocate(5, 25);
        getSpeedSlider().relocate(25, 19);
        speedUpIcon.relocate(90, 25);
        getRewindButton().relocate(145, 8);
        getPlayButton().relocate(195, 0);
        getFastForwardButton().relocate(260, 8);
        elapsedTimeLabel.relocate(9, 47);
        getScrubber().relocate(55, 47);
        durationLabel.relocate(395, 47);
    }

    protected JFXSlider getSpeedSlider() {
        if (speedSlider == null) {
            // We'll use 4 as the max shuttle rate for now
            double v = 4.0 * 1000;

            speedSlider = new JFXSlider(0, v, 2000);
            speedSlider.setPrefWidth(60);
            speedSlider.setIndicatorPosition(JFXSlider.IndicatorPosition.RIGHT);
        }
        return speedSlider;
    }

    protected Button getFastForwardButton() {
        if (fastForwardButton == null) {
            Text icon = glyphsFactory.createIcon(MaterialIcon.FAST_FORWARD, "30px");
            icon.setFill(color);
            fastForwardButton = new JFXButton();
            fastForwardButton.setGraphic(icon);
            fastForwardButton.setPrefSize(30, 30);
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
        }
        return rewindButton;
    }

    protected Button getPlayButton() {
        if (playButton == null) {
            Text icon = glyphsFactory.createIcon(MaterialIcon.PLAY_ARROW, "50px");
            icon.setFill(color);
            playButton = new JFXButton();
            playButton.setGraphic(icon);
            playButton.setPrefSize(30, 30);
        }
        return playButton;
    }

    public JFXSlider getScrubber() {
        if (scrubber == null) {
            scrubber = new JFXSlider(0, 1000, 0);
            scrubber.setPrefWidth(325);
        }
        return scrubber;
    }

    private String formatSeconds(long seconds) {
        return String.format("%d:%02d:%02d", seconds / 3600, (seconds % 3600) / 60, (seconds % 60));
    }


}
