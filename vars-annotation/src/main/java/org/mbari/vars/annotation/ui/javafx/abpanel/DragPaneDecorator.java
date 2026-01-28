package org.mbari.vars.ui.javafx.abpanel;

import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.Pane;
import org.mbari.vars.ui.UIToolBox;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Drag pane decorator for the association button pane. 
 * Matches org.mbari.vars.ui.javafx.cbpanel.DragPaneDecorator
 * 
 * @author Kevin Barnard
 * @since 2022-03-15T14:30:00
 */
public class DragPaneDecorator {
    private final AssocButtonFactory assocButtonFactory;
    private final UIToolBox toolBox;
    private final AssocButtonPrefs assocButtonPrefs;
    private volatile boolean locked = false;

    public DragPaneDecorator(UIToolBox toolBox, AssocButtonPrefs assocButtonPrefs) {
        this.toolBox = toolBox;
        this.assocButtonPrefs = assocButtonPrefs;
        assocButtonFactory = new AssocButtonFactory(toolBox);
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
                // Try to create a button, and add it to the pane
                if (!locked) {
                    Optional<Button> opt = addButton(db.getString(), pane, evt.getScreenX(), evt.getScreenY());
                    success = opt.isPresent();
                }
            }
            evt.setDropCompleted(success);
            evt.consume();
        });

        return pane;
    }

    public Optional<Button> addButton(String namedAssociationString, Pane pane) {
        Optional<Button> existingButton = findButtonByNamedAssociationString(namedAssociationString, pane);
        existingButton.ifPresent(btn -> pane.getChildren().remove(btn));
        return addButton(namedAssociationString, pane, pane.getChildren().size());
    }

    public Optional<Button> addButton(final String namedAssociationString, final Pane pane, final int idx) {
        Optional<Button> existingButton = findButtonByNamedAssociationString(namedAssociationString, pane);
        AtomicInteger insertAt = new AtomicInteger(idx);
        existingButton.ifPresent(button -> {
            int j = pane.getChildren().indexOf(button);
            if (j != idx) {
                if (j < idx) {
                    insertAt.set(j);
                }
            }
            pane.getChildren().remove(button);
        });

        Optional<Button> opt = buildButton(namedAssociationString);
        opt.ifPresent(button -> {
            int i = insertAt.get();
            if (i >= pane.getChildren().size()) {
                pane.getChildren().add(button);
            } else {
                pane.getChildren().add(insertAt.get(), button);
            }
        });

        return opt;
    }

    public Optional<Button> addButton(String namedAssociationString, Pane pane, double screenX, double screenY) {
        int idx = positionToButtonIndex(pane, screenX, screenY);
        return addButton(namedAssociationString, pane, idx);
    }

    private Optional<Button> buildButton(String namedAssociationString) {
        return assocButtonPrefs.findPreferences()
                .flatMap(prefs -> assocButtonFactory.buildFromString(namedAssociationString, prefs));
    }

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

    private Optional<Button> findButtonByNamedAssociationString(String namedAssociationString, Pane pane) {
        return pane.getChildren()
                .stream()
                .filter(node -> node instanceof Button)
                .map(node -> (Button) node)
                .filter(button -> button.getUserData().toString().equals(namedAssociationString))
                .findAny();
    }
}