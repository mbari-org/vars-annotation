package org.mbari.vars.annotation.services.oni;

import java.util.prefs.Preferences;

/**
 * @author Brian Schlining
 * @since 2017-06-13T11:06:00
 */
public interface PreferencesFactory {

    default Preferences localSystemRoot() {
        return Preferences.systemRoot();
    }

    default Preferences localUserRoot() {
        return Preferences.userRoot();
    }

    Preferences remoteSystemRoot();

    Preferences remoteUserRoot(String userName);

}
