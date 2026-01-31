package org.mbari.vars.annotation.ui.commands;

import org.mbari.vars.annosaurus.sdk.r1.models.Image;
import org.mbari.vars.annotation.etc.rxjava.EventBus;
import org.mbari.vars.annosaurus.sdk.r1.AnnotationService;
import org.mbari.vars.annotation.model.CreatedImageData;
import org.mbari.vars.annotation.model.ImageData;
import org.mbari.vars.annotation.ui.UIToolBox;
import org.mbari.vars.annotation.ui.javafx.ImageArchiveServiceDecorator;
import org.mbari.vars.annotation.ui.messages.ShowAlert;
import org.mbari.vars.annotation.ui.messages.ShowExceptionAlert;
import org.mbari.vars.annotation.ui.messages.ShowWarningAlert;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;

public class FramegrabUploadCmd implements Command {

    private final ImageData imageData;
    private volatile Image pngImageRef;
    private volatile Image jpgImageRef;

    public FramegrabUploadCmd(ImageData imageData) {
        this.imageData = imageData;
    }

    @Override
    public void apply(UIToolBox toolBox) {

        if (imageData != null) {
            final var media = toolBox.getData().getMedia();
            final var videoReferenceUuid = imageData.getVideoReferenceUuid();
            if (media.getVideoReferenceUuid() == videoReferenceUuid) {

                final var annotationService = toolBox.getServices().annotationService();

                ImageArchiveServiceDecorator decorator = new ImageArchiveServiceDecorator(toolBox);
                decorator.createImageFromExistingImageData(media, imageData, ImageArchiveServiceDecorator.ImageTypes.PNG)
                        .thenCompose(pngOpt -> {
                            if (pngOpt.isPresent()) {
                                CreatedImageData createdImageData = pngOpt.get();
                                pngImageRef = createdImageData.getImage();
                                return annotationService.createImage(createdImageData.getImage())
                                        .thenCompose(image -> decorator.createJpegWithOverlay(media,
                                                imageData,
                                                createdImageData.getImageUploadResults())
                                                .thenApply(jpgOpt -> {
                                                    jpgOpt.ifPresent(cid -> jpgImageRef = cid.getImage());
                                                    return jpgOpt;
                                                }));
                            }
                            else {
                                throw new RuntimeException("Failed to upload framgrab");
                            }
                        })
                        .whenComplete((opt, throwable) -> {
                            // refresh whether is succeeds or fails
                            boolean deleteImage = false;
                            ResourceBundle i18n = toolBox.getI18nBundle();
                            if (pngImageRef == null) {
                                String msg = i18n.getString("commands.framecapture.fail.noimage");
                                showWarningAlert(toolBox, msg, throwable);
                            }
                            decorator.refreshRelatedAnnotations(pngImageRef.getImageReferenceUuid(), deleteImage);
                        });


            }
        }
    }

    @Override
    public void unapply(UIToolBox toolBox) {

        AnnotationService annotationService = toolBox.getServices().annotationService();
        ImageArchiveServiceDecorator decorator = new ImageArchiveServiceDecorator(toolBox);
        EventBus eventBus = toolBox.getEventBus();
        List<CompletableFuture<?>> futures = new ArrayList<>();
        if (pngImageRef != null) {
            futures.add(annotationService.deleteImage(pngImageRef.getImageReferenceUuid()));
        }
        if (jpgImageRef != null) {
            futures.add(annotationService.deleteImage(jpgImageRef.getImageReferenceUuid()));
        }
        CompletableFuture[] futuresArray = futures.toArray(new CompletableFuture[futures.size()]);
        CompletableFuture.allOf(futuresArray)
                .thenAccept(v -> {
                    // The png is the first one created so it HAS to be present for the others to exist
                    // We only need this one to find all the annotations that were affected
                    if (pngImageRef != null) {
                        decorator.refreshRelatedAnnotations(pngImageRef.getImageReferenceUuid());
                    }
                });
    }

    @Override
    public String getDescription() {
        return "Uploaded existing image";
    }

    private void showWarningAlert(UIToolBox toolBox, String content) {
        showWarningAlert(toolBox, content, null);
    }


    private void showWarningAlert(UIToolBox toolBox, String content, Throwable throwable) {
        ResourceBundle i18n = toolBox.getI18nBundle();
        String title = i18n.getString("commands.framecapture.title");
        String header = i18n.getString("commands.framecapture.header");
        EventBus eventBus = toolBox.getEventBus();

        ShowAlert alert = (throwable == null) ?
                new ShowWarningAlert(title, header, content) :
                new ShowExceptionAlert(title, header, content, new RuntimeException(content, throwable));
        eventBus.send(alert);
    }
}
