package org.mbari.vars.ui.commands;

import io.reactivex.Observable;
import org.mbari.vars.ui.EventBus;
import org.mbari.vars.ui.UIToolBox;
import org.mbari.vars.ui.events.AnnotationsAddedEvent;
import org.mbari.vars.ui.events.AnnotationsRemovedEvent;
import org.mbari.vars.ui.events.AnnotationsSelectedEvent;
import org.mbari.vars.ui.messages.ShowAlert;
import org.mbari.vars.ui.messages.ShowExceptionAlert;
import org.mbari.vars.services.model.Annotation;
import org.mbari.vars.services.model.ConceptDetails;
import org.mbari.vars.core.util.AsyncUtils;
import org.mbari.vcr4j.VideoIndex;

import java.util.Objects;
import java.util.Optional;
import java.util.ResourceBundle;


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

    private void createAnnotation(UIToolBox toolBox, String primaryConcept, VideoIndex videoIndex) {
        Annotation a0 = CommandUtil.buildAnnotation(toolBox.getData(), primaryConcept, videoIndex);

        Observable<Annotation> observable = AsyncUtils.observe(toolBox.getServices()
                .getAnnotationService()
                .createAnnotation(a0));

        observable.filter(Objects::nonNull)
                .subscribe(a -> {
                    this.annotation = a;
                    EventBus eventBus = toolBox.getEventBus();
                    eventBus.send(new AnnotationsAddedEvent(a));
                    eventBus.send(new AnnotationsSelectedEvent(a));
                }, t -> sendAlertMsg(toolBox, t));

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
        Observable<VideoIndex> videoIndexObservable = Observable.defer(() ->
                AsyncUtils.observe(toolBox.getMediaPlayer()
                    .requestVideoIndex()));
        Observable<Optional<ConceptDetails>> conceptObservable = AsyncUtils.observe(toolBox.getServices()
                .getConceptService()
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
                    .getAnnotationService()
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
