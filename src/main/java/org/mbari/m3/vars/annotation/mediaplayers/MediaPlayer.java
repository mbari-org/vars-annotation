package org.mbari.m3.vars.annotation.mediaplayers;

import org.mbari.m3.vars.annotation.model.Media;
import org.mbari.m3.vars.annotation.services.ImageCaptureService;
import org.mbari.vcr4j.VideoError;
import org.mbari.vcr4j.VideoIO;
import org.mbari.vcr4j.VideoIndex;
import org.mbari.vcr4j.VideoState;
import org.mbari.vcr4j.commands.VideoCommands;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;

/**
 * @author Brian Schlining
 * @since 2017-08-10T08:20:00
 */
public class MediaPlayer<S extends VideoState, E extends VideoError> extends org.mbari.vcr4j.VideoController<S, E> {

    private final ImageCaptureService imageCaptureService;
    private final Runnable shutdownHook;
    private final Media media;

    public MediaPlayer(Media media, ImageCaptureService imageCaptureService, VideoIO<S, E> videoIO) {
        this(media, imageCaptureService, videoIO, () -> {});
    }

    public MediaPlayer(Media media, ImageCaptureService imageCaptureService, VideoIO<S, E> io, Runnable shutdownHook) {
        super(io);
        this.media = media;
        this.imageCaptureService = imageCaptureService;
        this.shutdownHook = shutdownHook;
    }

    public ImageCaptureService getImageCaptureService() {
        return imageCaptureService;
    }

    /**
     * This may need to be overridden in some cases
     * @return
     */
    public String getConnectionID() {
        return getVideoIO().getConnectionID();
    }

    public CompletableFuture<Boolean> requestIsStopped() {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        getVideoIO().getStateObservable()
                .take(1)
                .forEach(s -> future.complete(s.isStopped()));
        getVideoIO().send(VideoCommands.REQUEST_STATUS);
        return future;
    }

    public CompletableFuture<Boolean> requestIsPlaying() {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        getVideoIO().getStateObservable()
                .take(1)
                .forEach(s -> future.complete(s.isPlaying()));
        getVideoIO().send(VideoCommands.REQUEST_STATUS);
        return future;
    }

    public CompletableFuture<VideoIndex> requestVideoIndex() {
        CompletableFuture<VideoIndex> future = new CompletableFuture<>();
        getVideoIO().getIndexObservable()
                .take(1)
                .forEach(future::complete);
        getVideoIO().send(VideoCommands.REQUEST_INDEX);
        return future;
    }

    public Media getMedia() {
        return media;
    }

    public void close() {
        try {
            shutdownHook.run();
            imageCaptureService.dispose();
            getVideoIO().close();
        }
        catch (Exception e) {
            Logger log = LoggerFactory.getLogger(getClass());
            log.warn("An exception was thrown while shutting down a MediaPlayer", e);
        }
    }



}