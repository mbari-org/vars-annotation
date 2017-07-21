package org.mbari.m3.vars.annotation.services;

import org.mbari.vcr4j.VideoError;
import org.mbari.vcr4j.VideoIO;
import org.mbari.vcr4j.VideoIndex;
import org.mbari.vcr4j.VideoState;
import org.mbari.vcr4j.commands.VideoCommands;

import java.util.concurrent.CompletableFuture;

/**
 * @author Brian Schlining
 * @since 2017-07-20T16:18:00
 */
public class VideoControlService<S extends VideoState, E extends VideoError> extends org.mbari.vcr4j.VideoController<S, E> {

    private final ImageCaptureService imageCaptureService;

    public VideoControlService(ImageCaptureService imageCaptureService, VideoIO<S, E> videoIO) {
        super(videoIO);
        this.imageCaptureService = imageCaptureService;
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

    public CompletableFuture<Boolean> isStopped() {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        getVideoIO().getStateObservable().take(1).forEach(s -> future.complete(s.isStopped()));
        getVideoIO().send(VideoCommands.REQUEST_STATUS);
        return future;
    }

    public CompletableFuture<Boolean> isPlaying() {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        getVideoIO().getStateObservable()
                .take(1)
                .forEach(s -> future.complete(s.isPlaying()));
        getVideoIO().send(VideoCommands.REQUEST_STATUS);
        return future;
    }

    public CompletableFuture<VideoIndex> getVideoIndex() {
        CompletableFuture<VideoIndex> future = new CompletableFuture<>();
        getVideoIO().getIndexObservable()
                .take(1)
                .forEach(future::complete);
        getVideoIO().send(VideoCommands.REQUEST_INDEX);
        return future;
    }

    public void close() {
        getVideoIO().close();
        imageCaptureService.dispose();
    }


}