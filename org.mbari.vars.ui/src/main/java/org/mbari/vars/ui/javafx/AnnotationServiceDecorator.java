package org.mbari.vars.ui.javafx;


import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import org.mbari.vars.core.EventBus;
import org.mbari.vars.ui.UIToolBox;
import org.mbari.vars.ui.events.AnnotationsAddedEvent;
import org.mbari.vars.ui.events.AnnotationsChangedEvent;
import org.mbari.vars.ui.events.AnnotationsRemovedEvent;
import org.mbari.vars.ui.events.AnnotationsSelectedEvent;
import org.mbari.vars.ui.messages.HideProgress;
import org.mbari.vars.ui.messages.SetProgress;
import org.mbari.vars.ui.messages.ShowNonfatalErrorAlert;
import org.mbari.vars.ui.messages.ShowProgress;
import org.mbari.vars.services.model.*;
import org.mbari.vars.services.AnnotationService;
import org.mbari.vars.services.MediaService;
import org.mbari.vars.services.RequestPager;
import org.mbari.vars.core.util.AsyncUtils;
import org.mbari.vars.ui.util.JFXUtilities;
import org.mbari.vars.ui.services.CachedReferenceNumberDecorator;
import org.mbari.vars.ui.services.ConcurrentAnnotationDecorator;
import org.mbari.vcr4j.VideoIndex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.CompletableFuture;
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

    private enum PagingStyle {
        PARALLEL,
        SEQUENTIAL
    }

    private final Logger log = LoggerFactory.getLogger(getClass());
    private final UIToolBox toolBox;
    private final int chunkSize;
    private final Duration chunkTimeout;
    private PagingStyle pagingStyle;
    private final int numberSimultaneousPages;

    // When loading concurrent annotations we need to avoid swamping the annosaurus
    private final ExecutorService pagingExecutor = Executors.newSingleThreadExecutor();

    public AnnotationServiceDecorator(UIToolBox toolBox) {
        this.toolBox = toolBox;
        this.chunkSize = toolBox.getConfig().getInt("annotation.service.page.size");
        this.chunkTimeout = toolBox.getConfig().getDuration("annotation.service.timeout");
        this.numberSimultaneousPages = toolBox.getConfig().getInt("annotation.service.page.count");

        /*
         When deploed on infrastructures with multiple annosaurus servers we
         can do a load in parallel as we are less likely to overwhelm the servers.

         Otherwise, it's best to load pages sequentially
         */
        try {
            String p = toolBox.getConfig().getString("annotation.service.paging");
            if (p.startsWith("par")) {
                pagingStyle = PagingStyle.PARALLEL;
            }
            else {
                pagingStyle = PagingStyle.SEQUENTIAL;
            }
        }
        catch (Exception e) {
            log.warn("Failed to read 'annotation.service.paging' from config file.", e);
            pagingStyle = PagingStyle.SEQUENTIAL;
        }
    }

    public CompletableFuture<Void> findAnnotations(UUID videoReferenceUuid) {
        return findAnnotations(videoReferenceUuid, toolBox.getExecutorService());
    }
    /**
     * Find all annotations for a given video reference. The calls to retrieve
     * the annotations are
     * @param videoReferenceUuid
     */
    public CompletableFuture<Void>  findAnnotations(UUID videoReferenceUuid, ExecutorService executor) {
        AnnotationService service = toolBox.getServices().getAnnotationService();
        EventBus eventBus = toolBox.getEventBus();
        AtomicInteger loadedAnnotationCount = new AtomicInteger(0);
        return service.countAnnotations(videoReferenceUuid)
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
                                    // Show error dialog
                                    showFindAnnotationsError(videoReferenceUuid, ex);
                                }
                            }));
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
                    List.of(content1, videoReferenceUuid.toString(), content2));
            log.error(msg, ex);

            Exception e = ex instanceof Exception ? (Exception) ex : new RuntimeException(msg, ex);
            eventBus.send(new ShowNonfatalErrorAlert(title, header, msg, e));
        });

    }

    private CompletableFuture<Void> loadAnnotationPages(AtomicInteger loadedAnnotationCount,
                                                         int totalAnnotationCount,
                                                         AnnotationCount ac,
                                                         boolean sendNotifications,
                                                         Media masterMedia,
                                                         ExecutorService executor) {

        AnnotationService service = toolBox.getServices()
                .getAnnotationService();

        EventBus eventBus = toolBox.getEventBus();

        Function<RequestPager.Page, List<Annotation>> function = (page) -> {
            try {
                return service.findAnnotations(ac.getVideoReferenceUuid(), page.getLimit(), page.getOffset(), false)
                        .get(chunkTimeout.toMillis(), TimeUnit.MILLISECONDS);
            } catch (Exception e) {
                log.info("A page request failed.", e);
                throw new RuntimeException(e);
            }
        };


        CompletableFuture<Void> future = new CompletableFuture<>();

        if (ac.getCount() > 0) {
            int threadCount = pagingStyle.equals(PagingStyle.PARALLEL) ? numberSimultaneousPages : 1;
            RequestPager<List<Annotation>> pager = new RequestPager<>(function, 2, threadCount);
            RequestPager.Runner<List<Annotation>> runner = pager.build(ac.getCount(), chunkSize);
            Observable<List<Annotation>> observable = runner.getObservable();
            observable.subscribeOn(Schedulers.io())
                    .map(annotations -> masterMedia == null ? annotations : filterWithinMedia(annotations, masterMedia))
                    .subscribe(annotations -> updateUI(eventBus,
                            annotations,
                            sendNotifications,
                            totalAnnotationCount,
                            loadedAnnotationCount),
                            future::completeExceptionally,
                            () -> {
                                log.info("Loaded annotations for " + ac.getVideoReferenceUuid());
                                future.complete(null);
                            });
            runner.run();
        }
        else {
            future.complete(null);
        }

        return future;
    }


    private CompletableFuture<Void> loadAnnotationPagesOld(AtomicInteger loadedAnnotationCount,
                                                        int totalAnnotationCount,
                                                        AnnotationCount ac,
                                                        boolean sendNotifications,
                                                        Media masterMedia,
                                                        ExecutorService executor) {

        CompletableFuture<Void> cf;
        switch (pagingStyle) {
            case PARALLEL: cf = loadAnnotationPagesPar(loadedAnnotationCount,
                    totalAnnotationCount,
                    ac,
                    sendNotifications,
                    masterMedia,
                    executor);
                 break;
            default: cf = loadAnnotationPagesSeq(loadedAnnotationCount,
                    totalAnnotationCount,
                    ac,
                    sendNotifications,
                    masterMedia,
                    executor);
                break;
        }
        return cf;

    }

    private CompletableFuture<Void> loadAnnotationPagesSeq(AtomicInteger loadedAnnotationCount,
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
                    String msg = "Failed to load page chunk (" + offset + " to " +
                            offset + limit + ")";
                    Exception e0 = new RuntimeException(msg, e);
                    cf.completeExceptionally(e0);
                    break;
                }
            }
            cf.complete(null);
        };

        executor.submit(task);

        return cf;

    }

    private CompletableFuture<Void> loadAnnotationPagesPar(AtomicInteger loadedAnnotationCount,
                                                        int totalAnnotationCount,
                                                        AnnotationCount ac,
                                                        boolean sendNotifications,
                                                        Media masterMedia,
                                                        ExecutorService executor) {


        int n = (int) Math.ceil(ac.getCount() / (double) chunkSize);
        CompletableFuture[] futures = new CompletableFuture[n];
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
            futures[i] = future;
        }

        return CompletableFuture.allOf(futures);

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

        return service.findAnnotations(videoReferenceUuid, limit, offset, false)
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


    /**
     *
     * @param videoReferenceUuids
     * @deprecated Use {@link ConcurrentAnnotationDecorator} instead
     */
    @Deprecated(forRemoval = true)
    public void findConcurrentAnnotations(Collection<UUID> videoReferenceUuids) {
        AnnotationService service = toolBox.getServices().getAnnotationService();
        List<AnnotationCount> annotationCounts = videoReferenceUuids.stream()
                .map(uuid -> service.countAnnotations(uuid).join())
                .filter(ac -> ac.getCount() > 0)
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

    public void refreshAnnotationsViewByIndices(Set<VideoIndex> videoIndices) {
        List<Annotation> annotations = new ArrayList<>(toolBox.getData().getAnnotations());
        Set<VideoIndex> annotationIndices = annotations.stream()
                .map(ImagedMoment::toVideoIndex)
                .collect(Collectors.toSet());
        annotationIndices.retainAll(videoIndices);
        Set<UUID> observationUuids = annotations.stream()
                .filter(a -> {
                    VideoIndex vi = a.toVideoIndex();
                    return annotationIndices.contains(vi);
                })
                .map(Annotation::getObservationUuid)
                .collect(Collectors.toSet());
        refreshAnnotationsView(observationUuids);
    }



    /**
     * Finds all the reference numbers used in a video sequence
     * @param media
     * @return
     * @deprecated Use {@link CachedReferenceNumberDecorator} instead
     */
    @Deprecated(forRemoval = true)
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

    /**
     *
     * @param media
     * @param associationKey
     * @param concept
     * @return
     * @deprecated Use {@link CachedReferenceNumberDecorator} instead
     */
    @Deprecated(forRemoval = true)
    public CompletableFuture<List<Association>> findReferenceNumberAssociationsForConcept(Media media,
                                                                                          String associationKey,
                                                                                          String concept) {

        CompletableFuture<List<Association>> f = new CompletableFuture<>();
        MediaService mediaService = toolBox.getServices().getMediaService();
        AnnotationService annotationService = toolBox.getServices().getAnnotationService();

        mediaService.findByVideoSequenceName(media.getVideoSequenceName())
                .thenCompose(medias ->
                        AsyncUtils.collectAll(medias,
                                m -> annotationService.findByVideoReferenceAndLinkNameAndConcept(m.getVideoReferenceUuid(),
                                        associationKey,
                                        concept)))
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

