package org.mbari.vars.annotation.ui.mediaplayers.sharktopoda2;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mbari.vars.annotation.model.Framegrab;
import org.mbari.vcr4j.remote.control.RVideoIO;
import org.mbari.vcr4j.remote.control.commands.FrameCaptureCmd;
import org.mbari.vcr4j.remote.control.commands.FrameCaptureDoneCmd;
import org.mbari.vcr4j.remote.control.commands.RResponse;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.net.DatagramSocket;
import java.nio.file.Path;
import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

public class ImageCaptureServiceImplTest {

    private RVideoIO io;
    private UUID videoUuid;

    @AfterEach
    public void cleanup() {
        if (io != null) {
            io.close();
        }
    }

    /**
     * Wires up a service backed by a real RVideoIO (pointed at an unused port), starts a
     * capture on a background thread, and waits until the capture has subscribed and sent
     * its FrameCaptureCmd.
     */
    private record RunningCapture(ImageCaptureServiceImpl service,
                                  CompletableFuture<Framegrab> result,
                                  UUID imageReferenceUuid) {
    }

    private RunningCapture startCapture(File file) throws Exception {
        int port;
        try (var s = new DatagramSocket(0)) {
            port = s.getLocalPort();
        }
        videoUuid = UUID.randomUUID();
        io = new RVideoIO(videoUuid, "localhost", port);

        var service = new ImageCaptureServiceImpl();
        service.setIo(io);

        // capture() subscribes to the event bus, then sends the FrameCaptureCmd. Seeing
        // the command on the io's command subject proves the subscription is active.
        var frameCaptureCmd = new CompletableFuture<FrameCaptureCmd>();
        io.getCommandSubject()
                .ofType(FrameCaptureCmd.class)
                .subscribe(frameCaptureCmd::complete);

        var result = CompletableFuture.supplyAsync(() -> service.capture(file));
        var cmd = frameCaptureCmd.get(10, TimeUnit.SECONDS);
        return new RunningCapture(service, result, cmd.getValue().getImageReferenceUuid());
    }

    /**
     * The FrameCaptureDoneCmd is delivered by PlayerIO's single UDP receive thread, which
     * also has to send the UDP response back to Sharktopoda and receive every subsequent
     * datagram. Reading the captured image off disk must therefore NOT run on the thread
     * that calls eventBus.send() — a slow decode would stall the receive loop and drop
     * packets.
     *
     * The image file is a FIFO, so the decode blocks until this test provides a writer.
     * If send() is blocked too, the handling is running on the delivery thread.
     */
    @Test
    public void frameCaptureDoneHandlingDoesNotBlockTheDeliveryThread(@TempDir Path tempDir) throws Exception {
        assumeFalse(System.getProperty("os.name").toLowerCase().contains("windows"),
                "FIFO-based test requires a POSIX platform");

        var fifo = tempDir.resolve("framegrab.png");
        var mkfifo = new ProcessBuilder("mkfifo", fifo.toString()).start();
        assertEquals(0, mkfifo.waitFor(), "mkfifo failed");

        var capture = startCapture(fifo.toFile());

        // Simulate PlayerIO's receive thread delivering the done command. It must return
        // promptly even though the image cannot be read yet.
        var doneCmd = new FrameCaptureDoneCmd(videoUuid, capture.imageReferenceUuid(),
                fifo.toString(), 1000L, RResponse.OK);
        var delivery = CompletableFuture.runAsync(() -> capture.service().getEventBus().send(doneCmd));
        assertDoesNotThrow(() -> delivery.get(2, TimeUnit.SECONDS),
                "The delivery thread was blocked by the image decode");

        // Unblock the decode: give the FIFO a writer and immediately close it (EOF).
        // The decode yields a null image, which is fine — capture() must still complete.
        try (var out = new FileOutputStream(fifo.toFile())) {
            // no bytes; the reader sees EOF
        }
        assertDoesNotThrow(() -> capture.result().get(15, TimeUnit.SECONDS),
                "capture() never completed after the image became readable");
    }

    /**
     * A FrameCaptureDoneCmd left over from an earlier capture (e.g. one that timed out and
     * whose done command arrived late) must not complete the current capture — it points
     * at the wrong image. Only the done command carrying this capture's imageReferenceUuid
     * counts.
     */
    @Test
    public void captureIgnoresDoneCommandsFromOtherCaptures(@TempDir Path tempDir) throws Exception {
        var imageFile = tempDir.resolve("framegrab.png");
        ImageIO.write(new BufferedImage(4, 4, BufferedImage.TYPE_INT_RGB), "png", imageFile.toFile());

        var capture = startCapture(imageFile.toFile());
        var eventBus = capture.service().getEventBus();

        // Stale done from some earlier capture: different imageReferenceUuid, pointing at
        // an image that no longer exists
        eventBus.send(new FrameCaptureDoneCmd(videoUuid, UUID.randomUUID(),
                tempDir.resolve("stale.png").toString(), 999L, RResponse.OK));
        // The done command for THIS capture
        eventBus.send(new FrameCaptureDoneCmd(videoUuid, capture.imageReferenceUuid(),
                imageFile.toString(), 1000L, RResponse.OK));

        var framegrab = capture.result().get(15, TimeUnit.SECONDS);
        assertTrue(framegrab.getImage().isPresent(),
                "capture() completed with the stale done command instead of this capture's");
        assertEquals(Duration.ofMillis(1000),
                framegrab.getVideoIndex().flatMap(vi -> vi.getElapsedTime()).orElse(null),
                "capture() used the stale done command's elapsed time");
    }

    /**
     * Sharktopoda reports a failed frame capture with status=failed. That must surface as
     * an error, not as a Framegrab with no image in it.
     */
    @Test
    public void captureThrowsWhenThePlayerReportsFailure(@TempDir Path tempDir) throws Exception {
        var imageFile = tempDir.resolve("framegrab.png");

        var capture = startCapture(imageFile.toFile());
        capture.service().getEventBus().send(new FrameCaptureDoneCmd(videoUuid,
                capture.imageReferenceUuid(), imageFile.toString(), 1000L, RResponse.FAILED));

        assertThrows(ExecutionException.class, () -> capture.result().get(15, TimeUnit.SECONDS),
                "A failed frame capture must not complete normally");
    }
}
