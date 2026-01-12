package org.mbari.vars.services.model;

import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

/**
 * @author Brian Schlining
 * @since 2017-05-11T13:27:00
 */
public class Media {
    private UUID videoSequenceUuid;
    private UUID videoReferenceUuid;
    private UUID videoUuid;
    private String videoSequenceName;
    private String cameraId;
    private URI uri;
    private Instant startTimestamp;
    private Duration durationMillis;
    private String container;
    private Integer width;
    private Integer height;
    private Double frameRate;
    private Long sizeBytes;
    private byte[] sha512;
    private String videoCodec;
    private String audioCodec;
    private String description;
    private String videoName;
    private String videoDescription;
    private String videoSequenceDescription;

    public Media() {
    }

    /**
     * Copy constructor
     * @param media
     */
    public Media(Media media) {
        videoSequenceUuid = media.videoSequenceUuid;
        videoReferenceUuid = media.videoReferenceUuid;
        videoUuid = media.videoUuid;
        videoSequenceName = media.videoSequenceName;
        cameraId = media.cameraId;
        uri = media.uri;
        startTimestamp = media.startTimestamp;
        durationMillis = media.durationMillis;
        container = media.container;
        width = media.width;
        height = media.height;
        frameRate = media.frameRate;
        sizeBytes = media.sizeBytes;
        sha512 = Arrays.copyOf(media.sha512, media.sha512.length);
        videoCodec = media.videoCodec;
        audioCodec = media.audioCodec;
        description = media.description;
        videoName = media.videoName;
        videoDescription = media.videoDescription;
        videoSequenceDescription = media.videoSequenceDescription;
    }

    public String getVideoCodec() {
        return videoCodec;
    }

    public void setVideoCodec(String videoCodec) {
        this.videoCodec = videoCodec;
    }

    public String getAudioCodec() {
        return audioCodec;
    }

    public void setAudioCodec(String audioCodec) {
        this.audioCodec = audioCodec;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getVideoDescription() {
        return videoDescription;
    }

    public void setVideoDescription(String videoDescription) {
        this.videoDescription = videoDescription;
    }

    public String getVideoSequenceDescription() {
        return videoSequenceDescription;
    }

    public void setVideoSequenceDescription(String videoSequenceDescription) {
        this.videoSequenceDescription = videoSequenceDescription;
    }

    public UUID getVideoSequenceUuid() {
        return videoSequenceUuid;
    }

    public void setVideoSequenceUuid(UUID videoSequenceUuid) {
        this.videoSequenceUuid = videoSequenceUuid;
    }

    public UUID getVideoReferenceUuid() {
        return videoReferenceUuid;
    }

    public void setVideoReferenceUuid(UUID videoReferenceUuid) {
        this.videoReferenceUuid = videoReferenceUuid;
    }

    public UUID getVideoUuid() {
        return videoUuid;
    }

    public void setVideoUuid(UUID videoUuid) {
        this.videoUuid = videoUuid;
    }

    public String getVideoSequenceName() {
        return videoSequenceName;
    }

    public void setVideoSequenceName(String videoSequenceName) {
        this.videoSequenceName = videoSequenceName;
    }

    public String getCameraId() {
        return cameraId;
    }

    public void setCameraId(String cameraId) {
        this.cameraId = cameraId;
    }

    public URI getUri() {
        return uri;
    }

    public void setUri(URI uri) {
        this.uri = uri;
    }

    public Instant getStartTimestamp() {
        return startTimestamp;
    }

    public void setStartTimestamp(Instant startTimestamp) {
        this.startTimestamp = startTimestamp;
    }

    public Duration getDuration() {
        return durationMillis;
    }

    public void setDuration(Duration duration) {
        this.durationMillis = duration;
    }

    public String getContainer() {
        return container;
    }

    public void setContainer(String container) {
        this.container = container;
    }

    public Integer getWidth() {
        return width;
    }

    public void setWidth(Integer width) {
        this.width = width;
    }

    public Integer getHeight() {
        return height;
    }

    public void setHeight(Integer height) {
        this.height = height;
    }

    public Double getFrameRate() {
        return frameRate;
    }

    public void setFrameRate(Double frameRate) {
        this.frameRate = frameRate;
    }

    public Long getSizeBytes() {
        return sizeBytes;
    }

    public void setSizeBytes(Long sizeBytes) {
        this.sizeBytes = sizeBytes;
    }

    public byte[] getSha512() {
        return sha512;
    }

    public void setSha512(byte[] sha512) {
        this.sha512 = sha512;
    }

    public String getVideoName() {
        return videoName;
    }

    public void setVideoName(String videoName) {
        this.videoName = videoName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (videoReferenceUuid == null) return false;

        Media media = (Media) o;

        return videoReferenceUuid.equals(media.videoReferenceUuid);
    }

    @Override
    public int hashCode() {
        return videoReferenceUuid.hashCode();
    }

    public Optional<Duration> toMediaElapsedTime(Annotation annotation) {
        if (annotation.getVideoReferenceUuid().equals(videoReferenceUuid) && annotation.getElapsedTime() != null) {
            return Optional.of(annotation.getElapsedTime());
        }
        else if (annotation.getRecordedTimestamp() != null && startTimestamp != null) {
            return Optional.of(Duration.between(startTimestamp, annotation.getRecordedTimestamp()));
        }
        return Optional.empty();
    }
}
