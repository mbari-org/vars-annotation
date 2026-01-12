/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package mbarix4j.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

/**
 *
 * @author brian
 */
public class IOUtilities {

    /**
     * Copy ALL available data from one stream into another
     * @param in
     * @param out
     * @throws IOException
     */
    public static void copy(InputStream in, OutputStream out) throws IOException {
        ReadableByteChannel source = Channels.newChannel(in);
        WritableByteChannel target = Channels.newChannel(out);

        ByteBuffer buffer = ByteBuffer.allocate(16 * 1024);
        while (source.read(buffer) != -1) {
            buffer.flip(); // Prepare the buffer to be drained
            while (buffer.hasRemaining()) {
                target.write(buffer);
            }
            buffer.clear(); // Empty buffer to get ready for filling
        }

        source.close();
        target.close();

    }

}
