package org.mbari.m3.vars.annotation.ui;

import org.mbari.m3.vars.annotation.EventBus;
import org.mbari.m3.vars.annotation.UIToolBox;
import org.mbari.m3.vars.annotation.events.AnnotationsAddedEvent;
import org.mbari.m3.vars.annotation.messages.HideProgress;
import org.mbari.m3.vars.annotation.messages.ShowProgress;
import org.mbari.m3.vars.annotation.services.AnnotationService;

import java.util.UUID;

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
                                    if (j == n - 1) {
                                        eventBus.send(new HideProgress());
                                    }
                                });
                    }
                });
    }

}
