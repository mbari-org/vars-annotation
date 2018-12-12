package org.mbari.m3.vars.annotation.ui.cbpanel;

import javafx.collections.ListChangeListener;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import org.mbari.m3.vars.annotation.EventBus;
import org.mbari.m3.vars.annotation.services.ConceptService;
import org.mbari.m3.vars.annotation.util.JFXUtilities;

import java.util.ResourceBundle;
import java.util.prefs.Preferences;

/**
 * Highlights the last button pressed
 * @author Brian Schlining
 * @since 2018-12-12T11:17:00
 */
public class ConceptButtonPaneWithHighlightController extends ConceptButtonPaneController {

    private Button lastButtonPressed;
    private static final String STYLE_CLASS = "cbpanel-label-overview";

    private EventHandler<MouseEvent> eventHandler = event -> {
        if (lastButtonPressed != null) {
            JFXUtilities.removeAttention(lastButtonPressed);
        }

        lastButtonPressed = (Button) event.getSource();
        if (lastButtonPressed != null) {
            JFXUtilities.attractAttention(lastButtonPressed);
        }
    };

    public ConceptButtonPaneWithHighlightController(String name,
                                                    ConceptService conceptService,
                                                    Preferences panePreferences,
                                                    EventBus eventBus,
                                                    ResourceBundle i18n) {
        super(conceptService, panePreferences, eventBus, i18n);
        Label label = new Label(name);
        label.setUnderline(true);
        label.getStyleClass().add(STYLE_CLASS);
        getPane().getChildren().add(0, label);
    }

    /**
     * Change the layout to a VBox. Disable drag and drop
     * @return
     */
    @Override
    public Pane getPane() {
        if (pane == null) {
            pane = new VBox();
            pane.setUserData(this);
            pane.getChildren().addListener((ListChangeListener<Node>) c -> {

                while (c.next()) {
                    c.getAddedSubList()
                            .stream()
                            .filter(node -> node instanceof Button)
                            .map(node -> (Button) node)
                            .forEach(button -> {
                                button.addEventHandler(MouseEvent.MOUSE_CLICKED,
                                        eventHandler);
                            });

                    c.getRemoved()
                            .forEach(node -> node.removeEventHandler(MouseEvent.MOUSE_CLICKED,
                                    eventHandler));
                }
            });
            loadButtonsFromPreferences();
        }
        return pane;
    }
}


