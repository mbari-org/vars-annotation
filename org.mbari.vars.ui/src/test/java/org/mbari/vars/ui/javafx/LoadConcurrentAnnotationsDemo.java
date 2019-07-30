package org.mbari.vars.ui.javafx;

import org.mbari.m3.vars.annotation.AppDemo;
import org.mbari.vars.ui.Initializer;
import org.mbari.vars.ui.UIToolBox;
import org.mbari.vars.ui.services.ConcurrentAnnotationDecorator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

/**
 * @author Brian Schlining
 * @since 2019-05-14T14:37:00
 */
public class LoadConcurrentAnnotationsDemo {

    public static void main(String[] args) {
        System.getProperties().setProperty("user.timezone", "UTC");

        Logger log = LoggerFactory.getLogger(AppDemo.class);
        Initializer.getToolBox()
                .getEventBus()
                .toObserverable()
                .subscribe(e -> log.debug(e.toString()));

        UIToolBox toolBox = Initializer.getToolBox();
//        AnnotationServiceDecorator decorator = new AnnotationServiceDecorator(toolBox);
        UUID uuid = UUID.fromString("1e0aaaae-f9d7-4b3c-a043-bb45a2fed5bf");
//        toolBox.getServices()
//                .getMediaService()
//                .findConcurrentByVideoReferenceUuid(uuid)
//                .thenApply(ms -> ms.stream()
//                        .filter(m -> !m.getVideoReferenceUuid().equals(uuid))
//                        .map(Media::getVideoReferenceUuid)
//                        .collect(Collectors.toList()))
//                .thenAccept(decorator::findConcurrentAnnotations);

        ConcurrentAnnotationDecorator decorator2 = new ConcurrentAnnotationDecorator(toolBox);
        toolBox.getServices()
                .getMediaService()
                .findConcurrentByVideoReferenceUuid(uuid)
                .thenAccept(ms -> ms.stream()
                        .filter(m -> m.getVideoReferenceUuid().equals(uuid))
                        .forEach(decorator2::loadConcurrentAnnotations))
                .thenAccept(v -> System.out.println("DONE"));



    }
}
