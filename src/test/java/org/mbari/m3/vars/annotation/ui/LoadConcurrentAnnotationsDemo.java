package org.mbari.m3.vars.annotation.ui;

import org.mbari.m3.vars.annotation.App;
import org.mbari.m3.vars.annotation.AppDemo;
import org.mbari.m3.vars.annotation.Initializer;
import org.mbari.m3.vars.annotation.UIToolBox;
import org.mbari.m3.vars.annotation.model.Media;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;
import java.util.stream.Collectors;

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
        AnnotationServiceDecorator decorator = new AnnotationServiceDecorator(toolBox);
        UUID uuid = UUID.fromString("1e0aaaae-f9d7-4b3c-a043-bb45a2fed5bf");
        toolBox.getServices()
                .getMediaService()
                .findConcurrentByVideoReferenceUuid(uuid)
                .thenApply(ms -> ms.stream()
                        .filter(m -> !m.getVideoReferenceUuid().equals(uuid))
                        .map(Media::getVideoReferenceUuid)
                        .collect(Collectors.toList()))
                .thenAccept(decorator::findConcurrentAnnotations);


    }
}
