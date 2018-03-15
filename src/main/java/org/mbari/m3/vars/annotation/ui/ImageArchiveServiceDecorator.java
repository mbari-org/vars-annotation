package org.mbari.m3.vars.annotation.ui;

import javafx.collections.ObservableList;
import org.mbari.awt.image.ImageUtilities;
import org.mbari.m3.vars.annotation.EventBus;
import org.mbari.m3.vars.annotation.Initializer;
import org.mbari.m3.vars.annotation.UIToolBox;
import org.mbari.m3.vars.annotation.commands.CommandUtil;
import org.mbari.m3.vars.annotation.events.AnnotationsAddedEvent;
import org.mbari.m3.vars.annotation.events.AnnotationsChangedEvent;
import org.mbari.m3.vars.annotation.events.AnnotationsRemovedEvent;
import org.mbari.m3.vars.annotation.events.AnnotationsSelectedEvent;
import org.mbari.m3.vars.annotation.model.Annotation;
import org.mbari.m3.vars.annotation.model.Framegrab;
import org.mbari.m3.vars.annotation.model.ImageUploadResults;
import org.mbari.m3.vars.annotation.model.Media;
import org.mbari.m3.vars.annotation.services.AnnotationService;
import org.mbari.m3.vars.annotation.services.ImageArchiveService;
import org.mbari.vcr4j.VideoIndex;
import org.mbari.vcr4j.time.Timecode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.LineMetrics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author Brian Schlining
 * @since 2017-09-06T16:31:00
 */
public class ImageArchiveServiceDecorator {

    private static final DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("uuuuMMdd'T'HHmmss'Z'")
            .withZone(ZoneOffset.UTC);


    private final UIToolBox toolBox;
    private final ImageArchiveService imageArchiveService;
    private final String copyrightOwner;
    private final Logger log = LoggerFactory.getLogger(getClass());


    public ImageArchiveServiceDecorator(UIToolBox toolBox) {
        this.toolBox = toolBox;
        this.imageArchiveService = toolBox.getServices().getImageArchiveService();
        copyrightOwner = toolBox.getConfig().getString("app.image.copyright.owner");
    }

    public void compressFramegrab(Media media, Framegrab framegrab, ImageUploadResults imageUploadResults) {
        if (framegrab.getImage().isPresent() && framegrab.getVideoIndex().isPresent()) {
            Thread thread = new Thread(() -> {

                // -- Write image locally
                String[] overlayText = createOverlayText(copyrightOwner, framegrab, imageUploadResults);
                BufferedImage imageWithOverlay = createImageWithOverlay(framegrab.getImage().get(), overlayText);
                String name = buildName(media.getVideoReferenceUuid(), framegrab.getVideoIndex().get(), ".jpg");
                File localImageFile = buildLocalImageFile(media, ".jpg");
                try {
                    ImageUtilities.saveImage(imageWithOverlay, localImageFile);
                }
                catch (IOException e) {
                    log.error("Failed to save jpg image to " + localImageFile.getAbsolutePath(), e);
                }

                // Upload
                String deploymentId = CommandUtil.getDeploymentId(media);
                imageArchiveService.upload(media.getCameraId(), deploymentId, name, localImageFile.toPath())
                        .thenAccept(iur -> {
                            URL url;
                            try {
                                url = iur.getUri().toURL();
                            }
                            catch (MalformedURLException e) {
                                throw new RuntimeException("Image URI of " + iur.getUri() +
                                        " can not be converted to a URL", e);
                            }
                            createImageInDatastore(toolBox, media, framegrab, url)
                                .thenAccept(img ->   refreshRelatedAnnotations(toolBox, img.getImageReferenceUuid(), false))
                                .thenAccept(v -> localImageFile.delete());
                        });

            }, "Creating-JPG-" + Instant.now());

            thread.start();
        }
    }

    public static void refreshRelatedAnnotations(UIToolBox toolBox, UUID imageReferenceUuid, boolean deleteImage) {
        AnnotationService annotationService = toolBox.getServices().getAnnotationService();
        EventBus eventBus = toolBox.getEventBus();
        annotationService.findByImageReference(imageReferenceUuid)
                .thenAccept(annotations -> {
                    if (deleteImage) {
                        annotationService.deleteImage(imageReferenceUuid);
                    }
                    if (!annotations.isEmpty()) {
                        List<Annotation> annotations1 = new CopyOnWriteArrayList<>();
                        CompletableFuture[] completableFutures = annotations.stream()
                                .map(a -> annotationService.findByUuid(a.getObservationUuid()).thenApply(annotations1::add))
                                .toArray(CompletableFuture[]::new);
                        CompletableFuture.allOf(completableFutures)
                                .thenAccept(w -> {
                                    eventBus.send(new AnnotationsChangedEvent(annotations1));
                                    // Update any annotations that may have been effected by image deletion
//                                    List<Annotation> selectedAnnotations =
//                                            new ArrayList<>(toolBox.getData().getSelectedAnnotations());
//                                    eventBus.send(new AnnotationsRemovedEvent(annotations1));
//                                    eventBus.send(new AnnotationsAddedEvent(annotations1));
//                                    eventBus.send(new AnnotationsSelectedEvent(selectedAnnotations));
                                });
                    }
                });
    }

    public static CompletableFuture<org.mbari.m3.vars.annotation.model.Image> createImageInDatastore(UIToolBox toolBox, Media media, Framegrab framegrab, URL imageUrl) {
        Optional<java.awt.Image> awtImageOpt = framegrab.getImage();
        if (!awtImageOpt.isPresent()) {
            // TODO show alert that framecapture failed
            throw new RuntimeException("No image was captured");
        }
        else {
            String ext = parseExtension(imageUrl);
            String description = null;
            switch(ext) {
                case "jpg":
                    description = "compressed image";
                    break;
                case "png":
                    description = "uncompressed image";
                    break;
                default:
                    description = null;
            }
            java.awt.Image awtImage = awtImageOpt.get();
            BufferedImage bi = ImageUtilities.toBufferedImage(awtImage);
            org.mbari.m3.vars.annotation.model.Image image = new org.mbari.m3.vars.annotation.model.Image();
            image.setFormat("image/" + ext);
            image.setHeight(bi.getHeight());
            image.setWidth(bi.getWidth());
            image.setVideoReferenceUuid(media.getVideoReferenceUuid());
            image.setDescription(description);
            image.setUrl(imageUrl);

            // If no index use the current timestamp
            VideoIndex videoIndex = framegrab.getVideoIndex().orElse(new VideoIndex(Instant.now()));
            videoIndex.getElapsedTime().ifPresent(image::setElapsedTime);
            videoIndex.getTimecode().ifPresent(image::setTimecode);
            videoIndex.getTimestamp().ifPresent(image::setRecordedTimestamp);

            AnnotationService annotationService = toolBox.getServices().getAnnotationService();
            return annotationService.createImage(image);
        }
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
     * @param framegrab
     * @param imageUploadResults
     * @return  A string array of ext to be overlaid onto an image.
     */
    public static String[] createOverlayText(String copyrightOwner, Framegrab framegrab, ImageUploadResults imageUploadResults) {
        final String[] s = new String[4];
        Instant copyrightDate = getCopyrightDate(framegrab);
        int year = copyrightDate.atZone(ZoneId.of("UTC")).get(ChronoField.YEAR);
        imageUploadResults.getUri().toString().replace(".png", ".jpg");
        s[0] = "Copyright " + year + " " +  copyrightOwner;
        s[1] = imageUploadResults.getUri().toString();
        s[2] = copyrightDate.toString();
        s[3] = "";

        return s;
    }

    private static Instant getCopyrightDate(Framegrab framegrab) {
        AtomicReference<Instant> ref = new AtomicReference<>();
        framegrab.getVideoIndex().ifPresent(vi -> {
            if (vi.getTimestamp().isPresent()) {
                ref.set(vi.getTimestamp().get());
            }
            else {
                ref.set(Instant.now());
            }
        });
        return ref.get();
    }

    public static File buildLocalImageFile(Media media, String ext) {
        try {
            String deploymentKey = media.getVideoName() + "-" + media.getVideoReferenceUuid();
            String filename = deploymentKey + "-" + Instant.now() + ext;
            Path path = Paths.get(Initializer.getImageDirectory().toString(),
                filename);
            return path.toFile();
//            return File.createTempFile(deploymentKey + "-", ext,
//                    Initializer.getImageDirectory().toFile());
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
        if (timecode.isPresent()) {
            idx = timecode.get().toString().replace(':', '_');
        }
        else if (elapsedTime.isPresent()) {
            idx = elapsedTime.get().toMillis() + "";
        }
        else {
            idx = timeFormat.format(Instant.now());
        }
        return idx + "-" + videoReferenceUuid + ext;
    }

}
