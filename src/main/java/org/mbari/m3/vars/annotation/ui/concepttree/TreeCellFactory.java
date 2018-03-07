package org.mbari.m3.vars.annotation.ui.concepttree;

import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.control.TreeCell;
import javafx.scene.control.cell.TextFieldTreeCell;
import javafx.scene.input.*;
import org.mbari.m3.vars.annotation.model.Concept;
import org.mbari.m3.vars.annotation.model.ConceptDetails;
import org.mbari.m3.vars.annotation.services.ConceptService;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Brian Schlining
 * @since 2017-05-15T13:58:00
 */
class TreeCellFactory {


    private String styleClassPlain = "concepttree-treecell";
    private String styleClassWithMedia = "concepttree-treecell-media";
    private final ConceptService conceptService;

    TreeCellFactory(ConceptService conceptService) {
        this.conceptService = conceptService;
    }

    TreeCell<Concept> build() {
        return new ConceptTreeCell();
    }

    private final class ConceptTreeCell extends TextFieldTreeCell<Concept> {

        public ConceptTreeCell() {
            setOnDragDetected(evt -> {
                Concept concept = getItem();
                if (concept != null) {
                    // Drag the string name to some target.
                    Dragboard db = startDragAndDrop(TransferMode.ANY);
                    ClipboardContent content = new ClipboardContent();
                    content.putString(concept.getName());
                    db.setContent(content);
                    evt.consume();
                }
            });
            getStyleClass().add(styleClassPlain);
        }

        @Override
        public void updateItem(Concept item, boolean empty) {
            super.updateItem(item, empty);
            setStyle("");

            if (item == null || empty) {
                setText(null);
            }
            else {
                updateCell(item);
                if (item.getConceptDetails() == null) {
                    conceptService.findDetails(item.getName())
                            .thenAccept(opt -> {
                                opt.ifPresent(cd -> {
                                    item.setConceptDetails(cd);
                                    updateCell(item);
                                });
                            });
                }
            }
        }

        private String asString(Concept item) {
            List<String> ans = item.getAlternativeNames();
            String names = ans.stream()
                    .collect(Collectors.joining(", "));
            String s = ans.isEmpty() ? item.getName() :
                    item.getName() + " (" + names + ")";

            return s;
        }

        private void updateCell(Concept item) {
            String text = asString(item);
            Runnable r = () -> {
                setText(text);
                ConceptDetails cd = item.getConceptDetails();
                if (cd != null) {
                    if (!cd.getMedia().isEmpty()) {
                        getStyleClass().removeAll(styleClassPlain);
                        getStyleClass().addAll(styleClassWithMedia);
                    }
                    else {
                        getStyleClass().removeAll(styleClassWithMedia);
                        getStyleClass().addAll(styleClassPlain);
                    }
                }
            };
            if (Platform.isFxApplicationThread()) {
                r.run();
            }
            else {
                Platform.runLater(r);
            }
        }


    }



}
