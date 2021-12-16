package org.mbari.vars.services;

public class RemoteRequestException extends RuntimeException {
    public RemoteRequestException() {
    }

    public RemoteRequestException(String message) {
        super(message);
    }

    public RemoteRequestException(String message, Throwable cause) {
        super(message, cause);
    }

    public RemoteRequestException(Throwable cause) {
        super(cause);
    }

    public RemoteRequestException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
