package org.mbari.m3.vars.annotation.ui.shared;


import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import javax.annotation.Nonnull;

/**
 * Wrapper around ImageView that adds Utility methods
 * @author Brian Schlining
 * @since 2014-12-02T12:06:00
 */
public class ImageViewExt {
    private final ImageView imageView;
    private double actualScale = Double.NaN;
    private boolean doScaleRecompute = true;

    public ImageViewExt(@Nonnull ImageView imageView) {
        this.imageView = imageView;
        imageView.imageProperty().addListener(i -> doScaleRecompute = true);
        imageView.fitHeightProperty().addListener(i -> doScaleRecompute = true);
        imageView.fitWidthProperty().addListener(i -> doScaleRecompute = true);
    }

    public ImageView getImageView() {
        return imageView;
    }

    /**
     * Computes the actual scaling of an Image in an ImageView. If the preserveRatio
     * property on the ImageView is false the scaling has no meaning so NaN is
     * returned.
     *
     * @return The scale factor of the image in relation to display coordinates
     */
    public double computeActualScale() {

        if (!imageView.isPreserveRatio()) {
            actualScale = Double.NaN;
        }
        else if (doScaleRecompute || Double.isNaN(actualScale))  {
            Image localImage = imageView.getImage();
            Rectangle2D localViewport = imageView.getViewport();

            double w = 0;
            double h = 0;
            if (localViewport != null && localViewport.getWidth() > 0 && localViewport.getHeight() > 0) {
                w = localViewport.getWidth();
                h = localViewport.getHeight();
            } else if (localImage != null) {
                w = localImage.getWidth();
                h = localImage.getHeight();
            }

            double localFitWidth = imageView.getFitWidth();
            double localFitHeight = imageView.getFitHeight();

            if (w > 0 && h > 0 && (localFitWidth > 0 || localFitHeight > 0)) {
                if (localFitWidth <= 0 || (localFitHeight > 0 && localFitWidth * h > localFitHeight * w)) {
                    w = w * localFitHeight / h;
                } else {
                    w = localFitWidth;
                }

                actualScale = w / localImage.getWidth();
            }

            doScaleRecompute = false;

        }

        return actualScale;
    }
}
