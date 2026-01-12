package org.mbari.vars.core.crypto;

import org.junit.Test;
import static org.junit.Assert.*;

public class AESTest {
    @Test
    public void roundTripTest() {
        var s = "abcdefGhi89701?!_)";
        var aes = new AES("foobar");
        var e = aes.encrypt(s);
        var d = aes.decrypt(e);
        assertNotEquals(s, e);
        assertEquals(s, d);
    }
}
