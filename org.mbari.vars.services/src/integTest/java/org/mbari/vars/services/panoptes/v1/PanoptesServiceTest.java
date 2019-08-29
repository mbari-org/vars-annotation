package org.mbari.m3.vars.annotation.services.panoptes.v1;

import mbarix4j.net.URLUtilities;
import org.junit.Test;
import static org.junit.Assert.*;

import static org.mbari.vars.core.util.AsyncUtils.await;


import org.mbari.vars.services.ImageArchiveService;
import org.mbari.vars.services.TestToolbox;
import org.mbari.vars.services.model.ImageUploadResults;

import java.io.File;
import java.net.URL;
import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * @author Brian Schlining
 * @since 2017-09-06T10:57:00
 */
public class PanoptesServiceTest {

    private Duration timeout = Duration.ofMillis(45000);
    private ImageArchiveService service = TestToolbox.getServices().getImageArchiveService();

    @Test
    public void testUpload() {
        URL imageUrl = getClass().getResource("/images/00_02_25_20.png");
        File imageFile = URLUtilities.toFile(imageUrl);
        CompletableFuture<ImageUploadResults> f = service.upload("i2MAP", "9999",
                "00_02_25_20.png", imageFile.toPath());
        Optional<ImageUploadResults> results = await(f, timeout);
        assertTrue(results.isPresent());

    }

}
