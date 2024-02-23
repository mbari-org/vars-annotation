package org.mbari.vars.core.util;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.function.Supplier;

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

    public void log(String msg) {
        if (logger.isLoggable(level)) {
            if (cause == null) {
                logger.log(level, msg);
            } else {
                logger.log(level, msg, cause);
            }
        }
    }

    public void log(Supplier<String> fn) {
        if (logger.isLoggable(level)) {
            if (cause == null) {
                logger.log(level, fn);
            } else {
                logger.log(level, fn, cause);
            }
        }
    }
}

