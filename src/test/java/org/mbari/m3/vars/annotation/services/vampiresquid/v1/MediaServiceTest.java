package org.mbari.m3.vars.annotation.services.vampiresquid.v1;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.mbari.m3.vars.annotation.Initializer;
import org.mbari.m3.vars.annotation.mediaplayers.ships.MediaParams;
import org.mbari.m3.vars.annotation.model.Media;
import org.mbari.m3.vars.annotation.services.MediaService;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @author Brian Schlining
 * @since 2018-01-03T11:56:00
 */
public class MediaServiceTest {
    MediaService mediaService = Initializer.getInjector().getInstance(VamService.class);

    // THis passes but we're turning it off so that we don't put
    // bogus values in the database
    @Ignore
    @Test
    public void testCreate() throws InterruptedException,
            ExecutionException,
            URISyntaxException,
            TimeoutException {
        Instant now = Instant.now();
        CompletableFuture<Media> f = mediaService.create("Test-01",
                "Test", "Test-01-" + now,
                new URI(MediaParams.URI_PREFIX + "Test-01"), now);
        Media media = f.get(5000, TimeUnit.MILLISECONDS);
        Assert.assertNotNull(media);
    }

    @Test
    public void test02() throws Exception {
        CompletableFuture<List<Media>> f = mediaService.findByVideoSequenceName("Ventana 3937");
        List<Media> media = f.get(5000, TimeUnit.MILLISECONDS);
        Assert.assertTrue(!media.isEmpty());
    }
}
