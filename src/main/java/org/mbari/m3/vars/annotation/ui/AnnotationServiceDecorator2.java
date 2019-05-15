package org.mbari.m3.vars.annotation.ui;

import com.google.common.collect.Lists;
import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import org.mbari.m3.vars.annotation.AppConfig;
import org.mbari.m3.vars.annotation.EventBus;
import org.mbari.m3.vars.annotation.UIToolBox;
import org.mbari.m3.vars.annotation.events.AnnotationsAddedEvent;
import org.mbari.m3.vars.annotation.messages.HideProgress;
import org.mbari.m3.vars.annotation.messages.SetProgress;
import org.mbari.m3.vars.annotation.messages.ShowNonfatalErrorAlert;
import org.mbari.m3.vars.annotation.messages.ShowProgress;
import org.mbari.m3.vars.annotation.model.Annotation;
import org.mbari.m3.vars.annotation.model.AnnotationCount;
import org.mbari.m3.vars.annotation.model.Media;
import org.mbari.m3.vars.annotation.services.AnnotationService;
import org.mbari.m3.vars.annotation.services.RequestPager;
import org.mbari.m3.vars.annotation.services.annosaurus.v2.AnnoServiceV2;
import org.mbari.m3.vars.annotation.util.JFXUtilities;
import org.mbari.m3.vars.annotation.util.ListUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.ResourceBundle;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * @author Brian Schlining
 * @since 2019-05-14T14:45:00
 */
public class AnnotationServiceDecorator2 {

    private final Logger log = LoggerFactory.getLogger(getClass());
    private final UIToolBox toolBox;
    private final int pageSize;
    private final Duration pageTimeout;
    private final int numberSimultaneousPages;
    private final EventBus eventBus;

    private class RunUnit {
        private final Observable<Annotation> observable;
        private final Runnable starter;

        public RunUnit(Observable<Annotation> observable, Runnable starter) {
            this.observable = observable;
            this.starter = starter;
        }

        public Observable<Annotation> getObservable() {
            return observable;
        }

        public Runnable getStarter() {
            return starter;
        }
    }

    public AnnotationServiceDecorator2(UIToolBox toolBox) {
        this.toolBox = toolBox;
        eventBus = toolBox.getEventBus();
        AppConfig appConfig = toolBox.getAppConfig();
        pageSize = appConfig.getAnnotationServiceV1PageSize();
        pageTimeout = appConfig.getAnnotationServiceParamsV1().getTimeout();
        numberSimultaneousPages = appConfig.getAnnotationsServiceV1PageCount();
    }

    public void loadAnnotations(Media media) {
        if (media != null) {
            AnnotationService service = toolBox.getServices().getAnnotationService();
            service.countAnnotations(media.getVideoReferenceUuid())
                    .whenComplete((v, ex) -> {
                        if (ex != null) {
                            // TODO show error dialog
                        }
                        else {
                            loadAnnotations(Lists.newArrayList(v), media);
                        }
                    });
        }
    }

    public void loadConcurrentAnnotations(Media media) {
        final UUID uuid = media.getVideoReferenceUuid();
        AnnotationService service = toolBox.getServices().getAnnotationService();
        toolBox.getServices()
                .getMediaService()
                .findConcurrentByVideoReferenceUuid(uuid)
                .thenApply(ms -> ms.stream()
                        .filter(m -> !m.getVideoReferenceUuid().equals(uuid))
                        .peek(m -> log.debug("Examining concurrent media: {}", m.getUri()))
                        .map(m -> service.countAnnotations(m.getVideoReferenceUuid()).join())
                        .collect(Collectors.toList()))
                .thenAccept(xs -> loadAnnotations(xs, media));
    }

    private void loadAnnotations(Collection<AnnotationCount> annotationCounts,
        Media media) {
        int totalCount = annotationCounts.stream()
                .mapToInt(AnnotationCount::getCount)
                .sum();

        // Build fetch requests but dont' execute them yet
        List<RunUnit> runUnits = annotationCounts.stream()
                .map(this::buildFetchRequest)
                .collect(Collectors.toList());

        // Only allow annotations within the bounds of the media
        Predicate<Annotation> predicate = buildPredicate(media);

        // Count annotations so we can update progress bar
        AtomicInteger loadedCount = new AtomicInteger(0);

        // Configure observables
        runUnits.forEach(ru -> {
            Observable<Annotation> observable = ru.getObservable();

            // update progress bar
            observable.subscribe(a -> updateLoadProgress(totalCount,
                    loadedCount.incrementAndGet()),
                    e -> log.error("Failed to execute page load", e),
                    () -> log.debug("Completed media load"));

            // Update list of annotations in view
            observable
                    .filter(predicate::test)
                    .subscribe(a -> eventBus.send(new AnnotationsAddedEvent(a)));
        });
        eventBus.send(new ShowProgress());

        // Execute each request in order
        executeFetchRequests(ListUtils.head(runUnits), ListUtils.tail(runUnits));
    }

    private void updateLoadProgress(int totalCount, int currentCount) {
        double progress = currentCount / (double) totalCount;
        eventBus.send(new SetProgress(progress));
        if (progress >= 1.0) {
            eventBus.send(new HideProgress());
        }
    }

    private Predicate<Annotation> buildPredicate(Media media) {
        Instant startTime = media.getStartTimestamp();
        Instant endTime = media.getDuration() == null ? null : startTime.plus(media.getDuration());

        Predicate<Annotation> inMedia = a -> a.getVideoReferenceUuid().equals(media.getVideoReferenceUuid());

        Predicate<Annotation> betweenDates = a -> {
            Instant rt = a.getRecordedTimestamp();
            return  rt != null &&
                    (rt.equals(startTime) ||
                            rt.equals(endTime) ||
                            (rt.isAfter(startTime) && rt.isBefore(endTime)));
        };

        Predicate<Annotation> afterStartDate = a -> {
            Instant rt = a.getRecordedTimestamp();
            return  rt != null &&
                    (rt.equals(startTime) || (rt.isAfter(startTime)));
        };

        Predicate<Annotation> timePredicate =  endTime == null ? afterStartDate : betweenDates;

        return inMedia.or(timePredicate);
    }

    private void executeFetchRequests(RunUnit current, List<RunUnit> next) {
        log.info("Executing page requests for a media");
        Observable<Annotation> observable = current.getObservable();
        observable.subscribe(a -> {},
            e -> {},
            () -> {
                log.info("Finished page requests for media");
                if (next != null && !next.isEmpty()) {
                    RunUnit head = ListUtils.head(next);
                    List<RunUnit> tail = ListUtils.tail(next);
                    executeFetchRequests(head, tail);
                }
            });
        current.getStarter().run();
    }

    public RunUnit buildFetchRequest(AnnotationCount ac) {
//        AnnotationService service1 = toolBox.getServices().getAnnotationService();
        AnnoServiceV2 service2 = toolBox.getServices().getAnnoServiceV2();

        Function<RequestPager.Page, List<Annotation>> function = (page) -> {
            try {
                return service2.findAnnotations(ac.getVideoReferenceUuid(), page.getLimit(), page.getOffset())
                        .get(pageTimeout.toMillis(), TimeUnit.MILLISECONDS);
            } catch (Exception e) {
                log.info("A page request for " + ac.getVideoReferenceUuid() + " failed.", e);
                throw new RuntimeException(e);
            }
        };

        RequestPager<List<Annotation>> pager = new RequestPager<>(function, 2, numberSimultaneousPages);
        RequestPager.Runner<List<Annotation>> runner = pager.build(ac.getCount(), pageSize);
        Observable<List<Annotation>> observable = runner.getObservable();

        Subject<Annotation> s0 = PublishSubject.create();
        Subject<Annotation> subject = s0.toSerialized();
        observable.subscribeOn(Schedulers.io())
                .subscribe(annotations -> annotations.forEach(subject::onNext),
                        subject::onError,
                        () -> log.debug("Loaded annotations for {}", ac.getVideoReferenceUuid()));

        return new RunUnit(subject, runner);

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
