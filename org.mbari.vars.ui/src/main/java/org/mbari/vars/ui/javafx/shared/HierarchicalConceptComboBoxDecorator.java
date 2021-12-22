package org.mbari.vars.ui.javafx.shared;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ComboBox;
import org.mbari.vars.ui.UIToolBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

/**
 * @author Brian Schlining
 * @since 2017-07-19T16:04:00
 */
public class HierarchicalConceptComboBoxDecorator {

    private final ComboBox<String> comboBox;
    private final UIToolBox toolBox;
    private static final Logger log = LoggerFactory.getLogger(HierarchicalConceptComboBoxDecorator.class);

    public HierarchicalConceptComboBoxDecorator(ComboBox<String> comboBox, UIToolBox toolBox) {
        this.comboBox = comboBox;
        this.toolBox = toolBox;
        comboBox.setItems(FXCollections.observableArrayList());
    }

    public void setConcept(String concept) {
        setConcept(concept, concept);
//        log.debug("Setting concept to " + concept);
//        Platform.runLater(() -> {
//
//            if (concept != null) {
//                toolBox.getServices()
//                        .getConceptService()
//                        .findConcept(concept)
//                        .handle((opt, ex) -> {
//                            Platform.runLater(() -> {
//                                ObservableList<String> items = FXCollections.observableArrayList();
//                                if (ex != null) {
//                                    log.warn("Failed to look up " + concept, ex);
//                                    items.add(concept);
//                                } else {
//                                    List<String> names = opt.isPresent() ? opt.get().flatten() : Arrays.asList(concept);
//                                    items.addAll(names);
//                                }
//                                comboBox.setItems(items);
//                                comboBox.getSelectionModel().select(concept);
//
//                            });
//                            return null;
//                        });
//            }
//        });
    }

    public void setConcept(String concept, String selectedConcept) {
        log.debug("Setting concept to " + concept);
        Platform.runLater(() -> {

            if (concept != null) {
                toolBox.getServices()
                        .getConceptService()
                        .findConcept(concept)
                        .handle((opt, ex) -> {
                            Platform.runLater(() -> {
                                ObservableList<String> items = FXCollections.observableArrayList();
                                if (ex != null) {
                                    log.warn("Failed to look up " + concept, ex);
                                    items.add(concept);
                                } else {
                                    List<String> names = opt.isPresent() ? opt.get().flatten() : Arrays.asList(concept);
                                    items.addAll(names);
                                }
                                comboBox.setItems(items);
                                if (items.contains(selectedConcept)) {
                                    comboBox.getSelectionModel().select(selectedConcept);
                                }
                                else {
                                    comboBox.getSelectionModel().select(concept);
                                }
                            });
                            return null;
                        });
            }
        });
    }
}
