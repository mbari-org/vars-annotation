package org.mbari.m3.vars.annotation.services;

import org.mbari.m3.vars.annotation.model.Media;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * @author Brian Schlining
 * @since 2017-05-11T15:42:00
 */
public interface MediaService {

    CompletableFuture<Media> create();

    CompletableFuture<Media> findByUuid(UUID uuid);

    CompletableFuture<Media> findBySha512(byte[] sha512);

    CompletableFuture<List<Media>> findByVideoSequenceName(String videoSequenceName);

    CompletableFuture<List<Media>> findByVideoName(String videoName);

    CompletableFuture<List<String>> findAllVideoSequenceNames();

    CompletableFuture<List<Media>> findByCameraIdAndTimestamp(String cameraId, Instant timestamp);

    CompletableFuture<List<Media>> findByVideoSequenceNameAndTimestamp(String videoSequenceName, Instant timestamp);

    CompletableFuture<List<String>> findAllCameraIds();

    /**
     *
     * @return A list of VideoSequence names available for the camera id
     */
    CompletableFuture<List<String>> findVideoSequenceNamesByCameraId(String cameraId);

    CompletableFuture<List<String>> findVideoNamesByVideoSequenceName(String videoSequenceName);

}
