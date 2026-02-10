package org.mbari.vars.annotation.test.etc.jdk.crypto;

import org.junit.jupiter.api.Test;
import org.mbari.vars.annotation.etc.jdk.crypto.AES;

import static org.junit.jupiter.api.Assertions.*;

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
