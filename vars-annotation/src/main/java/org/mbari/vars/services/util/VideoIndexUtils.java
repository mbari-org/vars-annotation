package org.mbari.vars.services.util;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

import org.mbari.vars.services.model.Media;
import org.mbari.vcr4j.VideoIndex;
import org.mbari.vcr4j.time.Timecode;

public class VideoIndexUtils {

    /**
     * Calculates the duration between two VideoIndex(s). Delta is calcualted as (b - a)
     * @param a The first videoIndex
     * @param b The 2nd, and presumable larger, videoIndex
     * @return
     */
    public static Optional<Duration> diff(VideoIndex a, VideoIndex b) {
        Optional<Duration> delta = Optional.empty();
        if (a.getTimestamp().isPresent() && b.getTimestamp().isPresent()) {
            long millis = b.getTimestamp().get().toEpochMilli() - a.getTimestamp().get().toEpochMilli();
            delta = Optional.of(Duration.ofMillis(millis));
        }
        else if (a.getElapsedTime().isPresent() && b.getElapsedTime().isPresent()) {
            delta = Optional.of(b.getElapsedTime().get().minus(a.getElapsedTime().get()));
        }
        else if (a.getTimecode().isPresent() && b.getTimecode().isPresent()) {
            Timecode aa =  a.getTimecode().get();
            Timecode bb = b.getTimecode().get();
            if (aa.isComplete() && bb.isComplete()) {
                long millis = Math.round((bb.getSeconds() - aa.getSeconds()) * 1000L);
                delta = Optional.of(Duration.ofMillis(millis));
            }
        }
        return delta;
    }

    /**
     * Attempts to force resolution of missing fields in a video index. The following can occur:
     * - Elapsed time can be set if videoIndex has a timestamp
     * - Timestamp can be set if video has elapsed time.
     * @param videoIndex The video index into the media.
     * @param media The media that that the video index points into
     * @return The resolved video index.
     */
    public static VideoIndex resolve(VideoIndex videoIndex, Media media) {
        VideoIndex vi = videoIndex;

        if (videoIndex.getElapsedTime().isEmpty() && videoIndex.getTimestamp().isPresent()) {
            // Set the elpased time if needed
            Instant endTimestamp = media.getStartTimestamp().plus(media.getDuration());
            Instant timestamp = videoIndex.getTimestamp().get();
            if (timestamp.isBefore(endTimestamp)) {
                long millis = timestamp.toEpochMilli() - media.getStartTimestamp().toEpochMilli();
                if (millis >= 0) {
                    Optional<Duration> elapsedTime = Optional.of(Duration.ofMillis(millis));
                    vi = new VideoIndex(videoIndex.getTimestamp(), elapsedTime, vi.getTimecode());
                }
            }
        }
        else if (videoIndex.getTimestamp().isEmpty() && videoIndex.getElapsedTime().isPresent()) {
            // Set the timestamp if needed.
            Instant timestamp = media.getStartTimestamp().plus(videoIndex.getElapsedTime().get());
            vi = new VideoIndex(Optional.of(timestamp), videoIndex.getElapsedTime(), videoIndex.getTimecode());
        }
        return vi;
    }

    public static VideoIndex resolve(VideoIndex videoIndex, Media source, Media target) {
        VideoIndex vi = videoIndex;
        if (videoIndex.getTimestamp().isPresent()) {
            VideoIndex tmp = new VideoIndex(videoIndex.getTimestamp().get());
            vi = resolve(tmp, target);
        }
        else if (videoIndex.getElapsedTime().isPresent()) {
            VideoIndex viSource = resolve(new VideoIndex(videoIndex.getElapsedTime().get()), source);
            vi = resolve(viSource, source, target);
        }
        return vi;
    }
}
