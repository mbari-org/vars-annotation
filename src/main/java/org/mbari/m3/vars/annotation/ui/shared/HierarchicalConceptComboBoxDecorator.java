package org.mbari.m3.vars.annotation.ui.shared;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.scene.control.ComboBox;
import org.mbari.m3.vars.annotation.services.ConceptService;

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
        ObservableList<String> items = comboBox.getItems();
        items.clear();
        if (concept != null) {
            conceptService.findConcept(concept)
                    .thenAccept(opt -> {
                        opt.ifPresent(c -> {
                            Platform.runLater(() -> {
                                List<String> names = c.flatten();
                                items.setAll(names); // Add descendants and alternate names
                            });
                        });
                    });
        }
    }
}
