package org.mbari.m3.vars.annotation.ui;

import io.reactivex.Observable;
import javafx.util.Pair;
import org.mbari.m3.vars.annotation.EventBus;
import org.mbari.m3.vars.annotation.UIToolBox;
import org.mbari.m3.vars.annotation.events.AnnotationsAddedEvent;
import org.mbari.m3.vars.annotation.messages.HideProgress;
import org.mbari.m3.vars.annotation.messages.SetProgress;
import org.mbari.m3.vars.annotation.messages.ShowProgress;
import org.mbari.m3.vars.annotation.model.Annotation;
import org.mbari.m3.vars.annotation.model.AnnotationCount;
import org.mbari.m3.vars.annotation.model.Media;
import org.mbari.m3.vars.annotation.services.AnnotationService;
import org.mbari.m3.vars.annotation.util.AsyncUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * @author Brian Schlining
 * @since 2018-10-10T15:59:00
 */
public class AnnotationServiceDecorator2 {

    private final Logger log = LoggerFactory.getLogger(getClass());
    private final UIToolBox toolBox;
    private final int chunkSize;
    private final Duration chunkTimeout;

    public AnnotationServiceDecorator2(UIToolBox toolBox) {
        this.toolBox = toolBox;
        this.chunkSize = toolBox.getConfig().getInt("annotation.service.chunk.size");
        this.chunkTimeout = toolBox.getConfig().getDuration("annotation.service.timeout");
    }

    /**
     * Find all annotations for a given video reference. The calls to retrieve
     * the annotations are
     * @param videoReferenceUuid
     */
    public void findAnnotations(UUID videoReferenceUuid) {
        AnnotationService service = toolBox.getServices().getAnnotationService();
        AtomicInteger loadedAnnotationCount = new AtomicInteger(0);
        service.countAnnotations(videoReferenceUuid)
                .thenAccept(ac ->
                        loadAnnotationPages(loadedAnnotationCount,
                                ac.getCount(),
                                ac,
                                true,
                                null));
    }


    private CompletableFuture<Void> loadAnnotationPages(AtomicInteger loadedAnnotationCount,
                                                        int totalAnnotationCount,
                                                        AnnotationCount ac,
                                                        boolean sendNotifications,
                                                        Media masterMedia) {

        CompletableFuture<Void> cf = new CompletableFuture<>();
        EventBus eventBus = toolBox.getEventBus();

        if (sendNotifications) {
            eventBus.send(new ShowProgress());
        }

        Runnable task = () -> {
            int n = (int) Math.ceil(ac.getCount() / (double) chunkSize);
            for (int i = 0; i < n; i++) {
                long offset = i * chunkSize;
                long limit = chunkSize;
                CompletableFuture<Void> future = loadAnnotationPage(ac.getVideoReferenceUuid(),
                        limit,
                        offset,
                        sendNotifications,
                        totalAnnotationCount,
                        loadedAnnotationCount,
                        masterMedia);
                try {
                    future.get(chunkTimeout.toMillis(), TimeUnit.MILLISECONDS);
                }
                catch (Exception e) {
                    cf.completeExceptionally(e);
                    log.warn("Failed to load page chunk (" + offset + " to " +
                            offset + limit + ")", e);
                    break;
                }
            }
            cf.complete(null);
        };

        cf.whenComplete((v, ex) -> {
            if (sendNotifications) {
                eventBus.send(new HideProgress());
            }
            if (ex != null) {
                // TODO show error dialog
            }
        });

        toolBox.getExecutorService().submit(task);

        return cf;


    }


    private CompletableFuture<Void> loadAnnotationPage(UUID videoReferenceUuid,
                                                       long limit,
                                                       long offset,
                                                       boolean sendNotifications,
                                                       int totalAnnotationCount,
                                                       AtomicInteger loadedAnnotationCount,
                                                       Media media) {


        AnnotationService service = toolBox.getServices().getAnnotationService();
        EventBus eventBus = toolBox.getEventBus();

        return service.findAnnotations(videoReferenceUuid, limit, offset)
                .thenApply(annotations -> media == null ? annotations : filterWithinMedia(annotations, media))
                .thenAccept(annotations -> updateUI(eventBus,
                        annotations,
                        sendNotifications,
                        totalAnnotationCount,
                        loadedAnnotationCount));

    }

    private void updateUI(EventBus eventBus,
                          List<Annotation> annotations,
                          boolean sendNotifications,
                          int totalAnnotationCount,
                          AtomicInteger loadedAnnotationCount) {

        int loadedCount = loadedAnnotationCount.addAndGet(annotations.size());
        if (sendNotifications) {
            double progress = loadedCount / (double) totalAnnotationCount;
            eventBus.send(new SetProgress(progress));
        }

        eventBus.send(new AnnotationsAddedEvent(annotations));

    }

    private List<Annotation> filterWithinMedia(List<Annotation> annotations, Media media) {

        Instant startTime = media.getStartTimestamp();
        Instant endTime = media.getDuration() == null ? null : startTime.plus(media.getDuration());

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

        Predicate<Annotation> filter = endTime == null ? afterStartDate : betweenDates;

        return annotations.stream()
                .filter(filter)
                .collect(Collectors.toList());

    }




}
