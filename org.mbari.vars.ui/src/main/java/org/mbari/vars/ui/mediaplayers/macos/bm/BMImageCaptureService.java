package org.mbari.vars.ui.mediaplayers.macos.bm;

import org.mbari.vars.services.ImageCaptureService;
import org.mbari.vars.services.model.Framegrab;

import java.io.File;

public class BMImageCaptureService implements ImageCaptureService {

    @Override
    public Framegrab capture(File file) {
        return null;
    }

    @Override
    public void dispose() {

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