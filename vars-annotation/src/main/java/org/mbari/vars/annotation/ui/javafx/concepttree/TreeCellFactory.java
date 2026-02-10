package org.mbari.vars.annotation.ui.javafx.concepttree;

import javafx.application.Platform;
import javafx.scene.control.TreeCell;
import javafx.scene.control.cell.TextFieldTreeCell;
import javafx.scene.input.*;
import org.mbari.vars.annotation.ui.UIToolBox;
import org.mbari.vars.oni.sdk.r1.models.Concept;
import org.mbari.vars.oni.sdk.r1.models.ConceptDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Brian Schlining
 * @since 2017-05-15T13:58:00
 */
class TreeCellFactory {


    private static final Logger log = LoggerFactory.getLogger(TreeCellFactory.class);
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
            itemProperty().addListener((obs, oldItem, newItem) -> {
                if (newItem != null && newItem.getConceptDetails() == null) {
                    toolBox.getServices()
                            .conceptService()
                            .findDetails(newItem.getName())
                            .thenAccept(opt -> {
                                opt.ifPresent(cd -> {
//                                    item.setConceptDetails(cd);
                                    // as we are async, the cell may or may not contain the item
                                    // We'll update it just in case it's the same item.
                                    // The CachedConceptService adds the details to the concept.
                                    // the 'newItem' is just a reference to the concept in the cache
                                    // so we shouldn't have to do anything other than redraw the cell
                                    Platform.runLater(this::updateCell);
                                });
                            });
                }
            });

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
            log.debug("Updating cell for concept: " + item);
            if (item != null) {
                String text = asString(item);
                setText(text);
                var conceptService = toolBox.getServices().conceptService();
                conceptService.findDetails(item.getName())
                        .thenAccept(opt ->
                                Platform.runLater(() -> {
                                    opt.ifPresent(cd -> {
                                        if (!cd.getMedia().isEmpty()) {
                                            getStyleClass().removeAll(styleClassPlain);
                                            getStyleClass().addAll(styleClassWithMedia);
                                        } else {
                                            getStyleClass().removeAll(styleClassWithMedia);
                                            getStyleClass().addAll(styleClassPlain);
                                        }
                                    });
                                }));

            }
        }



    }



}
