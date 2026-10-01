package org.mbari.vars.annotation.ui.javafx.cbpanel;

import javafx.application.Platform;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mbari.vars.annotation.etc.rxjava.EventBus;
import org.mbari.vars.annotation.services.Services;
import org.mbari.vars.annotation.services.oni.PreferencesFactory;
import org.mbari.vars.annotation.ui.Data;
import org.mbari.vars.annotation.ui.Initializer;
import org.mbari.vars.annotation.ui.UIToolBox;
import org.mbari.vars.annotation.ui.javafx.MemPrefs;
import org.mbari.vars.oni.sdk.r1.models.User;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.prefs.Preferences;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Loading a user's tabs is asynchronous. Overlapping loads (a user change plus a services
 * reload, or two quick user changes) must leave only the latest user's tabs showing.
 * Otherwise the tab index no longer matches the stored node, and removing a tab edits the
 * wrong prefs.
 */
public class ConceptTabsUserSwitchTest {

    @BeforeAll
    public static void initJavaFx() {
        try {
            Platform.startup(() -> {});
        }
        catch (IllegalStateException e) {
            // Toolkit already running
        }
    }

    private static User user(String name) {
        return new User(name, "", "", "", "", "", name + "@mbari.org");
    }

    private static void seedTab(Preferences root, String username, String tabName) {
        root.node(username)
                .node(ConceptButtonPanesController.PREF_CP_NODE)
                .node(ConceptButtonPanesController.TAB_PREFIX + "0")
                .put(ConceptButtonPanesController.PREFKEY_TABNAME, tabName);
    }

    private static void fxAndWait(Runnable r) throws Exception {
        var done = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                r.run();
            }
            finally {
                done.countDown();
            }
        });
        assertTrue(done.await(5, TimeUnit.SECONDS), "FX thread did not run the task");
    }

    @Test
    public void overlappingLoadsShowOnlyTheLatestUsersTabs() throws Exception {
        var root = new MemPrefs(null, "", new AtomicReference<>());
        seedTab(root, "userA", "tabOfA");
        seedTab(root, "userB", "tabOfB");
        PreferencesFactory factory = new PreferencesFactory() {
            @Override public Preferences remoteSystemRoot() { return root; }
            @Override public Preferences remoteUserRoot(String userName) { return root.node(userName); }
        };

        var real = Initializer.getToolBox();
        var services = real.getServices();
        var toolBox = new UIToolBox(new Data(),
                new Services(services.annotationService(),
                        services.conceptService(),
                        services.imageArchiveService(),
                        services.mediaService(),
                        services.userService(),
                        services.preferencesService(),
                        factory),
                new EventBus(),
                real.getI18nBundle(),
                real.getConfig(),
                real.getStylesheets(),
                real.getExecutorService(),
                real.getAes());

        var controller = new AtomicReference<ConceptButtonPanesController>();
        var tabPane = new AtomicReference<TabPane>();
        fxAndWait(() -> {
            controller.set(new ConceptButtonPanesController(toolBox));
            tabPane.set((TabPane) controller.get().getRoot().getCenter());
        });

        // Two user changes back to back, from a non-FX thread like the real UserChangedEvent
        toolBox.getData().setUser(user("userA"));
        toolBox.getData().setUser(user("userB"));

        Thread.sleep(500);
        fxAndWait(() -> { });

        List<String> names = tabPane.get().getTabs().stream().map(Tab::getText).toList();
        assertEquals(List.of("tabOfB"), names);
    }
}
