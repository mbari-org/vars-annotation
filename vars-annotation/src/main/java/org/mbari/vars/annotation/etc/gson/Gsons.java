package org.mbari.vars.annotation.etc.gson;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.mbari.vars.annosaurus.sdk.r1.models.*;
import org.mbari.vcr4j.time.Timecode;

import java.lang.reflect.Type;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.UUID;

public class Gsons {

    public static Gson newSnakeCaseGson() {
        GsonBuilder gsonBuilder = new GsonBuilder()
                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
                .registerTypeAdapter(ImagedMoment.class, new AnnotationCreator())
                .registerTypeAdapter(Duration.class, new DurationConverter())
                .registerTypeAdapter(Timecode.class, new TimecodeConverter())
                .registerTypeAdapter(Instant.class, new InstantConverter())
                .registerTypeAdapter(byte[].class, new ByteArrayConverter());

        return gsonBuilder.create();

    }

    public static Gson newCamelCaseGson() {
        GsonBuilder gsonBuilder = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
                .registerTypeAdapter(Duration.class, new DurationConverter())
                .registerTypeAdapter(Timecode.class, new TimecodeConverter())
                .registerTypeAdapter(Instant.class, new InstantConverter())
                .registerTypeAdapter(byte[].class, new ByteArrayConverter());

        return gsonBuilder.create();
    }

    public final Type TYPE_LIST_ANCILLARY_DATA = new TypeToken<ArrayList<AncillaryData>>(){}.getType();
    public final Type TYPE_LIST_ANNOTATION = new TypeToken<ArrayList<Annotation>>(){}.getType();
    public final Type TYPE_LIST_ANNOTATION_COUNT = new TypeToken<ArrayList<AnnotationCount>>(){}.getType();
    public final Type TYPE_LIST_ASSOCIATION = new TypeToken<ArrayList<Association>>(){}.getType();
    public final Type TYPE_LIST_IMAGE = new TypeToken<ArrayList<Image>>(){}.getType();
    public final Type TYPE_LIST_IMAGED_MOMENT = new TypeToken<ArrayList<ImagedMoment>>(){}.getType();
    public final Type TYPE_LIST_INDEX = new TypeToken<ArrayList<Index>>(){}.getType();
    public final Type TYPE_LIST_STRING = new TypeToken<ArrayList<String>>(){}.getType();
    public final Type TYPE_LIST_USER = new TypeToken<ArrayList<User>>(){}.getType();
    public final Type TYPE_LIST_UUID = new TypeToken<ArrayList<UUID>>(){}.getType();
}
