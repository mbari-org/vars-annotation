package org.mbari.vars.annotation.ui.mediaplayers;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.mbari.vars.annosaurus.sdk.r1.models.Annotation;
import org.mbari.vars.annotation.etc.jdk.Loggers;
import org.mbari.vars.annotation.ui.UIToolBox;
import org.mbari.vars.vampiresquid.sdk.r1.models.Media;

import java.time.Duration;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Works out where an annotation falls in the currently open video. Used to decide if, and when, an
 * annotation's localizations can be shown in the video player.
 */
public final class AnnotationPlacement {

    private static final Loggers log = new Loggers(AnnotationPlacement.class);

    private static final Cache<UUID, Optional<Media>> mediaCache = Caffeine.newBuilder()
            .maximumSize(500)
            .expireAfterWrite(Duration.ofMinutes(2))
            .build();

    private AnnotationPlacement() {
        // No instantiation
    }

    /**
     * The annotation's elapsed time in the currently open video, if it can be shown in that video.
     * This may do a (cached) media lookup, so it can block. Avoid calling it on the UI threads
     * when the annotation may be from a different video.
     *
     * @param toolBox The toolbox
     * @param annotation The annotation
     * @return The elapsed time into the current video. Empty if the annotation isn't on footage that
     *  matches the current video, or if its elapsed time can't be worked out.
     */
    public static Optional<Duration> elapsedTimeInCurrentMedia(UIToolBox toolBox, Annotation annotation) {
        if (annotation == null || toolBox == null || toolBox.getData() == null) {
            return Optional.empty();
        }
        var currentMedia = toolBox.getData().getMedia();
        if (currentMedia == null || !isOnMatchingMedia(toolBox, annotation, currentMedia)) {
            return Optional.empty();
        }
        return elapsedTime(annotation, currentMedia);
    }

    /**
     * The annotation's elapsed time. If the annotation doesn't have one (e.g. it only has a recorded
     * timestamp), it's worked out from the recorded timestamp and the media's start time.
     *
     * @param annotation The annotation
     * @param media The media the elapsed time is relative to
     * @return The elapsed time. Empty if it can't be worked out or falls outside of the media
     */
    public static Optional<Duration> elapsedTime(Annotation annotation, Media media) {
        if (annotation.getElapsedTime() != null) {
            return Optional.of(annotation.getElapsedTime());
        }
        var recordedTimestamp = annotation.getRecordedTimestamp();
        var startTimestamp = media == null ? null : media.getStartTimestamp();
        if (recordedTimestamp == null || startTimestamp == null) {
            return Optional.empty();
        }
        var elapsedTime = Duration.between(startTimestamp, recordedTimestamp);
        if (elapsedTime.isNegative() ||
                (media.getDuration() != null && elapsedTime.compareTo(media.getDuration()) > 0)) {
            return Optional.empty();
        }
        return Optional.of(elapsedTime);
    }

    /**
     * An annotation can be shown in the current media if it was made on it, or on another media
     * with the same start time and size (e.g. a different encoding of the same video).
     */
    private static boolean isOnMatchingMedia(UIToolBox toolBox, Annotation annotation, Media currentMedia) {
        var annotationVideoReferenceUuid = annotation.getVideoReferenceUuid();
        if (Objects.equals(annotationVideoReferenceUuid, currentMedia.getVideoReferenceUuid())) {
            return true;
        }

        if (annotationVideoReferenceUuid == null) {
            return false;
        }

        return lookupMedia(toolBox, annotationVideoReferenceUuid)
                .map(annotationMedia ->
                        Objects.equals(annotationMedia.getStartTimestamp(), currentMedia.getStartTimestamp()) &&
                        Objects.equals(annotationMedia.getWidth(), currentMedia.getWidth()) &&
                        Objects.equals(annotationMedia.getHeight(), currentMedia.getHeight()))
                .orElse(false);
    }

    /**
     * Media lookups happen once per localization, in loops, on latency-sensitive threads
     * (the event bus, often the FX thread, during full localization reloads). Cache them
     * per videoReferenceUuid — including failures: when the media service is unreachable,
     * retrying for every localization would stall the caller up to 10 seconds each time.
     */
    private static Optional<Media> lookupMedia(UIToolBox toolBox, UUID videoReferenceUuid) {
        return mediaCache.get(videoReferenceUuid, uuid -> {
            try {
                return Optional.ofNullable(toolBox.getServices()
                        .mediaService()
                        .findByUuid(uuid)
                        .get(10, TimeUnit.SECONDS));
            }
            catch (Exception e) {
                log.atInfo()
                        .withCause(e)
                        .log(() -> "Unable to look up media with videoReferenceUuid=" + uuid);
                return Optional.empty();
            }
        });
    }
}
