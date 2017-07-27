package org.mbari.m3.vars.annotation.ui;

import com.anchorage.docks.node.DockNode;
import com.anchorage.docks.stations.DockStation;
import com.anchorage.system.AnchorageSystem;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXComboBox;
import de.jensd.fx.glyphs.GlyphsFactory;
import de.jensd.fx.glyphs.materialicons.MaterialIcon;
import de.jensd.fx.glyphs.materialicons.utils.MaterialIconFactory;
import io.reactivex.rxjavafx.observables.JavaFxObservable;
import javafx.collections.FXCollections;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import org.mbari.m3.vars.annotation.EventBus;
import org.mbari.m3.vars.annotation.UIToolBox;
import org.mbari.m3.vars.annotation.commands.RedoMsg;
import org.mbari.m3.vars.annotation.commands.UndoMsg;
import org.mbari.m3.vars.annotation.events.UserAddedEvent;
import org.mbari.m3.vars.annotation.events.UserChangedEvent;
import org.mbari.m3.vars.annotation.model.User;
import org.mbari.m3.vars.annotation.services.UserService;
import org.mbari.m3.vars.annotation.ui.annotable.AnnotationTableController;
import org.mbari.m3.vars.annotation.ui.cbpanel.ConceptButtonPanesController;
import org.mbari.m3.vars.annotation.ui.concepttree.SearchTreePaneController;

import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

/**
 * @author Brian Schlining
 * @since 2017-07-26T14:37:00
 */
public class AppPaneController {

    private BorderPane root;
    private DockStation dockStation;
    private AnnotationTableController annotationTableController;
    private ToolBar toolBar;
    private final UIToolBox toolBox;
    private ComboBox<String> usersComboBox;

    public AppPaneController(UIToolBox toolBox) {
        this.toolBox = toolBox;
        annotationTableController = new AnnotationTableController(toolBox);
    }

    public BorderPane getRoot() {
        if (root == null) {
            root = new BorderPane(getDockStation());
            root.setTop(getToolBar());
        }
        return root;
    }

    public DockStation getDockStation() {
        if (dockStation == null) {
            dockStation = AnchorageSystem.createStation();
            DockNode annotationNode = AnchorageSystem.createDock("Annotations", annotationTableController.getTableView());
            annotationNode.closeableProperty().set(false);
            annotationNode.dock(dockStation, DockNode.DockPosition.CENTER);

            SearchTreePaneController treeController = new SearchTreePaneController(toolBox.getServices().getConceptService(),
                    toolBox.getI18nBundle());
            DockNode treeNode = AnchorageSystem.createDock("Knowledgebase", treeController.getRoot());
            treeNode.closeableProperty().set(false);
            treeNode.setPrefSize(400, 400);
            treeNode.dock(dockStation, DockNode.DockPosition.RIGHT);

            ConceptButtonPanesController panesController = new ConceptButtonPanesController(toolBox);
            Pane pane = panesController.getRoot();
            pane.setPrefSize(800, 250);
            DockNode cbNode = AnchorageSystem.createDock("Quick Buttons", pane);
            cbNode.closeableProperty().set(false);
            cbNode.maximizableProperty().set(false);
            cbNode.dock(dockStation, DockNode.DockPosition.BOTTOM);


        }
        return dockStation;
    }

    public ToolBar getToolBar() {
        if (toolBar == null) {
            ResourceBundle bundle = toolBox.getI18nBundle();
            GlyphsFactory gf = MaterialIconFactory.get();

            Text undoIcon = gf.createIcon(MaterialIcon.UNDO, "30px");
            Button undoButton = new JFXButton();
            undoButton.setGraphic(undoIcon);
            undoButton.setOnAction(e -> toolBox.getEventBus().send(new UndoMsg()));

            Text redoIcon = gf.createIcon(MaterialIcon.REDO, "30px");
            Button redoButton = new JFXButton();
            redoButton.setGraphic(redoIcon);
            redoButton.setOnAction(e -> toolBox.getEventBus().send(new RedoMsg()));

            toolBar = new ToolBar(undoButton,
                    redoButton,
                    new Label(bundle.getString("apppane.label.user")),
                    getUsersComboBox());
        }
        return toolBar;
    }

    public ComboBox<String> getUsersComboBox() {
        if (usersComboBox == null) {
            UserService userService = toolBox.getServices().getUserService();
            usersComboBox = new JFXComboBox<>();

            // Listen to UserAddedEvent and add it to the combobox
            EventBus eventBus = toolBox.getEventBus();
            eventBus.toObserverable()
                    .ofType(UserAddedEvent.class)
                    .subscribe(event -> {
                        User user = event.get();
                        usersComboBox.getItems().add(user.getUsername());
                        FXCollections.sort(usersComboBox.getItems());
                        usersComboBox.getSelectionModel().select(user.getUsername());
                    });

            // When a username is selected send a change event
            JavaFxObservable.valuesOf(usersComboBox.getSelectionModel().selectedItemProperty())
                    .subscribe(s -> {
                        userService.findAllUsers()
                                .thenAccept(users -> {
                                    Optional<User> opt = users.stream()
                                            .filter(u -> u.getUsername().equals(s))
                                            .findFirst();
                                    opt.ifPresent(user -> eventBus.send(new UserChangedEvent(user)));
                                });
                    });

            // Populate the combobox and select the user form the OS
            userService.findAllUsers()
                    .thenAccept(users -> {
                        List<String> usernames = users.stream()
                                .map(User::getUsername)
                                .sorted()
                                .collect(Collectors.toList());
                        usersComboBox.setItems(FXCollections.observableList(usernames));
                        String defaultUser = System.getProperty("user.name");
                        usersComboBox.getSelectionModel().select(defaultUser);
                    });

        }
        return usersComboBox;
    }
}
