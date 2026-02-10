package org.mbari.vars.annotation.util;

import java.time.Duration;

/**
 * Custom formatters so that we are consistently formatting values displayed to users
 * @author Brian Schlining
 * @since 2017-06-02T08:46:00
 */
public class FormatUtils {

    /**
     * Make a human readable string (e.g. 10 GB or 100 MB) for a
     * size of a file in bytes
     * @param bytes The size of the file in bytes
     * @return A formated string of GB or MB
     */
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

    /**
     * Make a duration look pretty
     * @param duration The duration of interest.
     * @return String formated as +HH:MM:ss.sss
     */
    public static String formatDuration(Duration duration) {
        long seconds = duration.getSeconds();
        long absSeconds = Math.abs(seconds);
        long remainingMillis = Math.abs(duration.toMillis()) - absSeconds * 1000;
        double decimalSecs = (absSeconds % 60) + remainingMillis / 1000D;
        String positive = String.format(
                "%02d:%02d:%06.3f",
                absSeconds / 3600,
                (absSeconds % 3600) / 60,
                decimalSecs);

        return seconds < 0 ? "-" + positive : " " + positive;
    }

    public static String toHexString(byte[] bytes) {
        return HexUtils.printHexBinary(bytes);
    }

    public static byte[] fromHexString(String hex) {
        return HexUtils.parseHexBinary(hex);
    }
}
