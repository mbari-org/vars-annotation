package org.mbari.vars.ui.mediaplayers.macos.bm;

import org.mbari.vars.core.EventBus;
import org.mbari.vars.services.ImageCaptureService;
import org.mbari.vars.services.model.Framegrab;
import org.mbari.vars.ui.Initializer;
import org.mbari.vars.ui.mediaplayers.MediaPlayer;
import org.mbari.vars.ui.messages.ShowExceptionAlert;
import org.mbari.vcr4j.VideoError;
import org.mbari.vcr4j.VideoIndex;
import org.mbari.vcr4j.VideoState;
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
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.TimeUnit;

public class ImageCaptureServiceImpl implements ImageCaptureService {

    private static final Logger log = LoggerFactory.getLogger(ImageCaptureServiceImpl.class);
    private final String host;
    private final int port;
    private final String apiKey;
    private final Duration timeout;
    private final EventBus eventBus;
    private final ResourceBundle i18n;
    private Socket socket;
    private Writer outToSocket;
    private BufferedReader inFromSocket;


    public ImageCaptureServiceImpl(String host,
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
    }


    @Override
    public Framegrab capture(File file) {
        var path = file.toPath().toAbsolutePath().normalize();
        Framegrab framegrab = new Framegrab();
        // TODO - verify that the framegrab and video align at this index.
        //       if not, maybe index before and after framegrab and average the time
        //      we don't need to request time from the mediaplayer as this
        //      class is only used for real-time capture
        framegrab.setVideoIndex(new VideoIndex(Instant.now()));
        var success = requestFramegrab(path);
        if (success) {
            try {
                BufferedImage image = ImageIO.read(file);
                framegrab.setImage(image);
            } catch (Exception e) {
                log.warn("Image capture failed. Unable to read image back off disk", e);
            }
        }
        return framegrab;
    }


    /**
     * This methods blocks waiting for a response from the remote server
     * @param path
     * @return
     */
    private boolean requestFramegrab(Path path) {
        boolean success = false;
        initSocket();
        if (isSocketConnected(socket)) {
            var cmd = apiKey + "," + path + "\n"; // \n terminated CSV string

            try {
                outToSocket.write(cmd);
                outToSocket.flush();
            }
            catch (IOException e) {
                sendError("mediaplayer.macos.bm.error.out.content", e);
            }

            try {
                String response = null;
                while (response == null) {
                    response =  inFromSocket.readLine();
                }
                success = response.endsWith("OK");
            }
            catch (IOException e) {
                sendError("mediaplayer.macos.bm.error.in.content", e);
            }
        }
        return success;
    }

    @Override
    public void dispose() {
        try {
            outToSocket.close();
            inFromSocket.close();
            socket.close();
        }
        catch (IOException e) {
            sendError("mediaplayer.macos.bm.error.close.content", e);
        }
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

    private void initSocket() {
        if (!isSocketConnected(socket)) {
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
        }
    }

    public static ImageCaptureServiceImpl newInstance() {
        return new ImageCaptureServiceImpl(Settings.getHost(),
                Settings.getPort(),
                Settings.getApiKey(),
                Duration.ofSeconds(Settings.getTimeout()),
                Initializer.getToolBox().getEventBus(),
                Initializer.getToolBox().getI18nBundle());
    }
}

/*

#!/usr/bin/env scala-cli

import java.net.{InetAddress, InetSocketAddress, Socket}
import java.io.{BufferedReader, InputStreamReader, PrintWriter}
import java.nio.file.Paths

// using scala 3.1.0

if (args.length != 4) {
  println("Usage: TcpClient.sc <host> <port> <apikey> <file>")
  System.exit(1)
}

val hostname = args(0)
val port = args(1).toInt
val key = args(2)
val path = Paths.get(args(3))

// Connect to the server w/ 5 second timeout
val host = InetAddress.getByName(hostname)
val address = InetSocketAddress(host, port)
val socket = Socket(hostname, port)
// socket.connect(address, 5000)

val outToServer = PrintWriter(socket.getOutputStream())
val inFromServer = BufferedReader(InputStreamReader(socket.getInputStream()))

val msg = s"$key,$path\n" // must be \n terminated
println(s"SEND: $msg")

outToServer.write(msg)
outToServer.flush()

var response: String = null
while (response == null) {
  response = inFromServer.readLine()
  println(s"RECV: $response")
}

outToServer.close()
inFromServer.close()
socket.close()

 */