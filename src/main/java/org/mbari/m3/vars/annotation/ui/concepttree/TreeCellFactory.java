package org.mbari.m3.vars.annotation.ui.concepttree;

import javafx.event.EventHandler;
import javafx.scene.control.TreeCell;
import javafx.scene.control.cell.TextFieldTreeCell;
import javafx.scene.input.MouseEvent;
import org.mbari.m3.vars.annotation.model.Concept;
import org.mbari.m3.vars.annotation.model.ConceptDetails;
import org.mbari.m3.vars.annotation.services.ConceptService;

import java.util.stream.Collectors;

/**
 * @author Brian Schlining
 * @since 2017-05-15T13:58:00
 */
public class TreeCellFactory {

    private final ConceptService conceptService;

    public TreeCellFactory(ConceptService conceptService) {
        this.conceptService = conceptService;
    }

    public TreeCell<Concept> newTreeCell() {
        return new ConceptTreeCell();
    }

    private final class ConceptTreeCell extends TextFieldTreeCell<Concept> {

        MediaMouseEvent eventHandler = new MediaMouseEvent();

        @Override
        public void updateItem(Concept item, boolean empty) {
            super.updateItem(item, empty);
            setStyle("");
            if (item == null || empty) {
                setText(null);
            }
            else {
                String s = item.getName() + "[" + item.getRank() + "]";
                setText(s);
                conceptService.findDetails(item.getName())
                        .thenAccept(opt -> {
                            if (opt.isPresent()) {
                                ConceptDetails cd = opt.get();
                                // Add alternate names
                                if (!cd.getAlternateNames().isEmpty()) {
                                    String names = cd.getAlternateNames()
                                            .stream()
                                            .collect(Collectors.joining(", "));
                                    String t = item.getName() + " (" + names +
                                            ") [" + item.getRank() + "]";
                                    setText(t);
                                }
                                if (!cd.getMedia().isEmpty()) {
                                    setStyle("-fx-background-color: darkseagreen");
                                }

                                eventHandler.setConceptDetails(cd);
                            }
                            else {
                                eventHandler.setConceptDetails(null);
                            }
                        });

            }
        }


    }

    private final class MediaMouseEvent implements EventHandler<MouseEvent> {

        private ConceptDetails conceptDetails;

        @Override
        public void handle(MouseEvent event) {
            if (event.getClickCount() == 2) {
                // TODO open images in a separate window
            }
        }

        public ConceptDetails getConceptDetails() {
            return conceptDetails;
        }

        public void setConceptDetails(ConceptDetails conceptDetails) {
            this.conceptDetails = conceptDetails;
        }
    }

}
