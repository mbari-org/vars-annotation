package org.mbari.vars.annotation.services;

import java.net.ConnectException;
import java.net.NoRouteToHostException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Optional;
import java.util.concurrent.TimeoutException;

/**
 * Describes why VARS could not get its service endpoints from the configuration
 * server (Raziel). When this happens every service falls back to a no-op
 * implementation, so the UI uses this to tell the user that VARS is not connected.
 */
public class ServiceConfigurationException extends RuntimeException {

    public enum Reason {
        /** There is no raziel.txt login file, e.g. on first launch */
        NOT_CONFIGURED,
        /** The server did not respond within the configured raziel.timeout */
        TIMED_OUT,
        /** The connection was refused or the host could not be reached */
        UNREACHABLE,
        /** The server's host name could not be resolved */
        UNKNOWN_HOST,
        /** The server responded but did not return any service endpoints */
        NO_SERVICES,
        OTHER
    }

    private final Reason reason;
    private final URL url;

    public ServiceConfigurationException(Reason reason, URL url, String message, Throwable cause) {
        super(message, cause);
        this.reason = reason;
        this.url = url;
    }

    public Reason getReason() {
        return reason;
    }

    /**
     * @return The configuration server's URL. Empty when no server has been configured.
     */
    public Optional<URL> getUrl() {
        return Optional.ofNullable(url);
    }

    /**
     * Builds an exception from a failed attempt to reach the configuration server.
     */
    public static ServiceConfigurationException from(URL url, Throwable throwable) {
        var root = rootCause(throwable);
        var message = "Unable to load service endpoints from the configuration server at " + url;
        return new ServiceConfigurationException(classify(root), url, message, root);
    }

    private static Reason classify(Throwable root) {
        if (root instanceof TimeoutException) {
            return Reason.TIMED_OUT;
        }
        if (root instanceof UnknownHostException) {
            return Reason.UNKNOWN_HOST;
        }
        if (root instanceof ConnectException
                || root instanceof NoRouteToHostException
                || root instanceof SocketTimeoutException) {
            return Reason.UNREACHABLE;
        }
        return Reason.OTHER;
    }

    /**
     * Unwraps CompletionException/RuntimeException wrappers to the underlying cause.
     */
    static Throwable rootCause(Throwable throwable) {
        var t = throwable;
        while (t.getCause() != null && t.getCause() != t) {
            t = t.getCause();
        }
        return t;
    }
}
