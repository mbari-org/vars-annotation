package org.mbari.m3.vars.annotation.messages;

/**
 * @author Brian Schlining
 * @since 2017-07-28T13:34:00
 */
public class SetProgress implements Message {
    private double progress;

    public SetProgress(double progress) {
        this.progress = progress;
    }
}
