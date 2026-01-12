package org.mbari.vars.services;

public class RemoteAuthException extends RuntimeException {

    public RemoteAuthException() {
    }

    public RemoteAuthException(String message) {
        super(message);
    }

    public RemoteAuthException(String message, Throwable cause) {
        super(message, cause);
    }

    public RemoteAuthException(Throwable cause) {
        super(cause);
    }

    public RemoteAuthException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
