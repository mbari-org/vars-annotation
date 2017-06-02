package org.mbari.m3.vars.annotation;

import java.time.Duration;

/**
 * Custom formatters so that we are consistently formatting values displayed to users
 * @author Brian Schlining
 * @since 2017-06-02T08:46:00
 */
public class FormatUtils {

    public static String formatSizeBytes(Long bytes) {
        Double gb = bytes * 1e-9;
        if (gb > 1) {
            return String.format("%.2f GB", gb);
        }
        else {
            Double mb = bytes * 1e-6;
            return String.format("%.2f MB", mb);
        }
    }

    public static String formatDuration(Duration duration) {
        long seconds = duration.getSeconds();
        long absSeconds = Math.abs(seconds);
        String positive = String.format(
                "%02d:%02d:%02d",
                absSeconds / 3600,
                (absSeconds % 3600) / 60,
                absSeconds % 60);
        return seconds < 0 ? "-" + positive : "+" + positive;
    }
}
