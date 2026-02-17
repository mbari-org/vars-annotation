package org.mbari.vars.annotation.ui.javafx.shared;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ComboBox;
import org.mbari.vars.annotation.etc.jdk.Loggers;
import org.mbari.vars.annotation.ui.UIToolBox;

import java.util.List;

/**
 * @author Brian Schlining
 * @since 2017-07-19T16:04:00
 */
public class HierarchicalConceptComboBoxDecorator {

    private final ComboBox<String> comboBox;
    private final UIToolBox toolBox;
    private static final Loggers log = new Loggers(HierarchicalConceptComboBoxDecorator.class);

    public HierarchicalConceptComboBoxDecorator(ComboBox<String> comboBox, UIToolBox toolBox) {
        this.comboBox = comboBox;
        this.toolBox = toolBox;
        comboBox.setItems(FXCollections.observableArrayList());
    }

    public void setConcept(String concept) {
        setConcept(concept, concept);
    }

    public void setConcept(String concept, String selectedConcept) {
//        log.debug("Setting concept to " + concept);
        Platform.runLater(() -> {
            if (concept != null) {


                toolBox.getServices()
                        .conceptService()
                        .findPhylogenyDown(concept)
                        .handle((opt, ex) -> {

                            // Build list of concepts to display
                            ObservableList<String> items = FXCollections.observableArrayList();
                            if (ex != null) {
                                log.atWarn().withCause(ex).log("Failed to look up " + concept);
                                items.add(concept);
                            } else {
                                List<String> names = opt.isPresent() ? opt.get().flatten() : List.of(concept);
//                                log.debug("Adding " + names);
                                items.addAll(names);
                            }

                            Platform.runLater(() -> {
                                comboBox.setItems(items);
                                String selected = items.contains(selectedConcept) ? selectedConcept : concept;
//                                log.debug("Selecting " + selected);
                                comboBox.getSelectionModel().select(selected);
                            });
                            return null;
                        });
            }
        });
    }
}
