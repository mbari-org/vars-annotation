package org.mbari.m3.vars.annotation.model;

import org.mbari.vcr4j.VideoIndex;

import java.util.Optional;

/**
 * @author Brian Schlining
 * @since 2017-08-31T15:03:00
 */
public class Framegrab {
    private Optional<java.awt.Image> image = Optional.empty();
    private Optional<VideoIndex> videoIndex = Optional.empty();

    public Framegrab() {}

    public Framegrab(java.awt.Image image) {
        this.image = Optional.ofNullable(image);
    }

    public Framegrab(java.awt.Image image, VideoIndex videoIndex) {
        this.image = Optional.ofNullable(image);
        this.videoIndex = Optional.ofNullable(videoIndex);
    }

    public Optional<java.awt.Image> getImage() {
        return image;
    }

    public Optional<VideoIndex> getVideoIndex() {
        return videoIndex;
    }

    public void setImage(java.awt.Image image) {
        this.image = Optional.ofNullable(image);
    }

    public void setVideoIndex(VideoIndex videoIndex) {
        this.videoIndex = Optional.ofNullable(videoIndex);
    }
}
