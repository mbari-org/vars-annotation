package org.mbari.m3.vars.annotation.ui.abpanel;

import com.jfoenix.controls.JFXButton;
import de.jensd.fx.glyphs.materialicons.MaterialIcon;
import de.jensd.fx.glyphs.materialicons.utils.MaterialIconFactory;
import javafx.collections.ListChangeListener;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import org.mbari.m3.vars.annotation.UIToolBox;
import org.mbari.m3.vars.annotation.messages.ShowNonfatalErrorAlert;
import org.mbari.m3.vars.annotation.model.Association;
import org.mbari.m3.vars.annotation.model.User;
import org.mbari.m3.vars.annotation.ui.shared.animation.FlashTransition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author Brian Schlining
 * @since 2017-10-11T15:25:00
 */
public class AssocButtonPaneController {

    private Pane pane;
    private final UIToolBox toolBox;
    private Button addButton;
    private final AssocSelectionDialogController controller;
    private final AssocButtonFactory buttonFactory;

    private static final String PREF_BUTTON_NAME = "name";
    private static final String PREF_BUTTON_ORDER = "order";
    private static final String PREF_BUTTON_ASSOCIATION = "association";
    private static final String PREF_AP_NODE = "org.mbari.m3.vars.annotation.ui.abpanel.AssocButtonPaneController";
    private static final String BAD_KEY = "__unknown__";

    private final Logger log = LoggerFactory.getLogger(getClass());

    public AssocButtonPaneController(UIToolBox toolBox) {
        this.toolBox = toolBox;
        controller = AssocSelectionDialogController.newInstance(toolBox);
        buttonFactory = new AssocButtonFactory(toolBox);
        toolBox.getData()
                .userProperty()
                .addListener(e -> loadButtonsFromPreferences());
    }

    private Optional<Preferences> findPreferences() {
        Preferences prefs = null;
        User user = toolBox.getData().getUser();
        if (user != null) {
            Preferences userPreferences = toolBox.getServices()
                    .getPreferencesFactory()
                    .remoteUserRoot(user.getUsername());
            prefs = userPreferences.node(PREF_AP_NODE);
            log.debug("Using ");
        }
        return Optional.ofNullable(prefs);
    }

    public Pane getPane() {
        if (pane == null) {
            pane = new FlowPane();
            pane.setUserData(this);
            pane.setPrefSize(300, 200);
            pane.getChildren().add(getAddButton());
            loadButtonsFromPreferences();
            // Save everything when a new button is added or removed
            pane.getChildren()
                    .addListener((ListChangeListener<Node>) c ->  saveButtonsToPreferences());
        }
        return pane;
    }

    private Button getAddButton() {
        if (addButton == null) {
            addButton = new JFXButton();
            String tooltip = toolBox.getI18nBundle().getString("abpane.addbutton");
            MaterialIconFactory iconFactory = MaterialIconFactory.get();
            Text icon = iconFactory.createIcon(MaterialIcon.ADD, "30px");
            addButton.setTooltip(new Tooltip(tooltip));
            addButton.setGraphic(icon);
            addButton.setOnAction(v -> {
                Dialog<NamedAssociation> dialog = controller.getDialog();
                Optional<NamedAssociation> opt = dialog.showAndWait();
                opt.ifPresent(namedAssociation -> {
                    Button button = buttonFactory.build(namedAssociation);
                    if (!duplicateNameCheck(button)) {
                        getPane().getChildren().add(button);
                    }
                });
            });
        }
        return addButton;
    }

    private boolean duplicateNameCheck(Button button) {
        Optional<Button> match = getPane().getChildren()
                .stream()
                .filter(n -> n instanceof Button)
                .map(b -> (Button) b)
                .filter(b -> {
                    String text = b.getText();
                    return text != null && !text.isEmpty() && text.equalsIgnoreCase(button.getText());
                })
                .findFirst();

        // flash matching version
        match.ifPresent(btn -> {
            FlashTransition transition = new FlashTransition(btn);
            transition.play();
        });


        return match.isPresent();
    }

    private void loadButtonsFromPreferences() {
        Association nil = Association.NIL;
        Optional<Preferences> opt = findPreferences();
        opt.ifPresent(prefs -> {
            try {
                List<Button> buttons = Arrays.stream(prefs.childrenNames())
                        .map(nodeName -> {
                            Preferences buttonPreferences = prefs.node(nodeName);
                            String name = buttonPreferences.get(PREF_BUTTON_NAME, BAD_KEY);
                            int order = buttonPreferences.getInt(PREF_BUTTON_ORDER, 0);
                            String a = buttonPreferences.get(PREF_BUTTON_ASSOCIATION, nil.toString());
                            Association ass = Association.parse(a).orElse(nil);
                            Button button = buttonFactory.build(name, ass);
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
        });
    }

    private void saveButtonsToPreferences() {

        Optional<Preferences> opt = findPreferences();
        opt.ifPresent(prefs -> {
            List<Button> buttons = getPane().getChildren()
                    .stream()
                    .filter(n -> n.getUserData() instanceof Association)
                    .map(n -> (Button) n)
                    .collect(Collectors.toList());

            // Store existing buttons
            IntStream.range(0, buttons.size())
                    .forEach(i -> {
                        Button button = buttons.get(i);
                        String name = button.getText();
                        Association userdata = (Association) button.getUserData();
                        Preferences buttonPrefs = prefs.node(name);
                        buttonPrefs.putInt(PREF_BUTTON_ORDER, i);
                        buttonPrefs.put(PREF_BUTTON_NAME, name);
                        buttonPrefs.put(PREF_BUTTON_ASSOCIATION, userdata.toString());
                    });

            // Remove non-longer used buttons
            try {
                // Arrays.asList returns unmodifiable list. Need to create ArrayList.
                List<String> storedButtons = new ArrayList<>(Arrays.asList(prefs.childrenNames()));
                List<String> existingButtons = buttons.stream()
                        .map(Button::getText)
                        .collect(Collectors.toList());
                storedButtons.removeAll(existingButtons);
                storedButtons.forEach(s -> {
                    try {
                        prefs.node(s).removeNode();
                    }
                    catch (Exception e) {
                        log.error("Failed to delete concept button named '" + s + "'.", e);
                    }
                });
            }
            catch (Exception e) {
                toolBox.getEventBus()
                        .send(new ShowNonfatalErrorAlert("VARS Nonfatal Error",
                                "Failed to configure user interface",
                                "An error occurred when removing unused buttons from preferences",
                                e));
            }
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
