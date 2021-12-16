package org.mbari.vars.ui.javafx.shared;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ComboBox;
import org.mbari.vars.services.ConceptService;
import org.mbari.vars.ui.UIToolBox;

import java.util.Arrays;
import java.util.List;

/**
 * @author Brian Schlining
 * @since 2017-07-19T16:04:00
 */
public class HierarchicalConceptComboBoxDecorator {

    private final ComboBox<String> comboBox;
    private final UIToolBox toolBox;

    public HierarchicalConceptComboBoxDecorator(ComboBox<String> comboBox, UIToolBox toolBox) {
        this.comboBox = comboBox;
        this.toolBox = toolBox;
        comboBox.setItems(FXCollections.observableArrayList());
    }

    public void setConcept(String concept) {
        Platform.runLater(() -> {

            if (concept != null) {
                toolBox.getServices()
                        .getConceptService()
                        .findConcept(concept)
                        .handle((opt, ex) -> {
                            Platform.runLater(() -> {
                                ObservableList<String> items = FXCollections.observableArrayList();
                                if (ex != null) {
                                    items.add(concept);
                                } else {
                                    List<String> names = opt.isPresent() ? opt.get().flatten() : Arrays.asList(concept);
                                    items.addAll(names);
                                }
                                comboBox.setItems(items);
                                comboBox.getSelectionModel().select(concept);
                            });
                            return null;
                        });
            }
        });
    }
}
