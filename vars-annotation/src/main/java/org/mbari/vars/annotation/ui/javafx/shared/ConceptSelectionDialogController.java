package org.mbari.vars.annotation.ui.javafx.shared;

import io.reactivex.rxjava3.core.Observable;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import org.mbari.vars.annotation.ui.UIToolBox;
import org.mbari.vars.annotation.ui.messages.ReloadServicesMsg;
import org.mbari.vars.oni.sdk.r1.models.Concept;

/**
 * @author Brian Schlining
 * @since 2017-09-19T10:39:00
 */
public class ConceptSelectionDialogController {

    private Dialog<String> dialog;
    private ComboBox<String> comboBox;
    private StackPane comboBoxContainer;
    private ProgressIndicator loadingIndicator;
    private volatile boolean loading = false;
    private final UIToolBox toolBox;
    private volatile String concept;

    public ConceptSelectionDialogController(UIToolBox toolBox) {
        this.toolBox = toolBox;
        Observable<Object> observable = toolBox.getEventBus().toObserverable();
        observable.ofType(ReloadServicesMsg.class)
                .subscribe(m -> refresh());
    }

    public void requestFocus() {
        Platform.runLater(() -> getComboBox().requestFocus());
    }

    public String getConcept() {
        return concept;
    }

    public void setConcept(String concept, boolean force) {
        boolean doRefresh = force || concept != null && !concept.equals(this.concept);
        this.concept = concept;
        if (doRefresh) {
            refresh();
        }
    }

    public ComboBox<String> getComboBox() {
        if (comboBox == null) {
            comboBox = new ComboBox<>();
            new FilteredComboBoxDecorator<>(comboBox,
                    FilteredComboBoxDecorator.STARTSWITH_IGNORE_SPACES);
        }
        return comboBox;
    }

    private StackPane getComboBoxContainer() {
        if (comboBoxContainer == null) {
            loadingIndicator = new ProgressIndicator();
            loadingIndicator.setMaxSize(24, 24);
            var cb = getComboBox();
            // Reflect whatever loading state was set before the dialog was first built.
            cb.setVisible(!loading);
            loadingIndicator.setVisible(loading);
            comboBoxContainer = new StackPane(loadingIndicator, cb);
        }
        return comboBoxContainer;
    }

    public Dialog<String> getDialog() {
        if (dialog == null) {
            dialog = new Dialog<>();
            dialog.setResizable(true);
            DialogPane pane = dialog.getDialogPane();
            pane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
            pane.setContent(getComboBoxContainer());
            pane.getStylesheets().addAll(toolBox.getStylesheets());
            dialog.setResultConverter(btn -> {
                if (btn == ButtonType.OK) {
                    return getComboBox().getValue();
                }
                return null;
            });
            // Keep OK disabled while concepts are still loading.
            var okButton = (Button) pane.lookupButton(ButtonType.OK);
            if (okButton != null) {
                okButton.setDisable(loading);
            }
        }
        return dialog;
    }

    private void refresh() {
        if (concept == null) return;

        loading = true;
        // Show spinner on the FX thread if the container has already been built.
        Platform.runLater(() -> {
            if (comboBoxContainer != null) {
                getComboBox().setVisible(false);
                loadingIndicator.setVisible(true);
            }
            if (dialog != null) {
                var okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
                if (okButton != null) okButton.setDisable(true);
            }
        });

        toolBox.getServices()
                .conceptService()
                .findPhylogenyDown(concept)
                .thenAccept(opt -> Platform.runLater(() -> {
                    loading = false;
                    ComboBox<String> cb = getComboBox();
                    if (opt.isPresent()) {
                        Concept c = opt.get();
                        ObservableList<String> concepts = FXCollections.observableArrayList(c.flatten());
                        cb.setItems(concepts);
                        cb.getSelectionModel().select(c.getName());
                    } else {
                        cb.setItems(FXCollections.emptyObservableList());
                    }

                    if (comboBoxContainer != null) {
                        loadingIndicator.setVisible(false);
                        cb.setVisible(true);
                    }
                    if (dialog != null) {
                        var okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
                        if (okButton != null) okButton.setDisable(false);
                        var scene = dialog.getDialogPane().getScene();
                        if (scene != null && scene.getWindow() != null && scene.getWindow().isShowing()) {
                            scene.getWindow().sizeToScene();
                        }
                    }
                }));
    }
}
