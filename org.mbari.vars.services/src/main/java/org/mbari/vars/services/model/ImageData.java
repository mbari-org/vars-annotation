package org.mbari.vars.services.model;

import org.mbari.vars.services.util.ImageUtils;
import org.mbari.vcr4j.VideoIndex;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;
import java.util.UUID;

public class ImageData {

    private final UUID videoReferenceUuid;
    private final VideoIndex videoIndex;
    private final BufferedImage bufferedImage;
    private byte[] pngBytes;
    private byte[] jpegBytes;

    public ImageData(UUID videoReferenceUuid, VideoIndex videoIndex, BufferedImage bufferedImage) {
        this.videoReferenceUuid = videoReferenceUuid;
        this.videoIndex = videoIndex;
        this.bufferedImage = bufferedImage;
    }

    public UUID getVideoReferenceUuid() {
        return videoReferenceUuid;
    }

    public VideoIndex getVideoIndex() {
        return videoIndex;
    }

    public BufferedImage getBufferedImage() {
        return bufferedImage;
    }

    public byte[] getPngBytes() {
        if (pngBytes == null) {
            try {
                pngBytes = ImageUtils.toPngByteArray(bufferedImage);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return pngBytes;
    }

    public byte[] getJpegBytes() {
        if (jpegBytes == null) {
            try {
                jpegBytes = ImageUtils.toJpegByteArray(bufferedImage);
            }
            catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return jpegBytes;
    }

    public static ImageData from(UUID videoReferenceUuid, VideoIndex videoIndex, Path path)  {
        try {
            var bufferedImage = ImageIO.read(path.toFile());
            return new ImageData(videoReferenceUuid, videoIndex, bufferedImage);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
