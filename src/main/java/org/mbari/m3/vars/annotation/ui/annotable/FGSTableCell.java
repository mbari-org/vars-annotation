package org.mbari.m3.vars.annotation.ui.annotable;

import de.jensd.fx.glyphs.GlyphsFactory;
import de.jensd.fx.glyphs.materialicons.MaterialIcon;
import de.jensd.fx.glyphs.materialicons.utils.MaterialIconFactory;
import javafx.application.Platform;
import javafx.scene.control.TableCell;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import org.mbari.m3.vars.annotation.Initializer;
import org.mbari.m3.vars.annotation.model.Annotation;
import org.mbari.m3.vars.annotation.model.Media;
import org.mbari.m3.vars.annotation.util.JFXUtilities;

import java.util.List;

/**
 * @author Brian Schlining
 * @since 2017-06-02T10:29:00
 */
public class FGSTableCell extends TableCell<Annotation, FGSValue> {

    private GlyphsFactory glyphsFactory = MaterialIconFactory.get();
    private Text imageIcon = glyphsFactory.createIcon(MaterialIcon.IMAGE, "20px");
    private Text sampleIcon = glyphsFactory.createIcon(MaterialIcon.NATURE_PEOPLE, "20px");
    private Text concurrentIcon = glyphsFactory.createIcon(MaterialIcon.TIMELINE, "20px");
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