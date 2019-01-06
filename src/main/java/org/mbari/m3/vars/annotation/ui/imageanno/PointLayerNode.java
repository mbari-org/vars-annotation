package org.mbari.m3.vars.annotation.ui.imageanno;

import javafx.beans.value.ChangeListener;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Circle;
import org.mbari.m3.vars.annotation.model.Association;
import org.mbari.m3.vars.annotation.model.Image;
import org.mbari.m3.vars.annotation.ui.shared.ImageViewExt;

import javax.annotation.Nonnull;
import java.util.Optional;
import java.util.UUID;

public class PointLayerNode extends LayerNode<PointLayerNode.Datum, Circle> {

    public static class Datum {
        int x;
        int y;
        UUID imageReferenceUuid;
        int imageWidth;
        int imageHeight;
        String comment;

        public Datum() { }

        public Datum(Image image, Point2D point) {
            imageReferenceUuid = image.getImageReferenceUuid();
            if (image.getWidth() != null) {
                imageWidth = image.getWidth();
            }
            if (image.getHeight() != null) {
                imageHeight = image.getHeight();
            }
            x = (int) Math.round(point.getX());
            y = (int) Math.round(point.getY());
        }
    }


    protected PointLayerNode(Datum datum,
                          Circle shape,
                          ChangeListener<? super Number> resizeChangeListener,
                          Association association) {
        super(datum, shape, resizeChangeListener, association);
    }

    public static void resize(ImageViewExt imageViewExt,
                                  Datum datum,
                                  Circle shape) {
        ImageView imageView = imageViewExt.getImageView();
        Bounds boundsInParent = imageView.getBoundsInParent();
        double scale = imageViewExt.computeActualScale();
        double x = datum.x * scale + boundsInParent.getMinX();
        double y = datum.y * scale + boundsInParent.getMinY();
        shape.setCenterX(x);
        shape.setCenterX(y);
    }

    /**
     * Converts a point association into a drawable stuff. Expects the mime-type to
     * be "application/json" with a JSON linkValue of the format:
     * "{x:int, y:int, image_reference_uuid: UUID, image_with: int,
     *   image_height: int, comment: String}"
     * @param imageViewExt The ImageViewExt we will be drawining into
     * @param association The association we want to parse to a point annotation
     * @param linkName This is used to filter for point associations. Non point
     *                 associations won't be parsed (Optional.empty)
     * @return empty if a non-parseable association.
     */
    public static Optional<PointLayerNode> fromAssociation(@Nonnull ImageViewExt imageViewExt,
                                                           @Nonnull Association association,
                                                           @Nonnull String linkName) {
        Optional<PointLayerNode> opt = Optional.empty();
        if (association.getLinkName().equals(linkName) &&
                association.getMimeType().equalsIgnoreCase("application/json")) {

            final Datum datum = GSON.fromJson(association.getLinkValue(),
                    Datum.class);
            Circle shape = new Circle();
            resize(imageViewExt, datum, shape);
            ChangeListener<? super Number> resizeChangeListener = (obs, oldv, newv) ->
                resize(imageViewExt, datum, shape);
            PointLayerNode layerNode = new PointLayerNode(datum,
                    shape,
                    resizeChangeListener,
                    association);
            opt = Optional.of(layerNode);
        }
        return opt;
    }

}
