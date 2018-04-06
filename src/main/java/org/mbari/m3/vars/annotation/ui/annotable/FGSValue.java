package org.mbari.m3.vars.annotation.ui.annotable;

import org.mbari.m3.vars.annotation.Initializer;
import org.mbari.m3.vars.annotation.model.Annotation;
import org.mbari.m3.vars.annotation.model.Media;

import java.util.List;

/**
 * @author Brian Schlining
 * @since 2018-04-05T15:07:00
 */
public class FGSValue {

    private final Annotation annotation;
    private static final List<String> sampleKeys = Initializer.getToolBox()
            .getConfig()
            .getStringList("app.annotation.sample.linknames");

    public FGSValue(Annotation annotation) {
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
                        .anyMatch(a ->  a.getLinkName() != null &&
                                sampleKeys.contains(a.getLinkName())
                        );
    }

    boolean isConcurrent() {
        Media media = Initializer.getToolBox().getData().getMedia();
        return media != null &&
                annotation != null &&
                !annotation.getVideoReferenceUuid().equals(media.getVideoReferenceUuid());
    }
}