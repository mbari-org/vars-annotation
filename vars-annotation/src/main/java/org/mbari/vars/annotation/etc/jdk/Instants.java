package org.mbari.vars.annotation.etc.jdk;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class Instants {

    private static ZoneId utcZone  = ZoneId.of("UTC");
    public static DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ISO_DATE_TIME.withZone(utcZone);
    public static DateTimeFormatter COMPACT_TIME_FORMATTER   =
            DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmssX").withZone(utcZone);
    public static DateTimeFormatter COMPACT_TIME_FORMATTER_MS =
            DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss.SSSX").withZone(utcZone);
    public static DateTimeFormatter COMPACT_TIME_FORMATTER_NS =
            DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss.SSSSSSX").withZone(utcZone);

    public static Optional<Instant> parseIso8601(String s) {
        return  parse(s, TIME_FORMATTER)
                .or(() -> parse(s, COMPACT_TIME_FORMATTER))
                .or(() -> parse(s, COMPACT_TIME_FORMATTER_MS))
                .or(() -> parse(s, COMPACT_TIME_FORMATTER_NS));
    }

    private static Optional<Instant> parse(String s, DateTimeFormatter formatter) {
        try {
            return Optional.of(Instant.from(formatter.parse(s)));
        }
        catch (Exception e) {
            return Optional.empty();
        }
    }
}
