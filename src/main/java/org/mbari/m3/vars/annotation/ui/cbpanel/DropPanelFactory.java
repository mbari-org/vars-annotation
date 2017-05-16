package org.mbari.m3.vars.annotation.ui.cbpanel;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.control.Button;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.FlowPane;
import javafx.util.Duration;

import java.util.Optional;

/**
 * https://docs.oracle.com/javafx/2/drag_drop/jfxpub-drag_drop.htm
 * @author Brian Schlining
 * @since 2017-05-16T16:30:00
 */
public class DropPanelFactory {

    public FlowPane build() {
        FlowPane pane = new FlowPane();
        pane.setOnDragOver(evt -> {
            if (evt.getGestureSource() != pane &&
                    evt.getDragboard().hasString()) {
                evt.acceptTransferModes(TransferMode.ANY);
            }
            evt.consume();
        });

        pane.setOnDragDropped(evt -> {
            Dragboard db = evt.getDragboard();
            boolean success = false;
            if (db.hasString()) {
                // TODO create a button and add to pane
                success = true;
            }
            // Let the source know whether the string was dropped successfully
            evt.setDropCompleted(success);
            evt.consume();
        });

        return pane;
    }

    private void handleDrop(String name, FlowPane pane) {
        Optional<Button> existingButton = pane.getChildren()
                .stream()
                .filter(n -> n instanceof Button)
                .map(n -> (Button) n)
                .filter(n -> n.getText().equals(name))
                .findAny();

        // If found flash button
        existingButton.ifPresent(btn -> {
            Timeline t = new Timeline(new KeyFrame(Duration.seconds(0.25), e -> {btn.setStyle("-fx-background-color: red");}),
                    new KeyFrame(Duration.seconds(0.25), e -> {btn.setStyle("");}));
            t.setCycleCount(4);
            t.play();
        });
        // TODO if not found create a new button and add to the pane
    }
}
