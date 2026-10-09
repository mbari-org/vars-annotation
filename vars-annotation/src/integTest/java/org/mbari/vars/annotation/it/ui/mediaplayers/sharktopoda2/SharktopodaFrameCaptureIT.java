package org.mbari.vars.annotation.it.ui.mediaplayers.sharktopoda2;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mbari.vars.annotation.model.Framegrab;
import org.mbari.vars.annotation.ui.mediaplayers.sharktopoda2.ImageCaptureServiceImpl;
import org.mbari.vcr4j.commands.RemoteCommands;
import org.mbari.vcr4j.commands.SeekElapsedTimeCmd;
import org.mbari.vcr4j.commands.VideoCommands;
import org.mbari.vcr4j.remote.control.RVideoIO;
import org.mbari.vcr4j.remote.control.RemoteControl;
import org.mbari.vcr4j.remote.control.commands.*;

import java.io.File;
import java.math.BigInteger;
import java.net.DatagramSocket;
import java.net.URI;
import java.time.Duration;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Exercises {@link ImageCaptureServiceImpl} against a <b>real</b> running Sharktopoda using
 * vcr4j-remote, i.e. the same code path VARS uses. Complements {@link ImageCaptureServiceTest},
 * which uses a mock.
 *
 * <p>Skipped unless {@code SHARKTOPODA_IT_VIDEO} is set:</p>
 * <pre>
 * SHARKTOPODA_IT_VIDEO=file:///path/to/video.mp4 SHARKTOPODA_IT_PORT=8800 \
 *     ./gradlew integrationTest --tests '*SharktopodaFrameCaptureIT'
 * </pre>
 *
 * <p>The video is opened in a new Sharktopoda window under a random UUID and closed afterwards.
 * Expected elapsed times follow the REQUIREMENTS.md contract: seeking to T shows the first frame
 * whose PTS &gt;= T, reported as that PTS truncated to whole milliseconds.</p>
 */
@EnabledIfEnvironmentVariable(named = "SHARKTOPODA_IT_VIDEO", matches = ".+")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class SharktopodaFrameCaptureIT {

    private static final Logger log = Logger.getLogger(SharktopodaFrameCaptureIT.class.getName());

    private final UUID videoUuid = UUID.randomUUID();
    private RemoteControl remoteControl;
    private RVideoIO io;
    private ImageCaptureServiceImpl imageCaptureService;
    private Path tempDir;
    /** Frame duration in seconds as an exact fraction (numerator, denominator) */
    private long frameNum;
    private long frameDen;
    private long durationMillis;

    @BeforeAll
    void setUp() throws Exception {
        String video = System.getenv("SHARKTOPODA_IT_VIDEO");
        int port = Integer.parseInt(System.getenv().getOrDefault("SHARKTOPODA_IT_PORT", "8800"));
        tempDir = Files.createTempDirectory("sharktopoda-it-");

        int localPort;
        try (var s = new DatagramSocket(0)) {
            localPort = s.getLocalPort();
        }

        imageCaptureService = new ImageCaptureServiceImpl();
        var openDone = new CompletableFuture<OpenDoneCmd.Request>();
        remoteControl = new RemoteControl.Builder(videoUuid)
                .remotePort(port)
                .port(localPort)
                .whenFrameCaptureIsDone(imageCaptureService.getEventBus()::send)
                .whenOpenIsDone(openDone::complete)
                .build()
                .orElseThrow(() -> new IllegalStateException("Unable to connect to Sharktopoda on port " + port));
        io = remoteControl.getVideoIO();
        imageCaptureService.setIo(io);

        io.send(new OpenCmd(videoUuid, URI.create(video).toURL()));
        var done = openDone.get(30, TimeUnit.SECONDS);
        assertEquals(RResponse.OK, done.getStatus(), "open done failed: " + done.getCause());

        // Surface vcr4j errors in the test log (they are otherwise only published to an observable)
        io.getErrorObservable().subscribe(e -> log.warning("vcr4j error: " + e.getMessage()
                + (e.getException() == null ? "" : " - " + e.getException())));

        // Use the focused window's info: Sharktopoda brings the newly opened window to the front.
        // ('request all information' fails to parse in vcr4j if any open window's id is not a
        // UUID, e.g. a video opened from Sharktopoda's File menu, whose id is its path.)
        // RVideoIO.send is synchronous, so subscribe first and the info arrives during send.
        var infos = new CompletableFuture<List<? extends VideoInfo>>();
        var disposable = io.getVideoInfoObservable().subscribe(infos::complete);
        io.send(RemoteCommands.REQUEST_VIDEO_INFO);
        var info = infos.get(5, TimeUnit.SECONDS).stream()
                .filter(v -> videoUuid.equals(v.getUuid()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("The newly opened video is not the focused window"));
        disposable.dispose();
        durationMillis = info.getDurationMillis();
        setFrameDuration(info.getFrameRate());
        log.info("Opened " + video + " as " + videoUuid + " fps=" + info.getFrameRate()
                + " duration=" + durationMillis + " ms");

        // RVideoIO only dispatches specific command types; PauseCmd itself would be silently dropped
        io.send(VideoCommands.PAUSE);
    }

    @AfterAll
    void tearDown() throws Exception {
        if (io != null) {
            io.send(new CloseCmd(videoUuid));
        }
        if (remoteControl != null) {
            remoteControl.close();
        }
        if (tempDir != null) {
            try (var files = Files.walk(tempDir)) {
                files.sorted((a, b) -> b.compareTo(a)).map(Path::toFile).forEach(File::delete);
            }
        }
    }

    @ParameterizedTest(name = "seek {0} ms")
    @ValueSource(longs = {1000, 1001, 12345, 60000, 123457})
    @DisplayName("capture() returns the frame at the seeked position, and it round-trips")
    void captureAtSeekedPosition(long seekMillis) throws Exception {
        long expected = expectedMillis(seekMillis);

        var framegrab = seekAndCapture(seekMillis);
        long actual = elapsedMillis(framegrab);
        assertEquals(expected, actual, "elapsedTimeMillis for seek to " + seekMillis);

        // VARS stores `actual` on the annotation and later seeks to it: must be the same frame
        long roundTrip = elapsedMillis(seekAndCapture(actual));
        assertEquals(actual, roundTrip, "round-trip seek to " + actual);
    }

    @Test
    @DisplayName("capture near the end of the video")
    void captureNearEnd() throws Exception {
        long seekMillis = durationMillis - 2000;
        assertEquals(expectedMillis(seekMillis), elapsedMillis(seekAndCapture(seekMillis)));
    }

    @Test
    @DisplayName("back-to-back captures each get their own frame capture done")
    void backToBackCaptures() throws Exception {
        seek(30000);
        var names = new HashSet<String>();
        for (int i = 0; i < 3; i++) {
            var file = tempDir.resolve("burst-" + i + ".png").toFile();
            var framegrab = imageCaptureService.capture(file);
            assertTrue(framegrab.getImage().isPresent());
            assertEquals(expectedMillis(30000), elapsedMillis(framegrab));
            assertTrue(file.exists(), "PNG written for capture " + i);
            names.add(file.getName());
        }
        assertEquals(3, names.size());
    }

    @Test
    @DisplayName("capture to an existing file fails fast with Sharktopoda's cause")
    void captureToExistingFileFailsFast() throws Exception {
        var file = tempDir.resolve("exists.png").toFile();
        Files.writeString(file.toPath(), "placeholder");

        long start = System.nanoTime();
        var e = assertThrows(RuntimeException.class, () -> imageCaptureService.capture(file));
        long tookMillis = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);
        log.info("capture to existing file threw after " + tookMillis + " ms: " + e.getMessage());

        assertTrue(e.getMessage().contains("Image exists at location"),
                "message should carry Sharktopoda's cause but was: " + e.getMessage());
        assertTrue(tookMillis < 2000,
                "Sharktopoda rejects this immediately; capture() should not wait for the timeout. Took "
                        + tookMillis + " ms");
    }

    // -------------------------------------------------------------------------

    private void seek(long millis) throws InterruptedException {
        io.send(new SeekElapsedTimeCmd(Duration.ofMillis(millis)));
        Thread.sleep(500); // let the player settle on the frame
    }

    private Framegrab seekAndCapture(long millis) throws InterruptedException {
        seek(millis);
        var file = tempDir.resolve("capture-" + UUID.randomUUID() + ".png").toFile();
        var framegrab = imageCaptureService.capture(file);
        assertTrue(framegrab.getImage().isPresent(), "image present");
        var image = framegrab.getImage().get();
        assertTrue(image.getWidth(null) > 0 && image.getHeight(null) > 0, "image has size");
        assertTrue(file.delete(), "PNG was written to " + file); // 4K PNGs are big; clean as we go
        return framegrab;
    }

    private static long elapsedMillis(Framegrab framegrab) {
        return framegrab.getVideoIndex()
                .flatMap(vi -> vi.getElapsedTime())
                .orElseThrow()
                .toMillis();
    }

    /**
     * NTSC-style rates (e.g. 59.94006) are really N*1000/1001. Recover the exact fraction so
     * expected values aren't off by one from floating point.
     */
    private void setFrameDuration(double fps) {
        double ntsc = fps * 1001.0 / 1000.0;
        if (Math.abs(ntsc - Math.rint(ntsc)) < 1e-3 && Math.abs(fps - Math.rint(fps)) > 1e-3) {
            frameNum = 1001;
            frameDen = Math.round(ntsc) * 1000;
        }
        else {
            frameNum = 1;
            frameDen = Math.round(fps);
        }
    }

    /** First frame with PTS >= t, reported as PTS truncated to ms. Exact integer math. */
    private long expectedMillis(long tMillis) {
        // k = ceil(t / 1000 / (num/den)) = ceil(t * den / (1000 * num))
        var t = BigInteger.valueOf(tMillis);
        var num = BigInteger.valueOf(frameNum);
        var den = BigInteger.valueOf(frameDen);
        var thousand = BigInteger.valueOf(1000);
        var divisor = thousand.multiply(num);
        var qr = t.multiply(den).divideAndRemainder(divisor);
        var k = qr[1].signum() == 0 ? qr[0] : qr[0].add(BigInteger.ONE);
        // ptsMillis = floor(k * num * 1000 / den)
        return k.multiply(num).multiply(thousand).divide(den).longValueExact();
    }
}
