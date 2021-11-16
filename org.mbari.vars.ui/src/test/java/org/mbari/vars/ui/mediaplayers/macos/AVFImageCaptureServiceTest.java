package org.mbari.vars.ui.mediaplayers.macos;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
// import org.mbari.vars.avfoundation.AVFImageCapture;
// import org.mbari.vars.services.model.Framegrab;
// import org.mbari.vars.ui.mediaplayers.macos.avf.AVFImageCaptureService;

import java.awt.*;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.Collection;
import java.util.Optional;

/**
 * @author Brian Schlining
 * @since 2018-01-16T14:31:00
 */
public class AVFImageCaptureServiceTest {


    @Ignore
    @Test
    public void testImageCapture() throws Exception {
        // AVFImageCapture ic = new AVFImageCapture();
        // String[] devices = ic.videoDevicesAsStrings();
        // if (devices.length > 0) {
        //     ic.startSessionWithNamedDevice(devices[0]);
        //     Path path = Paths.get("target",
        //             getClass().getSimpleName() + "-0-" + Instant.now() + ".png");
        //     Optional<Image> png = ic.capture(path.toFile());
        //     Assert.assertTrue(png.isPresent());
        //     ic.stopSession();
        // }
        // else {
        //     System.err.println("No frame capture devices were found");
        // }
    }

    @Ignore
    @Test
    public void testImageCaptureService() throws IOException {
        // AVFImageCaptureService service = AVFImageCaptureService.getInstance();
        // Collection<String> devices = service.listDevices();
        // if (devices.size() > 0) {
        //     service.setDevice(devices.iterator().next());
        //     Path path = Paths.get("target",
        //             getClass().getSimpleName() + "-1-" + Instant.now() + ".png");
        //     Framegrab png = service.capture(path.toFile());
        //     Assert.assertTrue(png.getImage().isPresent());
        // }
        // else {
        //     System.err.println("No frame capture devices were found");
        // }
    }

    @Ignore
    @Test
    public void testImageCapture2() throws IOException {
//        AVFImageCaptureService service = AVFImageCaptureService.getInstance();
//        AVFImageCapture ic = service.getImageCapture();
//        String[] devices = ic.videoDevicesAsStrings();
//        if (devices.length > 0) {
//            ic.startSessionWithNamedDevice(devices[0]);
//            Path path = Paths.get("target",
//                    getClass().getSimpleName() + "-1-" + Instant.now() + ".png");
//            Optional<Image> png = ic.capture(path.toFile());
//            ic.stopSession();
//            Assert.assertTrue(png.isPresent());
//
//        }
//        else {
//            System.err.println("No frame capture devices were found");
//        }
    }
}
