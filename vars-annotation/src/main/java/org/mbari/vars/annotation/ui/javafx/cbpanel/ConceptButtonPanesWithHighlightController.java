package org.mbari.vars.annotation.ui.javafx.cbpanel;

import javafx.application.Platform;
import javafx.scene.layout.HBox;
import org.mbari.vars.annotation.util.Preconditions;
import org.mbari.vars.annotation.ui.UIToolBox;
import org.mbari.vars.annotation.etc.jdk.Loggers;
import org.mbari.vars.oni.sdk.r1.models.User;
import org.mbari.vars.annotation.ui.messages.ReloadServicesMsg;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

/**
 * @author Brian Schlining
 * @since 2018-12-12T11:34:00
 */
public class ConceptButtonPanesWithHighlightController {

    private HBox root;
    private final UIToolBox toolBox;
    private final ResourceBundle i18n;
    private final AtomicInteger loadGeneration = new AtomicInteger();
    private final Loggers log = new Loggers(getClass());

    public ConceptButtonPanesWithHighlightController(UIToolBox toolBox) {
        Preconditions.checkNotNull(toolBox, "The UIToolbox arg can not be null");
        this.toolBox = toolBox;
        this.i18n = toolBox.getI18nBundle();
        toolBox.getData()
                .userProperty()
                .addListener(e -> loadTabsFromPreferences());
        toolBox.getEventBus()
                .toObserverable()
                .ofType(ReloadServicesMsg.class)
                .subscribe(msg -> loadTabsFromPreferences());

    }

    public HBox getRoot() {
        if (root == null) {
            root = new HBox();
        }
        return root;
    }

    private void loadTabsFromPreferences() {

        if (getRoot().isVisible()) {
            final int generation = loadGeneration.incrementAndGet();
            Platform.runLater(() -> getRoot().getChildren().clear());
            Optional<Preferences> tabsPrefsOpt = getTabsPreferences();
            tabsPrefsOpt.ifPresent(tabsPrefs -> {
                // Reading prefs is a remote call. Do it off the FX thread.
                toolBox.getExecutorService().submit(() -> {
                    List<LoadedTab> loadedTabs;
                    try {
                        loadedTabs = Arrays.stream(tabsPrefs.childrenNames())
                                .map(tabName -> {
                                    Preferences tabPrefs = tabsPrefs.node(tabName);
                                    return new LoadedTab(tabPrefs,
                                            tabPrefs.get(ConceptButtonPanesController.PREFKEY_TABNAME, "dummy"));
                                })
                                .toList();
                    } catch (BackingStoreException e) {
                        log.atError().log("VARS had a problem loading user tabs for user: " + toolBox.getData().getUser());
                        loadedTabs = List.of();
                    }

                    final var finalTabs = loadedTabs;
                    Platform.runLater(() -> {
                        // A newer load replaces this one; don't add this user's tabs as well
                        if (generation != loadGeneration.get()) {
                            return;
                        }
                        getRoot().getChildren().clear();
                        finalTabs.forEach(loaded -> {
                            ConceptButtonPaneWithHighlightController controller =
                                    new ConceptButtonPaneWithHighlightController(loaded.name(), toolBox, loaded.prefs());
                            controller.setLocked(true);
                            getRoot().getChildren().add(controller.getPane());
                        });
                    });
                });
            });
        }
    }

    private record LoadedTab(Preferences prefs, String name) {}

    private Optional<Preferences> getTabsPreferences() {
        Preferences cpPrefs = null;
        User user = toolBox.getData().getUser();
        if (user != null) {
            Preferences userPreferences = toolBox.getServices()
                    .preferencesFactory()
                    .remoteUserRoot(user.getUsername());
            cpPrefs = userPreferences.node(ConceptButtonPanesController.PREF_CP_NODE);
        }
        return Optional.ofNullable(cpPrefs);
    }


}
