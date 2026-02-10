package org.mbari.vars.annotation.test.ui.domain;



import org.junit.jupiter.api.Test;
import org.mbari.vars.annotation.etc.jdk.crypto.AES;
import org.mbari.vars.annotation.services.raziel.Raziel;

import static org.junit.jupiter.api.Assertions.*;

import java.net.URL;
import java.nio.file.Files;

public class RazielConnectionParamsTest {

    @Test
    public void roundTripTest() {

        try {
            var aes = new AES("foobar");
            var rcp = new Raziel.ConnectionParams(new URL("http://localhost:8080/"), "brian@mbari.org", "sooperscrtpwrd");

            var path = Files.createTempFile("vars-raziel-test", ".txt");
            rcp.write(path, aes);
//            var msg = String.join("\n", Files.readAllLines(path));
//            System.out.println(msg);

            var opt = Raziel.ConnectionParams.read(path, aes);
            assertTrue(opt.isPresent());
            var newRcp = opt.get();
            Files.delete(path);
            assertEquals(rcp, newRcp);
        }
        catch (Exception e) {
//            e.printStackTrace();
            fail();
        }
    }
}
