/**
 *  The design of the package is:
 *
 *  <h2>Configuration</h2>
 *  <pre>
 *  LocalizationSettings -- Bean class that contains all settings needed for the controllers
 *
 *  LocalizationSettingsPaneController -- UI or setting localization prefs
 *
 *  LocalizationPrefs -- Class that manages local stroage of LocalizationSettings
 *  </pre>
 *
 *  <h2>Controllers</h2>
 *  <pre>
 *  LocalizationLifecycleController -- Creates LocalizationController as needed
 *     | 1
 *     | 0..1
 *  LocalizationController  -- Wrapper around in and out controllers
 *     |                 |
 *  IncomingController   |  -- Handles localizations from remote app
 *                       |
 *       OutgoingController -- Handles selection, add, remove from vars-annotation
 *  </pre>
 */
package org.mbari.vars.ui.mediaplayers.sharktopoda.localization;