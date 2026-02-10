package org.mbari.vars.annotation.util;

import org.mbari.vars.annotation.etc.jdk.Loggers;

import java.util.prefs.Preferences;

/**
 * @author Brian Schlining
 * @since 2017-06-14T11:41:00
 */
public class PreferenceUtils {

    public static void copyPrefs(Preferences source, Preferences destination) {
        try {
            // Copy the key/value pairs first
            String[] prefKeys = source.keys();
            for (int i = 0; i < prefKeys.length; i++) {
                destination.put(prefKeys[i], source.get(prefKeys[i], ""));
            }

            // Now grab all the names of the children nodes
            String[] childrenNames = source.childrenNames();

            // Recursively copy the preference nodes
            for (int i = 0; i < childrenNames.length; i++) {
                copyPrefs(source.node(childrenNames[i]),
                        destination.node(childrenNames[i]));
            }
        }
        catch (Exception e) {
            new Loggers(PreferenceUtils.class).atError()
                    .withCause(e).log("Failed to copy preferences.");
        }
    }
}
