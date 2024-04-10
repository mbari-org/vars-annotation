package org.mbari.vars.ui.javafx.cbpanel;

import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;
import org.mbari.vars.core.EventBus;
import org.mbari.vars.ui.UIToolBox;
import org.mbari.vars.ui.messages.*;
import org.mbari.vars.services.ConceptService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author Brian Schlining
 * @since 2017-06-13T14:13:00
 */
public class ConceptButtonPaneController {
    protected Pane pane;
    private final Preferences panePreferences;
    private final UIToolBox toolBox;

    private final EventBus eventBus;
    private final ResourceBundle i18n;
    private final DragPaneDecorator dragPaneDecorator;

    private static final String PREF_BUTTON_NAME = "name";
    private static final String PREF_BUTTON_ORDER = "order";
    private static final String BAD_KEY = "__unknown__";

    private final Logger log = LoggerFactory.getLogger(getClass());

    /**
     *
     * @param toolBox
     * @param panePreferences Holds the button preferences for this tab
     */
    public ConceptButtonPaneController(UIToolBox toolBox,
                                       Preferences panePreferences) {
        this.toolBox = toolBox;
        this.panePreferences = panePreferences;
        this.eventBus = toolBox.getEventBus();
        this.i18n = toolBox.getI18nBundle();

        dragPaneDecorator = new DragPaneDecorator(toolBox);

    }

    public void setLocked(boolean locked) {
        dragPaneDecorator.setLocked(locked);
    }

    public boolean isLocked() {
        return dragPaneDecorator.isLocked();
    }

    public Pane getPane() {
        if (pane == null) {
            pane = new FlowPane();
            pane.setUserData(this);
            pane.setPrefSize(800, 250);
            dragPaneDecorator.decorate(pane);
            loadButtonsFromPreferences();
            // Save everything when a new button is added or removed
            // TODO: Change this to save if the user is changed?
            pane.getChildren()
                    .addListener((ListChangeListener<Node>) c ->  saveButtonsToPreferences());
        }
        return pane;
    }

    public DragPaneDecorator getDragPaneDecorator() {
        return dragPaneDecorator;
    }

    protected void loadButtonsFromPreferences() {
        ConceptButtonFactory factory =
                new ConceptButtonFactory(toolBox);
        var eventBus = toolBox.getEventBus();
        // Load async so we don't block ui
//        toolBox.getExecutorService().submit(() -> {
        Thread.ofVirtual().start(() -> {
            try {
                // NOTE: Don't add any progress bar. That's done in ConceptButtonPanesController
                List<Button> buttons = Arrays.stream(panePreferences.childrenNames())
                        .map(nodeName -> {
                            Preferences buttonPreferences = panePreferences.node(nodeName);
                            String conceptName = buttonPreferences.get(PREF_BUTTON_NAME, BAD_KEY);
                            int order = buttonPreferences.getInt(PREF_BUTTON_ORDER, 0);
                            Button button = factory.build(conceptName);
                            return new ButtonPref(button, order);
                        })
                        .filter(buttonPref -> !buttonPref.getButton().getText().equals(BAD_KEY))
                        .sorted(Comparator.comparingInt(ButtonPref::getOrder))
                        .map(ButtonPref::getButton)
                        .collect(Collectors.toList());
                Platform.runLater(() -> getPane().getChildren().addAll(buttons));
            }
            catch (Exception e) {
                eventBus.send(new ShowNonfatalErrorAlert(
                        i18n.getString("cbpanel.alert.prefsfail.load.title"),
                        i18n.getString("cbpanel.alert.prefsfail.load.header"),
                        i18n.getString("cbpanel.alert.prefsfail.load.content"),
                        e));
            }
        });

    }

    /**
     * When removing the tab pane
     */
    private void saveButtonsToPreferences() {
        List<Button> buttons = getPane().getChildren()
                .stream()
                .filter(n -> n.getUserData().toString().equalsIgnoreCase(ConceptButtonFactory.USERDATA))
                .map(n -> (Button) n)
                .toList()

        // Store existing buttons
        IntStream.range(0, buttons.size())
                .forEach(i -> {
                    Button button = buttons.get(i);
                    String name = button.getText();
                    Preferences prefs = panePreferences.node(name);
                    prefs.putInt(PREF_BUTTON_ORDER, i);
                    prefs.put(PREF_BUTTON_NAME, name);
                });

        // Remove non-longer used buttons
        try {
            // Arrays.asList returns unmodifiable list. Need to create ArrayList.
            List<String> storedButtons = new ArrayList<>(Arrays.asList(panePreferences.childrenNames()));
            List<String> existingButtons = buttons.stream()
                    .map(Button::getText)
                    .collect(Collectors.toList());
            storedButtons.removeAll(existingButtons);
            storedButtons.forEach(s -> {
                try {
                    panePreferences.node(s).removeNode();
                }
                catch (Exception e) {
                    log.error("Failed to delete concept button named '" + s + "'.", e);
                }
            });
        }
        catch (Exception e) {
            eventBus.send(new ShowNonfatalErrorAlert(
                    i18n.getString("cbpanel.alert.prefsfail.save.title"),
                    i18n.getString("cbpanel.alert.prefsfail.save.header"),
                    i18n.getString("cbpanel.alert.prefsfail.save.content"),
                    e));
        }

    }
}

class ButtonPref {

    private final Button button;
    private final int order;

    ButtonPref(Button button, int order) {
        this.button = button;
        this.order = order;
    }

    Button getButton() {
        return button;
    }

    int getOrder() {
        return order;
    }
}
