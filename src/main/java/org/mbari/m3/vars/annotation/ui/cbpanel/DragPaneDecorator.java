package org.mbari.m3.vars.annotation.ui.cbpanel;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.Pane;
import javafx.util.Duration;
import org.mbari.m3.vars.annotation.EventBus;
import org.mbari.m3.vars.annotation.services.ConceptService;

import javax.inject.Inject;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Decorates a pane to accept drag and drop of text to create buttons. Also,
 * allows buttons to be dragged and reordered. It also has a locked method that
 * prevents buttons from being reordered or added.
 *
 * @author Brian Schlining
 * @since 2017-05-17T15:15:00
 */
public class DragPaneDecorator {

    private final ConceptButtonFactory conceptButtonFactory;
    private final ConceptService conceptService;
    private boolean locked = false;

    @Inject
    public DragPaneDecorator(ConceptService conceptService, EventBus eventBus, ResourceBundle i18n) {
        this.conceptService = conceptService;
        conceptButtonFactory = new ConceptButtonFactory(conceptService, eventBus, i18n);
    }


    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    public Pane decorate(Pane pane) {
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
                // Create a button and add to pane
                if (!locked) {
                    addButton(db.getString(), pane, evt.getScreenX(), evt.getScreenY());
                }
                success = true;
            }
            // Let the source know whether the string was dropped successfully
            evt.setDropCompleted(success);
            evt.consume();
        });

        return pane;
    }

    public void addButton(String name, Pane pane) {
        Optional<Button> existingButton = findButtonByName(name, pane);
        existingButton.ifPresent(btn -> pane.getChildren().remove(btn));
        addButton(name, pane, pane.getChildren().size());
    }

    public void addButton(final String name, final Pane pane, final int idx) {
        // Make sure that it's a name found in the knowledgebase
        //System.out.println("Add button named " + name + " at idx = " + idx);
        Optional<Button> existingButton = findButtonByName(name, pane);
        AtomicInteger insertAt = new AtomicInteger(idx);
        existingButton.ifPresent(button -> {
            int j = pane.getChildren().indexOf(button);
            if (j != idx) {
                if (j < idx) {
                    insertAt.set(idx - 1);
                }
            }
            pane.getChildren().remove(button);
        });

        final Button button = buildButton(name, pane);
        int i = insertAt.get();
        if (i >= pane.getChildren().size()) {
            pane.getChildren().add(button);
        }
        else {
            pane.getChildren().add(insertAt.get(), button);
        }

        // Flash the newly added button
        // TODO pull flash color out to CSS. Not this isn't working yet. Look into it
        Timeline t = new Timeline(
                new KeyFrame(Duration.seconds(0.25), e -> button.setStyle("")),
                new KeyFrame(Duration.seconds(0.25), e -> button.setStyle("-fx-background-color: red")),
                new KeyFrame(Duration.seconds(0.25), e -> button.setStyle("")));
        t.setCycleCount(6);
        t.setAutoReverse(true);
        t.setOnFinished(event -> button.setStyle(""));
        t.play();

        conceptService.findAllNames()
                .thenApply(names -> {
                    if (!names.contains(name)) {
                        pane.getChildren().remove(button);
                    }
                    return null;
                });

    }

    public void addButton(String name, Pane pane, double screenX, double screenY) {
        int idx = positionToButtonIndex(pane, screenX, screenY);
        addButton(name, pane, idx);
    }

    private Button buildButton(String name, Pane pane) {
        Button button = conceptButtonFactory.build(name);
        button.setOnDragDetected(evt -> {
            if (name != null) {
                // Drag the string name to some target.
                Dragboard db = button.startDragAndDrop(TransferMode.MOVE);
                ClipboardContent content = new ClipboardContent();
                content.putString(name);
                db.setContent(content);
                evt.consume();
            }
        });

        return button;
    }

    /**
     * Get the index into the panes children that this node should be inserted to.
     * @param screenX Drop screen x position
     * @param screenY Drop screen y position
     * @param pane The pane of interest.
     * @return The index to insert the button into the panes children.
     */
    private int positionToButtonIndex(Pane pane, double screenX, double screenY) {
        int idx = pane.getChildren().size();
        for (Node node: pane.getChildren()) {
            Bounds bounds = node.localToScreen(node.getBoundsInLocal());
            if (bounds.contains(screenX, screenY)) {
                idx = pane.getChildren().indexOf(node);
            }
        }
        return idx;
    }

    private Optional<Button> findButtonByName(String name, Pane pane) {
        return pane.getChildren()
                .stream()
                .filter(n -> n instanceof Button)
                .map(n -> (Button) n)
                .filter(n -> n.getText().equals(name))
                .findAny();
    }


}
