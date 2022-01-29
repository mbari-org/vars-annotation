package org.mbari.vars.ui.javafx.imgfx;

import javafx.geometry.Rectangle2D;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;


public class Magnifier {

    private final ImageView magnifiedView = new ImageView();
    private final ImageView imageView;

    public Magnifier(ImageView imageView) {
        this.imageView = imageView;
        init();
    }

    private void init() {
        magnifiedView.setPreserveRatio(true);
        magnifiedView.imageProperty().bind(imageView.imageProperty());
        magnifiedView.setScaleX(3);
        magnifiedView.setScaleY(3);
        imageView.addEventHandler(MouseEvent.MOUSE_MOVED, event -> {
            var x = event.getX() - magnifiedView.getFitWidth();
            var y = event.getY() - magnifiedView.getFitHeight();
            var viewPort = new Rectangle2D(x, y, magnifiedView.getFitWidth(), magnifiedView.getFitHeight());
            magnifiedView.setViewport(viewPort);
        });
    }

    public ImageView getImageView() {
        return magnifiedView;
    }
}
