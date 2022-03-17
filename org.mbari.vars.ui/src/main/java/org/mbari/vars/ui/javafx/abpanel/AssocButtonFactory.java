package org.mbari.vars.ui.javafx.abpanel;

import com.jfoenix.controls.JFXButton;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Pane;
import org.mbari.vars.core.EventBus;
import org.mbari.vars.ui.UIToolBox;
import org.mbari.vars.ui.commands.CreateAssociationsCmd;
import org.mbari.vars.services.model.Annotation;
import org.mbari.vars.services.model.Association;

import java.util.ArrayList;
import java.util.Optional;

/**
 * @author Brian Schlining
 * @since 2017-10-11T15:30:00
 */
public class AssocButtonFactory {

    private final UIToolBox toolBox;
    private static final String styleClass = "abpanel-button";


    public AssocButtonFactory(UIToolBox toolBox) {
        this.toolBox = toolBox;
    }

    public Button build(String name, Association association) {
        return build(new NamedAssociation(association.getLinkName(), association.getToConcept(), association.getLinkValue(), name));
    }

    public Button build(NamedAssociation namedAssociation) {

        EventBus eventBus = toolBox.getEventBus();

        String name = namedAssociation.getName();
        Association association = namedAssociation.asAssociation();

        Button button = new JFXButton(name);
        button.setUserData(namedAssociation);
        button.getStyleClass().add(styleClass);
        button.setOnAction(event -> {
            ArrayList<Annotation> annotations = new ArrayList<>(toolBox.getData().getSelectedAnnotations());
            eventBus.send(new CreateAssociationsCmd(association, annotations));
        });

        button.setTooltip(new Tooltip(AssocToString.asString(namedAssociation)));

        ContextMenu contextMenu = new ContextMenu();
        MenuItem deleteButton = new MenuItem(toolBox.getI18nBundle().getString("cbpanel.conceptbutton.delete"));
        deleteButton.setOnAction(event ->
                ((Pane) button.getParent()).getChildren().remove(button));
        contextMenu.getItems().addAll(deleteButton);
        button.setContextMenu(contextMenu);

        button.setOnDragDetected(evt -> {
            if (button.getUserData() != null) {
                // Drag the string name to some target.
                Dragboard db = button.startDragAndDrop(TransferMode.MOVE);
                ClipboardContent content = new ClipboardContent();
                // Drag contents are "linkName | toConcept | linkValue | buttonName"
                content.putString(((NamedAssociation) button.getUserData()).toString());
                db.setContent(content);
                evt.consume();
            }
        });

        return button;

    }

    Optional<Button> buildFromString(String namedAssociationString) {
        Optional<NamedAssociation> opt = NamedAssociation.parseNamed(namedAssociationString);
        if (opt.isPresent()) {
            return Optional.of(build(opt.get()));
        }
        return Optional.empty();
    }

    public static boolean isAssocButton(Node node) {
        boolean isBtn = false;
        if (node instanceof Button) {
            Button button = (Button) node;
            isBtn = button.getStyleClass().contains(styleClass)
                    && button.getUserData() instanceof NamedAssociation;
        }
        return isBtn;
    }

}
