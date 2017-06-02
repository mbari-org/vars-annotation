package org.mbari.m3.vars.annotation.ui.annotable;

import de.jensd.fx.glyphs.GlyphsFactory;
import de.jensd.fx.glyphs.materialicons.MaterialIcon;
import de.jensd.fx.glyphs.materialicons.utils.MaterialIconFactory;
import javafx.scene.Node;
import javafx.scene.control.TableCell;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import org.mbari.m3.vars.annotation.Constants;
import org.mbari.m3.vars.annotation.model.Annotation;

import java.util.List;

/**
 * @author Brian Schlining
 * @since 2017-06-02T10:29:00
 */
public class FGSTableCell extends TableCell<Annotation, FGSValue> {

    private GlyphsFactory glyphsFactory = MaterialIconFactory.get();
    private Text imageIcon = glyphsFactory.createIcon(MaterialIcon.IMAGE, "20px");
    private Text sampleIcon = glyphsFactory.createIcon(MaterialIcon.NATURE_PEOPLE, "20px");
    private final Node graphic = new HBox(imageIcon, sampleIcon);
    private final String iconInactiveStyle = "icon-inactive";
    private final String iconActiveImageStyle = "annotable-image-active";
    private final String iconActiveSampleStyle = "annotable-sample-active";

    public FGSTableCell() {
        setGraphic(graphic);
        imageIcon.getStyleClass().addAll(iconInactiveStyle);
        sampleIcon.getStyleClass().addAll(iconInactiveStyle);
    }

    @Override
    protected void updateItem(FGSValue item, boolean empty) {
        super.updateItem(item, empty);

        if (item == null || empty) {
            setText(null);
            imageIcon.getStyleClass().remove(0);
            imageIcon.getStyleClass().addAll(iconInactiveStyle);
            sampleIcon.getStyleClass().remove(0);
            sampleIcon.getStyleClass().addAll(iconInactiveStyle);
        }
        else {
            String imageStyle = item.hasImage() ? iconActiveImageStyle : iconInactiveStyle;
            String sampleStyle = item.hasSample() ? iconActiveSampleStyle : iconInactiveStyle;
            imageIcon.getStyleClass().remove(0);
            imageIcon.getStyleClass().addAll(imageStyle);
            sampleIcon.getStyleClass().remove(0);
            sampleIcon.getStyleClass().addAll(sampleStyle);
        }
    }
}

class FGSValue {

    private final Annotation annotation;
    private static final List<String> sampleKeys = Constants.CONFIG
            .getStringList("app.annotation.sample.linknames");

    FGSValue(Annotation annotation) {
        this.annotation = annotation;
    }

    boolean hasImage() {
        return annotation != null &&
                annotation.getImages() != null &&
                !annotation.getImages().isEmpty();
    }

    boolean hasSample() {
        return annotation != null &&
                annotation.getAssociations() != null &&
                annotation.getAssociations()
                        .stream()
                        .anyMatch(a ->  a.getLinkValue() != null &&
                                sampleKeys.contains(a.getLinkValue())
                        );
    }
}