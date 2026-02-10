package org.mbari.vars.annotation.ui.commands;

import io.reactivex.rxjava3.core.Observable;
import org.mbari.vars.annotation.etc.rxjava.EventBus;
import org.mbari.vars.annotation.ui.UIToolBox;
import org.mbari.vars.annotation.ui.events.AnnotationsAddedEvent;
import org.mbari.vars.annotation.ui.events.AnnotationsChangedEvent;
import org.mbari.vars.annotation.ui.events.AnnotationsRemovedEvent;
import org.mbari.vars.annotation.ui.events.AnnotationsSelectedEvent;
import org.mbari.vars.annotation.ui.messages.ShowAlert;
import org.mbari.vars.annotation.ui.messages.ShowExceptionAlert;
import org.mbari.vars.annosaurus.sdk.r1.models.Annotation;
import org.mbari.vars.annotation.etc.rxjava.AsyncUtils;
import org.mbari.vars.annotation.ui.messages.ShowInfoAlert;
import org.mbari.vars.oni.sdk.r1.models.ConceptDetails;
import org.mbari.vcr4j.VideoIndex;

import java.util.*;


/**
 * @author Brian Schlining
 * @since 2017-07-26T11:21:00
 */
public class CreateAnnotationFromConceptCmd implements Command {

    private final String concept;
    private volatile Annotation annotation;
    private final Object transientKey = UUID.randomUUID();

    public CreateAnnotationFromConceptCmd(String concept) {
        this.concept = concept;
    }

    private void createAnnotation(UIToolBox toolBox, String primaryConcept, VideoIndex videoIndex) {
        Annotation a0 = CommandUtil.buildAnnotation(toolBox.getData(), primaryConcept, videoIndex);
        a0.setTransientKey(transientKey);
        final EventBus eventBus = toolBox.getEventBus();
        eventBus.send(new AnnotationsAddedEvent(a0));
        eventBus.send(new AnnotationsSelectedEvent(a0));

        toolBox.getServices()
                .annotationService()
                .createAnnotation(a0)
                .handle((a, throwable) -> {
                    if (throwable != null) {
                        sendAlertMsg(toolBox, throwable);
                        eventBus.send(new AnnotationsRemovedEvent(a0));
                    }
                    else {
                        a.setTransientKey(transientKey);
                        this.annotation = a;
                        eventBus.send(new AnnotationsChangedEvent(CreateAnnotationFromConceptCmd.class, List.of(a)));
                        eventBus.send(new AnnotationsSelectedEvent(a));
                    }

                    return null;
                });


//        Observable<Annotation> observable = AsyncUtils.observe(toolBox.getServices()
//                .annotationService()
//                .createAnnotation(a0));
//
//        observable.filter(Objects::nonNull)
//                .subscribe(a -> {
//                    a.setTransientKey(transientKey);
//                    this.annotation = a;
//                    eventBus.send(new AnnotationsChangedEvent(CreateAnnotationFromConceptCmd.class, List.of(a)));
//                    eventBus.send(new AnnotationsSelectedEvent(a));
//                }, t -> sendAlertMsg(toolBox, t));

    }

    private void sendAlertMsg(UIToolBox toolBox, Throwable t) {
        ResourceBundle i18n = toolBox.getI18nBundle();
        String title = i18n.getString("commands.createannotation.title");
        String header = i18n.getString("commands.createannotation.header");
        String content = i18n.getString("commands.createannotation.content");
        Exception e;
        if (t instanceof Exception) {
            e = (Exception) t;
        }
        else {
            e = new RuntimeException(t);
        }
        ShowAlert msg = new ShowExceptionAlert(title, header, content, e);
        toolBox.getEventBus().send(msg);
    }

    @Override
    public void apply(UIToolBox toolBox) {
        var mediaPlayer = toolBox.getMediaPlayer();
        if (mediaPlayer == null) {
            var i18n = toolBox.getI18nBundle();
            var title = i18n.getString("commands.createannotation.title");
            var header = i18n.getString("commands.createannotation.header");
            var content = i18n.getString("commands.createannotation.content.nomedia");
            var msg = new ShowInfoAlert(title, header, content);
            toolBox.getEventBus().send(msg);
            return;
        }
        Observable<VideoIndex> videoIndexObservable = Observable.defer(() ->
                AsyncUtils.observe(toolBox.getMediaPlayer()
                    .requestVideoIndex()));
        Observable<Optional<ConceptDetails>> conceptObservable = AsyncUtils.observe(toolBox.getServices()
                .conceptService()
                .findDetails(concept));
        Observable<CreateDatum> lookupObservable = Observable.combineLatest(conceptObservable,
                videoIndexObservable,
                CreateDatum::new);

        lookupObservable.filter(cd -> cd.conceptDetails.isPresent())
                .subscribe(cd -> createAnnotation(toolBox,
                        cd.conceptDetails.get().getName(),
                        cd.videoIndex),
                        throwable -> sendAlertMsg(toolBox, throwable));
    }


    @Override
    public void unapply(UIToolBox toolBox) {
        if (annotation != null) {
            toolBox.getServices()
                    .annotationService()
                    .deleteAnnotation(annotation.getObservationUuid())
                    .thenAccept(a -> {
                        toolBox.getEventBus()
                                .send(new AnnotationsRemovedEvent(annotation));
                        annotation = null;
                    });
        }
    }

    @Override
    public String getDescription() {
        return "Create Annotation using " + concept;
    }
}

class CreateDatum {
    final Optional<ConceptDetails> conceptDetails;
    final VideoIndex videoIndex;

    CreateDatum(Optional<ConceptDetails> conceptDetails, VideoIndex videoIndex) {
        this.conceptDetails = conceptDetails;
        this.videoIndex = videoIndex;
    }
}
