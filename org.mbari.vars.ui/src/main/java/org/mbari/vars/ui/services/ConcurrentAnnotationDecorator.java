package org.mbari.vars.ui.services;

import com.google.common.collect.Lists;
import org.mbari.vars.ui.AppConfig;
import org.mbari.vars.ui.EventBus;
import org.mbari.vars.ui.UIToolBox;
import org.mbari.vars.ui.events.AnnotationsAddedEvent;
import org.mbari.vars.ui.messages.HideProgress;
import org.mbari.vars.ui.messages.SetProgress;
import org.mbari.vars.ui.messages.ShowNonfatalErrorAlert;
import org.mbari.vars.services.model.*;
import org.mbari.vars.services.AnnotationService;
import org.mbari.vars.services.MediaService;
import org.mbari.vars.services.RequestPager;
import org.mbari.vars.javafx.util.JFXUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Decorates annotation and media services to load concurrent annotations. That
 * is annotations from media from the same video sequence that overlap in time
 * with the currently annotated media.
 *
 * @author Brian Schlining
 * @since 2019-05-14T14:45:00
 */
public class ConcurrentAnnotationDecorator {

    private final Logger log = LoggerFactory.getLogger(getClass());
    private final UIToolBox toolBox;
    private final int pageSize;
    private final Duration pageTimeout;
    private final int numberSimultaneousPages;
    private final EventBus eventBus;

    public ConcurrentAnnotationDecorator(UIToolBox toolBox) {
        this.toolBox = toolBox;
        eventBus = toolBox.getEventBus();
        AppConfig appConfig = toolBox.getAppConfig();
        pageSize = appConfig.getAnnotationServiceV1PageSize();
        pageTimeout = appConfig.getAnnotationServiceParamsV1().getTimeout();
        numberSimultaneousPages = appConfig.getAnnotationsServiceV1PageCount();
    }

    private CompletableFuture<Optional<ConcurrentRequestCount>> countConcurrentAnnotations(Media media) {
        log.debug("Counting concurrent annotations related to {}", media.getUri());
        if (media.getStartTimestamp() != null && media.getDuration() != null) {
            final UUID uuid = media.getVideoReferenceUuid();
            final Instant start = media.getStartTimestamp();
            final Instant end = start.plus(media.getDuration());

            AnnotationService annotationService = toolBox.getServices().getAnnotationService();
            MediaService mediaService = toolBox.getServices()
                    .getMediaService();

            return mediaService.findConcurrentByVideoReferenceUuid(uuid)
                    .thenCompose(ms -> {
                        List<UUID> otherMedia = ms.stream()
                                .filter(m -> !m.getVideoReferenceUuid().equals(uuid))
                                .map(Media::getVideoReferenceUuid)
                                .collect(Collectors.toList());
                        if (otherMedia.isEmpty()) {
                            return CompletableFuture.completedFuture(Optional.empty());
                        }
                        else {
                            ConcurrentRequest cr = new ConcurrentRequest(start, end, otherMedia);
                            return annotationService.countByConcurrentRequest(cr)
                                    .thenApply(Optional::ofNullable);
                        }

                    });
        }
        else {
            log.warn("Media '{}' does not have both startTimestamp and Duration " +
                    "needed to calculate limits for loading concurrent annotations", media.getUri());
            return CompletableFuture.completedFuture(Optional.empty());
        }
    }


    public void loadConcurrentAnnotations(Media media) {

        countConcurrentAnnotations(media)
                .handle((opt, ex) -> {
                    if (ex != null) {
                        // TODO show error dialog
                    }
                    else {
                        if (opt.isPresent()) {
                            log.debug("Loading concurrent annotations related to {}", media.getUri());
                            loadAnnotations(media, opt.get());
                        }
                        else {
                            log.warn("Did not get a concurrent request count for {}", media.getUri());
                        }
                    }
                    return null;
                });

    }

    private void loadAnnotations(Media media, ConcurrentRequestCount count) {
        AtomicInteger loadedCount = new AtomicInteger(0);
        RequestPager.Runner<List<Annotation>> runner = buildRequestRunner(count);
        runner.getObservable()
                .subscribe(annos -> doNext(annos, count.getCount(), loadedCount),
                        ex -> doError(media, ex),
                        () -> doComplete(media, loadedCount));
        runner.run();
    }

    private void doNext(List<Annotation> annotations, long totalCount, AtomicInteger loadedCount) {
        eventBus.send(new AnnotationsAddedEvent(annotations));
        updateLoadProgress(totalCount, loadedCount.addAndGet(annotations.size()));
    }

    private void doError(Media media, Throwable e) {
        log.error("An error occurred while loading concurrent media for " + media.getUri(), e);
        showFindAnnotationsError(media.getVideoReferenceUuid(), e);
    }

    private void doComplete(Media media, AtomicInteger loadedCount) {
        log.info("Loaded {} concurrent annotations for {}", loadedCount.get(), media.getUri());
    }

    public RequestPager.Runner<List<Annotation>> buildRequestRunner(ConcurrentRequestCount count) {

        AnnotationService annotationService = toolBox.getServices().getAnnotationService();

        Function<RequestPager.Page, List<Annotation>> function = (page) -> {
            try {
                return  annotationService.findByConcurrentRequest(count.getConcurrentRequest(),
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
        eventBus.send(new SetProgress(progress));
        if (progress >= 1.0) {
            eventBus.send(new HideProgress());
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
