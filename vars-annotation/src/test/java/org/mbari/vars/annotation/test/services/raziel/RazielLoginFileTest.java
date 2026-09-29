package org.mbari.vars.annotation.test.services.raziel;

import org.junit.jupiter.api.Test;
import org.mbari.vars.annotation.services.raziel.Raziel;

import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RazielLoginFileTest {

    // What /config/auth/encode returns: url, AES-encoded username, AES-encoded password
    private static final String LOGIN_FILE = "http://localhost:8400/config\nQUVTLXVzZXI=\nQUVTLXB3ZA==";

    @Test
    public void parseTakesUrlFromFirstLineAndKeepsTextVerbatim() {
        var opt = Raziel.LoginFile.parse(LOGIN_FILE);
        assertTrue(opt.isPresent());
        var loginFile = opt.get();
        assertEquals("http://localhost:8400/config", loginFile.url().toExternalForm());
        assertEquals(LOGIN_FILE, loginFile.content());
    }

    @Test
    public void parseIgnoresSurroundingWhitespaceInTheUrlLine() {
        var opt = Raziel.LoginFile.parse("  http://localhost:8400/config \nabc\ndef\n");
        assertTrue(opt.isPresent());
        assertEquals("http://localhost:8400/config", opt.get().url().toExternalForm());
    }

    @Test
    public void parseRejectsAnythingThatIsNotAThreeLineLoginFile() {
        assertFalse(Raziel.LoginFile.parse(null).isPresent());
        assertFalse(Raziel.LoginFile.parse("").isPresent());
        assertFalse(Raziel.LoginFile.parse("http://localhost:8400/config").isPresent());
        assertFalse(Raziel.LoginFile.parse("http://localhost:8400/config\nonlyone").isPresent());
        assertFalse(Raziel.LoginFile.parse("not a url\nabc\ndef").isPresent());
    }

    @Test
    public void writeThenReadRoundTrips() throws Exception {
        var path = Files.createTempFile("vars-raziel-test", ".txt");
        try {
            var loginFile = Raziel.LoginFile.parse(LOGIN_FILE).orElseThrow();
            loginFile.write(path);
            assertEquals(LOGIN_FILE, Files.readString(path)); // stored exactly as the server sent it
            assertEquals(loginFile, Raziel.LoginFile.read(path).orElseThrow());
        }
        finally {
            Files.deleteIfExists(path);
        }
    }

    @Test
    public void readOfMissingFileIsEmpty() throws Exception {
        var path = Files.createTempFile("vars-raziel-test", ".txt");
        Files.delete(path);
        assertFalse(Raziel.LoginFile.read(path).isPresent());
    }
}
