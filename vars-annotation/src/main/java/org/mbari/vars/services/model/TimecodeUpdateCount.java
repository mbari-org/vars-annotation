package org.mbari.vars.services.model;

/**
 * @author Brian Schlining
 * @since 2019-02-07T11:15:00
 */
public class TimecodeUpdateCount {

    private Integer annotationCount;
    private Integer timestampsUpdated;

    public TimecodeUpdateCount(Integer annotationCount, Integer timestampsUpdated) {
        this.annotationCount = annotationCount;
        this.timestampsUpdated = timestampsUpdated;
    }

    public Integer getAnnotationCount() {
        return annotationCount;
    }

    public Integer getTimestampsUpdated() {
        return timestampsUpdated;
    }
}
