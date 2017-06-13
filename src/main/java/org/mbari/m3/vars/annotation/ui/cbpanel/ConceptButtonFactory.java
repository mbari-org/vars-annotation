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
    private final ResourceBundle i18n;
    private final ConceptService conceptService;
    // We use the userdata field to let us know that this is a conceptbutton.
    public static final String BUTTON_USERDATA = "ConceptButton";

    @Inject
    public ConceptButtonFactory(ConceptService conceptService, EventBus eventBus, ResourceBundle i18n) {
        this.eventBus = eventBus;
        this.i18n = i18n;
        this.conceptService = conceptService;
    }

    public Button build(String name) {

        Button button = new JFXButton(name);
        button.setUserData(BUTTON_USERDATA);
        button.getStyleClass().add("cbpanel-button");
        button.setOnAction(event ->
            eventBus.send(new CreateAnnotation(button.getText())));

        // Add contextMenu
        ContextMenu contextMenu = new ContextMenu();
        MenuItem showInTreeItem = new MenuItem(i18n.getString("cbpanel.conceptbutton.findconcept"));
        showInTreeItem.setOnAction(event -> {
                ShowConceptInTreeView msg = new ShowConceptInTreeView(button.getText());
                eventBus.send(msg);
        });
        MenuItem deleteButton = new MenuItem(i18n.getString("cbpanel.conceptbutton.delete"));
        deleteButton.setOnAction(event ->
            ((Pane) button.getParent()).getChildren().remove(button));
        contextMenu.getItems().addAll(showInTreeItem, deleteButton);
        button.setContextMenu(contextMenu);

        conceptService.findDetails(name)
                .thenApply(opt -> {
                    if (!opt.isPresent()) {
                        Platform.runLater(() -> button.setDisable(true));
                    }
                    return null;
                });

        return button;
    }
}
