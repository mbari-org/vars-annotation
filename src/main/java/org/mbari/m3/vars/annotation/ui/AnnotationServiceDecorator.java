package org.mbari.m3.vars.annotation.ui;

import io.reactivex.Observable;

import org.mbari.m3.vars.annotation.EventBus;
import org.mbari.m3.vars.annotation.UIToolBox;
import org.mbari.m3.vars.annotation.events.AnnotationsAddedEvent;
import org.mbari.m3.vars.annotation.events.AnnotationsChangedEvent;
import org.mbari.m3.vars.annotation.events.AnnotationsRemovedEvent;
import org.mbari.m3.vars.annotation.events.AnnotationsSelectedEvent;
import org.mbari.m3.vars.annotation.messages.HideProgress;
import org.mbari.m3.vars.annotation.messages.SetProgress;
import org.mbari.m3.vars.annotation.messages.ShowProgress;
import org.mbari.m3.vars.annotation.model.Annotation;
import org.mbari.m3.vars.annotation.model.AnnotationCount;
import org.mbari.m3.vars.annotation.model.Association;
import org.mbari.m3.vars.annotation.model.Image;
import org.mbari.m3.vars.annotation.model.Media;
import org.mbari.m3.vars.annotation.services.AnnotationService;
import org.mbari.m3.vars.annotation.services.MediaService;
import org.mbari.m3.vars.annotation.util.AsyncUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * @author Brian Schlining
 * @since 2018-10-10T15:59:00
 */
public class AnnotationServiceDecorator {

    private final Logger log = LoggerFactory.getLogger(getClass());
    private final UIToolBox toolBox;
    private final int chunkSize;
    private final Duration chunkTimeout;

    // When loading concurrent annotations we need to avoid swamping the annosaurus
    private final ExecutorService pagingExecutor = Executors.newSingleThreadExecutor();

    public AnnotationServiceDecorator(UIToolBox toolBox) {
        this.toolBox = toolBox;
        this.chunkSize = toolBox.getConfig().getInt("annotation.service.chunk.size");
        this.chunkTimeout = toolBox.getConfig().getDuration("annotation.service.timeout");
    }

    public void findAnnotations(UUID videoReferenceUuid) {
        findAnnotations(videoReferenceUuid, toolBox.getExecutorService());
    }
    /**
     * Find all annotations for a given video reference. The calls to retrieve
     * the annotations are
     * @param videoReferenceUuid
     */
    public void findAnnotations(UUID videoReferenceUuid, ExecutorService executor) {
        AnnotationService service = toolBox.getServices().getAnnotationService();
        EventBus eventBus = toolBox.getEventBus();
        AtomicInteger loadedAnnotationCount = new AtomicInteger(0);
        service.countAnnotations(videoReferenceUuid)
                .whenComplete((v, ex) -> eventBus.send(new ShowProgress()))
                .thenAccept(ac ->
                        loadAnnotationPages(loadedAnnotationCount,
                                ac.getCount(),
                                ac,
                                true,
                                null,
                                executor)
                            .whenComplete((v, ex) -> {
                                eventBus.send(new HideProgress());
                                if (ex != null) {
                                    log.error("Failed to load annotations for video_reference_uuid: " +
                                            videoReferenceUuid, ex);
                                    // TODO show error dialog
                                }
                            }));
    }


    private CompletableFuture<Void> loadAnnotationPages(AtomicInteger loadedAnnotationCount,
                                                        int totalAnnotationCount,
                                                        AnnotationCount ac,
                                                        boolean sendNotifications,
                                                        Media masterMedia,
                                                        ExecutorService executor) {

        CompletableFuture<Void> cf = new CompletableFuture<>();

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

        executor.submit(task);

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

        if (annotations != null) {
            int loadedCount = loadedAnnotationCount.addAndGet(annotations.size());
            if (sendNotifications) {
                double progress = loadedCount / (double) totalAnnotationCount;
                eventBus.send(new SetProgress(progress));
            }

            eventBus.send(new AnnotationsAddedEvent(annotations));
        }

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


    public void findConcurrentAnnotations(Collection<UUID> videoReferenceUuids) {
        AnnotationService service = toolBox.getServices().getAnnotationService();
        List<AnnotationCount> annotationCounts = videoReferenceUuids.stream()
                .map(uuid -> service.countAnnotations(uuid).join())
                .collect(Collectors.toList());

        loadConcurrentAnnotations(annotationCounts);
    }

    private void loadConcurrentAnnotations(Collection<AnnotationCount> annotationCounts) {

        String debugMsg = annotationCounts.stream()
                .map(ac -> "Found concurrent Video Reference " +
                        ac.getVideoReferenceUuid() + " with " + ac.getCount() + " annotations")
                .collect(Collectors.joining("\n"));
        log.debug(debugMsg);


        int totalCount = annotationCounts.stream()
                .mapToInt(AnnotationCount::getCount)
                .sum();
        AtomicInteger loadedCount = new AtomicInteger(0);
        Media masterMedia = toolBox.getData().getMedia();
        EventBus eventBus = toolBox.getEventBus();
        eventBus.send(new ShowProgress());

        for (AnnotationCount ac : annotationCounts) {
            loadAnnotationPages(loadedCount,
                    totalCount,
                    ac,
                    true,
                    masterMedia,
                    pagingExecutor).join();
        }
        eventBus.send(new HideProgress());

    }

    /**
     * Removes all annotations form the UI except for ones associated with the given
     * videoReferenceUuid.
     *
     * @param videoReferenceUuid UUID for a videoreference whose annotations to keep in the view
     */
    public void removeAnnotationsExceptFor(UUID videoReferenceUuid) {
        List<Annotation> removeMe = toolBox.getData()
                .getAnnotations()
                .stream()
                .filter(a -> !a.getVideoReferenceUuid().equals(videoReferenceUuid))
                .collect(Collectors.toList());
        EventBus eventBus = toolBox.getEventBus();
        eventBus.send(new AnnotationsSelectedEvent(new ArrayList<>()));
        eventBus.send(new AnnotationsRemovedEvent(removeMe));
    }

    /**
     * Refreshes the view for Annotations that already exist but are modified.
     * @param observationUuids
     */
    public void refreshAnnotationsView(Set<UUID> observationUuids) {

        final EventBus eventBus = toolBox.getEventBus();
        final AnnotationService annotationService = toolBox.getServices().getAnnotationService();
        AsyncUtils.collectAll(observationUuids, annotationService::findByUuid)
                .thenAccept(annotations -> eventBus.send(new AnnotationsChangedEvent(annotations)));

    }

    public void refreshAnnotationsView(UUID observationUuid) {
        Set<UUID> uuids = new HashSet<>();
        uuids.add(observationUuid);
        refreshAnnotationsView(uuids);
    }

    /**
     * Finds all the reference numbers used in a video sequence
     * @param media
     * @return
     */
    public CompletableFuture<List<Association>> findReferenceNumberAssociations(Media media, String associationKey) {
        CompletableFuture<List<Association>> f = new CompletableFuture<>();
        MediaService mediaService = toolBox.getServices().getMediaService();
        AnnotationService annotationService = toolBox.getServices().getAnnotationService();

        mediaService.findByVideoSequenceName(media.getVideoSequenceName())
                .thenCompose(medias -> AsyncUtils.collectAll(medias, m ->
                        annotationService.findByVideoReferenceAndLinkName(m.getVideoReferenceUuid(), associationKey)))
                .thenAccept(associationLists -> {
                    List<Association> associations = associationLists.stream()
                            .flatMap(List::stream)
                            .collect(Collectors.toList());
                    f.complete(associations);
                });

        return f;
    }

    public CompletableFuture<List<Annotation>> findAnnotationsForImages(Collection<Image> images) {
        AnnotationService annotationService = toolBox.getServices().getAnnotationService();
        Function<Image, CompletableFuture<List<Annotation>>> findAnnosFn = image ->
                annotationService.findByImageReference(image.getImageReferenceUuid());

        return AsyncUtils.collectAll(images, findAnnosFn)
                .thenApply(annotationLists -> annotationLists.stream()
                        .flatMap(List::stream)
                        .collect(Collectors.toList()));
    }


}
