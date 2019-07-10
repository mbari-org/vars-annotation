package org.mbari.m3.vars.annotation.services;

import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;
import org.mbari.m3.vars.annotation.AppConfig;
import org.mbari.m3.vars.annotation.EventBus;
import org.mbari.m3.vars.annotation.UIToolBox;
import org.mbari.m3.vars.annotation.model.Annotation;
import org.mbari.m3.vars.annotation.model.AnnotationCount;
import org.mbari.m3.vars.annotation.model.Media;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

/**
 * Load annotations from multiple media
 * @author Brian Schlining
 * @since 2019-07-09T15:49:00
 */
public class MultipleMediaAnnotationDecorator {

    private final UIToolBox toolBox;
    private final int pageSize;
    private final Duration pageTimeout;
    private final int numberSimultaneousPages;
    private final EventBus eventBus;

    public MultipleMediaAnnotationDecorator(UIToolBox toolBox) {
        this.toolBox = toolBox;
        eventBus = toolBox.getEventBus();
        AppConfig appConfig = toolBox.getAppConfig();
        pageSize = appConfig.getAnnotationServiceV1PageSize();
        pageTimeout = appConfig.getAnnotationServiceParamsV1().getTimeout();
        numberSimultaneousPages = appConfig.getAnnotationsServiceV1PageCount();
    }

    public Observable<List<Annotation>> loadAnnotations(List<Media> medias) {
        AnnotationService annotationService = toolBox.getServices().getAnnotationService();

        PublishSubject<List<Annotation>> subject = PublishSubject.create();

        List<AnnotationCount> annotationCounts = countAnnotations(medias);
        for (AnnotationCount ac : annotationCounts) {
            loadAnnotations(ac, subject);
        }

        return subject;
    }

    private void loadAnnotations(AnnotationCount count, Subject<List<Annotation>> subject) {

    }

    private List<AnnotationCount> countAnnotations(List<Media> medias) {
        AnnotationService annotationService = toolBox.getServices().getAnnotationService();

        List<AnnotationCount> list = new ArrayList<>();
        for (Media media : medias) {
            try {
                AnnotationCount annotationCount = annotationService.countAnnotations(media.getVideoReferenceUuid())
                        .get(pageTimeout.toMillis(), TimeUnit.MILLISECONDS);
                list.add(annotationCount);
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                // TODO notify user of failure
            }
        }
        return list;
    }
}
