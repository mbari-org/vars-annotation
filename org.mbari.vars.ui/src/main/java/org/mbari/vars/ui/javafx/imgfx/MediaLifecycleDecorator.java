package org.mbari.vars.ui.javafx.imgfx;

import javafx.stage.Stage;
import org.mbari.vars.services.model.Image;
import org.mbari.vars.services.model.Media;
import org.mbari.vars.ui.util.URLUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Comparator;
import java.util.stream.Collectors;

public class MediaLifecycleDecorator {

    private final IFXToolBox toolBox;
    private final Stage stage;
    private final Comparator<Image> imageComparator =
            Comparator.comparing(a -> URLUtils.filename(a.getUrl()));
    private static final Logger log = LoggerFactory.getLogger(MediaLifecycleDecorator.class);

    public MediaLifecycleDecorator(Stage stage, IFXToolBox toolBox) {
        this.stage = stage;
        this.toolBox = toolBox;
        init();
    }

    private void init() {
        toolBox.getUIToolBox()
                .getData()
                .mediaProperty()
                .addListener((obs, oldv, newv) -> {
                    if (stage.isShowing()) {
                        // When the stage is hidden we don't want it responding to any events.
                        // By setting media to null all other data values become null, so
                        // no events/work is triggered
                        setMedia(newv);
                    }
                    else {
                        setMedia(null);
                    }
                });
    }

    public void setMedia(Media media) {
        if (media == null) {
            toolBox.getData()
                    .getImages()
                    .setAll(Collections.emptyList());
        }
        else {
            // Add all the images sorted by name
            toolBox.getUIToolBox()
                    .getServices()
                    .getAnnotationService()
                    .findImagesByVideoReferenceUuid(media.getVideoReferenceUuid())
                    .thenAccept(images -> {
                        var sortedImages = images
                                .stream()
                                .sorted(imageComparator)
                                .collect(Collectors.toList());
                        log.debug("Found {} images for media: {}", sortedImages.size(), media.getUri());
                        try {
                            toolBox.getData().getImages().setAll(sortedImages);
                        }
                        catch (Exception e) {
                            log.error("Failed to set images to IFXData object", e);
                        }
                    });


        }
    }
}
