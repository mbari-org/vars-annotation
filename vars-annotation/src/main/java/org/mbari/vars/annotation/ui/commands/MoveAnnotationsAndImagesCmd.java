package org.mbari.vars.annotation.ui.commands;


import org.mbari.vars.annotation.ui.UIToolBox;
import org.mbari.vars.annosaurus.sdk.r1.models.Annotation;
import org.mbari.vars.annotation.ui.events.AnnotationsRemovedEvent;
import org.mbari.vars.annotation.ui.events.MediaChangedEvent;
import org.mbari.vars.vampiresquid.sdk.r1.models.Media;
import org.mbari.vars.annosaurus.sdk.r1.AnnotationService;
import org.mbari.vars.annotation.ui.javafx.AnnotationServiceDecorator;
import org.mbari.vcr4j.util.Preconditions;
import org.mbari.vars.annotation.etc.jdk.Loggers;

import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author Brian Schlining
 * @since 2019-04-16T16:26:00
 */
public class MoveAnnotationsAndImagesCmd implements Command {

    private final Loggers log = new Loggers(getClass());
    private final List<Annotation> originalAnnotations;
    private List<Annotation> changedAnnotations;
    private final Media media;

    public MoveAnnotationsAndImagesCmd(List<Annotation> annotations, Media media) {
        Preconditions.checkArgument(annotations != null, "Annotations List can not be null");

        this.media = media;

        // -- Aggregate source data
        originalAnnotations = annotations.stream()
                .filter(i -> !i.getVideoReferenceUuid().equals(media.getVideoReferenceUuid()))
                .collect(Collectors.toList());

    }

    @Override
    public void apply(UIToolBox toolBox) {
        var imagedMomentUuids = originalAnnotations.stream()
                .map(Annotation::getImagedMomentUuid)
                .toList();

        var annotationService = toolBox.getServices().annotationService();
        annotationService.bulkMove(media.getVideoReferenceUuid(), imagedMomentUuids, media.getStartTimestamp())
                        .thenAccept(count -> {
                           log.atInfo().log("Moved " + count + " annotations");
                           if (count.count() != imagedMomentUuids.size()) {
                               log.atWarn().log(String.format("Failed to move all annotations. Expected %d but was %d", imagedMomentUuids.size(), count.count()));
                           }
                            var event = new AnnotationsRemovedEvent(null, originalAnnotations);
                            toolBox.getEventBus().send(event);
                        });

    }

    @Override
    public void unapply(UIToolBox toolBox) {
        final AnnotationService annotationService = toolBox.getServices().annotationService();
        final Duration timeout = getTimeout(toolBox);
        originalAnnotations.forEach(a -> {
            try {
                annotationService.updateAnnotation(a).get(timeout.toMillis(), TimeUnit.MILLISECONDS);
            } catch (Exception e) {
                log.atError().withCause(e).log("Failed to unapply move and update annotation " + a.getObservationUuid());
            }
        });
        var currentMedia = toolBox.getData().getMedia();
        var event = new MediaChangedEvent(this.getClass(), currentMedia);
        toolBox.getEventBus().send(event);

    }

    @Override
    public String getDescription() {
        return "Move " + originalAnnotations.size() + " annotations and " +
                originalAnnotations.size() + " images to " + media.getUri();
    }


    private Duration getTimeout(UIToolBox toolBox) {
        Duration timeout = Duration.ofSeconds(5);
        try {
            timeout = toolBox.getConfig()
                    .getDuration("annotation.service.timeout");
        }
        catch (Exception e) {
            log.atWarn().log("'annotation.service.timeout' is not defined in configuration.");
        }
        return timeout;
    }

}
