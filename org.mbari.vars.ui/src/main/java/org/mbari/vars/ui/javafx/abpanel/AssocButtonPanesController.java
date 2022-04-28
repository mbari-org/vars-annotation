package org.mbari.vars.ui.javafx.abpanel;

import java.util.ResourceBundle;

import org.mbari.vars.ui.UIToolBox;
import org.mbari.vars.ui.javafx.Icons;
import org.mbari.vars.core.util.Preconditions;

import com.jfoenix.controls.JFXButton;
import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Pane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * @author Kevin Barnard
 * @since 2022-03-15T11:54:00
 */
public class AssocButtonPanesController {
    private final UIToolBox toolBox;
    private final ResourceBundle i18n;

    private BorderPane root;
    private VBox controlPane;
    private AssocButtonPaneController assocButtonPaneController;

    private Button addButton;
    private Button lockButton;
    private AssocSelectionDialogController dialogController;

    private final Logger log = LoggerFactory.getLogger(getClass());

    private BooleanProperty lockProperty = new SimpleBooleanProperty(false);

    public AssocButtonPanesController(UIToolBox toolBox) {
        Preconditions.checkNotNull(toolBox, "The UIToolbox arg can not be null");
        this.toolBox = toolBox;
        this.i18n = toolBox.getI18nBundle();
    }

    public AssocButtonPaneController getButtonPaneController() {
        if (assocButtonPaneController == null) {
            assocButtonPaneController = new AssocButtonPaneController(toolBox);
        }
        return assocButtonPaneController;
    }

    public AssocSelectionDialogController getDialogController() {
        if (dialogController == null) {
            dialogController = AssocSelectionDialogController.newInstance(toolBox);
        }
        return dialogController;
    }

    public Pane getRoot() {
        if (root == null) {
            root = new BorderPane();
            root.setCenter(getButtonPaneController().getPane());
            root.setRight(getControlPane());
        }
        return root;
    }

    private Button getAddButton() {
        if (addButton == null) {
            addButton = new JFXButton();
            String tooltip = toolBox.getI18nBundle().getString("abpane.addbutton");
            Text icon = Icons.ADD.standardSize();
            addButton.setTooltip(new Tooltip(tooltip));
            addButton.setGraphic(icon);
            addButton.setOnAction(v -> {
                Dialog<NamedAssociation> dialog = getDialogController().getDialog();
                getDialogController().requestFocus();
                Optional<NamedAssociation> opt = dialog.showAndWait();
                opt.ifPresent(assocButtonPaneController::addButton);
                getDialogController().reset();
            });
        }
        return addButton;
    }

    private Button getLockButton() {
        if (lockButton == null) {
            lockButton = new JFXButton();
            Text lockIcon = Icons.LOCK.standardSize();
            Text unlockIcon = Icons.LOCK_OPEN.standardSize();
            lockButton.setGraphic(lockIcon);
            lockButton.setOnAction(e -> {
                boolean v = lockProperty.get();
                lockProperty.set(!v);
            });
            lockProperty.addListener((obj, oldV, newV) -> {
                Text icon = newV ? lockIcon : unlockIcon;
                lockButton.setGraphic(icon);
                getButtonPaneController().setLocked(newV);
            });
            lockProperty.set(true);
        }
        return lockButton;
    }

    private Pane getControlPane() {
        if (controlPane == null) {
            controlPane = new VBox(getAddButton(), getLockButton());
        }
        return controlPane;
    }

}