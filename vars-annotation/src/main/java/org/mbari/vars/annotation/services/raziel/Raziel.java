package org.mbari.vars.annotation.services.raziel;

import org.mbari.vars.annotation.etc.jdk.Loggers;
import org.mbari.vars.annotation.ui.Initializer;
import org.mbari.vars.raziel.sdk.r1.RazielKiotaClient;
import org.mbari.vars.raziel.sdk.r1.models.BearerAuth;
import org.mbari.vars.raziel.sdk.r1.models.EndpointConfig;
import org.mbari.vars.raziel.sdk.r1.models.EndpointStatus;
import org.mbari.vars.raziel.sdk.r1.models.ServiceStatus;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class Raziel {

    private static final Loggers log = new Loggers(Raziel.class);

    /**
     * The credentials a user types into the UI. These are only held in memory. What gets saved to disk
     * is a {@link LoginFile}, which Raziel builds from these (see {@link #encode(ConnectionParams)}).
     */
    public record ConnectionParams(URL url, String username, String password) {
    }

    /**
     * The contents of <code>raziel.txt</code>: the text returned by Raziel's <code>/config/auth/encode</code>
     * endpoint. It's three lines (the Raziel url, the AES-encoded username, the AES-encoded password).
     * The client can't decode it, it can only send it back to <code>/config/auth/login</code> in exchange
     * for an access token. We keep the text exactly as Raziel sent it.
     *
     * @param url The Raziel url (the first line of the file)
     * @param content The complete text of the login file
     */
    public record LoginFile(URL url, String content) {

        /**
         * @param content The text of a login file
         * @return The parsed login file, or empty if the text isn't a 3-line login file that starts with a url
         */
        public static Optional<LoginFile> parse(String content) {
            if (content == null) {
                return Optional.empty();
            }
            var lines = content.strip().lines().toList();
            if (lines.size() < 3 || lines.stream().anyMatch(String::isBlank)) {
                return Optional.empty();
            }
            try {
                var url = URI.create(lines.get(0).strip()).toURL();
                return Optional.of(new LoginFile(url, content));
            }
            catch (Exception e) {
                return Optional.empty();
            }
        }

        public void write(Path file) throws IOException {
            Files.writeString(file, content, StandardCharsets.UTF_8);
        }

        public static Optional<LoginFile> read(Path file) {
            if (Files.exists(file)) {
                log.atInfo().log("Reading Raziel login file: " + file);
                try {
                    var loginFile = parse(Files.readString(file, StandardCharsets.UTF_8));
                    if (loginFile.isEmpty()) {
                        log.atWarn().log(() -> "The file at " + file + " is not a valid Raziel login file");
                    }
                    return loginFile;
                }
                catch (IOException e) {
                    log.atWarn().withCause(e).log(() -> "Unable to read the Raziel login file at " + file);
                }
            }
            return Optional.empty();
        }

        public static Path path() {
            var settingsDirectory = Initializer.getSettingsDirectory();
            return settingsDirectory.resolve("raziel.txt");
        }

        public static Optional<LoginFile> load() {
            return read(path());
        }
    }

    private static URI correctUrl(URL baseUrl) {
        return baseUrl.toExternalForm().endsWith("/config")
                ? URI.create(baseUrl.toExternalForm().substring(0, baseUrl.toExternalForm().length() - "/config".length()))
                : URI.create(baseUrl.toExternalForm());
    }

    public static CompletableFuture<BearerAuth> authenticate(URL baseUrl, String username, String password) {
        var client = newClient(baseUrl);
        return client.authenticate(username, password);
    }


    /**
     * Asks Raziel to build the login file for a user. The credentials are checked by Raziel (an invalid
     * user/password completes the future exceptionally) and encoded on the server.
     *
     * @param params The user's credentials and the Raziel url
     * @return The login file to save as raziel.txt
     */
    public static CompletableFuture<LoginFile> encode(ConnectionParams params) {
        var client = newClient(params.url());
        return client.encode(params.username(), params.password(), params.url().toExternalForm())
                .thenApply(text -> LoginFile.parse(text)
                        .orElseThrow(() -> new IllegalStateException(
                                "Raziel returned something that is not a login file")));
    }

    /**
     * Exchanges a login file (raziel.txt) for an access token.
     *
     * @return An authentication token (Bearer)
     */
    public static CompletableFuture<BearerAuth> login(LoginFile loginFile) {
        var client = newClient(loginFile.url());
        return client.login(loginFile.content());
    }

    public static CompletableFuture<List<EndpointConfig>> endpoints(URL baseUrl, String jwt) {
        var client = newClient(baseUrl);
        return client.endpoints(jwt);
    }

    public static CompletableFuture<List<ServiceStatus>> healthStatus(URL baseUrl) {
        var client = newClient(baseUrl);
        return client.healthStatus();
    }

    public static CompletableFuture<Set<EndpointStatus>> checkStatus(URL baseUrl, String username, String password) {
        var client = newClient(baseUrl);
        return client.checkStatus(username, password);
    }


    public static RazielKiotaClient newClient(URL baseUrl) {
        return new RazielKiotaClient(correctUrl(baseUrl));
    }

}
