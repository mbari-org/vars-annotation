package org.mbari.m3.vars.annotation.ui;

import com.google.common.util.concurrent.AtomicDouble;
import org.mbari.m3.vars.annotation.EventBus;
import org.mbari.m3.vars.annotation.UIToolBox;
import org.mbari.m3.vars.annotation.events.AnnotationsAddedEvent;
import org.mbari.m3.vars.annotation.messages.HideProgress;
import org.mbari.m3.vars.annotation.messages.SetProgress;
import org.mbari.m3.vars.annotation.messages.ShowProgress;
import org.mbari.m3.vars.annotation.services.AnnotationService;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

/**
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
                        int j = i;
                        service.findAnnotations(videoReferenceUuid, limit, offset)
                                .thenAccept(annotations -> {
                                    eventBus.send(new AnnotationsAddedEvent(annotations));
                                    int c = loadedAnnotationCount.addAndGet(annotations.size());
                                    double progress = c / ac.getCount().doubleValue();
                                    if (progress >= 1.0) {
                                        eventBus.send(new HideProgress());
                                    }
                                    else {
                                        eventBus.send(new SetProgress(progress));
                                    }
                                });
                    }
                });
    }

}
