package org.mbari.vars.annotation.it.ui.mediaplayers.sharktopoda2;

import com.google.gson.Gson;
import org.junit.jupiter.api.*;
import org.mbari.vars.annotation.ui.mediaplayers.sharktopoda2.ImageCaptureServiceImpl;
import org.mbari.vcr4j.remote.control.RemoteControl;
import org.mbari.vcr4j.remote.control.RVideoIO;
import org.mbari.vcr4j.remote.control.commands.FrameCaptureDoneCmd;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test for {@link ImageCaptureServiceImpl} using a mock Sharktopoda UDP server.
 *
 * No real Sharktopoda or video player is required — the mock server handles the full
 * two-way UDP protocol used by vcr4j-remote.
 *
 * <h2>UDP message flow</h2>
 * <pre>
 *   capture() caller
 *       |
 *       v
 *   RVideoIO.send(FrameCaptureCmd)
 *       |-- [UDP → SHARKTOPODA_PORT] --> MockSharktopoda
 *       |<-- [immediate ACK        ] --
 *       |    (blocks up to 1 second waiting for this ACK)
 *       |
 *   blockingFirst() subscribes to eventBus  ← SUBSCRIPTION HAPPENS HERE
 *
 *   Meanwhile, MockSharktopoda (on its own thread, after a configurable delay):
 *       |-- [UDP → LOCAL_LISTENER_PORT] --> PlayerIO
 *       PlayerIO → RxControlRequestHandler → frameCaptureDoneFn → eventBus.send()
 *       eventBus.send() → PublishSubject.onNext(FrameCaptureDoneCmd)
 *       → received by blockingFirst() subscriber → capture() returns
 * </pre>
 *
 * <h2>Race condition</h2>
 * If the mock sends {@code FrameCaptureDoneCmd} during the ACK-wait window (< ~1 second),
 * the event is emitted to the {@code PublishSubject} before {@code blockingFirst()} has
 * subscribed. Since a {@code PublishSubject} does not replay past events, the event is
 * permanently lost and {@code capture()} times out after 10 seconds.
 *
 * This happens on machines where frame capture completes quickly (fast local storage, etc.).
 *
 * <h2>Expected fix</h2>
 * Subscribe to the eventBus <em>before</em> calling {@code io.send()}, so no event can
 * be missed. For example, using a {@code CompletableFuture} or
 * {@link io.reactivex.rxjava3.subjects.ReplaySubject}.
 */
public class ImageCaptureServiceTest {

    private static final Logger log = Logger.getLogger(ImageCaptureServiceTest.class.getName());

    /** Port that MockSharktopoda listens on (VARS Annotation sends FrameCaptureCmd here). */
    private static final int SHARKTOPODA_PORT = 19800;

    /** Port that PlayerIO listens on (MockSharktopoda sends FrameCaptureDoneCmd here). */
    private static final int LOCAL_LISTENER_PORT = 19899;

    private MockSharktopoda mockSharktopoda;
    private RemoteControl remoteControl;
    private ImageCaptureServiceImpl imageCaptureService;
    private Path tempDir;

    @BeforeEach
    void setUp() throws Exception {
        tempDir = Files.createTempDirectory("framecapture-test-");

        mockSharktopoda = new MockSharktopoda(SHARKTOPODA_PORT);
        mockSharktopoda.start();

        imageCaptureService = new ImageCaptureServiceImpl();

        var remoteControlOpt = new RemoteControl.Builder(UUID.randomUUID())
                .remotePort(SHARKTOPODA_PORT)
                .port(LOCAL_LISTENER_PORT)
                .whenFrameCaptureIsDone(imageCaptureService.getEventBus()::send)
                .build();

        assertTrue(remoteControlOpt.isPresent(),
                "RemoteControl failed to build — is port " + SHARKTOPODA_PORT + " available?");
        remoteControl = remoteControlOpt.get();
        imageCaptureService.setIo(remoteControl.getVideoIO());

        // Give PlayerIO time to start its server loop
        Thread.sleep(150);
    }

    @AfterEach
    void tearDown() throws Exception {
        if (remoteControl != null) remoteControl.close();
        if (mockSharktopoda != null) mockSharktopoda.stop();
        try (var files = Files.walk(tempDir)) {
            files.sorted((a, b) -> b.compareTo(a)).map(Path::toFile).forEach(File::delete);
        }
    }

    // -------------------------------------------------------------------------
    // Normal case: FrameCaptureDoneCmd arrives after subscription
    // -------------------------------------------------------------------------

    /**
     * Verifies that capture succeeds when Sharktopoda takes longer than the ACK-wait
     * window (~1 second) to complete the frame grab.
     *
     * At 1500 ms, {@code blockingFirst()} is already subscribed when
     * {@code FrameCaptureDoneCmd} arrives, so the event is received correctly.
     *
     * This simulates slow machines, slow disks, or network-mounted storage.
     */
    @Test
    @DisplayName("Capture succeeds when FrameCaptureDoneCmd arrives after subscription (1500ms)")
    void testCaptureSucceedsWithSlowResponse() throws Exception {
        mockSharktopoda.setFrameCaptureDoneDelayMs(1500);
        var outputFile = tempDir.resolve("capture-slow.png").toFile();

        log.info("=== TEST: slow response (1500 ms) — expects PASS ===");
        var framegrab = imageCaptureService.capture(outputFile);

        assertNotNull(framegrab, "capture() must return a non-null Framegrab");
        assertTrue(framegrab.getImage().isPresent(), "Framegrab must contain a non-null image");
        var img = framegrab.getImage().get();
        log.info("PASS: image " + img.getWidth(null) + "×" + img.getHeight(null));
    }

    // -------------------------------------------------------------------------
    // Race condition: FrameCaptureDoneCmd arrives before subscription
    // -------------------------------------------------------------------------

    /**
     * Reliably triggers the race condition in {@link ImageCaptureServiceImpl#capture(File)}.
     *
     * The mock is put into "race mode": it writes the PNG and sends {@code FrameCaptureDoneCmd}
     * to {@code PlayerIO} <em>before</em> it sends the immediate ACK back to {@code RVideoIO}.
     * This means:
     * <ol>
     *   <li>{@code RVideoIO.sendCommand()} is still blocked waiting for its ACK.</li>
     *   <li>{@code PlayerIO} receives {@code FrameCaptureDoneCmd} and calls
     *       {@code eventBus.send()} — but no subscriber exists yet.</li>
     *   <li>The event is permanently lost by the {@code PublishSubject}.</li>
     *   <li>ACK finally arrives; {@code io.send()} returns; {@code blockingFirst()} subscribes.</li>
     *   <li>{@code capture()} waits 10 s then throws {@link java.util.concurrent.TimeoutException}.</li>
     * </ol>
     *
     * This is the same failure mode as real Sharktopoda on fast machines: it completes
     * the frame capture and sends {@code FrameCaptureDoneCmd} before the ACK crosses the
     * network, so the event arrives while {@code blockingFirst()} has not yet subscribed.
     *
     * The test currently logs the failure rather than failing the build so you can
     * observe the behaviour. Un-comment the {@code fail()} call to enforce it once fixed.
     */
    @Test
    @DisplayName("Race condition: FrameCaptureDoneCmd sent before ACK — reliably exposes the bug")
    void testCaptureRaceConditionDoneBeforeAck() throws Exception {
        mockSharktopoda.setRaceMode(true);
        var outputFile = tempDir.resolve("capture-race.png").toFile();

        log.info("=== TEST: race mode — FrameCaptureDoneCmd sent before ACK ===");
        try {
            var framegrab = imageCaptureService.capture(outputFile);
            if (framegrab != null && framegrab.getImage().isPresent()) {
                var img = framegrab.getImage().get();
                log.warning("capture() unexpectedly succeeded — race did not trigger");
                log.warning("  Image: " + img.getWidth(null) + "×" + img.getHeight(null));
            } else {
                log.warning("capture() returned but framegrab or image is absent");
            }
        } catch (Exception e) {
            log.severe("=== RACE CONDITION CONFIRMED ===");
            log.severe("capture() threw: " + e.getClass().getName() + ": " + e.getMessage());
            log.severe("");
            log.severe("What happened:");
            log.severe("  1. capture() created the observable — NOT yet subscribed");
            log.severe("  2. io.send(FrameCaptureCmd) began blocking for ACK in RVideoIO.sendCommand()");
            log.severe("  3. MockSharktopoda sent FrameCaptureDoneCmd to PlayerIO (done-before-ACK mode)");
            log.severe("  4. PlayerIO received it and called eventBus.send() — NO subscriber exists yet");
            log.severe("  5. PublishSubject emitted once to no subscribers; event is gone forever");
            log.severe("  6. ACK arrived; io.send() returned; blockingFirst() subscribed — too late");
            log.severe("  7. capture() waited 10 s and timed out");
            log.severe("");
            log.severe("Fix: subscribe to eventBus BEFORE calling io.send(), e.g.:");
            log.severe("  CompletableFuture<Framegrab> future = new CompletableFuture<>();");
            log.severe("  eventBus.toObserverable()");
            log.severe("      .ofType(FrameCaptureDoneCmd.class)");
            log.severe("      .map(this::captureDone)");
            log.severe("      .timeout(10, TimeUnit.SECONDS)");
            log.severe("      .subscribe(future::complete, future::completeExceptionally);");
            log.severe("  io.send(new FrameCaptureCmd(...));   // send AFTER subscription");
            log.severe("  return future.get(10, TimeUnit.SECONDS);");
            fail("Race condition: FrameCaptureDoneCmd was lost. " + e.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // Timing characterisation sweep (diagnostic / slow)
    // -------------------------------------------------------------------------

    /**
     * Sweeps through a range of response delays to find the race-condition threshold.
     *
     * Expected pattern (before fix):
     * <pre>
     *   delay &lt; ~1000 ms  →  FAIL  (event arrives before subscription)
     *   delay &gt; ~1000 ms  →  PASS  (event arrives after subscription)
     * </pre>
     *
     * <strong>Warning:</strong> each failed capture blocks for the 10-second
     * {@code ImageCaptureServiceImpl} timeout, so this test can take over a minute.
     * It is {@link org.junit.jupiter.api.Disabled @Disabled} by default; enable it
     * manually when characterising a specific machine.
     */
    @Test
    @Disabled("Slow diagnostic sweep — enable manually to characterise race-condition window")
    @DisplayName("Timing sweep: find race-condition threshold (diagnostic, slow)")
    void testCaptureTimingSweep() throws Exception {
        int[] delaysMs = {50, 200, 500, 800, 1100, 1400, 1800, 2200};

        log.info("=== TIMING SWEEP: characterising race-condition window ===");
        log.info(String.format("%-12s  %-8s  %-12s  %s",
                "delay(ms)", "result", "elapsed(ms)", "detail"));
        log.info("-".repeat(60));

        for (int delay : delaysMs) {
            mockSharktopoda.setFrameCaptureDoneDelayMs(delay);
            var outputFile = tempDir.resolve("sweep-" + delay + ".png").toFile();
            long start = System.currentTimeMillis();

            String status, detail;
            try {
                var fg = imageCaptureService.capture(outputFile);
                long elapsed = System.currentTimeMillis() - start;
                if (fg != null && fg.getImage().isPresent()) {
                    var img = fg.getImage().get();
                    status = "PASS";
                    detail = img.getWidth(null) + "×" + img.getHeight(null);
                } else {
                    status = "PASS(null)";
                    detail = "";
                }
                log.info(String.format("%-12d  %-8s  %-12d  %s", delay, status, elapsed, detail));
            } catch (Exception e) {
                long elapsed = System.currentTimeMillis() - start;
                status = "FAIL";
                detail = e.getClass().getSimpleName() + ": " + e.getMessage();
                log.warning(String.format("%-12d  %-8s  %-12d  %s", delay, status, elapsed, detail));
            }
        }

        log.info("=== TIMING SWEEP COMPLETE — review log output above ===");
    }

    // =========================================================================
    // Mock Sharktopoda
    // =========================================================================

    /**
     * Minimal UDP server that simulates Sharktopoda's frame-capture protocol.
     *
     * <ol>
     *   <li>Receives any command → sends immediate ACK on the same socket.</li>
     *   <li>Receives {@code connect} → records the caller's callback host:port.</li>
     *   <li>Receives {@code frame capture} → (after configured delay) writes a test
     *       PNG to the requested path, then sends {@code frame capture done} to the
     *       registered callback address.</li>
     * </ol>
     */
    static class MockSharktopoda {

        private static final Logger log = Logger.getLogger(MockSharktopoda.class.getName());
        private static final Gson GSON = RVideoIO.GSON;

        private final int port;
        private DatagramSocket socket;
        private final ExecutorService executor = Executors.newCachedThreadPool();
        private volatile boolean running = false;
        private volatile int frameCaptureDoneDelayMs = 500;

        /**
         * When true, sends {@code FrameCaptureDoneCmd} to {@code PlayerIO} BEFORE sending
         * the immediate ACK to {@code RVideoIO}. This reliably triggers the race condition
         * because {@code RVideoIO.socket.receive()} is still blocking when the event is
         * published to the {@code eventBus} — so {@code blockingFirst()} has not subscribed.
         */
        private volatile boolean raceMode = false;

        /** Filled in when the first {@code connect} command is received. */
        private volatile int callbackPort = -1;
        private volatile InetAddress callbackAddress;

        MockSharktopoda(int port) {
            this.port = port;
        }

        void setFrameCaptureDoneDelayMs(int ms) {
            this.frameCaptureDoneDelayMs = ms;
        }

        void setRaceMode(boolean enabled) {
            this.raceMode = enabled;
        }

        void start() throws SocketException {
            socket = new DatagramSocket(port);
            running = true;
            executor.submit(this::serverLoop);
            log.info("MockSharktopoda listening on UDP port " + port);
        }

        void stop() {
            running = false;
            if (socket != null && !socket.isClosed()) socket.close();
            executor.shutdownNow();
        }

        private void serverLoop() {
            byte[] buf = new byte[65536];
            while (running) {
                try {
                    var packet = new DatagramPacket(buf, buf.length);
                    socket.receive(packet);

                    String raw = new String(packet.getData(), 0, packet.getLength(), StandardCharsets.UTF_8);
                    log.info("MockSharktopoda <<< " + raw.strip());

                    var parsed = GSON.fromJson(raw, CommandEnvelope.class);
                    String command = parsed != null ? parsed.command : null;
                    if (command == null) {
                        // Probably an ACK response — ignore
                        continue;
                    }

                    switch (command) {
                        case "connect"       -> handleConnect(parsed, packet.getAddress(), packet.getPort());
                        case "frame capture" -> handleFrameCapture(parsed, packet.getAddress(), packet.getPort());
                        default              -> sendAck(command, packet.getAddress(), packet.getPort());
                    }
                } catch (Exception e) {
                    if (running) log.warning("MockSharktopoda: " + e.getMessage());
                }
            }
        }

        private void handleConnect(CommandEnvelope env, InetAddress sender, int senderPort) throws IOException {
            callbackPort = env.port;
            try {
                callbackAddress = (env.host != null && !env.host.isBlank())
                        ? InetAddress.getByName(env.host)
                        : sender;
            } catch (UnknownHostException e) {
                log.warning("MockSharktopoda: cannot resolve '" + env.host + "', using sender");
                callbackAddress = sender;
            }
            log.info("MockSharktopoda: callback registered → "
                    + callbackAddress.getHostAddress() + ":" + callbackPort);
            sendAck("connect", sender, senderPort);
        }

        private void handleFrameCapture(CommandEnvelope env, InetAddress sender, int senderPort)
                throws IOException {
            if (raceMode) {
                handleFrameCaptureRaceMode(env, sender, senderPort);
            } else {
                handleFrameCaptureNormalMode(env, sender, senderPort);
            }
        }

        /**
         * Normal mode: send ACK first, then after {@code frameCaptureDoneDelayMs} write
         * the PNG and send {@code FrameCaptureDoneCmd}.  At delays > ~1 s,
         * {@code blockingFirst()} is already subscribed when the done event arrives.
         */
        private void handleFrameCaptureNormalMode(CommandEnvelope env, InetAddress sender, int senderPort)
                throws IOException {
            log.info("MockSharktopoda: FrameCapture (normal) → " + env.imageLocation
                    + "  (done in " + frameCaptureDoneDelayMs + " ms)");

            sendAck("frame capture", sender, senderPort);

            final int delay = frameCaptureDoneDelayMs;
            final CommandEnvelope snapshot = env;
            executor.submit(() -> {
                try {
                    Thread.sleep(delay);
                    writeTestPng(snapshot.imageLocation);
                    sendFrameCaptureDone(snapshot.uuid, snapshot.imageReferenceUuid,
                            snapshot.imageLocation, 5000L);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                } catch (Exception ex) {
                    log.warning("MockSharktopoda async handler: " + ex.getMessage());
                }
            });
        }

        /**
         * Race mode: write the PNG and send {@code FrameCaptureDoneCmd} to {@code PlayerIO}
         * FIRST, then (after a short pause to ensure delivery) send the ACK back to
         * {@code RVideoIO}.
         *
         * <p>While {@code RVideoIO.sendCommand()} is blocked waiting for the ACK,
         * {@code PlayerIO} receives {@code FrameCaptureDoneCmd} on its background thread
         * and calls {@code eventBus.send()}.  Because {@code blockingFirst()} has not yet
         * subscribed (it is called only after {@code io.send()} returns), the
         * {@code PublishSubject} emits to zero subscribers and the event is lost.</p>
         */
        private void handleFrameCaptureRaceMode(CommandEnvelope env, InetAddress sender, int senderPort)
                throws IOException {
            log.info("MockSharktopoda: FrameCapture (RACE MODE) → " + env.imageLocation
                    + "  — sending FrameCaptureDoneCmd BEFORE ACK");

            // Write image and send done FIRST — PlayerIO will publish to eventBus
            // while RVideoIO is still waiting for the ACK below.
            try {
                writeTestPng(env.imageLocation);
                sendFrameCaptureDone(env.uuid, env.imageReferenceUuid, env.imageLocation, 5000L);
                // Brief pause to give PlayerIO time to process the done event before the ACK
                // unblocks RVideoIO and allows blockingFirst() to subscribe.
                Thread.sleep(50);
            } catch (Exception e) {
                log.warning("MockSharktopoda race setup failed: " + e.getMessage());
            }

            // Now send the ACK — this releases RVideoIO.sendCommand(), after which
            // capture() will call blockingFirst() and find an empty eventBus.
            sendAck("frame capture", sender, senderPort);
        }

        private void writeTestPng(String imageLocation) throws IOException {
            if (imageLocation == null || imageLocation.isBlank()) return;
            var file = new File(imageLocation);
            file.getParentFile().mkdirs();
            var img = new BufferedImage(320, 240, BufferedImage.TYPE_INT_RGB);
            var g = img.createGraphics();
            g.setColor(Color.DARK_GRAY);
            g.fillRect(0, 0, 320, 240);
            g.setColor(Color.WHITE);
            g.setFont(new Font("SansSerif", Font.BOLD, 14));
            g.drawString("MockSharktopoda", 95, 115);
            g.drawString("Test Frame Capture", 78, 135);
            g.dispose();
            ImageIO.write(img, "png", file);
            log.info("MockSharktopoda: wrote " + file.length() + " bytes to " + imageLocation);
        }

        private void sendFrameCaptureDone(UUID videoUuid, UUID imageReferenceUuid,
                                          String imageLocation, long elapsedMs) {
            if (callbackPort <= 0 || callbackAddress == null) {
                log.severe("MockSharktopoda: no callback registered — cannot send FrameCaptureDoneCmd");
                return;
            }

            var cmd = new FrameCaptureDoneCmd(videoUuid, imageReferenceUuid, imageLocation, elapsedMs, "ok");
            String json = GSON.toJson(cmd.getValue());
            log.info("MockSharktopoda: sending FrameCaptureDoneCmd to "
                    + callbackAddress.getHostAddress() + ":" + callbackPort);
            log.info("MockSharktopoda >>> " + json.strip());

            byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
            var packet = new DatagramPacket(bytes, bytes.length, callbackAddress, callbackPort);
            try (var s = new DatagramSocket()) {
                s.send(packet);
                log.info("MockSharktopoda: FrameCaptureDoneCmd sent");
            } catch (IOException e) {
                log.severe("MockSharktopoda: FAILED to send FrameCaptureDoneCmd: " + e.getMessage());
            }
        }

        private void sendAck(String command, InetAddress address, int port) throws IOException {
            String ack = "{\"response\":\"" + command + "\",\"status\":\"ok\"}";
            byte[] bytes = ack.getBytes(StandardCharsets.UTF_8);
            socket.send(new DatagramPacket(bytes, bytes.length, address, port));
            log.info("MockSharktopoda: >>> ACK '" + command + "'");
        }

        /**
         * Catch-all POJO that GSON can deserialise from any command JSON.
         * Fields not present in a given command are simply left null.
         */
        static class CommandEnvelope {
            String command;
            UUID uuid;
            UUID imageReferenceUuid;
            String imageLocation;
            // ConnectCmd fields
            Integer port;
            String host;
        }
    }
}
