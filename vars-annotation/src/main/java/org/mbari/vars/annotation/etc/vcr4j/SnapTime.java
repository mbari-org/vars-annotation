package org.mbari.vars.annotation.etc.vcr4j;

import org.mbari.vcr4j.VideoIndex;
import org.mbari.vcr4j.time.Timecode;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * Represents an instant of time related to a Video tape. This object combines 'real' time,
 * represented by a date object, with VCR time, represented by a tape time-code.
 *
 * @author Brian Schlining
 * @since 2013-02-15
 */
public class SnapTime {

    private final static NumberFormat format4i = new DecimalFormat("0000");
    private final static NumberFormat format3i = new DecimalFormat("000");
    private final static DateFormat dateFormat = new SimpleDateFormat("EEE MMM d HH:mm:ss yyyy");
    private final static DateFormat timezoneFormat = new SimpleDateFormat("ZZ");
    private final Calendar gmtCalendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"));

    private final Instant observationTimestamp;
    private final Date observationDate;
    private final VideoIndex videoIndex;

    public SnapTime(final Instant observationTimestamp, final VideoIndex videoIndex) {
        this.observationTimestamp = observationTimestamp;
        this.videoIndex = videoIndex;
        this.observationDate = Date.from(observationTimestamp);
    }

    public VideoIndex getVideoIndex() {
        return videoIndex;
    }

    public Instant getObservationTimestamp() {
        return observationTimestamp;
    }

    public Date getObservationDate() {
        return observationDate;
    }


    /**
     * @return  The timezone offset between local and GMT
     */
    String getGmtOffset() {
        return timezoneFormat.format(observationDate);
    }

    public String getTimecodeString() {
        Timecode placeholder;

        if (videoIndex.getTimecode().isPresent()) {             // Check timecode first
            placeholder = videoIndex.getTimecode().get();
        }
        else if (videoIndex.getElapsedTime().isPresent()) {     // If no timecode, try elapsed time
            Duration duration = videoIndex.getElapsedTime().get();
            double frames = duration.toMillis() / 10D; // centi-seconds
            placeholder = new Timecode(frames, 100D);
        }
        else {        // final case create name from date
            Instant timestamp = videoIndex.getTimestamp().get();
            double centisecs = timestamp.toEpochMilli() / 10D;
            placeholder = new Timecode(centisecs, 100D);
        }

        return placeholder.toString();
    }


    public String getFramegrabName() {
        return getTimecodeString().replace(":", "_");
    }


    /**
     * @return  The current time in seconds
     */
    long getTimeInSecs() {
        return observationDate.getTime() / 1000L;
    }

    /**
     * @return  YYYYDDD
     */
    public String getTrackingNumber() {
        return getYear() + getDayOfYear();
    }

    /**
     * @return  YYYY
     */
    String getYear() {
        return format4i.format(gmtCalendar.get(Calendar.YEAR));
    }

    /**
     * @return  DDD
     */
    private String getDayOfYear() {
        return format3i.format(gmtCalendar.get(Calendar.DAY_OF_YEAR));
    }

    /**
     * @return time formatted for the GMT timezone
     */
    String getFormattedGmtTime() {
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        return dateFormat.format(getObservationDate());
    }

    /**
     * @return time formatted for the local timezone
     */
    String getFormattedLocalTime() {
        dateFormat.setTimeZone(TimeZone.getDefault());

        return dateFormat.format(observationDate);
    }

}
