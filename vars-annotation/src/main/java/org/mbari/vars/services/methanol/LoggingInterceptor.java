package org.mbari.vars.services.methanol;

import com.github.mizosoft.methanol.Methanol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public final class LoggingInterceptor implements Methanol.Interceptor {
    private static final Logger logger = LoggerFactory.getLogger(LoggingInterceptor.class.getName());

    @Override
    public <T> HttpResponse<T> intercept(HttpRequest request, Chain<T> chain)
            throws IOException, InterruptedException {
        logRequest(request);
        return toLoggingChain(request, chain).forward(request);
    }

    @Override
    public <T> CompletableFuture<HttpResponse<T>> interceptAsync(
            HttpRequest request, Chain<T> chain) {
        logRequest(request);
        return toLoggingChain(request, chain).forwardAsync(request);
    }

    private static void logRequest(HttpRequest request) {
        logger.atInfo().log(() -> String.format("Sending %s%n%s", request, headersToString(request.headers())));
    }

    private static <T> Chain<T> toLoggingChain(HttpRequest request, Chain<T> chain) {
        var sentAt = Instant.now();
        return chain.withBodyHandler(responseInfo -> {
            logger.atInfo().log(() -> String.format(
                    "Completed %s %s with %d in %sms%n%s",
                    request.method(),
                    request.uri(),
                    responseInfo.statusCode(),
                    Duration.between(sentAt, Instant.now()).toMillis(),
                    headersToString(responseInfo.headers())));
            
            // Apply the original BodyHandler
            return chain.bodyHandler().apply(responseInfo);
        });
    }

    private static String headersToString(HttpHeaders headers) {
        return headers.map().entrySet().stream()
                .map(entry -> entry.getKey() + ": " + String.join(", ", entry.getValue()))
                .collect(Collectors.joining(System.lineSeparator()));
    }
}