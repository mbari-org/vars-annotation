package org.mbari.vars.annotation.ui.javafx.abpanel;

import org.mbari.vars.oni.sdk.r1.models.User;
import org.mbari.vars.annotation.etc.jdk.Loggers;
import org.mbari.vars.annotation.ui.UIToolBox;

import java.util.Optional;
import java.util.prefs.Preferences;

public class AssocButtonPrefs {

    private final Loggers log = new Loggers(getClass());

    private static final String PREF_BUTTON_NAME = "name";
    private static final String PREF_BUTTON_ORDER = "order";
    private static final String PREF_BUTTON_ASSOCIATION = "association";
    private static final String PREF_AP_NODE = "org.mbari.m3.vars.annotation.ui.abpanel.AssocButtonPaneController";


    private final UIToolBox toolBox;

    public AssocButtonPrefs(UIToolBox toolBox) {
        this.toolBox = toolBox;
    }

    public Optional<Preferences> findPreferences() {
        Preferences prefs = null;
        User user = toolBox.getData().getUser();
        if (user != null) {
            Preferences userPreferences = toolBox.getServices()
                    .preferencesFactory()
                    .remoteUserRoot(user.getUsername());
            prefs = userPreferences.node(PREF_AP_NODE);
            log.atDebug().log("Using " + prefs);
        }
        return Optional.ofNullable(prefs);
    }


}
