package org.mbari.vars.annotation.test.ui.javafx.shared;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.mbari.vars.annotation.ui.javafx.shared.FilteredComboBoxDecorator;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Needs a real JavaFX toolkit. On macOS, starting JavaFX in the shared test JVM deadlocks any later
 * test that initializes AWT (e.g. ImageUtils), so this only runs on request:
 * <pre>FXTESTS=true ./gradlew test --tests "*FilteredComboBoxDecoratorTest*"</pre>
 */
@EnabledIfEnvironmentVariable(named = "FXTESTS", matches = "true")
public class FilteredComboBoxDecoratorTest {

    private static final List<String> ITEMS = List.of("Apple", "Apricot", "Banana", "Cherry");

    @BeforeAll
    public static void startFx() throws Exception {
        var latch = new CountDownLatch(1);
        try {
            Platform.startup(latch::countDown);
        }
        catch (IllegalStateException e) {
            latch.countDown(); // already started
        }
        latch.await(10, TimeUnit.SECONDS);
    }

    private static <T> T fx(Callable<T> c) throws Exception {
        var task = new FutureTask<>(c);
        Platform.runLater(task);
        return task.get(10, TimeUnit.SECONDS);
    }

    private static void type(ComboBox<String> cb, String text, KeyCode code) {
        cb.fireEvent(new KeyEvent(KeyEvent.KEY_PRESSED, text, text, code, false, false, false, false));
    }

    /** Returns the number of items visible in the combobox after typing 'A' */
    private int filteredSizeAfterTypingA(boolean replaceItems) throws Exception {
        return fx(() -> {
            var cb = new ComboBox<String>();
            cb.setItems(FXCollections.observableArrayList(ITEMS));
            new FilteredComboBoxDecorator<>(cb, FilteredComboBoxDecorator.STARTSWITH_IGNORE_SPACES);
            var stage = new Stage();
            stage.setScene(new Scene(cb));
            stage.show();
            if (replaceItems) {
                cb.setItems(FXCollections.observableArrayList(ITEMS));
            }
            type(cb, "a", KeyCode.A);
            var size = cb.getItems().size();
            stage.close();
            return size;
        });
    }

    /**
     * Regression: decorating a combobox that is already showing (like SampleBC does when the samplers
     * finish loading after the dialog opens) replaces its default skin. The disposed skin left a listener
     * on the items list, so the next setItems NPE'd in ComboBoxListViewSkin. JavaFX hands exceptions
     * thrown by list listeners to the uncaught exception handler, so we have to capture them there.
     */
    @Test
    public void setItemsAfterDecoratingAShowingComboBox() throws Exception {
        var errors = new java.util.ArrayList<Throwable>();
        var size = fx(() -> {
            var thread = Thread.currentThread();
            var originalHandler = thread.getUncaughtExceptionHandler();
            thread.setUncaughtExceptionHandler((t, e) -> errors.add(e));
            try {
                var cb = new ComboBox<String>();
                var stage = new Stage();
                stage.setScene(new Scene(cb));
                stage.show();
                var defaultSkin = cb.getSkin(); // hold it so GC can't hide the bug
                new FilteredComboBoxDecorator<>(cb, FilteredComboBoxDecorator.STARTSWITH_IGNORE_SPACES);
                cb.setItems(FXCollections.observableArrayList(ITEMS));
                type(cb, "a", KeyCode.A);
                var n = cb.getItems().size();
                stage.close();
                return defaultSkin == null ? -1 : n;
            }
            finally {
                thread.setUncaughtExceptionHandler(originalHandler);
            }
        });
        assertEquals(List.of(), errors);
        assertEquals(2, size);
    }

    @Test
    public void filtersWithInitialItems() throws Exception {
        assertEquals(2, filteredSizeAfterTypingA(false));
    }

    /** Regression: a new list with the same contents used to bypass the decorator */
    @Test
    public void filtersAfterItemsAreReplaced() throws Exception {
        assertEquals(2, filteredSizeAfterTypingA(true));
    }

    /**
     * Type a filter, pick an item from the popup's list the way a click does, close the popup.
     * Returns the combobox's value once the event queue has drained.
     */
    private String valueAfterPickingFilteredItem(boolean replaceItems, boolean preselectFirst, String typed, KeyCode code, String pick) throws Exception {
        var holder = new Object[2];
        fx(() -> {
            var cb = new ComboBox<String>();
            cb.setItems(FXCollections.observableArrayList(ITEMS));
            new FilteredComboBoxDecorator<>(cb, FilteredComboBoxDecorator.STARTSWITH_IGNORE_SPACES);
            var stage = new Stage();
            stage.setScene(new Scene(cb));
            stage.show();
            if (replaceItems) {
                cb.setItems(FXCollections.observableArrayList(ITEMS));
            }
            if (preselectFirst) {
                cb.getSelectionModel().select(ITEMS.getFirst()); // the dialog pre-selects the root concept
            }
            type(cb, typed, code);
            @SuppressWarnings("unchecked")
            var listView = (javafx.scene.control.ListView<String>)
                    ((javafx.scene.control.skin.ComboBoxListViewSkin<String>) cb.getSkin()).getPopupContent();
            listView.getSelectionModel().select(listView.getItems().indexOf(pick));
            cb.hide();
            holder[0] = cb;
            holder[1] = stage;
            return null;
        });
        fx(() -> null); // let anything the skin queued with runLater run
        fx(() -> null);
        return fx(() -> {
            var cb = (ComboBox<String>) holder[0];
            var value = cb.getValue();
            ((Stage) holder[1]).close();
            return value;
        });
    }

    @Test
    public void pickedItemSurvivesClosingPopup() throws Exception {
        assertEquals("Cherry", valueAfterPickingFilteredItem(false, false, "c", KeyCode.C, "Cherry"));
        assertEquals("Apricot", valueAfterPickingFilteredItem(false, false, "a", KeyCode.A, "Apricot"));
    }

    @Test
    public void pickedItemSurvivesClosingPopupAfterItemsReplaced() throws Exception {
        assertEquals("Cherry", valueAfterPickingFilteredItem(true, false, "c", KeyCode.C, "Cherry"));
        assertEquals("Apricot", valueAfterPickingFilteredItem(true, false, "a", KeyCode.A, "Apricot"));
    }

    @Test
    public void pickedItemSurvivesClosingPopupWhenFirstItemWasPreselected() throws Exception {
        assertEquals("Cherry", valueAfterPickingFilteredItem(false, true, "c", KeyCode.C, "Cherry"));
        assertEquals("Apricot", valueAfterPickingFilteredItem(false, true, "a", KeyCode.A, "Apricot"));
        assertEquals("Cherry", valueAfterPickingFilteredItem(true, true, "c", KeyCode.C, "Cherry"));
    }

    @Test
    public void keyboardPickFromFilteredList() throws Exception {
        var holder = new Object[2];
        fx(() -> {
            var cb = new ComboBox<String>();
            cb.setItems(FXCollections.observableArrayList(ITEMS));
            new FilteredComboBoxDecorator<>(cb, FilteredComboBoxDecorator.STARTSWITH_IGNORE_SPACES);
            var stage = new Stage();
            stage.setScene(new Scene(cb));
            stage.show();
            cb.getSelectionModel().select(ITEMS.getFirst());
            type(cb, "a", KeyCode.A);
            type(cb, "", KeyCode.DOWN);
            cb.hide();
            holder[0] = cb;
            holder[1] = stage;
            return null;
        });
        fx(() -> null);
        fx(() -> null);
        var v = fx(() -> {
            var cb = (ComboBox<String>) holder[0];
            var value = cb.getValue();
            ((Stage) holder[1]).close();
            return value;
        });
        assertEquals("Apricot", v);
    }

    private static final List<String> NAMES = List.of("1-gallon paint bucket", "Apple", "Banana", "Grimpoteuthis",
            "Grimpoteuthis abyssicola", "Zebra", "object", "objects of desire", "octopus");

    /** Returns [value, displayed text, selectedIndex, index of value in items] after typing and closing the popup */
    private List<Object> typeAndClose(String preselect, String typed, String pick) throws Exception {
        return typeAndClose(NAMES, false, preselect, typed, pick);
    }

    private List<Object> typeAndClose(List<String> names, boolean setItemsAfterDecorating, String preselect, String typed, String pick) throws Exception {
        var holder = new Object[2];
        fx(() -> {
            var cb = new ComboBox<String>();
            if (!setItemsAfterDecorating) {
                cb.setItems(FXCollections.observableArrayList(names));
            }
            new FilteredComboBoxDecorator<>(cb, FilteredComboBoxDecorator.STARTSWITH_IGNORE_SPACES);
            var stage = new Stage();
            stage.setScene(new Scene(cb));
            stage.show();
            if (setItemsAfterDecorating) {
                cb.setItems(FXCollections.observableArrayList(names)); // like ConceptSelectionDialogController
            }
            cb.getSelectionModel().select(preselect);
            for (char ch : typed.toCharArray()) {
                type(cb, String.valueOf(ch), KeyCode.getKeyCode(String.valueOf(ch).toUpperCase()));
            }
            if (pick != null) {
                cb.getSelectionModel().select(pick);
            }
            cb.hide();
            holder[0] = cb;
            holder[1] = stage;
            return null;
        });
        fx(() -> null);
        fx(() -> null);
        return fx(() -> {
            var cb = (ComboBox<String>) holder[0];
            var skin = (javafx.scene.control.skin.ComboBoxListViewSkin<String>) cb.getSkin();
            var display = ((javafx.scene.control.ListCell<?>) skin.getDisplayNode()).getText();
            var out = List.<Object>of(cb.getValue(), String.valueOf(display),
                    cb.getSelectionModel().getSelectedIndex(), cb.getItems().indexOf(cb.getValue()));
            ((Stage) holder[1]).close();
            return out;
        });
    }

    @Test
    public void displayMatchesValueWhenReselectingCurrentValue() throws Exception {
        // the dialog pre-selects "object"; the user types "object" and accepts it
        assertEquals(List.of("object", "object", 6, 6), typeAndClose("object", "object", null));
    }

    @Test
    public void displayMatchesValueWhenPickingDifferentValue() throws Exception {
        assertEquals(List.of("Grimpoteuthis", "Grimpoteuthis", 3, 3), typeAndClose("object", "grimpoteuthis", null));
    }

    private static List<String> bigList() {
        var xs = new java.util.ArrayList<String>();
        for (int i = 0; i < 4000; i++) {
            xs.add(String.format("Zed %04d", i));
        }
        xs.addAll(NAMES);
        xs.sort(String.CASE_INSENSITIVE_ORDER);
        return xs;
    }

    @Test
    public void displayMatchesValueInBigListSetAfterDecorating() throws Exception {
        var names = bigList();
        var idx = names.indexOf("object");
        assertEquals(List.of("object", "object", idx, idx), typeAndClose(names, true, "object", "object", null));
        var g = names.indexOf("Grimpoteuthis");
        assertEquals(List.of("Grimpoteuthis", "Grimpoteuthis", g, g), typeAndClose(names, true, "object", "grimpoteuthis", null));
    }

    @Test
    public void enterAcceptsTypedName() throws Exception {
        var holder = new Object[2];
        var names = bigList();
        fx(() -> {
            var cb = new ComboBox<String>();
            new FilteredComboBoxDecorator<>(cb, FilteredComboBoxDecorator.STARTSWITH_IGNORE_SPACES);
            var stage = new Stage();
            stage.setScene(new Scene(cb));
            stage.show();
            cb.setItems(FXCollections.observableArrayList(names));
            cb.getSelectionModel().select("object");
            for (char ch : "object".toCharArray()) {
                type(cb, String.valueOf(ch), KeyCode.getKeyCode(String.valueOf(ch).toUpperCase()));
            }
            cb.fireEvent(new KeyEvent(KeyEvent.KEY_PRESSED, "", "", KeyCode.ENTER, false, false, false, false));
            cb.fireEvent(new KeyEvent(KeyEvent.KEY_RELEASED, "", "", KeyCode.ENTER, false, false, false, false));
            holder[0] = cb;
            holder[1] = stage;
            return null;
        });
        fx(() -> null);
        fx(() -> null);
        var out = fx(() -> {
            var cb = (ComboBox<String>) holder[0];
            var skin = (javafx.scene.control.skin.ComboBoxListViewSkin<String>) cb.getSkin();
            var msg = List.<Object>of(cb.getValue(), cb.getSelectionModel().getSelectedIndex(),
                    cb.getItems().indexOf("object"), String.valueOf(((javafx.scene.control.ListCell<?>) skin.getDisplayNode()).getText()));
            ((Stage) holder[1]).close();
            return msg;
        });
        var idx = names.indexOf("object");
        assertEquals(List.of("object", idx, idx, "object"), out);
    }

    /**
     * Regression: after the filter is cleared the skin can leave the button cell pointing at a stale row
     * (e.g. row 0) even though the value/selection are right. The displayed text must follow the value.
     */
    @Test
    public void displayFollowsValueEvenIfButtonCellIndexIsStale() throws Exception {
        var names = bigList();
        var out = fx(() -> {
            var cb = new ComboBox<String>();
            new FilteredComboBoxDecorator<>(cb, FilteredComboBoxDecorator.STARTSWITH_IGNORE_SPACES);
            cb.setItems(FXCollections.observableArrayList(names));
            var stage = new Stage();
            stage.setScene(new Scene(cb));
            stage.show();
            cb.getSelectionModel().select("object");
            var skin = (javafx.scene.control.skin.ComboBoxListViewSkin<String>) cb.getSkin();
            var cell = (javafx.scene.control.ListCell<?>) skin.getDisplayNode();
            var before = cell.getText();
            cell.updateIndex(0); // the stale state seen in the app
            var after = cell.getText();
            stage.close();
            return List.of(before, String.valueOf(after));
        });
        assertEquals(List.of("object", "object"), out);
    }
}
