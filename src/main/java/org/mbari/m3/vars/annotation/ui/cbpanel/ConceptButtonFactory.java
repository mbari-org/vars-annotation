package org.mbari.m3.vars.annotation.ui.cbpanel;

import com.jfoenix.controls.JFXButton;
import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.Pane;
import org.mbari.m3.vars.annotation.EventBus;
import org.mbari.m3.vars.annotation.commands.CreateAnnotation;
import org.mbari.m3.vars.annotation.commands.ShowConceptInTreeView;
import org.mbari.m3.vars.annotation.services.ConceptService;

import javax.inject.Inject;
import java.util.ResourceBundle;

/**
 * @author Brian Schlining
 * @since 2017-05-17T10:04:00
 */
public class ConceptButtonFactory {

    private final EventBus eventBus;
    private final ResourceBundle uiBundle;
    private final ConceptService conceptService;

    @Inject
    public ConceptButtonFactory(ConceptService conceptService, EventBus eventBus, ResourceBundle uiBundle) {
        this.eventBus = eventBus;
        this.uiBundle = uiBundle;
        this.conceptService = conceptService;
    }

    public Button build(String name) {

        Button button = new JFXButton(name);
        button.getStyleClass().add("cbpanel-button");
        button.setOnAction(event ->
            eventBus.send(new CreateAnnotation(button.getText())));

        // Add contextMenu
        ContextMenu contextMenu = new ContextMenu();
        MenuItem showInTreeItem = new MenuItem(uiBundle.getString("cbpanel.conceptbutton.findconcept"));
        showInTreeItem.setOnAction(event -> {
                ShowConceptInTreeView msg = new ShowConceptInTreeView(button.getText());
                eventBus.send(msg);
        });
        MenuItem deleteButton = new MenuItem(uiBundle.getString("cbpanel.conceptbutton.delete"));
        deleteButton.setOnAction(event ->
            ((Pane) button.getParent()).getChildren().remove(button));
        contextMenu.getItems().addAll(showInTreeItem, deleteButton);
        button.setContextMenu(contextMenu);

        conceptService.findDetails(name)
                .thenApply(opt -> {
                    if (!opt.isPresent()) {
                        // TODO when a button is disabled the context menu is also disabled
                        // and there's now way to remove it. We should autoremove
                        Platform.runLater(() -> button.setDisable(true));
                    }
                    return null;
                });

        return button;
    }
}
