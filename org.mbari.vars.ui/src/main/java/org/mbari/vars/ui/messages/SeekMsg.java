package org.mbari.vars.ui.messages;

import org.mbari.vars.core.EventBus;
import org.mbari.vars.services.model.Annotation;
import org.mbari.vars.services.model.Media;

/**
 * @author Brian Schlining
 * @since 2017-06-02T11:49:00
 */
public class SeekMsg<T> {

    private final T index;

    public SeekMsg(T index) {
        this.index = index;
    }

    public T getIndex() {
        return index;
    }

    /**
     * Generic seek logic. Takes a media and an annotation, determines the correct
     * index to seek to, then publishes a seek event.
     * @param media The media that is currently playing and that we want to seek into
     * @param annotation The annotation that we want to seek to
     * @param eventBus Where the SeekMsg will be published
     */
    public static void seek(Media media, Annotation annotation, EventBus eventBus) {
        if (media != null && annotation != null) {
            // If annotation is on it's native media, just use it's native index
            if (annotation.getVideoReferenceUuid().equals(media.getVideoReferenceUuid())) {
                if (annotation.getTimecode() != null) {
                    eventBus.send(new SeekMsg<>(annotation.getTimecode()));
                } else if (annotation.getElapsedTime() != null) {
                    eventBus.send(new SeekMsg<>(annotation.getElapsedTime()));
                } else if (annotation.getRecordedTimestamp() != null) {
                    eventBus.send(new SeekMsg<>(annotation.getRecordedTimestamp()));
                }
            }
            else {
                // If not on the native media use the recordedTimestamp if available
                // Otherwise fall back to elasped time (although this could jump
                // to wrong point if video files have different start times)
                if (annotation.getRecordedTimestamp() != null) {
                    eventBus.send(new SeekMsg<>(annotation.getRecordedTimestamp()));
                }
                else if (annotation.getElapsedTime() != null) {
                    eventBus.send(new SeekMsg<>(annotation.getElapsedTime()));
                }
            }
        }
    }
}
