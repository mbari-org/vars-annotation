package org.mbari.vars.ui.javafx.shared;

import com.jfoenix.controls.JFXComboBox;
import io.reactivex.Observable;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import org.mbari.vars.ui.UIToolBox;
import org.mbari.vars.ui.messages.ClearCacheMsg;
import org.mbari.vars.services.model.Concept;

/**
 * @author Brian Schlining
 * @since 2017-09-19T10:39:00
 */
public class ConceptSelectionDialogController {

    private Dialog<String> dialog;
    private ComboBox<String> comboBox;
    private final UIToolBox toolBox;
    private volatile String concept;

    public ConceptSelectionDialogController(UIToolBox toolBox) {
        this.toolBox = toolBox;
        Observable<Object> observable = toolBox.getEventBus().toObserverable();
        observable.ofType(ClearCacheMsg.class)
                .subscribe(m -> refresh());
    }

    public void requestFocus() {
        Platform.runLater(() -> getComboBox().requestFocus());
    }

    public String getConcept() {
        return concept;
    }

    public void setConcept(String concept) {
        boolean doRefresh = concept != null && !concept.equals(this.concept);
        this.concept = concept;
        if (doRefresh) {
            refresh();
        }
    }

    public ComboBox<String> getComboBox() {
        if (comboBox == null) {
            comboBox = new JFXComboBox<>();
            new FilteredComboBoxDecorator<>(comboBox,
                    FilteredComboBoxDecorator.STARTSWITH_IGNORE_SPACES);
        }
        return comboBox;
    }

    public Dialog<String> getDialog() {
        if (dialog == null) {
            dialog = new Dialog<>();
            DialogPane pane = dialog.getDialogPane();
            pane.getButtonTypes()
                    .addAll(ButtonType.OK, ButtonType.CANCEL);
            pane.setContent(getComboBox());
            pane.getStylesheets().addAll(toolBox.getStylesheets());
            dialog.setResultConverter(btn -> {
                if (btn == ButtonType.OK) {
                    return getComboBox().getValue();
                }
                return null;
            });
        }
        return dialog;
    }

    private void refresh() {
        if (concept != null) {
            toolBox.getServices()
                    .getConceptService()
                    .findConcept(concept)
                    .thenAccept(opt -> {
                        Platform.runLater(() -> {
                            ComboBox<String> cb = getComboBox();
                            if (opt.isPresent()) {
                                Concept c = opt.get();
                                ObservableList<String> concepts = FXCollections.observableArrayList(c.flatten());
                                cb.setItems(concepts);
                                cb.getSelectionModel().select(c.getName());
                            }
                            else {
                                cb.setItems(FXCollections.emptyObservableList());
                            }
                        });

                    });
        }
    }

}
