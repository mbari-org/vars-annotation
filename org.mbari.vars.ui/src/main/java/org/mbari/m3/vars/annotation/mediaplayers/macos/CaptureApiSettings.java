package org.mbari.m3.vars.annotation.mediaplayers.macos;

import java.util.prefs.Preferences;

/**
 * @author Brian Schlining
 * @since 2018-04-25T10:55:00
 */
public class CaptureApiSettings {

    public static final String DEVICE_KEY = "macos-imagecapture-device";
    public static final String CAPTURE_API_KEY ="macos-imagecapture-api";

    public static String getSelectedDevice() {
        Preferences prefs = Preferences.userNodeForPackage(MacImageCaptureSettingsPaneController.class);
        return prefs.get(DEVICE_KEY, "");
    }

    public static String getSelectedCaptureApi() {
        Preferences prefs = Preferences.userNodeForPackage(MacImageCaptureSettingsPaneController.class);
        return prefs.get(CAPTURE_API_KEY, "");
    }

    public static void setCaptureDevice(CaptureApi captureApi, String deviceName) {
        Preferences prefs = Preferences.userNodeForPackage(MacImageCaptureSettingsPaneController.class);
        prefs.put(CAPTURE_API_KEY, captureApi.getName());
        prefs.put(DEVICE_KEY, deviceName);
    }



}
