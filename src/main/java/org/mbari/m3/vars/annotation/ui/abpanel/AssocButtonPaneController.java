package org.mbari.m3.vars.annotation.ui.abpanel;

import javafx.scene.control.Button;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;
import org.mbari.m3.vars.annotation.UIToolBox;
import org.mbari.m3.vars.annotation.messages.ShowNonfatalErrorAlert;
import org.mbari.m3.vars.annotation.model.Association;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author Brian Schlining
 * @since 2017-10-11T15:25:00
 */
public class AssocButtonPaneController {

    private Pane pane;
    private final Preferences panePreferences;
    private final UIToolBox toolBox;

    private static final String PREF_BUTTON_NAME = "name";
    private static final String PREF_BUTTON_ORDER = "order";
    private static final String PREF_BUTTON_ASSOCIATION = "association";
    private static final String BAD_KEY = "__unknown__";

    private final Logger log = LoggerFactory.getLogger(getClass());

    public AssocButtonPaneController(Preferences panePreferences, UIToolBox toolBox) {
        this.panePreferences = panePreferences;
        this.toolBox = toolBox;
    }

    public Pane getPane() {
        if (pane == null) {
            pane = new FlowPane();
            pane.setUserData(this);
            pane.setPrefSize(300, 200);

        }
        return pane;
    }

    private void loadBUttonsFromPreferences() {
        AssocButtonFactory factory = new AssocButtonFactory(toolBox);
        Association nil = Association.NIL;
        try {
            List<Button> buttons = Arrays.stream(panePreferences.childrenNames())
                    .map(nodeName -> {
                        Preferences buttonPreferences = panePreferences.node(nodeName);
                        String name = buttonPreferences.get(PREF_BUTTON_NAME, BAD_KEY);
                        int order = buttonPreferences.getInt(PREF_BUTTON_ORDER, 0);
                        String a = buttonPreferences.get(PREF_BUTTON_ASSOCIATION, nil.toString());
                        Association ass = Association.parse(a).orElse(nil);
                        Button button = factory.build(name, ass);
                        return new ButtonPref(button, order);
                    })
                    .filter(buttonPref -> !buttonPref.getButton().getText().equals(BAD_KEY))
                    .sorted(Comparator.comparingInt(ButtonPref::getOrder))
                    .map(ButtonPref::getButton)
                    .collect(Collectors.toList());
            getPane().getChildren().addAll(buttons);
        }
        catch (Exception e) {
            toolBox.getEventBus()
                    .send(new ShowNonfatalErrorAlert("VARS Nonfatal Error",
                            "Failed to configure user interface",
                            "An error occurred when loading association buttons from preferences",
                            e));
        }
    }

    private void saveButtonsToPreferences() {
        List<Button> buttons = getPane().getChildren()
                .stream()
                .filter(n -> n.getUserData() instanceof Association)
                .map(n -> (Button) n)
                .collect(Collectors.toList());

        try {
            panePreferences.clear();
        } catch (BackingStoreException e) {
            log.warn("Failed to clear preference node for association button pane");
        }
        // Store existing buttons
        IntStream.range(0, buttons.size())
                .forEach(i -> {
                    Button button = buttons.get(i);
                    String name = button.getText();
                    Association userdata = (Association) button.getUserData();
                    Preferences prefs = panePreferences.node(name);
                    prefs.putInt(PREF_BUTTON_ORDER, i);
                    prefs.put(PREF_BUTTON_NAME, name);
                    prefs.put(PREF_BUTTON_ASSOCIATION, userdata.toString());
                });
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


}
