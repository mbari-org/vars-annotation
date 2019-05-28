package org.mbari.m3.vars.annotation.model;

/**
 * @author Brian Schlining
 * @since 2019-05-28T11:03:00
 */
public class ConcurrentRequestCount {

    private ConcurrentRequest concurrentRequest;
    private Long count;

    public ConcurrentRequestCount(ConcurrentRequest concurrentRequest, Long count) {
        this.concurrentRequest = concurrentRequest;
        this.count = count;
    }

    public ConcurrentRequest getConcurrentRequest() {
        return concurrentRequest;
    }

    public Long getCount() {
        return count;
    }
}
