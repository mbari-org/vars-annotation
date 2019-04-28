package org.mbari.m3.vars.annotation.commands;

import io.reactivex.Observable;
import org.mbari.m3.vars.annotation.EventBus;
import org.mbari.m3.vars.annotation.UIToolBox;
import org.mbari.m3.vars.annotation.events.AnnotationsAddedEvent;
import org.mbari.m3.vars.annotation.events.AnnotationsRemovedEvent;
import org.mbari.m3.vars.annotation.events.AnnotationsSelectedEvent;
import org.mbari.m3.vars.annotation.messages.ShowAlert;
import org.mbari.m3.vars.annotation.messages.ShowExceptionAlert;
import org.mbari.m3.vars.annotation.messages.ShowFatalErrorAlert;
import org.mbari.m3.vars.annotation.messages.ShowWarningAlert;
import org.mbari.m3.vars.annotation.model.Annotation;
import org.mbari.m3.vars.annotation.model.Concept;
import org.mbari.m3.vars.annotation.model.ConceptDetails;
import org.mbari.m3.vars.annotation.util.AsyncUtils;
import org.mbari.util.Tuple2;
import org.mbari.vcr4j.VideoIndex;

import javax.swing.text.html.Option;
import java.util.Objects;
import java.util.Optional;
import java.util.ResourceBundle;
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
