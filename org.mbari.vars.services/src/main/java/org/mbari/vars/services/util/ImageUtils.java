package org.mbari.vars.services.util;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class ImageUtils {

    private ImageUtils() {
        // No instantiation
    }

    private static byte[] toImageByteArray(BufferedImage image, String format) throws IOException {
        var baos = new ByteArrayOutputStream();
        ImageIO.write(image, format, baos);
        return baos.toByteArray();
    }

    public static byte[] toJpegByteArray(BufferedImage image) throws IOException {
        return toImageByteArray(image, "jpg");
    }

    public static byte[] toPngByteArray(BufferedImage image) throws IOException {
        return toImageByteArray(image, "png");
    }
}
