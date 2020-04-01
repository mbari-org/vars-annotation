package org.mbari.vars.ui.mediaplayers.sharktopoda.localization;

import org.mbari.vars.ui.AppConfig;
import org.mbari.vcr4j.sharktopoda.client.localization.Preconditions;

/**
 * Immutable bean class that stores values to configure the ZeroMQ localization communications.
 *
 * @author Brian Schlining
 * @since 2020-03-05T16:09:00
 */
public class LocalizationSettings {

    private int incomingPort;
    private String incomingTopic;
    private int outgoingPort;
    private String outgoingTopic;
    private boolean enabled;

    public LocalizationSettings(int incomingPort,
                                String incomingTopic,
                                int outgoingPort,
                                String outgoingTopic,
                                boolean enabled) {
        Preconditions.require(incomingTopic != null, "incomingTopic can not be null");
        Preconditions.require(!incomingTopic.isBlank(), "incomingTopic can not be blank");
        Preconditions.require(!incomingTopic.isEmpty(), "incomingTopic can not be empty");
        Preconditions.require(outgoingTopic != null, "outgoingTopic can not be null");
        Preconditions.require(!outgoingTopic.isBlank(), "outgoingTopic can not be blank");
        Preconditions.require(!outgoingTopic.isEmpty(), "outgoingTopic can not be empty");
        this.incomingPort = incomingPort;
        this.incomingTopic = incomingTopic;
        this.outgoingPort = outgoingPort;
        this.outgoingTopic = outgoingTopic;
        this.enabled = enabled;
    }

    public LocalizationSettings(AppConfig appConfig) {
        this(appConfig.getLocalizationDefaultsIncomingPort(),
                appConfig.getLocalizationDefaultsIncomingTopic(),
                appConfig.getLocalizationDefaultsOutgoingPort(),
                appConfig.getLocalizationDefaultsOutgoingTopic(),
                false);
    }

    public int getIncomingPort() {
        return incomingPort;
    }

    public String getIncomingTopic() {
        return incomingTopic;
    }

    public int getOutgoingPort() {
        return outgoingPort;
    }

    public String getOutgoingTopic() {
        return outgoingTopic;
    }

    public boolean isEnabled() {
        return enabled;
    }
}
