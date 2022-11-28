package org.mbari.vars.services.noop;

import org.mbari.vars.services.ImageArchiveService;
import org.mbari.vars.services.model.ImageUploadResults;

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
