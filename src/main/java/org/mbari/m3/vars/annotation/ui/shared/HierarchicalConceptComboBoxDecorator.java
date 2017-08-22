package org.mbari.m3.vars.annotation.ui.shared;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ComboBox;
import org.mbari.m3.vars.annotation.services.ConceptService;

import java.util.Arrays;
import java.util.List;

/**
 * @author Brian Schlining
 * @since 2017-07-19T16:04:00
 */
public class HierarchicalConceptComboBoxDecorator {

    private final ComboBox<String> comboBox;
    private final ConceptService conceptService;

    public HierarchicalConceptComboBoxDecorator(ComboBox<String> comboBox, ConceptService conceptService) {
        this.comboBox = comboBox;
        this.conceptService = conceptService;
    }

    public void setConcept(String concept) {
        Platform.runLater(() -> {
            ObservableList<String> items = comboBox.getItems();
            items.clear();
            if (concept != null) {
                conceptService.findConcept(concept)
                        .handle((opt, ex) -> {
                            Platform.runLater(() -> {
                                if (ex != null) {
                                    items.add(concept);
                                } else {
                                    List<String> names = opt.isPresent() ? opt.get().flatten() : Arrays.asList(concept);
                                    items.setAll(names);
                                    //comboBox.setItems(FXCollections.observableArrayList(names));
                                    comboBox.getSelectionModel().select(concept);
                                }
                            });
                            return null;
                        });
            }
        });
    }
}
