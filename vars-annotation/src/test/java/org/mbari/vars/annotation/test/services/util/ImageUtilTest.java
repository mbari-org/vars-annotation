package org.mbari.vars.annotation.test.services.util;

import org.junit.jupiter.api.Test;
import org.mbari.vars.annotation.util.ImageUtils;

import static  org.junit.jupiter.api.Assertions.*;

import javax.imageio.ImageIO;
import java.io.ByteArrayInputStream;
import java.io.IOException;

public class ImageUtilTest {

    @Test
    public void testToJpegImageByteArray() throws IOException  {

        // Read image and verify that it's the correct size
        var url = getClass().getResource("/images/20220221T153508Z--1c91ee22-18c7-4083-b5e9-7903ffb14db3.jpeg");
        assertNotNull(url);
        var image = ImageIO.read(url);
        assertNotNull(image);
        assertEquals(3840, image.getWidth());
        assertEquals(2160, image.getHeight());

        // Convert to jpeg bytes
        var bytes = ImageUtils.toJpegByteArray(image);
        assertNotNull(bytes);
        assertEquals(1246458, bytes.length);

        // Convert bytes back to a buffered image and verify the size
        var image2 = ImageIO.read(new ByteArrayInputStream(bytes));
        assertNotNull(image2);
        assertEquals(3840, image2.getWidth());
        assertEquals(2160, image2.getHeight());

    }

    @Test
    public void testToPngImageByteArray() throws IOException  {

        // Read image and verify that it's the correct size
        var url = getClass().getResource("/images/20220221T153508Z--1c91ee22-18c7-4083-b5e9-7903ffb14db3.jpeg");
        assertNotNull(url);
        var image = ImageIO.read(url);
        assertNotNull(image);
        assertEquals(3840, image.getWidth());
        assertEquals(2160, image.getHeight());

        // Convert to jpeg bytes
        var bytes = ImageUtils.toPngByteArray(image);
        assertNotNull(bytes);
        assertEquals(18321946, bytes.length);

        // Convert bytes back to a buffered image and verify the size
        var image2 = ImageIO.read(new ByteArrayInputStream(bytes));
        assertNotNull(image2);
        assertEquals(3840, image2.getWidth());
        assertEquals(2160, image2.getHeight());

    }
}
