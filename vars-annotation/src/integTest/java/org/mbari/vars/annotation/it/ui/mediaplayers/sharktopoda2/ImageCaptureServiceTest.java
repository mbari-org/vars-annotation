package org.mbari.vars.annotation.it.ui.mediaplayers.sharktopoda2;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.mbari.vars.annotation.ui.mediaplayers.sharktopoda2.MediaControlsFactoryImpl;
import org.mbari.vars.vampiresquid.sdk.r1.models.Media;

import java.util.UUID;

public class ImageCaptureServiceTest {

    @Test
    public void testImageCapture() throws Exception {

        var mediaControlsFactory = new MediaControlsFactoryImpl();

        var video = getClass().getResource("/videos/h264_test.mp4");
        assertNotNull(video);

        var media = new Media();
        media.setUri(video.toURI());
        media.setVideoName("h264_test.mp4");
        media.setVideoSequenceName("h264_test");
        media.setCameraId("Test");
        media.setVideoSequenceUuid(UUID.randomUUID());
        media.setVideoUuid(UUID.randomUUID());
        media.setVideoReferenceUuid(UUID.randomUUID());

        var mediaPlayer = mediaControlsFactory.open(media, 8800, 5544);

    }
}
