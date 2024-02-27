package org.mbari.vars.core.util;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Fluent API for logging. This is a simple wrapper around the Java 9+ System.Logger API. It
 * provides a fluent API for logging.
 *
 * @param logger The logger to use
 * @param level The logging level
 * @param cause The cause of the log message
 */
public record Logging(Logger logger, Level level, Throwable cause) {

    public Logging(Logger logger) {
        this(logger, Level.OFF, null);
    }

    public Logging(String loggerName) {
        this(System.getLogger(loggerName), Level.OFF, null);
    }

    public Logging(Class<?> clazz) {
        this(clazz.getName());
    }

    public Logging atTrace() {
        return new Logging(logger, Level.TRACE, cause);
    }

    public Logging atDebug() {
        return new Logging(logger, Level.DEBUG, cause);
    }

    public Logging atInfo() {
        return new Logging(logger, Level.INFO, cause);
    }

    public Logging atWarn() {
        return new Logging(logger, Level.WARNING, cause);
    }

    public Logging atError() {
        return new Logging(logger, Level.ERROR, cause);
    }

    public Logging withCause(Throwable t) {
        return new Logging(logger, level, t);
    }

    /**
     * Log a message
     * @param msg The message to log
     */
    public void log(String msg) {
        if (logger.isLoggable(level)) {
            if (cause == null) {
                logger.log(level, msg);
            } else {
                logger.log(level, msg, cause);
            }
        }
    }

    /**
     * Log a message
     * @param fn A supplier that returns the message to log
     */
    public void log(Supplier<String> fn) {
        if (logger.isLoggable(level)) {
            if (cause == null) {
                logger.log(level, fn);
            } else {
                logger.log(level, fn, cause);
            }
        }
    }

    /**
     * Log an HTTP response
     * @param response The response to log
     */
    public void logResponse(HttpResponse<?> response) {
        if (logger.isLoggable(level)) {
            var req = response.request();
            var headers = response.headers().map().entrySet().stream()
                    .map(e -> e.getKey() + ": " + String.join(", ", e.getValue()))
                    .collect(Collectors.joining(System.lineSeparator()));
            log(() -> "RESPONSE: " + req.method() + " " + req.uri() + " [" + response.statusCode()
                    + "] \n" + headers + "\n\n" + response.body());
        }
    }

    /**
     * Log an HTTP request
     * @param request The request to log
     * @param body The body of the request
     */
    public void logRequest(HttpRequest request, String body) {
        if (logger.isLoggable(level)) {
            var headers = request.headers().map().entrySet().stream()
                    .map(e -> e.getKey() + ": " + String.join(", ", e.getValue()))
                    .collect(Collectors.joining(System.lineSeparator()));
            var bodyString = (body == null) ? "" : "\n\n" + body;
            log(() -> "REQUEST: " + request.method() + " " + request.uri() + "\n" + headers + bodyString);
        }
    }
}

