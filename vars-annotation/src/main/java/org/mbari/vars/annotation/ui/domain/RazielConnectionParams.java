package org.mbari.vars.annotation.ui.domain;

import org.mbari.vars.core.crypto.AES;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

public record RazielConnectionParams(URL url, String username, String password) {

    public void write(Path file, AES aes) throws IOException {
        var s = url.toExternalForm() + "\n" + aes.encrypt(username) + "\n" + aes.encrypt(password);
        Files.writeString(file, s, StandardCharsets.UTF_8);
    }

    public static Optional<RazielConnectionParams> read(Path file, AES aes) {
        if (Files.exists(file)) {
            try {
                var lines = Files.readAllLines(file);
                var url = URI.create(lines.get(0)).toURL();
                var username  = aes.decrypt(lines.get(1));
                var password = aes.decrypt(lines.get(2));
                return Optional.of(new RazielConnectionParams(url, username, password));
            }
            catch (Exception e) {
                LoggerFactory
                        .getLogger(RazielConnectionParams.class)
                        .atWarn()
                        .log(() -> "The file at " + file + " does not contain valid connection info");
                return Optional.empty();
            }
        }
        return Optional.empty();
    }
}
