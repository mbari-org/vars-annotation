package org.mbari.m3.vars.annotation.services;

import org.mbari.m3.vars.annotation.model.ImageUploadResults;

import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

/**
 * @author Brian Schlining
 * @since 2017-08-31T14:05:00
 */
public interface ImageArchiveService {

    CompletableFuture<ImageUploadResults> upload(String cameraId, String deploymentId, String name, Path image);

    CompletableFuture<ImageUploadResults> locate(String cameraId, String deploymentId, String name);

}
