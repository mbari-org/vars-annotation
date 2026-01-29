package org.mbari.vars.annotation.ui.mediaplayers.macos.bm;

import io.reactivex.rxjava3.schedulers.Schedulers;
import io.reactivex.rxjava3.subjects.PublishSubject;
import org.mbari.vars.annotation.etc.rxjava.EventBus;
import org.mbari.vars.annotation.services.ImageCaptureService;
import org.mbari.vars.services.model.Framegrab;
import org.mbari.vars.annotation.ui.Initializer;
import org.mbari.vars.annotation.ui.messages.ShowExceptionAlert;
import org.mbari.vcr4j.VideoIndex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.ResourceBundle;
import java.util.concurrent.*;

public class RXImageCaptureServiceImpl implements ImageCaptureService {

    private static final Logger log = LoggerFactory.getLogger(RXImageCaptureServiceImpl.class);
    private final String host;
    private final int port;
    private final String apiKey;
    private final Duration timeout;
    private final EventBus eventBus;
    private final ResourceBundle i18n;
    private final PublishSubject<ExtendedFramegrab> framegrabs = PublishSubject.create();
    private volatile boolean doDispose = false;

    // Create a runnable/queue like commandmanager so that
    //        all socket requests are done on the same thread
    private final BlockingQueue<File> pendingQueue = new LinkedBlockingQueue<>();


    private final Thread thread;


    public RXImageCaptureServiceImpl(String host,
                                   int port,
                                   String apiKey,
                                   Duration timeout,
                                   EventBus eventBus,
                                   ResourceBundle i18n) {
        this.host = host;
        this.port = port;
        this.apiKey = apiKey;
        this.timeout = timeout;
        this.eventBus = eventBus;
        this.i18n = i18n;

        thread = new Thread(buildRunnable(), getClass().getName());
        thread.setDaemon(true);
        thread.start();

        framegrabs.doOnComplete(() -> log.atDebug().log(() -> "Closing framegrabs RX subject"));
    }

    @Override
    public Framegrab capture(File file) {
        var future = new CompletableFuture<Framegrab>();
        var subscription = framegrabs.subscribeOn(Schedulers.io())
                .filter(ef -> ef.file().toString().equals(file.toString()))
                .map(ExtendedFramegrab::framegrab)
                .subscribe(future::complete, future::completeExceptionally);
        var framegrab = new Framegrab();
        try {
            if (pendingQueue.offer(file)) {
                framegrab = future.get(timeout.toMillis(), TimeUnit.MILLISECONDS);
            }
        }
        catch (Exception e) {
            log.atWarn().setCause(e).log("Failed to capture framegrab");
        }
        subscription.dispose();
        return framegrab;
    }

    @Override
    public void dispose() {
        log.atDebug().log("Disposing ImageCaptureServiceImpl");
        doDispose = true;
        framegrabs.onComplete();
//        thread.interrupt();
    }

    private Runnable buildRunnable() {
        return () -> {

            // -- Connect Socket
            Socket socket = null;
            Writer outToSocket = null;
            BufferedReader inFromSocket = null;
            try {
                var inetAddress = InetAddress.getByName(host);
                var address = new InetSocketAddress(inetAddress, port);
                socket = new Socket();
                socket.setTcpNoDelay(true); //to disable Nagle's algorithm
                socket.setSoLinger(false, 0); // so socket doesn't stay open for a short time after close
                socket.setSoTimeout((int) timeout.toMillis()); // timeout in ms for the socket to wait for a response
                socket.connect(address, (int) timeout.toMillis());
                outToSocket = new PrintWriter(socket.getOutputStream());
                inFromSocket = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            }
            catch (Exception e) {
                log.warn("Failed to connect to TCP socket at " + host + ":" + port);
            }

            var ok = isSocketConnected(socket);

            while (ok) {
                File file = null;
                try {
                    file = pendingQueue.poll(timeout.toSeconds(), TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    // TODO handle error via event
                }
                if (file != null) {
                    var now = Instant.now();
                    var framegrab = new Framegrab();
                    var path = file.toPath().toAbsolutePath().normalize();
                    var success = requestFramegrab(outToSocket, inFromSocket, path);
                    if (success) {
                        try {
//                            Thread.sleep(1000); // HACK. libbmagic doesn't finish writing the file before sending OK
                            BufferedImage image = ImageIO.read(file);
                            framegrab.setImage(image);
                            framegrab.setVideoIndex(new VideoIndex(now));
                        } catch (Exception e) {
                            log.warn("Image capture failed. Unable to read image back off disk", e);
                            sendError("mediaplayer.macos.bm.error.nofg.content", e);
                        }
                    }
                    framegrabs.onNext(new ExtendedFramegrab(file, framegrab));
                }
                ok = isSocketConnected(socket) && !doDispose;
            }


            try {
                if (outToSocket != null) outToSocket.close();
                if (inFromSocket != null) inFromSocket.close();
                if (socket != null) socket.close();
            } catch (IOException e) {
                log.atError().setCause(e).log("Failed to close socket");
            }

        };
    }

    private static boolean isSocketConnected(Socket socket) {
        return socket != null && socket.isConnected() && !socket.isClosed();
    }

    private void sendError(String contentKey, Exception e) {
        var title = i18n.getString("mediaplayer.macos.bm.error.title");
        var content = i18n.getString(contentKey) + " " +
                host + ":" + port;
        var header = i18n.getString("mediaplayer.macos.bm.error.header");
        eventBus.send(new ShowExceptionAlert(title, header, content,e));
    }

    /**
     * This methods blocks waiting for a response from the remote server
     * @param path
     * @return
     */
    private boolean requestFramegrab(Writer outToSocket, BufferedReader inFromSocket, Path path) {
        boolean success = false;

        var cmd = apiKey + "," + path + "\n"; // \n terminated CSV string

        try {
            log.atDebug().log(() -> "Sending command: " + cmd);
            outToSocket.write(cmd);
            outToSocket.flush();
        }
        catch (IOException e) {
            log.atWarn()
                    .setCause(e)
                    .log(() -> "Unable to send command to remote server: " + cmd);
            sendError("mediaplayer.macos.bm.error.out.content", e);
        }

        try {
            String response = null;
            while (response == null) {
                response =  inFromSocket.readLine();
            }
            log.atDebug().log("Received response: " + response);
            success = response.endsWith("OK");
        }
        catch (IOException e) {
            log.atWarn()
                    .setCause(e)
                    .log(() -> "Unable to receive response from remote server");
            sendError("mediaplayer.macos.bm.error.in.content", e);
        }

        return success;
    }

    public static RXImageCaptureServiceImpl newInstance() {
        return new RXImageCaptureServiceImpl(Settings.getHost(),
                Settings.getPort(),
                Settings.getApiKey(),
                Duration.ofSeconds(Settings.getTimeout()),
                Initializer.getToolBox().getEventBus(),
                Initializer.getToolBox().getI18nBundle());
    }
}
