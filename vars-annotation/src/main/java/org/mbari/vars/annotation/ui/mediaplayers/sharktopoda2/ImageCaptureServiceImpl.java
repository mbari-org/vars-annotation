package org.mbari.vars.annotation.ui.mediaplayers.sharktopoda2;

import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import org.mbari.vars.annotation.etc.jdk.Loggers;
import org.mbari.vars.annotation.etc.rxjava.EventBus;
import org.mbari.vars.annotation.services.ImageCaptureService;
import org.mbari.vars.annotation.model.Framegrab;
import org.mbari.vars.annotation.ui.mediaplayers.sharktopoda.SharktopodaSettingsPaneController;
import org.mbari.vcr4j.VideoIndex;
import org.mbari.vcr4j.remote.control.commands.FrameCaptureCmd;
import org.mbari.vcr4j.remote.control.commands.FrameCaptureDoneCmd;
import org.mbari.vcr4j.remote.control.commands.RResponse;

import org.mbari.vcr4j.remote.control.RVideoIO;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class ImageCaptureServiceImpl implements ImageCaptureService {

    private final Loggers log = new Loggers(getClass());
    private RVideoIO io;

    private final EventBus eventBus = new EventBus();

    public ImageCaptureServiceImpl() {
        eventBus.toObserverable()
                .subscribe(obj -> log.debug("Received " + obj.toString()),
                        ex -> log.atWarn().withCause(ex).log("An exception was thrown"),
                        () -> log.info("Closed event bus"));
    }

    public void setIo(RVideoIO io) {
        this.io = io;
    }

    public EventBus getEventBus() {
        return eventBus;
    }

    /**
     * The framecapture flow is:
     * 1. method call: capture(file)
     * 2. send framecapture cmd to Sharktopoda
     * 3. listen for framecapturedone cmd from sharkdopoda
     * 4. Push done command to the eventbus in this service
     * 5. Observer for done command, then read data from disk
     * @param file
     * @return
     */
    @Override
    public Framegrab capture(File file) {
        if (io != null) {
            // Subscribe BEFORE sending the command. FrameCaptureDoneCmd arrives via
            // PlayerIO's background thread and is published to a PublishSubject. If
            // io.send() were called first it would block for up to 1 second waiting
            // for Sharktopoda's ACK, during which the done event could be emitted with
            // no subscriber — permanently lost.
            var imageReferenceUuid = UUID.randomUUID();
            var timeout = SharktopodaSettingsPaneController.getFramecaptureTimeout();
            var frameCaptureCmd = new FrameCaptureCmd(io.getUuid(), imageReferenceUuid, file.getAbsolutePath());
            var future = new CompletableFuture<Framegrab>();
            var disposables = new CompositeDisposable();

            // Sharktopoda answers 'frame capture' immediately. If it rejects the request
            // (e.g. "Image exists at location") no 'frame capture done' will follow, so fail
            // now with its cause instead of waiting for the timeout. RVideoIO passes this exact
            // command instance through, so match by identity.
            disposables.add(io.getResponseObservable()
                    .filter(r -> r.command() == frameCaptureCmd && !r.response().success())
                    .subscribe(r -> future.completeExceptionally(
                            new RuntimeException(rejectedMessage(r.response().getCause())))));

            // No answer at all (RVideoIO waits ~1 second) means Sharktopoda isn't listening
            disposables.add(io.getErrorObservable()
                    .filter(e -> e.isConnectionError()
                            && e.getVideoCommand().filter(c -> c == frameCaptureCmd).isPresent())
                    .subscribe(e -> future.completeExceptionally(new RuntimeException(
                            "Sharktopoda did not acknowledge the frame capture command. Check that it is " +
                                    "running and listening on the port set in the Sharktopoda preferences."))));

            disposables.add(eventBus.toObserverable()
                    .ofType(FrameCaptureDoneCmd.class)
                    // Only the done command for THIS capture counts. A stale done from an
                    // earlier capture (e.g. one that timed out and arrived late) points
                    // at the wrong image.
                    .filter(cmd -> imageReferenceUuid.equals(cmd.getValue().getImageReferenceUuid()))
                    .take(1)
                    // The done command is delivered by PlayerIO's single UDP receive
                    // thread, which must not be tied up reading the image off disk:
                    // Sharktopoda is still waiting for the UDP response, and any other
                    // incoming datagrams would back up and get dropped. Hop to an io
                    // thread before decoding.
                    .observeOn(Schedulers.io())
                    .map(this::captureDone)
                    .timeout(timeout.toMillis(), TimeUnit.MILLISECONDS)
                    .subscribe(future::complete, future::completeExceptionally));
            io.send(frameCaptureCmd);
            try {
                // Allow a little slack beyond the rx timeout so it reports the failure first
                return future.get(timeout.toMillis() + 1000, TimeUnit.MILLISECONDS);
            }
            catch (ExecutionException e) {
                // Thrown by the rx chain: either the rx timeout or a failure reported by captureDone
                var cause = e.getCause();
                if (cause instanceof TimeoutException) {
                    throw new RuntimeException(timeoutMessage(timeout), cause);
                }
                throw new RuntimeException(cause == null ? e.getMessage() : cause.getMessage(), cause);
            }
            catch (TimeoutException e) {
                throw new RuntimeException(timeoutMessage(timeout), e);
            }
            catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Frame capture was interrupted", e);
            }
            finally {
                disposables.dispose();
            }
        }
        else {
            throw new IllegalStateException("Not connected to Sharktopoda. Unable to send the frame capture command");
        }
    }

    private static String rejectedMessage(String cause) {
        return "Sharktopoda rejected the frame capture" + (cause == null || cause.isBlank() ? "" : ": " + cause);
    }

    private static String timeoutMessage(Duration timeout) {
        return "Sharktopoda did not finish the frame capture within " + timeout.toSeconds() +
                " seconds. The timeout can be increased in the Sharktopoda preferences.";
    }

    public final Framegrab captureDone(FrameCaptureDoneCmd cmd) {
        var request = cmd.getValue();
        if (RResponse.FAILED.equalsIgnoreCase(request.getStatus())) {
            throw new RuntimeException("Sharktopoda reported that the frame capture to "
                    + request.getImageLocation() + " failed");
        }
        var elapsedTime = Duration.ofMillis(request.getElapsedTimeMillis());
        var videoIndex = new VideoIndex(elapsedTime);
        BufferedImage image = null;
        var imageLocation = new File(request.getImageLocation());
        try {
            image = ImageIO.read(imageLocation);
        } catch (Exception e) {
            log.atWarn().withCause(e).log("Image capture failed. Unable to read image back off disk");
        }
        log.atDebug().log("Image capture complete. Read " + imageLocation.length() + " bytes from " + imageLocation.getAbsolutePath());
        return new Framegrab(image, videoIndex);
    }

    @Override
    public void dispose() {
    }
}
