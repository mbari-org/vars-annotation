package org.mbari.vars.annotation.etc.vcr4j;

import org.mbari.vcr4j.VideoState;

public class NoopVideoState implements VideoState {

    @Override
    public boolean isConnected() {
        return false;
    }

    @Override
    public boolean isCueingUp() {
        return false;
    }

    @Override
    public boolean isFastForwarding() {
        return false;
    }

    @Override
    public boolean isPlaying() {
        return false;
    }

    @Override
    public boolean isReverseDirection() {
        return false;
    }

    @Override
    public boolean isRewinding() {
        return false;
    }

    @Override
    public boolean isShuttling() {
        return false;
    }

    @Override
    public boolean isStopped() {
        return false;
    }
}
