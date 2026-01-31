package org.mbari.vars.annotation.services.noop;

import org.mbari.vars.annotation.services.ImageArchiveService;

import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

public class NoopImageArchiveService implements ImageArchiveService {
    @Override
    public CompletableFuture<ImageUploadResults> upload(String cameraId, String deploymentId, String name, Path image) {
        return CompletableFuture.failedFuture(new UnsupportedOperationException());
    }

    @Override
    public CompletableFuture<ImageUploadResults> locate(String cameraId, String deploymentId, String name) {
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<ImageUploadResults> upload(String cameraId, String deploymentId, String name, byte[] imageByes) {
        return CompletableFuture.failedFuture(new UnsupportedOperationException());
    }
}
