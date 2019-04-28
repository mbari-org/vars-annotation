package org.mbari.m3.vars.annotation.model;

import com.google.gson.annotations.SerializedName;
import org.mbari.vcr4j.VideoIndex;
import org.mbari.vcr4j.time.Timecode;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Brian Schlining
 * @since 2017-05-11T13:26:00
 */
public class Annotation implements ImagedMoment {

    private UUID observationUuid;
    private String concept;
    private String observer;
    private Instant observationTimestamp;
    private UUID videoReferenceUuid;
    private UUID imagedMomentUuid;
    private Timecode timecode;

    @SerializedName("elapsed_time_millis")
    private Duration elapsedTime;
    private Instant recordedTimestamp;
    @SerializedName("duration_millis")
    private Duration duration;
    private String group;
    private String activity;
    private List<Association> associations;
    private List<ImageReference> imageReferences;

    public Annotation() {
    }

    /**
     *
     * @param concept
     * @param observer
     * @deprecated
     */
    public Annotation(String concept, String observer) {
        this.concept = concept;
        this.observer = observer;
        this.observationTimestamp = Instant.now();
    }

    public Annotation(String concept,
                      String observer,
                      VideoIndex videoIndex,
                      UUID videoReferenceUuid) {
        this.concept = concept;
        this.observer = observer;
        this.observationTimestamp = Instant.now();
        this.videoReferenceUuid = videoReferenceUuid;
        videoIndex.getElapsedTime().ifPresent(this::setElapsedTime);
        videoIndex.getTimestamp().ifPresent(this::setRecordedTimestamp);
        videoIndex.getTimecode().ifPresent(this::setTimecode);
    }


    public Annotation(Annotation a) {
        observationUuid = a.observationUuid;
        concept = a.concept;
        observer = a.observer;
        observationTimestamp = a.observationTimestamp;
        videoReferenceUuid = a.videoReferenceUuid;
        imagedMomentUuid = a.imagedMomentUuid;
        timecode = a.timecode;
        elapsedTime = a.elapsedTime;
        recordedTimestamp = a.recordedTimestamp;
        duration = a.duration;
        group = a.group;
        activity = a.activity;
        if (a.associations == null) {
            associations = new ArrayList<>();
        }
        else {
            associations = a.associations
                    .stream()
                    .map(Association::new)
                    .collect(Collectors.toList());
        }

        if (a.imageReferences == null) {
            imageReferences = new ArrayList<>();
        }
        else {
            imageReferences = a.imageReferences
                    .stream()
                    .map(ImageReference::new)
                    .collect(Collectors.toList());
        }
    }

    public UUID getObservationUuid() {
        return observationUuid;
    }

    public void setObservationUuid(UUID observationUuid) {
        this.observationUuid = observationUuid;
    }

    public String getConcept() {
        return concept;
    }

    public void setConcept(String concept) {
        this.concept = concept;
    }

    public String getObserver() {
        return observer;
    }

    public void setObserver(String observer) {
        this.observer = observer;
    }

    public Instant getObservationTimestamp() {
        return observationTimestamp;
    }

    public void setObservationTimestamp(Instant observationTimestamp) {
        this.observationTimestamp = observationTimestamp;
    }

    public UUID getVideoReferenceUuid() {
        return videoReferenceUuid;
    }

    public void setVideoReferenceUuid(UUID videoReferenceUuid) {
        this.videoReferenceUuid = videoReferenceUuid;
    }

    public UUID getImagedMomentUuid() {
        return imagedMomentUuid;
    }

    public void setImagedMomentUuid(UUID imagedMomentUuid) {
        this.imagedMomentUuid = imagedMomentUuid;
    }

    public Timecode getTimecode() {
        return timecode;
    }

    public void setTimecode(Timecode timecode) {
        this.timecode = timecode;
    }

    public Duration getElapsedTime() {
        return elapsedTime;
    }

    public void setElapsedTime(Duration elapsedTime) {
        this.elapsedTime = elapsedTime;
    }

    public Instant getRecordedTimestamp() {
        return recordedTimestamp;
    }

    public void setRecordedTimestamp(Instant recordedTimestamp) {
        this.recordedTimestamp = recordedTimestamp;
    }

    public Duration getDuration() {
        return duration;
    }

    public void setDuration(Duration duration) {
        this.duration = duration;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getActivity() {
        return activity;
    }

    public void setActivity(String activity) {
        this.activity = activity;
    }

    public List<Association> getAssociations() {
        return associations == null ? new ArrayList<>() : associations;
    }

    public void setAssociations(List<Association> associations) {
        List<Association> ass = associations == null ? new ArrayList<>() : associations;
        this.associations = Collections.unmodifiableList(ass);
    }

    public List<ImageReference> getImages() {
        return imageReferences;
    }

    public void setImages(List<ImageReference> images) {
        this.imageReferences = images;
    }

    @Override
    public String toString() {
        int a = associations == null ? 0 : associations.size();
        int b = imageReferences == null ? 0 : imageReferences.size();
        return "Annotation{" +
                "observationUuid=" + observationUuid +
                ", concept='" + concept + '\'' +
                ", elapsedTime=" + elapsedTime +
                ", numOfAssociations=" + a +
                ", numOfImages=" + b +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Annotation that = (Annotation) o;

        return observationUuid.equals(that.observationUuid);
    }

    @Override
    public int hashCode() {
        return observationUuid.hashCode();
    }

    public VideoIndex getVideoIndex() {
        return new VideoIndex(Optional.ofNullable(recordedTimestamp),
                Optional.ofNullable(elapsedTime),
                Optional.ofNullable(timecode));
    }
}
