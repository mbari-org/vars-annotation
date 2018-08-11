package org.mbari.m3.vars.annotation.ui;

import com.google.common.collect.Lists;
import javafx.util.Pair;
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
import org.mbari.m3.vars.annotation.model.Media;
import org.mbari.m3.vars.annotation.services.AnnotationService;
import org.mbari.m3.vars.annotation.services.MediaService;
import org.mbari.m3.vars.annotation.util.AsyncUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * This decorator paginates the loading of annotations and sends the
 * appropriate notifications to the UI via the EventBus
 * @author Brian Schlining
 * @since 2017-07-28T13:17:00
 */
public class AnnotationServiceDecorator {

    private final Logger log = LoggerFactory.getLogger(getClass());
    private final UIToolBox toolBox;
    private final int chunkSize;
    private final Duration chunkTimeout;

    public AnnotationServiceDecorator(UIToolBox toolBox) {
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
                            Optional.empty()));
    }

    private CompletableFuture<Void> loadAnnotationPages(AtomicInteger loadedAnnotationCount,
                                                           int totalAnnotationCount,
                                                           AnnotationCount ac,
                                                           boolean sendNotifcations,
                                                           Optional<Media> masterMedia) {

        CompletableFuture<Void> cf = new CompletableFuture<>();
        AnnotationService service = toolBox.getServices().getAnnotationService();
        EventBus eventBus = toolBox.getEventBus();

        Optional<Pair<Instant, Instant>> mediaBounds = masterMedia.map(m -> {
            Instant startTimestamp = m.getStartTimestamp();
            Instant endTimestamp = m.getDuration() == null ? null : startTimestamp.plus(m.getDuration());
            return new Pair<>(startTimestamp, endTimestamp);
        });

        if (sendNotifcations) {
            eventBus.send(new ShowProgress());
        }

        Runnable task = () -> {
            int n = (int) Math.ceil(ac.getCount() / (double) chunkSize);
            for (int i = 0; i < n; i++) {
                long offset = i * chunkSize;
                long limit = chunkSize;

                CompletableFuture<Void> future = service.findAnnotations(ac.getVideoReferenceUuid(), limit, offset)
                        .thenAccept(annotations -> {
                            if (sendNotifcations && annotations.size() == 0) {
                                eventBus.send(new HideProgress());
                            } else {

                                int c = loadedAnnotationCount.addAndGet(annotations.size());
                                double progress = c / totalAnnotationCount;

                                List<Annotation> annos = annotations;
                                if (mediaBounds.isPresent()) {
                                    Pair<Instant, Instant> bounds = mediaBounds.get();
                                    Instant startTime = bounds.getKey();
                                    Instant endTime = bounds.getValue();
                                    if (startTime != null && endTime != null) {
                                        annos = annotations.stream()
                                                .filter(a -> {
                                                    Instant rt = a.getRecordedTimestamp();
                                                    return rt != null &&
                                                            (rt.equals(startTime) ||
                                                                    rt.equals(endTime) ||
                                                                    (rt.isAfter(startTime) && rt.isBefore(endTime)));
                                                })
                                                .collect(Collectors.toList());
                                    }
                                }
                                eventBus.send(new AnnotationsAddedEvent(annos));

                                if (sendNotifcations) {
                                    if (progress >= 1.0) {
                                        eventBus.send(new HideProgress());
                                    } else {
                                        eventBus.send(new SetProgress(progress));
                                    }
                                }
                            }
                        });

                try {
                    future.get(chunkTimeout.toMillis(), TimeUnit.MILLISECONDS);
                }
                catch (Exception e) {
                    log.warn("Failed to load page chunk (" + offset + " to " +
                            offset + limit + ")", e);
                }
            }
            cf.complete(null);

        };

        toolBox.getExecutorService().submit(task);

        return cf;

    }



    public void findConcurrentAnnotations(Collection<UUID> videoReferenceUuids) {
        AnnotationService service = toolBox.getServices().getAnnotationService();
        EventBus eventBus = toolBox.getEventBus();
        Optional<Media> media = Optional.ofNullable(toolBox.getData().getMedia());

        AtomicInteger loadedAnnotationCount = new AtomicInteger(0);

        CompletableFuture<AnnotationCount>[] futures = videoReferenceUuids.stream()
                .map(service::countAnnotations)
                .toArray(size -> new CompletableFuture[size]);

        CompletableFuture.allOf(futures)
                .thenApply(v -> Arrays.stream(futures)
                            .map(f -> {
                                Optional<AnnotationCount> opt = Optional.empty();
                                if (!f.isCompletedExceptionally()) {
                                    try {
                                        AnnotationCount p = f.get();
                                        loadedAnnotationCount.addAndGet(p.getCount());
                                        opt = Optional.of(p);
                                    }
                                    catch (Exception e) {
                                        // TODO log error?
                                    }
                                }
                                return opt;
                            })
                            .filter(Optional::isPresent)
                            .map(Optional::get)
                            .collect(Collectors.toList()))
                .thenAccept(counts -> {
                    eventBus.send(new ShowProgress());
                    int totalCount = counts.stream()
                            .mapToInt(AnnotationCount::getCount)
                            .sum();

                    for (AnnotationCount ac : counts) {
                        loadAnnotationPages(loadedAnnotationCount,
                                totalCount,
                                ac,
                                true,
                                media);
                    }
                });
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
//        AsyncUtils.collectAll(observationUuids, annotationService::findByUuid)
//                .thenAccept(annotations -> eventBus.send(new AnnotationsChangedEvent(annotations)));

        AsyncUtils.observeAll(observationUuids, annotationService::findByUuid)
                .subscribe(annotation -> eventBus.send(new AnnotationsChangedEvent(Arrays.asList(annotation))));

//        CopyOnWriteArrayList<Annotation> annotations = new CopyOnWriteArrayList<>();
//
//        CompletableFuture[] futures = observationUuids.stream()
//                .map(uuid -> annotationService.findByUuid(uuid)
//                        .thenAccept(annotations::add))
//                .toArray(i -> new CompletableFuture[i]);
//        CompletableFuture<Void> all = CompletableFuture.allOf(futures);
//
//        all.thenAccept(v ->
//            eventBus.send(new AnnotationsChangedEvent(annotations)));
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

//        toolBox.getServices()
//                .getMediaService()
//                .findByVideoSequenceName(media.getVideoSequenceName())
//                .thenAccept(medias -> {
//
//
//
//                    List<Association> associations = new CopyOnWriteArrayList<>();
//                    CompletableFuture[] futures = medias.stream()
//                            .map(m -> toolBox.getServices()
//                                    .getAnnotationService()
//                                    .findByVideoReferenceAndLinkName(m.getVideoReferenceUuid(), associationKey)
//                                    .thenAccept(associations::addAll))
//                            .toArray(i -> new CompletableFuture[i]);
//                    CompletableFuture.allOf(futures)
//                            .thenAccept(v -> f.complete(associations));
//                });
        return f;
    }

}
