package org.mbari.vars.annotation.services.noop;

import org.mbari.vars.annotation.services.oni.PreferencesFactory;

import java.util.prefs.Preferences;

public class NoopPreferencesFactory implements PreferencesFactory {

    @Override
    public Preferences remoteSystemRoot() {
        return localSystemRoot();
    }

    @Override
    public Preferences remoteUserRoot(String userName) {
        return localUserRoot();
    }
}
