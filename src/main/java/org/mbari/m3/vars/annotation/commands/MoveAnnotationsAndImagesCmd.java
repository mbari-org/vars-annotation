package org.mbari.m3.vars.annotation.commands;

import org.mbari.m3.vars.annotation.UIToolBox;
import org.mbari.m3.vars.annotation.model.Annotation;
import org.mbari.m3.vars.annotation.model.Image;
import org.mbari.m3.vars.annotation.model.ImagedMoment;
import org.mbari.m3.vars.annotation.model.Media;
import org.mbari.m3.vars.annotation.services.AnnotationService;
import org.mbari.m3.vars.annotation.ui.AnnotationServiceDecorator;
import org.mbari.vcr4j.VideoIndex;
import org.mbari.vcr4j.util.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

/**
 * @author Brian Schlining
 * @since 2019-04-16T16:26:00
 */
public class MoveAnnotationsAndImagesCmd implements Command {

    private final Logger log = LoggerFactory.getLogger(getClass());
    private final List<Annotation> originalAnnotations;
    private final List<Image> originalImages;
    private final List<Annotation> changedAnnotations;
    private final List<Image> changedImages;
    private final Media media;

    public MoveAnnotationsAndImagesCmd(List<Annotation> annotations, Media media) {
        Preconditions.checkArgument(annotations != null, "Annotations List can not be null");

        this.media = media;

        // -- Aggregate source data
        originalAnnotations = annotations.stream()
                .filter(i -> !i.getVideoReferenceUuid().equals(media.getVideoReferenceUuid()))
                .collect(Collectors.toList());
        originalImages = originalAnnotations.stream()
                .flatMap(a -> a.getImages()
                        .stream()
                        .map(i -> new Image(a, i)))
                .collect(Collectors.toList());

        // -- Aggregate modified data
        Instant now = Instant.now();
        changedAnnotations = originalAnnotations.stream()
                .map(Annotation::new)
                .map(a -> update(a, media))
                .peek(a -> a.setObservationTimestamp(now))
                .collect(Collectors.toList());
        changedImages = originalImages.stream()
                .map(Image::new)
                .map(i -> update(i, media))
                .collect(Collectors.toList());

    }

    @Override
    public void apply(UIToolBox toolBox) {
        doUpdate(toolBox, changedAnnotations, changedImages);
    }

    @Override
    public void unapply(UIToolBox toolBox) {
        doUpdate(toolBox, originalAnnotations, originalImages);
    }

    @Override
    public String getDescription() {
        return "Move " + originalAnnotations.size() + " annotations and " +
                originalImages.size() + " images to " + media.getUri();
    }

    private void doUpdate(UIToolBox toolBox,
                          List<Annotation> annotations,
                          List<Image> images) {
        try {
            updateAnnotations(toolBox, annotations);
            updateImages(toolBox, images);
        } catch (Exception e) {
            log.error("Failed to execute update", e);
        }

        refreshView(toolBox, annotations);
    }

    private void refreshView(UIToolBox toolBox, List<Annotation> annotations) {
//        Set<VideoIndex> indices = annotations.stream()
//                .map(ImagedMoment::toVideoIndex)
//                .collect(Collectors.toSet());
        AnnotationServiceDecorator asd = new AnnotationServiceDecorator(toolBox);
//        asd.refreshAnnotationsViewByIndices(indices);

        Set<UUID> observationUuids = annotations.stream()
                .map(Annotation::getObservationUuid)
                .collect(Collectors.toSet());
        asd.refreshAnnotationsView(observationUuids);
    }

    private void updateAnnotations(UIToolBox toolBox, List<Annotation> annotations)
            throws InterruptedException, ExecutionException, TimeoutException {
        Duration timeout = getTimeout(toolBox).multipliedBy(annotations.size());
        // We can update all annotations in one PUT request
        toolBox.getServices()
                .getAnnotationService()
                .updateAnnotations(annotations)
                .get(timeout.toMillis(), TimeUnit.MILLISECONDS);
    }

    private void updateImages(UIToolBox toolBox, List<Image> images) {

        Duration timeout = getTimeout(toolBox);

        AnnotationService annotationService = toolBox.getServices()
                .getAnnotationService();

        // Unfortunatly, only 1 image can be updated per put request.
        for (Image i : images) {
            try {
                annotationService.updateImage(i)
                        .get(timeout.toMillis(), TimeUnit.MILLISECONDS);
            }
            catch (Exception e) {
                log.error("Failed to update " + i.getUrl() + " in remote datastore", e);
            }
        }
    }

    private Duration getTimeout(UIToolBox toolBox) {
        Duration timeout = Duration.ofSeconds(5);
        try {
            timeout = toolBox.getConfig()
                    .getDuration("annotation.service.timeout");
        }
        catch (Exception e) {
            log.warn("'annotation.service.timeout' is not defined in configuration.");
        }
        return timeout;
    }



    private static <T extends ImagedMoment> T update(T a, Media media) {
        a.setVideoReferenceUuid(media.getVideoReferenceUuid());
        // Adjust recordedTimestamp if elapsedTime and media.startTimestamp are present
        if (a.getElapsedTime() != null) {
            if (media.getStartTimestamp() != null) {
                Instant recordedTimestamp = media.getStartTimestamp().plus(a.getElapsedTime());
                a.setRecordedTimestamp(recordedTimestamp);
            }
            else {
                a.setRecordedTimestamp(null);
            }
        }
        return a;
    }
}
