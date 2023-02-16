package org.mbari.vars.ui.javafx.cbpanel;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTabPane;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.ObservableList;
import javafx.geometry.Side;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import org.mbari.vars.core.util.Preconditions;
import org.mbari.vars.ui.UIToolBox;
import org.mbari.vars.ui.javafx.timeline.TimelineController;
import org.mbari.vars.ui.messages.*;
import org.mbari.vars.ui.javafx.Icons;
import org.mbari.vars.services.model.User;
import org.mbari.vars.services.util.PreferenceUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicReference;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import java.util.stream.IntStream;

/**
 * @author Brian Schlining
 * @since 2017-06-14T09:42:00
 */
public class ConceptButtonPanesController {

    private BorderPane root;
    private TabPane tabPane;
    private VBox controlPane;
    private final UIToolBox toolBox;
    private final ResourceBundle i18n;
    private TextInputDialog addDialog;
    private Alert removeDialog;
    // This is the name used by the original VARS for storing tab root prefs
    public static final String PREF_CP_NODE = "vars.annotation.ui.cbpanel.ConceptButtonPanel";
    public static final String PREFKEY_TABNAME = "tabName";
    public static final String TAB_PREFIX = "tab";

    private Logger log = LoggerFactory.getLogger(getClass());
    private BooleanProperty lockProperty = new SimpleBooleanProperty(false);
    private final ConceptButtonPanesWithHighlightController overviewController;
    private final TimelineController timelineController;

    public ConceptButtonPanesController(UIToolBox toolBox) {
        Preconditions.checkNotNull(toolBox, "The UIToolbox arg can not be null");
        this.toolBox = toolBox;
        this.i18n = toolBox.getI18nBundle();
        overviewController = new ConceptButtonPanesWithHighlightController(toolBox);
        timelineController = new TimelineController(toolBox);
        // add listener to Data.user. When changed remove all panes and reload
        toolBox.getData()
                .userProperty()
                .addListener(e -> loadTabsFromPreferences());
        toolBox.getEventBus()
                .toObserverable()
                .ofType(ReloadServicesMsg.class)
                .subscribe(msg -> loadTabsFromPreferences());
    }

    public BorderPane getRoot() {
        if (root == null) {
            root = new BorderPane();
            root.setCenter(getTabPane());
            root.setRight(getControlPane());
        }
        return root;
    }

    private VBox getControlPane() {
        if (controlPane == null) {


            // Add
            Button addButton = new JFXButton();
//            Text addIcon = gf.createIcon(MaterialIcon.ADD, "30px");
            Text addIcon = Icons.ADD.standardSize();
            addButton.setGraphic(addIcon);
            addButton.setTooltip(new Tooltip(toolBox.getI18nBundle().getString("cppanel.tabpane.add.tooltip")));
            addButton.setOnAction(e -> addTab());

            // Remove - Each tab is also individually closeable
            Button removeButton = new JFXButton();
//            Text removeIcon = gf.createIcon(MaterialIcon.REMOVE, "30px");
            Text removeIcon = Icons.REMOVE.standardSize();
            removeButton.setGraphic(removeIcon);
            removeButton.setTooltip(new Tooltip(i18n.getString("cppanel.tabpane.remove.tooltip")));
            removeButton.setOnAction(e -> {
                Tab tab = getTabPane().getSelectionModel().getSelectedItem();
                removeTab(tab);
            });

            // Lock
            Button lockButton = new JFXButton();
            Text lockIcon = Icons.LOCK.standardSize();
            Text unlockIcon = Icons.LOCK_OPEN.standardSize();
            lockButton.setGraphic(lockIcon);
            lockButton.setOnAction(e -> {
                boolean v = lockProperty.get();
                lockProperty.set(!v);
            });
            lockProperty.addListener((obj, oldV, newV) -> {
                Text icon = newV ? lockIcon : unlockIcon;
                lockButton.setGraphic(icon);
                // The Panes created by ConceptButtonPaneController contain the controller as UserData
                getTabPane().getTabs()
                        .stream()
                        .map(Tab::getContent)
                        .filter(n -> n.getUserData() instanceof ConceptButtonPaneController)
                        .map(n -> (ConceptButtonPaneController) n.getUserData())
                        .forEach(n -> n.setLocked(newV));
            });
            lockProperty.set(true);

            Button overviewButton = new JFXButton();
            Text overviewIcon = Icons.VIEW_COLUMN.standardSize();
            String overviewLabel = i18n.getString("cppanel.tabpane.overview.label");
            Tab overviewTab = new Tab(overviewLabel, new ScrollPane(overviewController.getRoot()));
            overviewButton.setGraphic(overviewIcon);
            overviewButton.setTooltip(new Tooltip(i18n.getString("cppanel.tabpane.overview.tooltip")));
            overviewButton.setOnAction(e -> {
                ObservableList<Tab> tabs = getTabPane().getTabs();
                if (tabs.contains(overviewTab)) {
                    tabs.remove(overviewTab);
                }
                else {
                    tabs.add(overviewTab);
                    getTabPane().getSelectionModel().select(overviewTab);
                }
            });

            Button timelineButton = new JFXButton();
            Text timelineIcon = Icons.TIMELINE.standardSize();
            String timelineLabel = i18n.getString("cppanel.tabpane.timeline.label");
            Tab timelineTab = new Tab(timelineLabel, timelineController.getRoot());
            timelineButton.setGraphic(timelineIcon);
            timelineButton.setTooltip(new Tooltip(i18n.getString("cppanel.tabpane.timeline.tooltip")));
            timelineButton.setOnAction(e -> {
                ObservableList<Tab> tabs = getTabPane().getTabs();
                if (tabs.contains(timelineTab)) {
                    tabs.remove(timelineTab);
                }
                else {
                    tabs.add(timelineTab);
                    getTabPane().getSelectionModel().select(timelineTab);
                }
            });

            // COntrol Pane
            controlPane = new VBox(addButton, removeButton, lockButton, overviewButton, timelineButton);
        }
        return controlPane;
    }

    private TabPane getTabPane() {
        if (tabPane == null) {
            tabPane = new JFXTabPane();
            tabPane.setSide(Side.BOTTOM);
        }
        return tabPane;
    }

    private void loadTabsFromPreferences() {
        Platform.runLater(() -> getTabPane().getTabs().clear());
        Optional<Preferences> tabsPrefsOpt = getTabsPreferences();
        if (tabsPrefsOpt.isPresent()) {
            Preferences tabsPrefs = tabsPrefsOpt.get();

            toolBox.getExecutorService().submit(() -> {
                try {
                    Arrays.stream(tabsPrefs.childrenNames())
                            .forEach(tabName -> {
                                Preferences tabPrefs = tabsPrefs.node(tabName);
                                String name = tabPrefs.get(PREFKEY_TABNAME, "dummy");
                                Platform.runLater(() -> {
                                    ConceptButtonPaneController controller = new ConceptButtonPaneController(
                                            toolBox, tabsPrefs.node(tabName));
                                    controller.setLocked(lockProperty.get());
                                    Tab tab = new Tab(name, controller.getPane());
                                    tab.setClosable(true);
                                    tab.setOnClosed(e -> removeTab(tab));
                                    getTabPane().getTabs().add(tab);
                                });

                            });
                }
                catch (BackingStoreException e) {
                    log.error("VARS had a problem loading user tabs for user: " + toolBox.getData().getUser());
                }
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
            cpPrefs = userPreferences.node(PREF_CP_NODE);
        }
        return Optional.ofNullable(cpPrefs);
    }

    private void removeTab(Tab tab) {
        Optional<Preferences> cpPrefsOpt = getTabsPreferences();
        if (cpPrefsOpt.isPresent()) {
            if (removeDialog == null) {
                removeDialog = new Alert(Alert.AlertType.CONFIRMATION);
                removeDialog.setTitle(i18n.getString("cppanel.tabpane.remove.title"));
                removeDialog.setHeaderText(i18n.getString("cppanel.tabpane.remove.header"));
                removeDialog.setContentText(i18n.getString("cppanel.tabpane.remove.content"));
                removeDialog.getDialogPane()
                        .getStylesheets()
                        .addAll(toolBox.getStylesheets());
            }
            Optional<ButtonType> buttonType = removeDialog.showAndWait();
            if (buttonType.get() == ButtonType.OK) {
                Preferences cpPrefs = cpPrefsOpt.get();
                int currentTabNumber = getTabPane().getSelectionModel().getSelectedIndex();
                try {
                    cpPrefs.node(TAB_PREFIX + currentTabNumber).removeNode();
                }
                catch (BackingStoreException e) {
                    toolBox.getEventBus()
                            .send(new ShowNonfatalErrorAlert(i18n.getString("cppanel.tabpane.remove.preferror.title"),
                                    i18n.getString("cppanel.tabpane.remove.preferror.header"),
                                    i18n.getString("cppanel.tabpane.remove.preferror.content"),
                                    e));
                }

                // Rename all the rest of the tabs to move them up.
                IntStream.range(currentTabNumber + 1, getTabPane().getTabs().size())
                        .forEach(i -> {
                            PreferenceUtils.copyPrefs(cpPrefs.node(TAB_PREFIX + i), cpPrefs.node(TAB_PREFIX + (i - 1)));
                            try {
                                cpPrefs.node(TAB_PREFIX + i).removeNode();
                            }
                            catch (BackingStoreException e) {
                                log.error("Failed to re-order tabs when one was deleted", e);
                            }
                        });
                loadTabsFromPreferences();
            }
        }
    }

    private void addTab() {
        User user = toolBox.getData().getUser();
        if (user == null) {
            return;
        }

        if (addDialog == null) {
            addDialog = new TextInputDialog();
            addDialog.setTitle(i18n.getString("cppanel.tabpane.add.title"));
            addDialog.setHeaderText(i18n.getString("cppanel.tabpane.add.header"));
            addDialog.setContentText(i18n.getString("cppanel.tabpane.add.content"));
            addDialog.getDialogPane()
                    .getStylesheets()
                    .addAll(toolBox.getStylesheets());
        }
        Optional<String> tabNameOpt = addDialog.showAndWait();

        if (tabNameOpt.isPresent()) {
            String tabName = tabNameOpt.get();
            try {
                Optional<Preferences> cpPrefsOpt = getTabsPreferences();

                // First thing to do is check to make sure no tab is already there with that name
                if (cpPrefsOpt.isPresent()) {
                    final Preferences cpPrefs = cpPrefsOpt.get();
                    String[] cpTabs;
                    try {
                        cpTabs = cpPrefs.childrenNames();
                    } catch (final BackingStoreException bse) {
                        toolBox.getEventBus()
                                .send(new ShowNonfatalErrorAlert(i18n.getString("cppanel.tabpane.addtab.preferror.title"),
                                        i18n.getString("cppanel.tabpane.addtab.preferror.header"),
                                        i18n.getString("cppanel.tabpane.addtab.preferror.content"),
                                        bse));
                        return;
                    }

                    boolean alreadyThere = false;
                    for (int j = 0; j < cpTabs.length; j++) {
                        final Preferences tabPrefs = cpPrefs.node(TAB_PREFIX + j);
                        if (tabPrefs.get(PREFKEY_TABNAME, "").equals(tabName)) {
                            alreadyThere = true;
                            break;
                        }
                    }

                    if (!alreadyThere) {
                        final Preferences newTabPrefs = cpPrefs.node(TAB_PREFIX + cpTabs.length);
                        newTabPrefs.put(PREFKEY_TABNAME, tabName);
                        ConceptButtonPaneController dropPanel = new ConceptButtonPaneController(toolBox,
                                newTabPrefs);
                        dropPanel.setLocked(lockProperty.get());
                        Tab tab = new Tab(tabName, dropPanel.getPane());
                        tab.setClosable(true);
                        tab.setOnClosed(e -> removeTab(tab));
                        getTabPane().getTabs().add(tab);
                        getTabPane().getSelectionModel().select(tab);
                    } else {
                        toolBox.getEventBus()
                                .send(new ShowInfoAlert(i18n.getString("cppanel.tabpane.addtab.dub.title"),
                                        i18n.getString("cppanel.tabpane.addtab.dup.header"),
                                        i18n.getString("cppanel.tabpane.addtab.dup.content")));
                    }
                }
            }
            catch (Exception e) {
                ResourceBundle i18n = toolBox.getI18nBundle();
                toolBox.getEventBus()
                        .send(new ShowNonfatalErrorAlert(i18n.getString("cppanel.tabpane.addtab.err.title"),
                                i18n.getString("cppanel.tabpane.addtab.err.header"),
                                i18n.getString("cppanel.tabpane.addtab.err.content"),
                                e));
            }
        }

    }
}
