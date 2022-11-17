package org.mbari.vars.ui.javafx;


import org.mbari.vars.core.EventBus;
import org.mbari.vars.ui.Initializer;
import org.mbari.vars.ui.UIToolBox;
import org.mbari.vars.ui.commands.CommandUtil;
import org.mbari.vars.ui.events.AnnotationsChangedEvent;
import org.mbari.vars.services.model.*;
import org.mbari.vars.services.AnnotationService;
import org.mbari.vars.core.util.AsyncUtils;
import org.mbari.vcr4j.VideoIndex;
import org.mbari.vcr4j.time.Timecode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.awt.Image;
import java.awt.font.FontRenderContext;
import java.awt.font.LineMetrics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.util.*;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * @author Brian Schlining
 * @since 2017-09-06T16:31:00
 */
public class ImageArchiveServiceDecorator {

    private static final DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("uuuuMMdd'T'HHmmss.SSS'Z'")
            .withZone(ZoneOffset.UTC);


    private final UIToolBox toolBox;
    private final String copyrightOwner;
    private final Logger log = LoggerFactory.getLogger(getClass());
    private final Duration timeout;

    public enum ImageTypes {
        JPG("jpg"), PNG("png");

        public final String extension;
        ImageTypes(String extension) {
            this.extension = extension;
        }
    }


    public ImageArchiveServiceDecorator(UIToolBox toolBox) {
        this.toolBox = toolBox;
        copyrightOwner = toolBox.getConfig().getString("app.image.copyright.owner");
        timeout = toolBox.getConfig().getDuration("annotation.service.timeout");
    }

    public CompletableFuture<Optional<CreatedImageData>> createJpegWithOverlay(Media media,
                                                                               ImageData imageData,
                                                                               ImageUploadResults imageUploadResults) {

        return createImageWithOverlay(imageData, imageUploadResults)
                .thenCompose(idWithOverlay -> createImageFromExistingImageData(media, idWithOverlay, ImageTypes.JPG));
    }

    private CompletableFuture<ImageData> createImageWithOverlay(ImageData imageData, ImageUploadResults imageUploadResults) {
        return CompletableFuture.supplyAsync(() -> {
            // -- Write image locally
            String[] overlayText = createOverlayText(copyrightOwner, imageData, imageUploadResults);
            BufferedImage imageWithOverlay = createImageWithOverlay(imageData.getBufferedImage(), overlayText);
            return new ImageData(imageData.getVideoReferenceUuid(), imageData.getVideoIndex(), imageWithOverlay);
        });
    }

    public CompletableFuture<Optional<CreatedImageData>> createImageFromExistingImageData(Media media, ImageData imageData, ImageTypes type) {

        CreatedImageData createdImageData = new CreatedImageData();

        String name = ImageArchiveServiceDecorator.buildName(media.getVideoReferenceUuid(), imageData.getVideoIndex(), type.extension);
        String deploymentId = CommandUtil.getDeploymentId(media);

        var bytes = switch(type) {
            case JPG -> imageData.getJpegBytes();
            case PNG -> imageData.getPngBytes();
        };

        log.atDebug().log(() -> "Creating image for " + media.getUri() + " at " + imageData.getVideoIndex() +
                " as " + type.extension + " with byte[" + bytes.length + "]");

        CompletableFuture<org.mbari.vars.services.model.Image> future = toolBox.getServices()
                .getImageArchiveService()
                .upload(media.getCameraId(), deploymentId, name, bytes)
                .thenCompose(imageUploadResults -> {
                    createdImageData.setImageUploadResults(imageUploadResults);
                    return createImageInDatastore(imageData, toUrl(imageUploadResults.getUri()));
                });

        return future.thenApply(image -> {
            createdImageData.setImage(image);
            return Optional.of(createdImageData);
        });

    }

    private URL toUrl(URI uri) {
        try {
            return uri.toURL();
        }
        catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }


    public void refreshRelatedAnnotations(UUID imageReferenceUuid) {
        refreshRelatedAnnotations(imageReferenceUuid, false);
    }


    public void refreshRelatedAnnotations(UUID imageReferenceUuid, boolean deleteImage) {
        final AnnotationService annotationService = toolBox.getServices().getAnnotationService();
        final EventBus eventBus = toolBox.getEventBus();
        toolBox.getExecutorService().submit(() -> {
            try {

                // Blocking call
                List<Annotation> affectedAnnotations = annotationService.findByImageReference(imageReferenceUuid)
                        .get(timeout.toMillis(), TimeUnit.MILLISECONDS);

                if (deleteImage) {
                    annotationService.deleteImage(imageReferenceUuid)
                            .thenCompose(b -> {
                                List<UUID> uuids = affectedAnnotations.stream()
                                        .map(Annotation::getObservationUuid)
                                        .collect(Collectors.toList());
                                return AsyncUtils.collectAll(uuids, annotationService::findByUuid);
                            })
                            .thenAccept(annotations -> eventBus.send(new AnnotationsChangedEvent(annotations)));
                } else {
                    eventBus.send(new AnnotationsChangedEvent(affectedAnnotations));
                }

            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

    }

    public CompletableFuture<org.mbari.vars.services.model.Image> createImageInDatastore(ImageData imageData, URL imageUrl) {


        // FIXME - Taking a framegrab of annotation with existing framegrab will
        // show user a warning. The new framegrab is actually uploaded to the
        // server, but when it tries to create a new Image with a URL identical
        // to one already in the database, the database will throw an error.
        // Also the image metadata is not getting updated either as the insert fails

        CompletableFuture<org.mbari.vars.services.model.Image> readImageFuture = CompletableFuture.supplyAsync(() -> {
            String ext = parseExtension(imageUrl);
            String description = null;
            switch (ext) {
                case "jpg":
                    description = "compressed image";
                    break;
                case "png":
                    description = "uncompressed image";
                    break;
                default:
                    description = null;
            }
            BufferedImage bi = imageData.getBufferedImage();
            org.mbari.vars.services.model.Image image = new org.mbari.vars.services.model.Image();
            image.setFormat("image/" + ext);
            image.setHeight(bi.getHeight());
            image.setWidth(bi.getWidth());
            image.setVideoReferenceUuid(imageData.getVideoReferenceUuid());
            image.setDescription(description);
            image.setUrl(imageUrl);

            VideoIndex videoIndex = imageData.getVideoIndex();
            videoIndex.getElapsedTime().ifPresent(image::setElapsedTime);
            videoIndex.getTimecode().ifPresent(image::setTimecode);
            videoIndex.getTimestamp().ifPresent(image::setRecordedTimestamp);
            return image;
        });

        final AnnotationService annotationService = toolBox.getServices().getAnnotationService();
        return readImageFuture.thenCompose(annotationService::createImage);

    }

    private static String parseExtension(URL url) {
        String[] parts = url.toExternalForm().split("\\.");
        return parts[parts.length - 1].toLowerCase();
    }



    /**
     * Add overlay text to the image and save as a .jpg file.
     *
     * @param  image        a java.awt.Image to add the text overlay to
     * @param  overlayText  The text to overlay onto the image
     * @return
     */
    public static BufferedImage createImageWithOverlay(final Image image, final String[] overlayText) {

        // Copy BufferedImage and set .jpg file name
        final BufferedImage bi = new BufferedImage(image.getWidth(null), image.getHeight(null),
                BufferedImage.TYPE_INT_RGB);
        Graphics2D g = (Graphics2D) bi.getGraphics();
        g.drawImage(image, 0, 0, null);
        final Font font = new Font("Monospaced", Font.PLAIN, 14);
        g.setFont(font);
        g.setColor(Color.CYAN);
        final FontRenderContext frc = g.getFontRenderContext();
        int x = 1;
        int n = 1;
        for (String s : overlayText) {
            LineMetrics lineMetrics = font.getLineMetrics(s, frc);
            float y = (lineMetrics.getHeight() + 1) * n + lineMetrics.getHeight();
            g.drawString(s, x, y);
            n++;
        }

        g.dispose();

        return bi;
    }

    /**
     * Creates the textual overlay for the preview image
     * @param copyrightOwner
     * @param imageData
     * @param imageUploadResults
     * @return  A string array of ext to be overlaid onto an image.
     */
    public static String[] createOverlayText(String copyrightOwner, ImageData imageData, ImageUploadResults imageUploadResults) {
        final String[] s = new String[4];
        Instant copyrightDate = getCopyrightDate(imageData);
        int year = copyrightDate.atZone(ZoneId.of("UTC")).get(ChronoField.YEAR);
        imageUploadResults.getUri().toString().replace(".png", ".jpg");
        s[0] = "Copyright " + year + " " +  copyrightOwner;
        s[1] = imageUploadResults.getUri().toString();
        s[2] = copyrightDate.toString();
        s[3] = "";

        return s;
    }

    private static Instant getCopyrightDate(ImageData imageData) {
        var vi = imageData.getVideoIndex();
        if (vi.getTimestamp().isPresent()) {
            return vi.getTimestamp().get();
        }
        else {
            return Instant.now();
        }
    }

    public static File buildLocalImageFile(Media media, String ext) {
        try {
            String deploymentKey = media.getVideoName() + "--" + media.getVideoReferenceUuid();
            String filename = deploymentKey + "--" + timeFormat.format(Instant.now()) + ext;
            //String filename = deploymentKey + "-" + Instant.now() + ext;
            Path path = Paths.get(Initializer.getImageDirectory().toString(),
                filename);
            return path.toFile();
        }
        catch (Exception e) {
            throw new RuntimeException("Failed to build a local image temp file", e);
        }
    }

    public static String buildName(UUID videoReferenceUuid, VideoIndex videoIndex, String ext) {

        if (!ext.startsWith(".")) {
            ext = "." + ext;
        }

        // index - uuid
        Optional<Timecode> timecode = videoIndex.getTimecode();
        Optional<Duration> elapsedTime = videoIndex.getElapsedTime();
        Optional<Instant> timestamp = videoIndex.getTimestamp();
        String idx;

        if (timestamp.isPresent()) {
            idx = timeFormat.format(timestamp.get());
        }
        else if (elapsedTime.isPresent()) {
            idx = elapsedTime.get().toMillis() + "";
        }
        else if (timecode.isPresent()) {
            idx = timecode.get().toString().replace(':', '_');
        }
        else {
            idx = timeFormat.format(Instant.now());
        }
        return idx + "--" + videoReferenceUuid + ext;
    }

}
