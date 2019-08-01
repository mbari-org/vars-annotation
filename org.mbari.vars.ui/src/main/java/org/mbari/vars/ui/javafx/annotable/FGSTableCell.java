package org.mbari.vars.ui.javafx.annotable;

import javafx.scene.control.TableCell;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import org.mbari.vars.ui.javafx.Icons;
import org.mbari.vars.services.model.Annotation;
import org.mbari.vars.ui.util.JFXUtilities;


/**
 * @author Brian Schlining
 * @since 2017-06-02T10:29:00
 */
public class FGSTableCell extends TableCell<Annotation, FGSValue> {


    private Text imageIcon = Icons.IMAGE.size(20);
    private Text sampleIcon = Icons.NATURE_PEOPLE.size(20);
    private Text concurrentIcon = Icons.TIMELINE.size(20);
    private final HBox graphic = new HBox();


    public FGSTableCell() {
        setGraphic(graphic);
    }

    @Override
    protected void updateItem(FGSValue item, boolean empty) {
        JFXUtilities.runOnFXThread(() -> {
            super.updateItem(item, empty);
            setText(null);
            graphic.getChildren().remove(imageIcon);
            graphic.getChildren().remove(sampleIcon);
            graphic.getChildren().remove(concurrentIcon);
            if (empty) {
                // Do nothing
            }
            else {
                graphic.getChildren().addAll(imageIcon, sampleIcon, concurrentIcon);
                String imageStyle = (item == null || !item.hasImage()) ? "icon-inactive" : "icon-active-image";
                String sampleStyle = (item == null || !item.hasSample()) ? "icon-inactive" : "icon-active-sample";
                String concurrentStyle = (item == null || !item.isConcurrent()) ? "icon-inactive" : "icon-active-concurrent";
                sampleIcon.getStyleClass().remove(0);
                sampleIcon.getStyleClass().add(sampleStyle);
                imageIcon.getStyleClass().remove(0);
                imageIcon.getStyleClass().add(imageStyle);
                concurrentIcon.getStyleClass().remove(0);
                concurrentIcon.getStyleClass().add(concurrentStyle);
            }
        });
    }
}