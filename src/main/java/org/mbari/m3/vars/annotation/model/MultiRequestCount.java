package org.mbari.m3.vars.annotation.model;

/**
 * @author Brian Schlining
 * @since 2019-07-10T16:26:00
 */
public class MultiRequestCount {
    private MultiRequest multiRequest;
    private Long count;

    public MultiRequestCount(MultiRequest multiRequest, Long count) {
        this.multiRequest = multiRequest;
        this.count = count;
    }

    public MultiRequest getMultiRequest() {
        return multiRequest;
    }

    public Long getCount() {
        return count;
    }
}
