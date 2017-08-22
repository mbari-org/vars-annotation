package org.mbari.m3.vars.annotation.ui;

import org.mbari.m3.vars.annotation.EventBus;
import org.mbari.m3.vars.annotation.UIToolBox;
import org.mbari.m3.vars.annotation.events.AnnotationsAddedEvent;
import org.mbari.m3.vars.annotation.events.AnnotationsRemovedEvent;
import org.mbari.m3.vars.annotation.events.AnnotationsSelectedEvent;
import org.mbari.m3.vars.annotation.messages.HideProgress;
import org.mbari.m3.vars.annotation.messages.SetProgress;
import org.mbari.m3.vars.annotation.messages.ShowProgress;
import org.mbari.m3.vars.annotation.model.Annotation;
import org.mbari.m3.vars.annotation.services.AnnotationService;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * This decorator paginates the loading of annotations and sends the
 * appropriate notifications to the UI via the EventBus
 * @author Brian Schlining
 * @since 2017-07-28T13:17:00
 */
public class AnnotationServiceDecorator {

    private final UIToolBox toolBox;
    private final int chunkSize;

    public AnnotationServiceDecorator(UIToolBox toolBox) {
        this.toolBox = toolBox;
        this.chunkSize = toolBox.getConfig().getInt("annotation.service.chunk.size");
    }

    public void findAnnotations(UUID videoReferenceUuid) {
        AnnotationService service = toolBox.getServices().getAnnotationService();
        EventBus eventBus = toolBox.getEventBus();

        AtomicInteger loadedAnnotationCount = new AtomicInteger(0);
        service.countAnnotations(videoReferenceUuid)
                .thenAccept(ac -> {
                    eventBus.send(new ShowProgress());
                    int n = (int) Math.ceil(ac.getCount() / (double) chunkSize);
                    for (int i = 0; i < n; i++) {
                        long offset = i * chunkSize;
                        long limit = chunkSize;
                        service.findAnnotations(videoReferenceUuid, limit, offset)
                                .thenAccept(annotations -> {
                                    if (annotations.size() == 0) {
                                        eventBus.send(new HideProgress());
                                    }
                                    else {
                                        eventBus.send(new AnnotationsAddedEvent(annotations));
                                        int c = loadedAnnotationCount.addAndGet(annotations.size());
                                        double progress = c / ac.getCount().doubleValue();
                                        if (progress >= 1.0) {
                                            eventBus.send(new HideProgress());
                                        }
                                        else {
                                            eventBus.send(new SetProgress(progress));
                                        }
                                    }
                                });
                    }
                });
    }

    public void findConcurrentAnnotations(Collection<UUID> videoReferenceUuids) {
        videoReferenceUuids.forEach(this::findAnnotations);
    }

    /**
     * Removes all annotations form the UI except for ones associated with the given
     * videoReferenceUuid.
     *
     * @param videoReferenceUuid UUID for a videoreference whose annotations to keep in the view
     */
    public void removeAnnotationsExceptFor(UUID videoReferenceUuid) {
        List<Annotation> removeMe = toolBox.getData()
                .getAnnotations()
                .stream()
                .filter(a -> !a.getVideoReferenceUuid().equals(videoReferenceUuid))
                .collect(Collectors.toList());
        EventBus eventBus = toolBox.getEventBus();
        eventBus.send(new AnnotationsSelectedEvent(new ArrayList<>()));
        eventBus.send(new AnnotationsRemovedEvent(removeMe));
    }

}
