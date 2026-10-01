package org.mbari.vars.annotation.ui.javafx.cbpanel;

import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.TabPane;
import javafx.scene.layout.HBox;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mbari.vars.annotation.etc.rxjava.EventBus;
import org.mbari.vars.annotation.services.Services;
import org.mbari.vars.annotation.services.oni.PreferencesFactory;
import org.mbari.vars.annotation.ui.Data;
import org.mbari.vars.annotation.ui.Initializer;
import org.mbari.vars.annotation.ui.UIToolBox;
import org.mbari.vars.annotation.ui.javafx.MemPrefs;
import org.mbari.vars.oni.sdk.r1.models.User;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BooleanSupplier;
import java.util.prefs.Preferences;

import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * User prefs live on a remote service, so reading them blocks. Loading tabs must do that off
 * the JavaFX thread and only build the UI on it.
 */
public class ConceptTabsFxThreadTest {

    private MemPrefs root;
    private UIToolBox toolBox;

    @BeforeAll
    public static void initJavaFx() {
        try {
            Platform.startup(() -> {});
        }
        catch (IllegalStateException e) {
            // Toolkit already running
        }
    }

    @BeforeEach
    public void setup() {
        root = new MemPrefs(null, "", new AtomicReference<>());
        root.node("userA")
                .node(ConceptButtonPanesController.PREF_CP_NODE)
                .node(ConceptButtonPanesController.TAB_PREFIX + "0")
                .put(ConceptButtonPanesController.PREFKEY_TABNAME, "tabOfA");
        PreferencesFactory factory = new PreferencesFactory() {
            @Override public Preferences remoteSystemRoot() { return root; }
            @Override public Preferences remoteUserRoot(String userName) { return root.node(userName); }
        };
        var real = Initializer.getToolBox();
        var services = real.getServices();
        toolBox = new UIToolBox(new Data(),
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
    }

    private <T> T onFx(java.util.function.Supplier<T> s) throws Exception {
        var result = new AtomicReference<T>();
        var done = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                result.set(s.get());
            }
            finally {
                done.countDown();
            }
        });
        if (!done.await(5, TimeUnit.SECONDS)) {
            throw new AssertionError("FX thread did not run the task");
        }
        return result.get();
    }

    private void await(BooleanSupplier condition, String message) throws Exception {
        long end = System.currentTimeMillis() + 5000;
        while (!condition.getAsBoolean()) {
            if (System.currentTimeMillis() > end) {
                throw new AssertionError(message);
            }
            Thread.sleep(25);
        }
    }

    @Test
    public void tabPaneReadsPrefsOffTheFxThread() throws Exception {
        var controller = onFx(() -> new ConceptButtonPanesController(toolBox));
        var tabPane = onFx(() -> (TabPane) controller.getRoot().getCenter());
        root.fxThreadAccess().set(false);

        toolBox.getData().setUser(new User("userA", "", "", "", "", "", "a@mbari.org"));

        await(() -> !tabPane.getTabs().isEmpty(), "Tabs were never loaded");
        assertFalse(root.fxThreadAccess().get(), "Prefs were read on the JavaFX thread");
    }

    @Test
    public void overviewReadsPrefsOffTheFxThread() throws Exception {
        var controller = onFx(() -> new ConceptButtonPanesWithHighlightController(toolBox));
        HBox box = onFx(controller::getRoot);
        root.fxThreadAccess().set(false);

        toolBox.getData().setUser(new User("userA", "", "", "", "", "", "a@mbari.org"));

        await(() -> !box.getChildren().isEmpty(), "Overview tabs were never loaded");
        assertFalse(root.fxThreadAccess().get(), "Prefs were read on the JavaFX thread");
    }
}
