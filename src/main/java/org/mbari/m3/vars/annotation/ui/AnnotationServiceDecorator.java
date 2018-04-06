package org.mbari.m3.vars.annotation.ui;

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

import java.time.Instant;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * This decorator paginates the loading of annotations and sends the
 * appropriate notifications to the UI via the EventBus
 * @author Brian Schlining
 * @since 2017-07-28T13:17:00
 */
public class AnnotationServiceDecorator {

    private final UIToolBox toolBox;
    private final int chunkSize;

    public AnnotationServiceDecorator(UIToolBox toolBox) {
        this.toolBox = toolBox;
        this.chunkSize = toolBox.getConfig().getInt("annotation.service.chunk.size");
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

        List<CompletableFuture<Void>> futures = new ArrayList<>();

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
            futures.add(future);
        }

        CompletableFuture[] cfs = futures.toArray(new CompletableFuture[futures.size()]);
        return CompletableFuture.allOf(cfs);

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
     * @param observationUuid
     */
    public void refreshAnnotationsView(Set<UUID> observationUuid) {
        CopyOnWriteArrayList<Annotation> annotations = new CopyOnWriteArrayList<>();
        final AnnotationService annotationService = toolBox.getServices().getAnnotationService();
        CompletableFuture[] futures = observationUuid.stream()
                .map(uuid -> annotationService.findByUuid(uuid)
                        .thenAccept(annotations::add))
                .toArray(i -> new CompletableFuture[i]);
        CompletableFuture<Void> all = CompletableFuture.allOf(futures);
        final EventBus eventBus = toolBox.getEventBus();
        all.thenAccept(v ->
            eventBus.send(new AnnotationsChangedEvent(annotations)));
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
        toolBox.getServices()
                .getMediaService()
                .findByVideoSequenceName(media.getVideoSequenceName())
                .thenAccept(medias -> {
                    List<Association> associations = new CopyOnWriteArrayList<>();
                    CompletableFuture[] futures = medias.stream()
                            .map(m -> toolBox.getServices()
                                    .getAnnotationService()
                                    .findByVideoReferenceAndLinkName(m.getVideoReferenceUuid(), associationKey)
                                    .thenAccept(associations::addAll))
                            .toArray(i -> new CompletableFuture[i]);
                    CompletableFuture.allOf(futures)
                            .thenAccept(v -> f.complete(associations));
                });
        return f;
    }

}
