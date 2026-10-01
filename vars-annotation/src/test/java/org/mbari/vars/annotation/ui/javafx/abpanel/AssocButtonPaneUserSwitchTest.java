package org.mbari.vars.annotation.ui.javafx.abpanel;

import javafx.application.Platform;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mbari.vars.annotation.etc.rxjava.EventBus;
import org.mbari.vars.annotation.services.Services;
import org.mbari.vars.annotation.services.oni.PreferencesFactory;
import org.mbari.vars.annotation.ui.Data;
import org.mbari.vars.annotation.ui.Initializer;
import org.mbari.vars.annotation.ui.UIToolBox;
import org.mbari.vars.annotation.ui.javafx.MemPrefs;
import org.mbari.vars.annotation.ui.events.ForceRedrawEvent;
import org.mbari.vars.oni.sdk.r1.models.User;

import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BooleanSupplier;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Loading a user's association buttons is asynchronous (the prefs are remote). Until the load
 * lands, the pane still holds the previous user's buttons. A change to the pane in that window
 * must not be saved: the prefs would be those of the new user, the buttons those of the old one.
 */
public class AssocButtonPaneUserSwitchTest {

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

    private static Set<String> childNames(Preferences prefs) throws BackingStoreException {
        return new TreeSet<>(Arrays.asList(prefs.childrenNames()));
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

    private static void await(BooleanSupplier condition, String message) throws Exception {
        long end = System.currentTimeMillis() + 5000;
        while (!condition.getAsBoolean()) {
            if (System.currentTimeMillis() > end) {
                throw new AssertionError(message);
            }
            Thread.sleep(25);
        }
    }

    @Test
    public void paneChangeBeforeNewUsersButtonsLoadIsNotSavedToNewUser() throws Exception {
        var gate = new AtomicReference<CountDownLatch>();
        var root = new MemPrefs(null, "", gate);
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

        var userA = user("userA");
        var userB = user("userB");
        var controller = new AssocButtonPaneController(toolBox);
        var pane = new AtomicReference<javafx.scene.layout.Pane>();

        // User A has one button. Wait for A's (empty) initial load so the add isn't racing it.
        var loaded = new CountDownLatch(1);
        toolBox.getEventBus().toObserverable()
                .ofType(ForceRedrawEvent.class)
                .subscribe(e -> loaded.countDown());
        toolBox.getData().setUser(userA);
        fxAndWait(() -> pane.set(controller.getPane()));
        assertTrue(loaded.await(5, TimeUnit.SECONDS), "User A's buttons never loaded");
        fxAndWait(() -> {
            controller.addButton(new NamedAssociation("eating", "nil", "self", "a1"));
        });
        var prefsA = root.node("userA").node("org.mbari.m3.vars.annotation.ui.abpanel.AssocButtonPaneController");
        var prefsB = root.node("userB").node("org.mbari.m3.vars.annotation.ui.abpanel.AssocButtonPaneController");
        await(() -> {
            try {
                return childNames(prefsA).equals(Set.of("a1"));
            }
            catch (BackingStoreException e) {
                return false;
            }
        }, "User A's button was never saved");

        // Switch to B while B's prefs are slow to load, then touch the pane
        gate.set(new CountDownLatch(1));
        fxAndWait(() -> toolBox.getData().setUser(userB));
        fxAndWait(() -> controller.addButton(new NamedAssociation("eating", "nil", "self", "touched")));
        gate.get().countDown();

        // Let the load and any save drain
        Thread.sleep(500);
        fxAndWait(() -> { });

        assertEquals(Set.of(), childNames(prefsB), "User A's buttons leaked into user B's prefs");
        assertEquals(Set.of("a1"), childNames(prefsA), "User A's prefs were changed");
        assertEquals(0, pane.get().getChildren().size(), "Pane should hold only user B's (no) buttons");
    }
}
