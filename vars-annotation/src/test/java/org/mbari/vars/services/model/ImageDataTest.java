package org.mbari.vars.services.model;

import org.junit.Test;
import org.mbari.vcr4j.VideoIndex;

import javax.imageio.ImageIO;

import java.time.Instant;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class ImageDataTest {

    @Test
    public void testJpegToBytes() throws Exception {
        // Read image and verify that it's the correct size
        var url = getClass().getResource("/images/20220221T153508Z--1c91ee22-18c7-4083-b5e9-7903ffb14db3.jpeg");
        assertNotNull(url);
        var image = ImageIO.read(url);
        assertNotNull(image);
        assertEquals(3840, image.getWidth());
        assertEquals(2160, image.getHeight());

        var imageData = new ImageData(UUID.randomUUID(), new VideoIndex(Instant.now()), image);

        var pngBytes = imageData.getPngBytes();
        assertNotNull(pngBytes);
        assertEquals(18321946, pngBytes.length);

        var jpgBytes = imageData.getJpegBytes();
        assertNotNull(jpgBytes);
        assertEquals(1246458, jpgBytes.length);
    }

    @Test
    public void testPngToBytes() throws Exception {
        // Read image and verify that it's the correct size
//        var url = getClass().getResource("/images/20220221T153508Z--1c91ee22-18c7-4083-b5e9-7903ffb14db3.jpeg");
        var url = getClass().getResource("/images/20170301T185553.000000Z--58c7854c-a518-4b3f-94ec-8024c1e376ad.png");
        assertNotNull(url);
        var image = ImageIO.read(url);
        System.out.println(image);
        assertNotNull(image);
        assertEquals(1280, image.getWidth());
        assertEquals(720, image.getHeight());

        var imageData = new ImageData(UUID.randomUUID(), new VideoIndex(Instant.now()), image);

        var pngBytes = imageData.getPngBytes();
        assertNotNull(pngBytes);
        assertEquals(1145704, pngBytes.length);

        var jpgBytes = imageData.getJpegBytes();
        assertNotNull(jpgBytes);
        assertEquals(83607, jpgBytes.length);
    }
}
