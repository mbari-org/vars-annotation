package org.mbari.vars.annotation.it.services.panoptes;




import static org.mbari.vars.annotation.etc.rxjava.AsyncUtils.await;


import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.mbari.vars.annosaurus.sdk.r1.models.ImageUploadResults;
import org.mbari.vars.annotation.model.ImageData;
import org.mbari.vars.annotation.services.ImageArchiveService;
import org.mbari.vars.annotation.it.services.TestToolbox;
import org.mbari.vars.annotation.it.util.URLUtilities;
import org.mbari.vcr4j.VideoIndex;

import java.io.File;
import java.net.URL;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * @author Brian Schlining
 * @since 2017-09-06T10:57:00
 */
public class PanoptesServiceTest {

    private final Duration timeout = Duration.ofMillis(45000);
    private final ImageArchiveService service = TestToolbox.getImageArchiveService();

    @Test
    public void testJpgUpload() {
        URL imageUrl = getClass().getResource("/images/20191120T221422Z--be9152f9-cb21-4d4f-8b40-00085ebba626.jpg");
        File imageFile = URLUtilities.toFile(imageUrl);
        CompletableFuture<ImageUploadResults> f = service.upload("i2MAP", "9999",
                "00_02_25_20.png", imageFile.toPath());
        Optional<ImageUploadResults> results = await(f, timeout);
        assertTrue(results.isPresent());
        System.out.println(results.get());
    }

    @Test
    public void testPngUploadFile() {
        URL imageUrl = getClass().getResource("/images/Ventana 3657 20111018T165317Z--d221b3f7-a9cc-4c31-ba29-86aaec9a11df--20250217T184259.137Z.png");
        File imageFile = URLUtilities.toFile(imageUrl);
        CompletableFuture<ImageUploadResults> f = service.upload("Ventana", "Ventana 3657",
                "Ventana 3657 20111018T165317Z--d221b3f7-a9cc-4c31-ba29-86aaec9a11df--20250217T184259.137Z.png", imageFile.toPath());
        Optional<ImageUploadResults> results = await(f, timeout);
        assertTrue(results.isPresent());
        System.out.println(results.get());
    }

    @Test
    public void testPngUploadBytes() {
        URL imageUrl = getClass().getResource("/images/Ventana 3657 20111018T165317Z--d221b3f7-a9cc-4c31-ba29-86aaec9a11df--20250217T184259.137Z.png");
        File imageFile = URLUtilities.toFile(imageUrl);
        var imageData = ImageData.from(UUID.randomUUID(), new VideoIndex(Instant.now()), imageFile.toPath());
        var pngBytes = imageData.getPngBytes();
        CompletableFuture<ImageUploadResults> f = service.upload("Ventana", "Ventana 3657",
                "Ventana 3657 20111018T165317Z--d221b3f7-a9cc-4c31-ba29-86aaec9a11df--20250217T184259.137Z.png", pngBytes);
        Optional<ImageUploadResults> results = await(f, timeout);
        assertTrue(results.isPresent());
        System.out.println(results.get());
    }

}