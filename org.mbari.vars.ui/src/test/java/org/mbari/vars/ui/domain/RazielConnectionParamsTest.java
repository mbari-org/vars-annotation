package org.mbari.vars.ui.domain;

import org.junit.Test;
import static org.junit.Assert.*;
import org.mbari.vars.core.crypto.AES;

import java.net.URL;
import java.nio.file.Files;

public class RazielConnectionParamsTest {

    @Test
    public void roundTripTest() {

        try {
            var aes = new AES("foobar");
            var rcp = new RazielConnectionParams(new URL("http://localhost:8080/"), "brian@mbari.org", "sooperscrtpwrd");

            var path = Files.createTempFile("vars-raziel-test", ".txt");
            rcp.write(path, aes);
//            var msg = String.join("\n", Files.readAllLines(path));
//            System.out.println(msg);

            var opt = RazielConnectionParams.read(path, aes);
            assertTrue(opt.isPresent());
            var newRcp = opt.get();
            Files.delete(path);
            assertEquals(rcp, newRcp);
        }
        catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }
}
