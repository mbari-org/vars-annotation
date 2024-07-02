package org.mbari.vars.ui.javafx.cbpanel;

import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.Pane;
import org.mbari.vars.core.EventBus;
import org.mbari.vars.ui.UIToolBox;
import org.mbari.vars.ui.commands.CreateAnnotationFromConceptCmd;
import org.mbari.vars.ui.messages.ShowConceptInTreeViewMsg;
import org.mbari.vars.services.ConceptService;

import java.util.ResourceBundle;

/**
 * @author Brian Schlining
 * @since 2017-05-17T10:04:00
 */
public class ConceptButtonFactory {

    private final UIToolBox toolBox;
    private final EventBus eventBus;
    private final ResourceBundle i18n;
    // We use the userdata field to let us know that this is a conceptbutton.
    public static final String USERDATA = ConceptButtonFactory.class.getSimpleName();

    public ConceptButtonFactory(UIToolBox toolBox) {
        this.toolBox = toolBox;
        this.eventBus = toolBox.getEventBus();
        this.i18n = toolBox.getI18nBundle();
    }

    public Button build(String name) {

        Button button = new Button(name);
        button.setUserData(USERDATA);
        button.getStyleClass().add("cbpanel-button");
        button.setOnAction(event ->
            eventBus.send(new CreateAnnotationFromConceptCmd(button.getText())));

        // Add contextMenu
        ContextMenu contextMenu = new ContextMenu();
        MenuItem showInTreeItem = new MenuItem(i18n.getString("cbpanel.conceptbutton.findconcept"));
        showInTreeItem.setOnAction(event -> {
                ShowConceptInTreeViewMsg msg = new ShowConceptInTreeViewMsg(button.getText());
                eventBus.send(msg);
        });
        MenuItem deleteButton = new MenuItem(i18n.getString("cbpanel.conceptbutton.delete"));
        deleteButton.setOnAction(event ->
            ((Pane) button.getParent()).getChildren().remove(button));
        contextMenu.getItems().addAll(showInTreeItem, deleteButton);
        button.setContextMenu(contextMenu);

        button.setOnDragDetected(evt -> {
            if (button.getText() != null) {
                // Drag the string name to some target.
                Dragboard db = button.startDragAndDrop(TransferMode.MOVE);
                ClipboardContent content = new ClipboardContent();
                content.putString(name);
                db.setContent(content);
                evt.consume();
            }
        });

        toolBox.getServices()
                .getConceptService()
                .findAllNames()
                .thenAccept(names -> {
                    if (!names.contains(name)) {
                        Platform.runLater(() -> {
                            button.getStyleClass().add("button-invalid");
                            button.setOnAction(e -> {});
                        });
                    }
                });

        return button;
    }
}
