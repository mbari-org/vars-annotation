package org.mbari.vars.services.panoptes.v1;

import org.junit.Test;
import static org.junit.Assert.*;

import static org.mbari.vars.core.util.AsyncUtils.await;


import org.mbari.vars.services.ImageArchiveService;
import org.mbari.vars.services.TestToolbox;
import org.mbari.vars.services.URLUtilities;
import org.mbari.vars.services.model.ImageUploadResults;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * @author Brian Schlining
 * @since 2017-09-06T10:57:00
 */
public class PanoptesServiceTest {

    private final Duration timeout = Duration.ofMillis(45000);
    private final ImageArchiveService service = TestToolbox.getServices().getImageArchiveService();

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
    public void testPngUpload() {
        URL imageUrl = getClass().getResource("/images/Ventana 3657 20111018T165317Z--d221b3f7-a9cc-4c31-ba29-86aaec9a11df--20250217T184259.137Z.png");
        File imageFile = URLUtilities.toFile(imageUrl);
        CompletableFuture<ImageUploadResults> f = service.upload("Ventana", "Ventana 3657",
                "Ventana 3657 20111018T165317Z--d221b3f7-a9cc-4c31-ba29-86aaec9a11df--20250217T184259.137Z.png", imageFile.toPath());
        Optional<ImageUploadResults> results = await(f, timeout);
        assertTrue(results.isPresent());
        System.out.println(results.get());

    }

}
