package org.mbari.m3.vars.annotation;

import io.reactivex.Observable;
import javafx.scene.Scene;
import org.mbari.m3.vars.annotation.commands.ClearCommandManagerMsg;
import org.mbari.m3.vars.annotation.events.*;
import org.mbari.m3.vars.annotation.model.Media;

import java.util.ArrayList;

/**
 * @author Brian Schlining
 * @since 2017-05-10T09:55:00
 */
public class AppController {
    private Scene scene;
    private final UIToolBox toolBox;

    public AppController(UIToolBox toolBox) {
        this.toolBox = toolBox;
        initialize();
    }

    public Scene getScene() {
        if (scene == null) {
            //scene = new Scene();

        }
        return scene;

    }

    private void initialize() {
        // wire up data to listen to events
        EventBus eventBus = toolBox.getEventBus();
        Data data = toolBox.getData();
        Observable<Object> eventObservable = eventBus.toObserverable();
        eventObservable.ofType(AnnotationsAddedEvent.class)
                .subscribe(e -> data.getAnnotations().addAll(e.get()));

        eventObservable.ofType(AnnotationsRemovedEvent.class)
                .subscribe(e -> data.getAnnotations().removeAll(e.get()));

        eventObservable.ofType(AnnotationsChangedEvent.class)
                .subscribe(e -> {
                    // They use observation UUID as hash key. Remove the existing ones and add the
                    // modified annotations.
                    data.getAnnotations().removeAll(e.get());
                    data.getAnnotations().addAll(e.get());
                });

        eventObservable.ofType(AnnotationsSelectedEvent.class)
                .subscribe(e -> data.setSelectedAnnotations(e.get()));

        eventObservable.ofType(MediaChangedEvent.class)
                .subscribe(e -> changeMedia(e.get()));

        eventObservable.ofType(UserChangedEvent.class)
                .subscribe(e -> data.setUser(e.get()));
    }

    private void changeMedia(Media newMedia) {
        EventBus eventBus = toolBox.getEventBus();
        Data data = toolBox.getData();

        // Clear out old data
        eventBus.send(new AnnotationsSelectedEvent(new ArrayList<>()));
        eventBus.send(new AnnotationsRemovedEvent(data.getAnnotations()));
        eventBus.send(new ClearCommandManagerMsg());

        // Load new data
        data.setMedia(newMedia);
        toolBox.getServices()
                .getAnnotationService()
                .findAnnotations(newMedia.getVideoReferenceUuid())
                .thenAccept(annotations -> eventBus.send(new AnnotationsAddedEvent(annotations)));

    }
}
