package org.mbari.vars.annotation.etc.jdk.logging;

import java.util.logging.Level;

public class Levels {

    public static Level parseLogLevel(String level) {
        var uppercase = level.toUpperCase();
        var logLevel = switch (uppercase) {
            case "TRACE" -> Level.FINEST;
            case "DEBUG" -> Level.FINEST;
            case "INFO" -> Level.INFO;
            case "WARN" -> Level.WARNING;
            case "ERROR" -> Level.SEVERE;
            case "OFF" -> Level.OFF;
            default -> null;
        };

        if (logLevel == null) {
            try {
                return Level.parse(uppercase);
            } catch (Exception e) {
                return Level.INFO;
            }
        }
        else {
            return logLevel;
        }
    }
}
