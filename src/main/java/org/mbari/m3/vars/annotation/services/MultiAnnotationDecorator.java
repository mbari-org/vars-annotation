package org.mbari.m3.vars.annotation.services;

import com.google.common.collect.Lists;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;
import org.mbari.m3.vars.annotation.AppConfig;
import org.mbari.m3.vars.annotation.EventBus;
import org.mbari.m3.vars.annotation.UIToolBox;
import org.mbari.m3.vars.annotation.events.AnnotationsAddedEvent;
import org.mbari.m3.vars.annotation.messages.HideProgress;
import org.mbari.m3.vars.annotation.messages.SetProgress;
import org.mbari.m3.vars.annotation.messages.ShowNonfatalErrorAlert;
import org.mbari.m3.vars.annotation.model.*;
import org.mbari.m3.vars.annotation.util.JFXUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author Brian Schlining
 * @since 2019-07-10T16:24:00
 */
public class MultiAnnotationDecorator {

    private final Logger log = LoggerFactory.getLogger(getClass());
    private final UIToolBox toolBox;
    private final int pageSize;
    private final Duration pageTimeout;
    private final int numberSimultaneousPages;
    private final EventBus notificationEventBus;

    /**
     *
     * @param toolBox
     * @param notificationEventBus Bus to post notifications too
     */
    public MultiAnnotationDecorator(UIToolBox toolBox,
                                    EventBus notificationEventBus) {
        this.toolBox = toolBox;
        this.notificationEventBus = notificationEventBus;
        AppConfig appConfig = toolBox.getAppConfig();
        pageSize = appConfig.getAnnotationServiceV1PageSize();
        pageTimeout = appConfig.getAnnotationServiceParamsV1().getTimeout();
        numberSimultaneousPages = appConfig.getAnnotationsServiceV1PageCount();
    }

    private CompletableFuture<Optional<MultiRequestCount>> countMultiAnnotations(Media media) {
        log.debug("Counting concurrent annotations related to {}", media.getUri());

        AnnotationService annotationService = toolBox.getServices().getAnnotationService();
        MediaService mediaService = toolBox.getServices()
                .getMediaService();

        return mediaService.findByVideoSequenceName(media.getVideoSequenceName())
                .thenCompose(ms -> {
                    List<UUID> otherMedia = ms.stream()
                            .map(Media::getVideoReferenceUuid)
                            .collect(Collectors.toList());
                    MultiRequest r = new MultiRequest(otherMedia);
                    return annotationService.countByMultiRequest(r)
                            .thenApply(Optional::ofNullable);
                });

    }


    public Observable<List<Annotation>> loadMultiAnnotations(Media media) {

        PublishSubject<List<Annotation>> subject = PublishSubject.create();
        Subject<List<Annotation>> serializedSubject = subject.toSerialized();

        countMultiAnnotations(media)
                .handle((opt, ex) -> {
                    if (ex != null) {
                        // TODO show error dialog
                        serializedSubject.onError(ex);
                    }
                    else {
                        if (opt.isPresent()) {
                            log.debug("Loading concurrent annotations related to {}", media.getUri());
                            loadAnnotations(media, opt.get()).subscribe(serializedSubject::onNext,
                                    serializedSubject::onError,
                                    serializedSubject::onComplete);

                        }
                        else {
                            log.warn("Did not get a concurrent request count for {}", media.getUri());
                            serializedSubject.onComplete();
                        }
                    }
                    return null;
                });

        return serializedSubject;

    }

    private Observable<List<Annotation>> loadAnnotations(Media media, MultiRequestCount count) {
        PublishSubject<List<Annotation>> subject = PublishSubject.create();
        Subject<List<Annotation>> serializedSubject = subject.toSerialized();
        AtomicInteger loadedCount = new AtomicInteger(0);
        RequestPager.Runner<List<Annotation>> runner = buildRequestRunner(count);
        return Observable.defer(() -> {
            runner.getObservable()
                    .subscribe(annos -> doNext(annos, count.getCount(), loadedCount, subject),
                            ex -> doError(media, ex, subject),
                            () -> doComplete(media, loadedCount, subject));
            runner.run();
            return runner.getObservable();
        });
    }

    private void doNext(List<Annotation> annotations,
                        long totalCount,
                        AtomicInteger loadedCount,
                        Subject<List<Annotation>> subject) {
        subject.onNext(annotations);
        updateLoadProgress(totalCount, loadedCount.addAndGet(annotations.size()));
    }

    private void doError(Media media, Throwable e, Subject<List<Annotation>> subject) {
        log.error("An error occurred while loading concurrent media for " + media.getUri(), e);
        showFindAnnotationsError(media.getVideoReferenceUuid(), e);
        subject.onError(e);
    }

    private void doComplete(Media media, AtomicInteger loadedCount, Subject<List<Annotation>> subject) {
        subject.onComplete();
        log.info("Loaded {} concurrent annotations for {}", loadedCount.get(), media.getUri());
    }

    public RequestPager.Runner<List<Annotation>> buildRequestRunner(MultiRequestCount count) {

        AnnotationService annotationService = toolBox.getServices().getAnnotationService();

        Function<RequestPager.Page, List<Annotation>> function = (page) -> {
            try {
                return  annotationService.findByMultiRequest(count.getMultiRequest(),
                        page.getLimit(), page.getOffset())
                        .get(pageTimeout.toMillis(), TimeUnit.MILLISECONDS);
            } catch (Exception e) {
                log.info("A page request for conccurent annotations from " + page.getOffset() + " to " +
                        page.getLimit() + page.getOffset() + " failed.", e);
                throw new RuntimeException(e);
            }
        };

        RequestPager<List<Annotation>> pager = new RequestPager<>(function, 2, numberSimultaneousPages);
        return pager.build(count.getCount().intValue(), pageSize);
    }



    private void updateLoadProgress(long totalCount, int currentCount) {
        double progress = currentCount / (double) totalCount;
        notificationEventBus.send(new SetProgress(progress));
        if (progress >= 1.0) {
            notificationEventBus.send(new HideProgress());
        }
    }




    private void showFindAnnotationsError(UUID videoReferenceUuid, Throwable ex) {
        JFXUtilities.runOnFXThread(() -> {
            EventBus eventBus = toolBox.getEventBus();
            ResourceBundle i18n = toolBox.getI18nBundle();
            String content1 = i18n.getString("annotationservicedecorator.findannotations.error.content1");
            String content2 = i18n.getString("annotationservicedecorator.findannotations.error.content2");
            String header = i18n.getString("annotationservicedecorator.findannotations.error.header");
            String title = i18n.getString("annotationservicedecorator.findannotations.error.title");

            String msg = String.join(" ",
                    Lists.newArrayList(content1, videoReferenceUuid.toString(), content2));
            log.error(msg, ex);

            Exception e = ex instanceof Exception ? (Exception) ex : new RuntimeException(msg, ex);
            eventBus.send(new ShowNonfatalErrorAlert(title, header, msg, e));
        });

    }
}
