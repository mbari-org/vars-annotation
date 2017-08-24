package org.mbari.m3.vars.annotation.commands;

import org.mbari.m3.vars.annotation.Data;
import org.mbari.m3.vars.annotation.EventBus;
import org.mbari.m3.vars.annotation.UIToolBox;
import org.mbari.m3.vars.annotation.events.AnnotationsAddedEvent;
import org.mbari.m3.vars.annotation.events.AnnotationsRemovedEvent;
import org.mbari.m3.vars.annotation.events.AnnotationsSelectedEvent;
import org.mbari.m3.vars.annotation.model.Annotation;
import org.mbari.m3.vars.annotation.model.Media;
import org.mbari.vcr4j.VideoIndex;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;


/**
 * @author Brian Schlining
 * @since 2017-07-26T11:21:00
 */
public class CreateAnnotationFromConceptCmd implements Command {

    private final String concept;
    private volatile Annotation annotation;

    public CreateAnnotationFromConceptCmd(String concept) {
        this.concept = concept;
    }

    @Override
    public void apply(UIToolBox toolBox) {
        toolBox.getMediaPlayer()
                .requestVideoIndex()
                .thenAccept(videoIndex -> {
                    Data data = toolBox.getData();
                    Media media = data.getMedia();
                    UUID videoReferenceUuid = media.getVideoReferenceUuid();
                    String observer = data.getUser().getUsername();
                    String group = data.getGroup();
                    String activity = data.getActivity();
                    Annotation a0 = new Annotation(concept,
                            observer,
                            videoIndex,
                            videoReferenceUuid);
                    a0.setGroup(group);
                    a0.setActivity(activity);
                    if (media.getStartTimestamp() != null ) {
                        // Calculate timestamp from media start time and annotation elapsed time
                        videoIndex.getElapsedTime()
                                .ifPresent(elapsedTime -> {
                                    Instant recordedDate = media.getStartTimestamp().plus(elapsedTime);
                                    a0.setRecordedTimestamp(recordedDate);
                                });
                    }
                    toolBox.getServices()
                            .getAnnotationService()
                            .createAnnotation(a0)
                            .thenAccept(a1 -> {
                                annotation = a1;
                                EventBus eventBus = toolBox.getEventBus();
                                eventBus.send(new AnnotationsAddedEvent(a1));
                                eventBus.send(new AnnotationsSelectedEvent(a1));
                            });
                });
    }

    @Override
    public void unapply(UIToolBox toolBox) {
        toolBox.getServices()
                .getAnnotationService()
                .deleteAnnotation(annotation.getObservationUuid())
                .thenAccept(a -> {
                    toolBox.getEventBus()
                            .send(new AnnotationsRemovedEvent(annotation));
                    annotation = null;
                });
    }

    @Override
    public String getDescription() {
        return "Create Annotation using " + concept;
    }
}
