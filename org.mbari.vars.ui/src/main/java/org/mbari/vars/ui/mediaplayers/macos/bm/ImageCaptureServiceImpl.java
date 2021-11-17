package org.mbari.vars.ui.mediaplayers.macos.bm;

import org.mbari.vars.services.ImageCaptureService;
import org.mbari.vars.services.model.Framegrab;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

public class ImageCaptureServiceImpl implements ImageCaptureService {

    private static final Logger log = LoggerFactory.getLogger(ImageCaptureServiceImpl.class);
    private final String host;
    private final int port;
    private final String apiKey;
    private Socket socket;


    public ImageCaptureServiceImpl(String host, int port, String apiKey) {
        this.host = host;
        this.port = port;
        this.apiKey = apiKey;
    }



    @Override
    public Framegrab capture(File file) {
        

    }

    @Override
    public void dispose() {

    }

    private static boolean isSocketConnected(Socket socket) {
        return socket != null && socket.isConnected() && !socket.isClosed();
    }

    private void initSocket() {
        if (!isSocketConnected(socket)) {
            try {
                var inetAddress = InetAddress.getByName(host);
                var address = new InetSocketAddress(inetAddress, port);
                socket = new Socket();
                socket.setTcpNoDelay(true); //to disable Nagle's algorithm
                socket.setSoLinger(false, 0); // so socket doenst' stay open for a short time after close
                socket.setSoTimeout(10000); // timeout in ms for the socket to wait for a response
                socket.connect(address, 5000);
            }
            catch (Exception e) {
                log.warn("Failed to connect to TCP socket at " + host + ":" + port);
            }
        }
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