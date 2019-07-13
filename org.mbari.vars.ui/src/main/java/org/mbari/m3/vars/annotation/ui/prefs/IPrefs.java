package org.mbari.m3.vars.annotation.ui.prefs;


/**
 * @author Brian Schlining
 * @since 2017-08-08T16:48:00
 */
public interface IPrefs {

    /**
     * Loads the appropriate preferences data and sets the correct fields
     * in the UI
     */
    void load();

    /**
     * Saves the preferences
     */
    void save();
}
