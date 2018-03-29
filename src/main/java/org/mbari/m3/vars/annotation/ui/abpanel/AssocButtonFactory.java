package org.mbari.m3.vars.annotation.ui.abpanel;

import com.jfoenix.controls.JFXButton;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Pane;
import org.mbari.m3.vars.annotation.EventBus;
import org.mbari.m3.vars.annotation.UIToolBox;
import org.mbari.m3.vars.annotation.commands.CreateAssociationsCmd;
import org.mbari.m3.vars.annotation.model.Annotation;
import org.mbari.m3.vars.annotation.model.Association;

import java.util.ArrayList;

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

        EventBus eventBus = toolBox.getEventBus();

        Button button = new JFXButton(name);
        button.setUserData(association);
        button.getStyleClass().add(styleClass);
        button.setOnAction(event -> {
            ArrayList<Annotation> annotations = new ArrayList<>(toolBox.getData().getSelectedAnnotations());
            eventBus.send(new CreateAssociationsCmd(association, annotations));
        });
        button.setTooltip(new Tooltip(association.toString()));

        ContextMenu contextMenu = new ContextMenu();
        MenuItem deleteButton = new MenuItem(toolBox.getI18nBundle().getString("cbpanel.conceptbutton.delete"));
        deleteButton.setOnAction(event ->
                ((Pane) button.getParent()).getChildren().remove(button));
        contextMenu.getItems().addAll(deleteButton);
        button.setContextMenu(contextMenu);

        return button;

    }

    Button build(NamedAssociation na) {
        return build(na.getName(), na);
    }

    public static boolean isAssocButton(Node node) {
        boolean isBtn = false;
        if (node instanceof Button) {
            Button button = (Button) node;
            isBtn = button.getStyleClass().contains(styleClass)
                    && button.getUserData() instanceof Association;
        }
        return isBtn;
    }

}
