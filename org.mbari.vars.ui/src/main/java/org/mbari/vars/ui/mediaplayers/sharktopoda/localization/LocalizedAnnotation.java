package org.mbari.vars.ui.mediaplayers.sharktopoda.localization;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import mbarix4j.util.Tuple2;
import org.mbari.vars.services.model.Annotation;
import org.mbari.vars.services.model.Association;
import org.mbari.vars.services.model.BoundingBox;
import org.mbari.vcr4j.sharktopoda.client.gson.DurationConverter;
import org.mbari.vcr4j.sharktopoda.client.localization.Localization;

import java.time.Duration;
import java.util.*;

public record LocalizedAnnotation(Annotation annotation, Association association) {

    public static String GENERATOR = "VARS Annotation";

    private static Gson gson = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
            .registerTypeAdapter(Duration.class, new DurationConverter())
            .create();

    public Optional<Localization> localization() {
        if (association.getLinkName().equalsIgnoreCase(BoundingBox.LINK_NAME) &&
                association.getMimeType().equalsIgnoreCase("application/json")) {
            BoundingBox box = gson.fromJson(association.getLinkValue(), BoundingBox.class);
            Localization x = new Localization(annotation.getConcept(),
                    annotation.getElapsedTime(),
                    association.getUuid(),
                    annotation.getVideoReferenceUuid(),
                    box.getX(),
                    box.getY(),
                    box.getWidth(),
                    box.getHeight(),
                    annotation.getDuration(),
                    annotation.getObservationUuid());
            return Optional.of(x);
        }
        return Optional.empty();
    }

    public static List<LocalizedAnnotation> from(Annotation a) {
        List<LocalizedAnnotation> xs = new ArrayList<>();
        for (Association ass: a.getAssociations()) {
            if (ass.getLinkName().equalsIgnoreCase(BoundingBox.LINK_NAME) &&
                    ass.getMimeType().equalsIgnoreCase("application/json")) {
                var x = new LocalizedAnnotation(a, ass);
                xs.add(x);
            }
        }
        return xs;
    }

    public static LocalizedAnnotation from(Localization x) {
        var annotation = new Annotation();
        annotation.setObservationUuid(x.getAnnotationUuid());
        annotation.setElapsedTime(x.getElapsedTime());
        annotation.setDuration(x.getDuration());

        var association = toAssociation(x);
        annotation.setAssociations(List.of(association));
        return new LocalizedAnnotation(annotation, association);

    }

    public static Association toAssociation(Localization x) {
        BoundingBox bb = new BoundingBox(x.getX(), x.getY(), x.getWidth(), x.getHeight(), GENERATOR);
        String json = gson.toJson(bb);

        return new Association(BoundingBox.LINK_NAME,
                Association.VALUE_SELF,
                json,
                "application/json",
                x.getLocalizationUuid());
    }

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
