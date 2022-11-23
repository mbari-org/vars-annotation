package org.mbari.vars.ui;

import javafx.scene.Scene;
import javafx.scene.control.TableView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import org.mbari.vars.core.EventBus;
import org.mbari.vars.services.model.Media;
import org.mbari.vars.ui.mediaplayers.MediaPlayer;
import org.mbari.vars.ui.messages.*;
import org.mbari.vars.services.model.Annotation;
import org.mbari.vars.ui.javafx.AppPaneController;
import org.mbari.vcr4j.VideoError;
import org.mbari.vcr4j.VideoState;
import org.mbari.vcr4j.sharktopoda.commands.SharkCommands;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Defines the key mappings that are registered on the main window (javafx Scene)
 * @author Brian Schlining
 * @since 2019-04-18T17:19:00
 */
public class KeyMapping {

    private final UIToolBox toolBox;
    private final Scene scene;
    private final AppPaneController paneController;
    private final EventBus eventBus;
    private Duration jump = Duration.ofSeconds(1);
    private Duration miniJump = Duration.ofMillis(100);
    //private final MediaPlayerDecorator mediaPlayerDecorator;

    private final KeyCombination.Modifier osModifier = KeyCombination.SHORTCUT_DOWN;

    private final KeyCombination.Modifier altModifier = KeyCombination.ALT_DOWN;

    private final KeyCombination.Modifier shiftModifier = KeyCombination.SHIFT_DOWN;


    public KeyMapping(UIToolBox toolBox, Scene scene, AppPaneController paneController) {
        this.toolBox = toolBox;
        this.scene = scene;
        this.paneController = paneController;
        eventBus = toolBox.getEventBus();
//        mediaPlayerDecorator = new MediaPlayerDecorator(toolBox);

        // Listen for changes to timeJump in Data. timeJump can be changed in a
        // SharktopodaSettingsPaneController
        toolBox.getData().timeJumpProperty().addListener((obj, oldV, newV) -> {
            if (newV != null) {
                setTimeJump(newV.intValue());
            }
        });
        setTimeJump(toolBox.getData().getTimeJump());
        apply();
    }

    private void setTimeJump(int millis) {
        Duration d = Duration.ofMillis(millis);
        jump = d;
        miniJump = d.dividedBy(10);
    }

    private void apply() {

        Map<KeyCodeCombination, Runnable> map = buildKeyMap();
        for (Map.Entry<KeyCodeCombination, Runnable> e : map.entrySet()) {
            scene.getAccelerators().put(e.getKey(), e.getValue());
        }

    }

    private Map<KeyCodeCombination, Runnable> buildKeyMap() {
        Map<KeyCodeCombination, Runnable> map = buildBaseKeyMap();
        map.putAll(buildVcr4jKeyMap());
        return map;
    }

    private Map<KeyCodeCombination, Runnable> buildBaseKeyMap() {

        Map<KeyCodeCombination, Runnable> map = new HashMap<>();

        map.put(new KeyCodeCombination(KeyCode.DOWN, osModifier, shiftModifier), () -> {
            var selectionModel = paneController.getAnnotationTableController()
                    .getTable()
                    .getSelectionModel();
            int idx = selectionModel.getMaxSelectionIndex() + 1;
            selectionModel.setSelectionInterval(idx, idx);
        });

        map.put(new KeyCodeCombination(KeyCode.UP, osModifier, shiftModifier), () -> {
            var selectionModel = paneController.getAnnotationTableController()
                    .getTable()
                    .getSelectionModel();
            int idx = selectionModel.getMinSelectionIndex() - 1;
            selectionModel.setSelectionInterval(idx, idx);
        });

        map.put(new KeyCodeCombination(KeyCode.S, osModifier, shiftModifier), () -> {
            var table = paneController.getAnnotationTableController()
                    .getTable();
            int row = table.getSelectedRow();
            var selectedItem = (Annotation) table.getValueAt(row, 0);
            Media media = toolBox.getData().getMedia();
            SeekMsg.seek(media, selectedItem, eventBus);
        });

        map.put(new KeyCodeCombination(KeyCode.N, osModifier),
                () -> eventBus.send(new NewAnnotationMsg()));

        map.put(new KeyCodeCombination(KeyCode.R, osModifier),
                () -> eventBus.send(new CopyAnnotationMsg()));

        map.put(new KeyCodeCombination(KeyCode.T, osModifier),
                () -> eventBus.send(new DuplicateAnnotationMsg()));

        map.put(new KeyCodeCombination(KeyCode.F, osModifier),
                () -> eventBus.send(new FramecaptureMsg()));

        map.put(new KeyCodeCombination(KeyCode.DELETE, osModifier),
                () -> eventBus.send(new DeleteAnnotationsMsg()));

        map.put(new KeyCodeCombination(KeyCode.Z, osModifier),
                () -> eventBus.send(new UndoMsg()));

        map.put(new KeyCodeCombination(KeyCode.Z, osModifier, KeyCombination.SHIFT_DOWN),
                () -> eventBus.send(new RedoMsg()));

        return map;

    }

    private final Map<KeyCodeCombination, Runnable> buildVcr4jKeyMap() {

        Map<KeyCodeCombination, Runnable> map = new HashMap<>();

        Runnable playToggle = () ->
            Optional.ofNullable(toolBox.getMediaPlayer()).ifPresent(mediaPlayer ->
                mediaPlayer.requestIsStopped()
                        .thenAccept(stopped -> {
                            if (stopped) {
                                mediaPlayer.play();
                            } else {
                                mediaPlayer.stop();
                            }
                        }));
        map.put(new KeyCodeCombination(KeyCode.SPACE, KeyCombination.CONTROL_DOWN), playToggle);
        map.put(new KeyCodeCombination(KeyCode.K, osModifier), playToggle);
        map.put(new KeyCodeCombination(KeyCode.DOWN, osModifier), playToggle);

        Consumer<MediaPlayer<?, ?>> jumpBack = mp -> {
            mp.stop();
            mp.requestVideoIndex().thenAccept(videoIndex ->
                    videoIndex.getElapsedTime().ifPresent(elapsedTime ->
                            mp.seek(elapsedTime.minus(jump))
                    ));
        };
        map.put(new KeyCodeCombination(KeyCode.COMMA, osModifier),
                execute(jumpBack));
        map.put(new KeyCodeCombination(KeyCode.LEFT, osModifier),
                execute(jumpBack));
        map.put(new KeyCodeCombination(KeyCode.M, osModifier),
                execute(mp -> {
                    mp.stop();
                    mp.requestVideoIndex().thenAccept(videoIndex ->
                            videoIndex.getElapsedTime().ifPresent(elapsedTime ->
                                    mp.seek(elapsedTime.minus(miniJump))
                            ));
                }));

        map.put(new KeyCodeCombination(KeyCode.PERIOD, osModifier),
                execute(mp -> {
                    mp.stop();
                    mp.requestVideoIndex().thenAccept(videoIndex ->
                            videoIndex.getElapsedTime().ifPresent(elapsedTime ->
                                    mp.seek(elapsedTime.plus(jump))
                            ));
                }));


        double slowRate = 0.03;
        map.put(new KeyCodeCombination(KeyCode.J, osModifier),
                execute((mp) -> mp.shuttle(-slowRate)));

        map.put(new KeyCodeCombination(KeyCode.L, osModifier),
                execute((mp) -> mp.shuttle(slowRate)));
        map.put(new KeyCodeCombination(KeyCode.RIGHT, osModifier),
                execute((mp) -> mp.shuttle(slowRate)));

        Consumer<MediaPlayer<?, ?>> frameAdvance = mp -> {
            mp.stop();
            mp.getVideoIO().send(SharkCommands.FRAMEADVANCE);
        };
        map.put(new KeyCodeCombination(KeyCode.I, osModifier),
                execute(frameAdvance));
        map.put(new KeyCodeCombination(KeyCode.UP, osModifier),
                execute(frameAdvance));

        double fastRate = 0.06;
        map.put(new KeyCodeCombination(KeyCode.SEMICOLON, osModifier),
                execute((mp) -> mp.shuttle(fastRate)));

        map.put(new KeyCodeCombination(KeyCode.G, osModifier),
                execute((mp) -> mp.shuttle(-fastRate)));

//        map.put(new KeyCodeCombination(KeyCode.LESS, osModifier),
//                execute(MediaPlayer::rewind));
//
//        map.put(new KeyCodeCombination(KeyCode.GREATER, osModifier),
//                execute(MediaPlayer::fastForward));

//        map.put(new KeyCodeCombination(KeyCode.Y, altModifier),
//                mediaPlayerDecorator::seekPreviousAnnotation);
//        map.put(new KeyCodeCombination(KeyCode.P, altModifier),
//                mediaPlayerDecorator::seekPreviousAnnotation);

        return map;

    }

    private Runnable execute(Consumer<MediaPlayer<? extends VideoState, ? extends VideoError>> fn) {
        return () -> Optional.ofNullable(toolBox.getMediaPlayer()).ifPresent(fn);
    }



}
