package org.mbari.vars.annotation.ui.mediaplayers.sharktopoda2;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.mbari.vars.annotation.util.Tuple2;
import org.mbari.vars.vampiresquid.sdk.r1.models.Media;
import org.mbari.vars.annosaurus.sdk.r1.models.Annotation;
import org.mbari.vars.annosaurus.sdk.r1.models.Association;
import org.mbari.vars.annosaurus.sdk.r1.models.BoundingBox;
import org.mbari.vars.annotation.etc.gson.DurationConverter;
import org.mbari.vars.annotation.ui.UIToolBox;
import org.mbari.vcr4j.remote.control.commands.localization.Localization;
import org.mbari.vars.annotation.etc.jdk.Loggers;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Builds a localization from an annotation and a "bounding box" associations. The resulting localizations
 * will map it's uuid to the associations uuid.
 * @param annotation The annotation
 * @param association
 */
public record LocalizedAnnotation(Annotation annotation, Association association) {

    private static Gson gson = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
            .registerTypeAdapter(Duration.class, new DurationConverter())
            .create();

    private static final Loggers log = new Loggers(LocalizedAnnotation.class);

    private static final Cache<UUID, Optional<Media>> mediaCache = Caffeine.newBuilder()
            .maximumSize(500)
            .expireAfterWrite(Duration.ofMinutes(2))
            .build();

    /**
     * Builds the localization from the annotation and association. Always returns a new instance
     * @return An optional localization. Will be none if the association is not a bounding box,
     *   or does not have a json mimetype or if there's an error parsing the json to a bounding box or
     *   if the annotation does not have an elapsedTime.
     */
    public Optional<Localization> toLocalization(UIToolBox toolBox) {
        if (annotation == null || association == null) {
            return Optional.empty();
        }

        if (association.getLinkName() != null &&
                association.getMimeType() != null &&
                association.getLinkName().equalsIgnoreCase(BoundingBox.LINK_NAME) &&
                association.getMimeType().equalsIgnoreCase("application/json") &&
                checkIfValidForCurrentMedia(toolBox)) {
            try {
                BoundingBox box = gson.fromJson(association.getLinkValue(), BoundingBox.class);
                if (box == null || annotation.getElapsedTime() == null) {
                    return Optional.empty();
                }

                var duration = annotation.getDuration() == null ? null : annotation.getDuration().toMillis();
                var localization = new Localization(association.getUuid(),
                        annotation.getConcept(),
                        annotation.getElapsedTime().toMillis(),
                        duration,
                        box.getX(),
                        box.getY(),
                        box.getWidth(),
                        box.getHeight(),
                        null);
                return Optional.of(localization);
            }
            catch (Exception e) {
                log.atWarn()
                        .withCause(e)
                        .log("Failed to build Localization from JSON: " + association.getLinkValue());
            }
        }
        log.atWarn().log("Annotation (observationUuid=" + annotation.getObservationUuid() + ") contains a localization that is not valid for the current media");
        return Optional.empty();
    }

    private boolean checkIfValidForCurrentMedia(UIToolBox toolBox) {
        if (annotation == null || annotation.getElapsedTime() == null || toolBox == null || toolBox.getData() == null) {
            return false;
        }

        var currentMedia = toolBox.getData().getMedia();
        if (currentMedia == null) {
            return false;
        }

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

    /**
     * Given an annotation extract all LocalizedAnnotations from it.
     * @param a The annotation
     * @return A List of
     */
    public static List<LocalizedAnnotation> from(Annotation a) {
        var xs = new ArrayList<LocalizedAnnotation>();
        for (Association ass: a.getAssociations()) {
            if (ass.getLinkName().equalsIgnoreCase(BoundingBox.LINK_NAME) &&
                    ass.getMimeType().equalsIgnoreCase("application/json")) {
                var x = new LocalizedAnnotation(a, ass);
                xs.add(x);
            }
        }
        return xs;
    }

    public static List<LocalizedAnnotation> from(Collection<Annotation> annotations) {
        return annotations.stream()
                .flatMap(a -> from(a).stream())
                .toList();
    }

    /**
     * Build an LocalizedAnnotation from a Localization. The relation between the annotation and
     * the association will be set. The association will not have a UUID.
     * @param x
     * @return
     */
    public static LocalizedAnnotation from(Localization x) {


        var annotation = new Annotation();
        annotation.setConcept(x.getConcept());

        if (x.getElapsedTimeMillis() != null) {
            var elapsedTime = Duration.ofMillis(x.getElapsedTimeMillis());
            annotation.setElapsedTime(elapsedTime);
        }
        if (x.getDurationMillis() != null && x.getDurationMillis() != 0L) {
            var duration = Duration.ofMillis(x.getDurationMillis());
            annotation.setDuration(duration);
        }

        var association = toAssociation(x);
        annotation.setAssociations(List.of(association));
        return new LocalizedAnnotation(annotation, association);

    }

    /**
     * Convert a Localization to a bounding box association
     * @param x The localization
     * @return The corresponding associations
     */
    public static Association toAssociation(Localization x) {
        BoundingBox bb = new BoundingBox(x.getX(), x.getY(), x.getWidth(), x.getHeight(), "VARS Annotation");
        String json = gson.toJson(bb);

        return new Association(BoundingBox.LINK_NAME,
                Association.VALUE_SELF,
                json,
                "application/json",
                x.getUuid());
    }

    /**
     * Search a list of LocalizedAnnotations for one with the corresponding uuid using a binary search
     * @param xs A list we want to search
     * @param associationUuid The UUID do search for
     * @return A tuple of the index in the list of the match and an optional matching LocalizedAssociation
     *  If the idx is < -1 it's the insertion point. (No match was found)
     */
    public static Tuple2<Integer, Optional<LocalizedAnnotation>> search(List<LocalizedAnnotation> xs, UUID associationUuid) {

        var ass = new Association(associationUuid, Association.NIL);
        var key = new LocalizedAnnotation(null, ass);
        int idx = Collections.binarySearch(xs, key, Comparator.comparing(a -> a.association().getUuid()));
        if (idx >= 0) {
            return new Tuple2<>(idx, Optional.of(xs.get(idx)));
        }
        return new Tuple2<>(idx, Optional.empty());
    }
}

