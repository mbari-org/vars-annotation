package org.mbari.vars.ui.javafx.concepttree;

import javafx.application.Platform;
import javafx.scene.control.TreeCell;
import javafx.scene.control.cell.TextFieldTreeCell;
import javafx.scene.input.*;
import org.mbari.vars.services.model.Concept;
import org.mbari.vars.services.model.ConceptDetails;
import org.mbari.vars.services.ConceptService;
import org.mbari.vars.ui.UIToolBox;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author Brian Schlining
 * @since 2017-05-15T13:58:00
 */
class TreeCellFactory {


    private String styleClassPlain = "concepttree-treecell";
    private String styleClassWithMedia = "concepttree-treecell-media";
    private final UIToolBox toolBox;

    TreeCellFactory(UIToolBox toolBox) {
        this.toolBox = toolBox;
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
                updateCell();
                if (item.getConceptDetails() == null) {
                    toolBox.getServices()
                            .getConceptService()
                            .findDetails(item.getName())
                            .thenAccept(opt -> {
                                opt.ifPresent(cd -> {
//                                    System.out.println("Loaded details for " + item.getName());
                                    item.setConceptDetails(cd);
                                    // as we are async, the cell may or may not contain the item
                                    // We'll update it just in case it's the same item.
                                    Platform.runLater(this::updateCell);
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

        private void updateCell() {
            Concept item = getItem();
            if (item != null) {
                String text = asString(item);
                setText(text);
                ConceptDetails cd = item.getConceptDetails();
                if (cd != null) {
                    if (!cd.getMedia().isEmpty()) {
                        getStyleClass().removeAll(styleClassPlain);
                        getStyleClass().addAll(styleClassWithMedia);
                    } else {
                        getStyleClass().removeAll(styleClassWithMedia);
                        getStyleClass().addAll(styleClassPlain);
                    }
                }
            }
        }



    }



}
