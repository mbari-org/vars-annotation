package org.mbari.vars.annotation.util;

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
        // We create a new bufferedimage dropping any alpha channels that might exist. Otherwise,
        // we can run buffered images created from PNG files that return 0 byte jpeg arrays.
        var bufferedImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
        var graphics = bufferedImage.getGraphics();
        graphics.drawImage(image, 0, 0, null);
        graphics.dispose();
        return toImageByteArray(bufferedImage, "jpg");
    }

    public static byte[] toPngByteArray(BufferedImage image) throws IOException {
        return toImageByteArray(image, "png");
    }
}
