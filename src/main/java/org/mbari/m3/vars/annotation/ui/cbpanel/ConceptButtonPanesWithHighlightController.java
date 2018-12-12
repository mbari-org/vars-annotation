package org.mbari.m3.vars.annotation.ui.cbpanel;

import com.google.common.base.Preconditions;
import javafx.application.Platform;
import javafx.scene.layout.HBox;
import org.mbari.m3.vars.annotation.UIToolBox;
import org.mbari.m3.vars.annotation.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
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
    private final Logger log = LoggerFactory.getLogger(getClass());

    public ConceptButtonPanesWithHighlightController(UIToolBox toolBox) {
        Preconditions.checkNotNull(toolBox, "The UIToolbox arg can not be null");
        this.toolBox = toolBox;
        this.i18n = toolBox.getI18nBundle();
        toolBox.getData()
                .userProperty()
                .addListener(e -> loadTabsFromPreferences());

    }

    public HBox getRoot() {
        if (root == null) {
            root = new HBox();
        }
        return root;
    }

    private void loadTabsFromPreferences() {

        if (getRoot().isVisible()) {
            Platform.runLater(() -> getRoot().getChildren().clear());
            Optional<Preferences> tabsPrefsOpt = getTabsPreferences();
            tabsPrefsOpt.ifPresent(tabsPrefs -> {
                Platform.runLater(() -> {
                    try {
                        Arrays.stream(tabsPrefs.childrenNames())
                                .forEach(tabName -> {
                                    Preferences tabPrefs = tabsPrefs.node(tabName);
                                    String name = tabPrefs.get(ConceptButtonPanesController.PREFKEY_TABNAME, "dummy");
                                    ConceptButtonPaneWithHighlightController controller = new ConceptButtonPaneWithHighlightController(name,
                                            toolBox.getServices().getConceptService(),
                                            tabsPrefs.node(tabName),
                                            toolBox.getEventBus(),
                                            i18n);
                                    controller.setLocked(true);
                                    getRoot().getChildren().add(controller.getPane());
                                });

                    } catch (BackingStoreException e) {
                        log.error("VARS had a problem loading user tabs for user: " + toolBox.getData().getUser());
                    }
                });
            });
        }
    }

    private Optional<Preferences> getTabsPreferences() {
        Preferences cpPrefs = null;
        User user = toolBox.getData().getUser();
        if (user != null) {
            Preferences userPreferences = toolBox.getServices()
                    .getPreferencesFactory()
                    .remoteUserRoot(user.getUsername());
            cpPrefs = userPreferences.node(ConceptButtonPanesController.PREF_CP_NODE);
        }
        return Optional.ofNullable(cpPrefs);
    }


}
