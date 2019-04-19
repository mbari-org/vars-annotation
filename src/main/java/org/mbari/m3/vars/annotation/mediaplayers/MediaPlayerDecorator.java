package org.mbari.m3.vars.annotation.mediaplayers;

import org.mbari.m3.vars.annotation.EventBus;
import org.mbari.m3.vars.annotation.UIToolBox;
import org.mbari.m3.vars.annotation.messages.SeekMsg;
import org.mbari.m3.vars.annotation.model.Annotation;
import org.mbari.vcr4j.VideoIndex;
import org.mbari.vcr4j.time.Timecode;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * @author Brian Schlining
 * @since 2019-04-19T13:44:00
 */
public class MediaPlayerDecorator {

    private final UIToolBox toolBox;

    public MediaPlayerDecorator(UIToolBox toolBox) {
        this.toolBox = toolBox;
    }

    public CompletableFuture<Optional<Annotation>> seekNextAnnotation() {
       return findAnnotation(true)
               .thenApply(a -> {
                   a.map(Annotation::getVideoIndex).ifPresent(this::seekByVideoIndex);
                   return a;
               });
    }

    public CompletableFuture<Optional<Annotation>> seekPreviousAnnotation() {
        return findAnnotation(false)
                .thenApply(a -> {
                    a.map(Annotation::getVideoIndex).ifPresent(this::seekByVideoIndex);
                    return a;
                });
    }

    public CompletableFuture<Optional<Annotation>> findAnnotation(boolean next) {
        final List<Annotation> annotations = new ArrayList<>(toolBox.getData().getAnnotations());
        // TODO Whoops! Have to sort the annotation in place here. Make cmp a class instance var.
        return toolBox.getMediaPlayer().requestVideoIndex()
                .thenApply(vi -> {
                    int i = -1;
                    if (vi.getTimecode().isPresent()) {
                        i = seekByTimecode(annotations, vi.getTimecode().get());
                    }
                    else if (vi.getElapsedTime().isPresent()) {
                        i = seekByElapsedTime(annotations, vi.getElapsedTime().get());
                    }
                    return i;
                })
                .thenApply(i -> {
                    int j = -1;
                    if (i < -1) {
                        j =  (next) ? -i : -i - 1;
                    }
                    else if (i > -1) {
                        j =  (next) ? i + 1 : i - 1;
                    }

                    Optional<Annotation> a = Optional.empty();
                    if (j > -1 && j < annotations.size()) {
                        a = Optional.of(annotations.get(j));
                    }
                    return a;
                });
    }




    private int seekByTimecode(List<Annotation> annotations, Timecode timecode) {
        Comparator<Annotation> cmp = (a, b) -> {
            String as = a.getVideoIndex()
                    .getTimecode()
                    .map(Object::toString)
                    .orElse(Timecode.EMPTY_TIMECODE_STRING);
            String bs = b.getVideoIndex()
                    .getTimecode()
                    .map(Object::toString)
                    .orElse(Timecode.EMPTY_TIMECODE_STRING);
            return as.compareTo(bs);
        };

        Annotation key = new Annotation();
        key.setTimecode(timecode);

        annotations.sort(cmp);
        return Collections.binarySearch(annotations, key, cmp);

    }

    private int seekByElapsedTime(List<Annotation> annotations, Duration elapsedTime) {
        Comparator<Annotation> cmp = (a, b) -> {
            Duration as = a.getVideoIndex()
                    .getElapsedTime()
                    .orElse(Duration.ZERO);
            Duration bs = b.getVideoIndex()
                    .getElapsedTime()
                    .orElse(Duration.ZERO);
            return as.compareTo(bs);
        };

        Annotation key = new Annotation();
        key.setElapsedTime(elapsedTime);

        annotations.sort(cmp);
        return Collections.binarySearch(annotations, key, cmp);

    }

    public void seekByVideoIndex(VideoIndex videoIndex) {
        EventBus eventBus = toolBox.getEventBus();
        if (videoIndex.getTimecode().isPresent()) {
            eventBus.send(new SeekMsg<>(videoIndex.getTimecode()));
        }
        else if (videoIndex.getElapsedTime().isPresent()) {
            eventBus.send(new SeekMsg<>(videoIndex.getElapsedTime()));
        }
        else if (videoIndex.getTimestamp().isPresent()) {
            eventBus.send(new SeekMsg<>(videoIndex.getTimestamp()));
        }
    }
}
