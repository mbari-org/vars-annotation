package org.mbari.vars.annotation.ui.mediaplayers.ships;

import org.mbari.vcr4j.VideoState;

/**
 * @author Brian Schlining
 * @since 2017-12-20T15:48:00
 */
public class ShipVideoState implements VideoState {

    @Override
    public boolean isConnected() {
        return true;
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
