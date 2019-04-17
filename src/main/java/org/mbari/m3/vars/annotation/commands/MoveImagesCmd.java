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
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author Brian Schlining
 * @since 2019-04-16T13:18:00
 */
public class MoveImagesCmd implements Command {

    private final Logger log = LoggerFactory.getLogger(getClass());
    private final Media media;
    private final List<Image> originalImages;
    private final List<Image> changedImages;

    public MoveImagesCmd(List<Image> originalImages, Media media) {
        Preconditions.checkArgument(originalImages != null,
                "Original images can not be null");
        this.originalImages = originalImages.stream()
                .filter(i -> !i.getVideoReferenceUuid().equals(media.getVideoReferenceUuid()))
                .collect(Collectors.toList());

        this.changedImages = this.originalImages.stream()
                .map(Image::new)
                .map(i -> update(i, media))
                .collect(Collectors.toList());
        this.media = media;
    }

    private static Image update(Image i, Media media) {
        i.setVideoReferenceUuid(media.getVideoReferenceUuid());
        // Adjust recordedTimestamp if elapsedTime and media.startTimestamp are present
        if (i.getElapsedTime() != null) {
            if (media.getStartTimestamp() != null) {
                Instant recordedTimestamp = media.getStartTimestamp().plus(i.getElapsedTime());
                i.setRecordedTimestamp(recordedTimestamp);
            }
            else {
                i.setRecordedTimestamp(null);
            }
        }
        return i;
    }

    @Override
    public void apply(UIToolBox toolBox) {
        doUpdate(toolBox, changedImages);
    }

    @Override
    public void unapply(UIToolBox toolBox) {
        doUpdate(toolBox, originalImages);
    }

    private void doUpdate(UIToolBox toolBox, List<Image> images) {
        updateImages(toolBox, images);
        refreshView(toolBox, images);
    }

    private void refreshView(UIToolBox toolBox, List<Image> images) {
        Set<VideoIndex> imageIndices = images.stream()
                .map(Image::toVideoIndex)
                .collect(Collectors.toSet());
        AnnotationServiceDecorator asd = new AnnotationServiceDecorator(toolBox);
        asd.refreshAnnotationsViewByIndices(imageIndices);
    }


    @Override
    public String getDescription() {
        return null;
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
    
    private void updateImages(UIToolBox toolBox, List<Image> images) {
        
        Duration timeout = getTimeout(toolBox);
        
        AnnotationService annotationService = toolBox.getServices()
                .getAnnotationService();

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
}
