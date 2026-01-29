package org.mbari.vars.annotation.services.oni;


import org.mbari.vars.annotation.util.Preconditions;
import org.mbari.vars.oni.sdk.r1.PreferencesService;

import java.util.prefs.Preferences;

/**
 * @author Brian Schlining
 * @since 2017-06-13T11:18:00
 */
public class WebPreferencesFactory implements PreferencesFactory {

    private final Preferences systemRoot;

    public WebPreferencesFactory(PreferencesService service, @Named("PREFS_TIMEOUT") Long timeoutMillis) {
        Preconditions.checkNotNull(service, "Missing a PreferencesService");
        Preconditions.checkNotNull(timeoutMillis, "Missing a timeout argument");
        Preconditions.checkArgument(timeoutMillis > 100, "The timeout was less than 100 ms. This isn't allowed.");
        this.systemRoot = new WebPreferences(service, timeoutMillis, null, "");
    }

    @Override
    public Preferences remoteSystemRoot() {
        return systemRoot;
    }

    @Override
    public Preferences remoteUserRoot(String userName) {
        return remoteSystemRoot().node(userName);
    }
}
