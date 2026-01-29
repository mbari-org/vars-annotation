package org.mbari.vars.annotation.services.raziel;

import org.mbari.vars.annotation.etc.jdk.Loggers;
import org.mbari.vars.annotation.etc.jdk.crypto.AES;
import org.mbari.vars.annotation.ui.Initializer;
import org.mbari.vars.raziel.sdk.r1.RazielKiotaClient;
import org.mbari.vars.raziel.sdk.r1.models.BearerAuth;
import org.mbari.vars.raziel.sdk.r1.models.EndpointConfig;
import org.mbari.vars.raziel.sdk.r1.models.ServiceStatus;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class Raziel {

    public record ConnectionParams(URL url, String username, String password) {

        public void write(Path file, AES aes) throws IOException {
            var s = url.toExternalForm() + "\n" + aes.encrypt(username) + "\n" + aes.encrypt(password);
            Files.writeString(file, s, StandardCharsets.UTF_8);
        }

        public static Optional<ConnectionParams> read(Path file, AES aes) {
            if (Files.exists(file)) {
                try {
                    var lines = Files.readAllLines(file);
                    var url = new URL(lines.get(0));
                    var username  = aes.decrypt(lines.get(1));
                    var password = aes.decrypt(lines.get(2));
                    return Optional.of(new ConnectionParams(url, username, password));
                }
                catch (Exception e) {
                    new Loggers(ConnectionParams.class)
                            .atWarn()
                            .log(() -> "The file at " + file + " does not contain valid connection info");
                    return Optional.empty();
                }
            }
            return Optional.empty();
        }

        public static Path path() {
            var settingsDirectory = Initializer.getSettingsDirectory();
            return settingsDirectory.resolve("raziel.txt");
        }

        public static Optional<ConnectionParams> load() {
            var path = path();
            if (Files.exists(path)) {
                return ConnectionParams.read(path, Initializer.getAes());
            }
            return Optional.empty();
        }
    }

    private URI correctUrl(URL baseUrl) {
        return baseUrl.toExternalForm().endsWith("/config")
                ? URI.create(baseUrl.toExternalForm().substring(0, baseUrl.toExternalForm().length() - "/config".length()))
                : URI.create(baseUrl.toExternalForm());
    }

    public CompletableFuture<BearerAuth> authenticate(URL baseUrl, String username, String password) {
        var razielUrl = correctUrl(baseUrl);
        var client = new RazielKiotaClient(razielUrl);
        return client.authenticate(username, password);
    }


    public CompletableFuture<List<EndpointConfig>> endpoints(URL baseUrl, String jwt) {
        var razielUrl = correctUrl(baseUrl);
        var client = new RazielKiotaClient(razielUrl);
        return client.endpoints(jwt);
    }

    public CompletableFuture<List<ServiceStatus>> healthStatus(URL baseUrl) {
        var razielUrl = correctUrl(baseUrl);
        var client = new RazielKiotaClient(razielUrl);
        return client.healthStatus();
    }



}
