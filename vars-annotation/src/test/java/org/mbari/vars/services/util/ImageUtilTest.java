package org.mbari.vars.services.util;

import org.junit.Test;
import org.mbari.vars.annotation.util.ImageUtils;

import static  org.junit.Assert.*;

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
        assertEquals(image.getWidth(), 3840);
        assertEquals(image.getHeight(), 2160);

        // Convert to jpeg bytes
        var bytes = ImageUtils.toJpegByteArray(image);
        assertNotNull(bytes);
//        System.out.println("Bytes = " + bytes.length);
        assertEquals(1246458, bytes.length);

        // Convert bytes back to a buffered image and verify the size
        var image2 = ImageIO.read(new ByteArrayInputStream(bytes));
        assertNotNull(image2);
        assertEquals(image2.getWidth(), 3840);
        assertEquals(image2.getHeight(), 2160);

    }

    @Test
    public void testToPngImageByteArray() throws IOException  {

        // Read image and verify that it's the correct size
        var url = getClass().getResource("/images/20220221T153508Z--1c91ee22-18c7-4083-b5e9-7903ffb14db3.jpeg");
        assertNotNull(url);
        var image = ImageIO.read(url);
        assertNotNull(image);
        assertEquals(image.getWidth(), 3840);
        assertEquals(image.getHeight(), 2160);

        // Convert to jpeg bytes
        var bytes = ImageUtils.toPngByteArray(image);
        assertNotNull(bytes);
//        System.out.println("Bytes = " + bytes.length);
        assertEquals(18321946, bytes.length);

        // Convert bytes back to a buffered image and verify the size
        var image2 = ImageIO.read(new ByteArrayInputStream(bytes));
        assertNotNull(image2);
        assertEquals(image2.getWidth(), 3840);
        assertEquals(image2.getHeight(), 2160);

    }
}
