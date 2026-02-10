package org.mbari.vars.annotation.etc.slf4j;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.System.LoggerFinder;
import java.util.ResourceBundle;

public final class Slf4jLoggerFinder extends LoggerFinder {

    @Override
    public System.Logger getLogger(String name, Module module) {
        Logger slf4j = LoggerFactory.getLogger(name);
        return new Slf4jSystemLogger(slf4j);
    }

    static final class Slf4jSystemLogger implements System.Logger {

        private final Logger logger;

        Slf4jSystemLogger(Logger logger) {
            this.logger = logger;
        }

        @Override
        public String getName() {
            return logger.getName();
        }

        @Override
        public boolean isLoggable(Level level) {
            return switch (level) {
                case TRACE -> logger.isTraceEnabled();
                case DEBUG -> logger.isDebugEnabled();
                case INFO  -> logger.isInfoEnabled();
                case WARNING -> logger.isWarnEnabled();
                case ERROR -> logger.isErrorEnabled();
                case ALL -> true;
                case OFF -> false;
            };
        }

        @Override
        public void log(Level level, ResourceBundle bundle, String msg, Throwable thrown) {
            logMessage(level, msg, thrown);
        }

        @Override
        public void log(Level level, ResourceBundle bundle, String format, Object... params) {
            String msg;
            if (params == null || params.length == 0) {
                msg = format;
            } else {
                try {
                    msg = String.format(format, params);
                } catch (Exception e) {
                    msg = format;
                }
            }
            logMessage(level, msg, null);
        }

        private void logMessage(Level level, String msg, Throwable t) {
            switch (level) {
                case TRACE -> logger.trace(msg, t);
                case DEBUG -> logger.debug(msg, t);
                case INFO  -> logger.info(msg, t);
                case WARNING -> logger.warn(msg, t);
                case ERROR -> logger.error(msg, t);
                default -> { /* OFF / ALL */ }
            }
        }
    }
}
