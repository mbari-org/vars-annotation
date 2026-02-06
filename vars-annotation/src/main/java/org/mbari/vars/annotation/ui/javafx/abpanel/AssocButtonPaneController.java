package org.mbari.vars.annotation.ui.javafx.abpanel;

import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;
import org.mbari.vars.annotation.ui.UIToolBox;
import org.mbari.vars.annotation.ui.events.ForceRedrawEvent;
import org.mbari.vars.annotation.ui.messages.ReloadServicesMsg;
import org.mbari.vars.annotation.ui.messages.ShowNonfatalErrorAlert;
import org.mbari.vars.annotation.etc.jdk.Loggers;
import org.mbari.vars.annosaurus.sdk.r1.models.Association;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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
    private final AssocButtonFactory buttonFactory;
    private final DragPaneDecorator dragPaneDecorator;

    private final AssocButtonPrefs assocButtonPrefs;

    private static final String PREF_BUTTON_NAME = "name";
    private static final String PREF_BUTTON_ORDER = "order";
    private static final String PREF_BUTTON_ASSOCIATION = "association";
    private static final String BAD_KEY = "__unknown__";

    private final Loggers log = new Loggers(getClass());
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    public AssocButtonPaneController(UIToolBox toolBox) {
        this.toolBox = toolBox;
        buttonFactory = new AssocButtonFactory(toolBox);
        assocButtonPrefs = new AssocButtonPrefs(toolBox);
        dragPaneDecorator = new DragPaneDecorator(toolBox, assocButtonPrefs);

        toolBox.getData()
                .userProperty()
                .addListener(e -> loadButtonsFromPreferences());
        toolBox.getEventBus()
                .toObserverable()
                .ofType(ReloadServicesMsg.class)
                .subscribe(msg -> loadButtonsFromPreferences());
    }



    public Pane getPane() {
        if (pane == null) {
            pane = new FlowPane();
            pane.setUserData(this);
            pane.setPrefSize(300, 200);
            dragPaneDecorator.decorate(pane);
            loadButtonsFromPreferences();
            // Save everything when a new button is added or removed
            pane.getChildren()
                    .addListener((ListChangeListener<Node>) c ->  saveButtonsToPreferences());
        }
        return pane;
    }

    public Button addButton(NamedAssociation namedAssociation) {
        var opt = assocButtonPrefs.findPreferences();
        if (opt.isEmpty()) {
            throw new IllegalStateException("Unable to find preferences to store information about " + namedAssociation);
        }
        Button button = buttonFactory.build(namedAssociation, opt.get());
        if (!duplicateNameCheck(button)) {
            getPane().getChildren().add(button);
        }
        return button;
    }

    public void setLocked(boolean locked) {
        dragPaneDecorator.setLocked(locked);
    }

    public boolean isLocked() {
        return dragPaneDecorator.isLocked();
    }

    public DragPaneDecorator getDragPaneDecorator() {
        return dragPaneDecorator;
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
            // TODO highlight button to notifiy user it exists
        });


        return match.isPresent();
    }

    private void loadButtonsFromPreferences() {
        Association nil = Association.NIL;
        Optional<Preferences> opt = assocButtonPrefs.findPreferences();
        opt.ifPresent(prefs -> {
            executorService.submit(() -> {
                try {
                    List<Button> buttons = Arrays.stream(prefs.childrenNames())
                            .map(nodeName -> {
                                Preferences buttonPreferences = prefs.node(nodeName);
                                String name = buttonPreferences.get(PREF_BUTTON_NAME, BAD_KEY);
                                int order = buttonPreferences.getInt(PREF_BUTTON_ORDER, 0);
                                String a = buttonPreferences.get(PREF_BUTTON_ASSOCIATION, nil.toString());
                                log.atDebug().log(() -> "Loading association button " + a);
                                Association ass = Association.parse(a).orElse(nil);
                                Button button = buttonFactory.build(name, ass, prefs);
                                return new ButtonPref(button, order);
                            })
                            .filter(buttonPref -> !buttonPref.getButton().getText().equals(BAD_KEY))
                            .sorted(Comparator.comparingInt(ButtonPref::getOrder))
                            .map(ButtonPref::getButton)
                            .collect(Collectors.toList());
                    Platform.runLater(() -> {
                        ObservableList<Node> children = getPane().getChildren();
                        List<Node> oldButtons = getPane().getChildren()
                                .stream()
                                .filter(AssocButtonFactory::isAssocButton)
                                .toList();
                        children.removeAll(oldButtons);
                        children.addAll(buttons);
                        toolBox.getEventBus().send(new ForceRedrawEvent());
                    });
                } catch (Exception e) {
                    ResourceBundle i18n = toolBox.getI18nBundle();
                    toolBox.getEventBus()
                            .send(new ShowNonfatalErrorAlert(
                                    i18n.getString("abpanel.alert.prefsfail.load.title"),
                                    i18n.getString("abpanel.alert.prefsfail.load.header"),
                                    i18n.getString("abpanel.alert.prefsfail.load.content"),
                                    e));
                }
            });
        });
    }

    private void saveButtonsToPreferences() {

        Optional<Preferences> opt = assocButtonPrefs.findPreferences();
        opt.ifPresent(prefs -> {
            executorService.submit(() -> {
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
                            Preferences buttonPrefs = prefs.node(name);
                            buttonPrefs.putInt(PREF_BUTTON_ORDER, i);
                            buttonPrefs.put(PREF_BUTTON_NAME, name);

                            // This fixes an issue with pull request for issue #130. Previously
                            // Assocation data was stored as the userdata. Now it's NamedAssocation,
                            // which doesn't get parse correctly when loaded as it has an extra '\' segment
                            // This fix forces all data to be stored as associations
                            var userData = button.getUserData();
                            if (userData instanceof Association a) {
                                buttonPrefs.put(PREF_BUTTON_ASSOCIATION, AssocToString.asString(a));
                            } else {
                                log.atWarn().log("Unable to store association button data using class: " + userData.getClass());
                            }
                        });
            });


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
