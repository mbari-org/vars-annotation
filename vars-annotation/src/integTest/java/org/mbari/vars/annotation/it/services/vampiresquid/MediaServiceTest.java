package org.mbari.vars.annotation.it.services.vampiresquid;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.mbari.vars.vampiresquid.sdk.r1.MediaService;
import org.mbari.vars.annotation.it.services.TestToolbox;
import org.mbari.vars.vampiresquid.sdk.r1.models.Media;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @author Brian Schlining
 * @since 2018-01-03T11:56:00
 */
public class MediaServiceTest {
    MediaService mediaService = TestToolbox.getMediaService();

    private byte[] randomSha512() {
        var b = new byte[64];
        new Random().nextBytes(b);
        return b;
    }

    // This passes but we're turning it off so that we don't put
    // bogus values in the database
    @Test
    public void testCreate() throws InterruptedException,
            ExecutionException,
            URISyntaxException,
            TimeoutException {
        Instant now = Instant.now();
        CompletableFuture<Media> f = mediaService.create("Test-01",
                "Test", "Test-01-" + now,
                new URI("urn:rtva:org.mbari:" + "Test-01"), now);
        Media media = f.get(5000, TimeUnit.MILLISECONDS);
        assertNotNull(media);
    }

    @Test
    public void testCreateMedia() throws Exception {
        Instant now = Instant.now();
        var media = new Media();
        media.setVideoSequenceName("Test-02");
        media.setCameraId("Test");
        media.setVideoName("Test-02-" + now);
        media.setUri(URI.create("http://www.foo.bar/test/Test-02-" + now.toEpochMilli() + ".mp4"));
        media.setStartTimestamp(now);
        media.setSha512(randomSha512());
        media.setFrameRate(24.0);
        var f = mediaService.create(media);
        var m = f.get(5000, TimeUnit.MILLISECONDS);
        assertNotNull(m);
        assertEquals(media.getVideoSequenceName(), m.getVideoSequenceName());
        assertEquals(media.getCameraId(), m.getCameraId());
        assertEquals(media.getVideoName(), m.getVideoName());
        assertEquals(media.getStartTimestamp(), m.getStartTimestamp());
        assertEquals(media.getUri(), m.getUri());
        assertNotNull(m.getVideoSequenceUuid());
        assertNotNull(m.getVideoUuid());
        assertNotNull(m.getVideoReferenceUuid());
        assertEquals(media.getFrameRate(), m.getFrameRate());
    }

    @Test
    public void testUpdate() throws Exception {
        Instant now = Instant.now();
        var media = new Media();
        media.setVideoSequenceName("Test-03");
        media.setCameraId("Test");
        media.setVideoName("Test-03-" + now);
        media.setUri(URI.create("http://www.foo.bar/test/Test-03-" + now.toEpochMilli() + ".mp4"));
        media.setStartTimestamp(now);
        media.setSha512(randomSha512());
        var f = mediaService.create(media);
        var m = f.get(5000, TimeUnit.MILLISECONDS);
        assertNotNull(m);
        assertNotNull(m.getVideoReferenceUuid());
        var then = now.plus(Duration.ofDays(3));
        var duration = Duration.ofMillis(1234);
        var m1 = mediaService.update(m.getVideoReferenceUuid(), then, duration).get(5000, TimeUnit.MILLISECONDS);
        assertNotNull(m1);
        assertEquals(duration, m1.getDuration());
        assertEquals(then, m1.getStartTimestamp());
    }

    @Test
    public void testUpdateMedia() throws Exception {
        var now = Instant.now();
        var media = new Media();
        media.setVideoSequenceName("Test-04");
        media.setCameraId("Test");
        media.setVideoName("Test-04-" + now);
        media.setUri(URI.create("http://www.foo.bar/test/Test-04-" + now.toEpochMilli() + ".mp4"));
        media.setStartTimestamp(now);
        media.setSha512(randomSha512());
        var f = mediaService.create(media);
        var m = f.get(5000, TimeUnit.MILLISECONDS);
        assertNotNull(m);
        var then = now.plus(Duration.ofDays(3));
        var duration = Duration.ofMillis(1234);
        m.setStartTimestamp(then);
        m.setDuration(duration);
        m.setWidth(200);
        m.setHeight(200);
        m.setFrameRate(24.0);
        var m1 = mediaService.update(m).get(5000, TimeUnit.MILLISECONDS);
        assertNotNull(m1);
        assertNotNull(m1.getVideoReferenceUuid());
        assertEquals(then, m1.getStartTimestamp());
        assertEquals(duration, m1.getDuration());
        assertEquals(m.getWidth().intValue(), m1.getWidth().intValue());
        assertEquals(m.getHeight().intValue(), m1.getHeight().intValue());
        assertEquals(m.getFrameRate(), m1.getFrameRate());
    }

    @Test
    public void testDelete() throws Exception {
        var now = Instant.now();
        var media = new Media();
        media.setVideoSequenceName("Test-05");
        media.setCameraId("Test");
        media.setVideoName("Test-05-" + now);
        media.setUri(URI.create("http://www.foo.bar/test/Test-05-" + now.toEpochMilli() + ".mp4"));
        media.setStartTimestamp(now);
        media.setSha512(randomSha512());
        var m = mediaService.create(media).get(5000, TimeUnit.MILLISECONDS);
        assertNotNull(m);
        var ok = mediaService.delete(m.getVideoReferenceUuid()).get(5000, TimeUnit.MILLISECONDS);
        assertTrue(ok);
        m = mediaService.findBySha512(media.getSha512()).get(5000, TimeUnit.MILLISECONDS);
        assertNull(m);
    }

    @Test
    public void testFindByVideoSequenceName() throws Exception {
        CompletableFuture<List<Media>> f = mediaService.findByVideoSequenceName("Ventana 3937");
        List<Media> media = f.get(5000, TimeUnit.MILLISECONDS);
        assertTrue(!media.isEmpty());
    }

    @Test
    public void testFindByUri() throws Exception {
        var now = Instant.now();
        var name = "Test " + Instant.now();
        var uri = URI.create("http://m3.shore.mbari.org/videos/M3/test/" + now.toEpochMilli() + ".mp4");
        var createFuture = mediaService.create(name, "Test", name, uri, now);
        var m0 = createFuture.get(5000, TimeUnit.MILLISECONDS);
        assertNotNull(m0);

        var f = mediaService.findByUri(uri);
        var m1 = f.get(5000, TimeUnit.MILLISECONDS);
        assertNotNull(m1);
        assertEquals(m0.getUri(), m0.getUri());
        assertEquals(m0.getStartTimestamp().toEpochMilli(), m1.getStartTimestamp().toEpochMilli());
        assertEquals(m0.getVideoSequenceName(), m1.getVideoSequenceName());
        assertEquals(m0.getVideoName(), m1.getVideoName());
    }

    @Test
    public void testFindByMissingUri() throws Exception {
        var f = mediaService.findByUri(URI.create("urn:i.dont.exist:v1234"));
        var m = f.get(5000, TimeUnit.MILLISECONDS);
        assertNull(m);
    }
}
