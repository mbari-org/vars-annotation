package org.mbari.m3.vars.annotation;

import javafx.scene.Scene;
import javafx.scene.control.TableView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import org.mbari.m3.vars.annotation.mediaplayers.MediaPlayer;
import org.mbari.m3.vars.annotation.messages.*;
import org.mbari.m3.vars.annotation.model.Annotation;
import org.mbari.m3.vars.annotation.ui.AppPaneController;
import org.mbari.util.SystemUtilities;
import org.mbari.vcr4j.VideoError;
import org.mbari.vcr4j.VideoState;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Brian Schlining
 * @since 2019-04-18T17:19:00
 */
public class KeyMapping {

    public static void apply(UIToolBox toolBox,
                             Scene scene,
                             AppPaneController paneController) {

        Map<KeyCodeCombination, Runnable> map = buildKeyMap(toolBox, scene, paneController);
        for (Map.Entry<KeyCodeCombination, Runnable> e : map.entrySet()) {
            scene.getAccelerators().put(e.getKey(), e.getValue());
        }

    }

    private static Map<KeyCodeCombination, Runnable> buildKeyMap(UIToolBox toolBox,
                                     Scene scene,
                                     AppPaneController paneController) {

        final EventBus eventBus = toolBox.getEventBus();

        KeyCombination.Modifier osModifier = SystemUtilities.isMacOS() ?
                KeyCombination.META_DOWN : KeyCombination.CONTROL_DOWN;

        Map<KeyCodeCombination, Runnable> map = new HashMap<>();
        map.put(new KeyCodeCombination(KeyCode.SPACE, KeyCombination.CONTROL_DOWN), () -> {
            MediaPlayer<? extends VideoState, ? extends VideoError> mediaPlayer = toolBox.getMediaPlayer();
            if (mediaPlayer != null) {
                mediaPlayer.requestIsPlaying()
                        .thenAccept(playing -> {
                            if (playing) {
                                mediaPlayer.stop();
                            } else {
                                mediaPlayer.play();
                            }
                        });
            }
        });

        map.put(new KeyCodeCombination(KeyCode.DOWN, osModifier), () -> {
            TableView.TableViewSelectionModel<Annotation> selectionModel = paneController.getAnnotationTableController()
                    .getTableView()
                    .getSelectionModel();
            int idx = selectionModel.getSelectedIndex();
            selectionModel.clearSelection();
            selectionModel.select(idx + 1);
        });

        map.put(new KeyCodeCombination(KeyCode.UP, osModifier), () -> {
            TableView.TableViewSelectionModel<Annotation> selectionModel = paneController.getAnnotationTableController()
                    .getTableView()
                    .getSelectionModel();
            int idx = selectionModel.getSelectedIndex();
            selectionModel.clearSelection();
            selectionModel.select(idx - 1);
        });

        map.put(new KeyCodeCombination(KeyCode.N, osModifier),
                () -> eventBus.send(new NewAnnotationMsg()));

        map.put(new KeyCodeCombination(KeyCode.G, osModifier),
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

}
