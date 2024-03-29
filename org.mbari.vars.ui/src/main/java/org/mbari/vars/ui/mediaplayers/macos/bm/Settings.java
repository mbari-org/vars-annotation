package org.mbari.vars.ui.mediaplayers.macos.bm;


import java.time.Duration;
import java.util.prefs.Preferences;

public class Settings {

    public static final String BM_HOST = "macos-libbmagic-host";
    public static final String BM_PORT ="macos-libbmagic-port";
    public static final String BM_API_KEY = "macos-libbmagic-key";
    public static final String BM_TIMEOUT = "macos-libbmagid-timeout-secs";

    public static String getHost() {
        Preferences prefs = Preferences.userNodeForPackage(ImageCaptureServiceImpl.class);
        return prefs.get(BM_HOST, "localhost");
    }

    public static Integer getPort() {
        Preferences prefs = Preferences.userNodeForPackage(ImageCaptureServiceImpl.class);
        return prefs.getInt(BM_PORT, 9000);
    }

    public static String getApiKey() {
        Preferences prefs = Preferences.userNodeForPackage(ImageCaptureServiceImpl.class);
        return prefs.get(BM_API_KEY, "0123456789");
    }

    public static Integer getTimeout() {
        Preferences prefs = Preferences.userNodeForPackage(ImageCaptureServiceImpl.class);
        return prefs.getInt(BM_TIMEOUT, 5);
    }

    public static void saveSettings(String host, int port, String apiKey, int timeoutSeconds) {
        Preferences prefs = Preferences.userNodeForPackage(ImageCaptureServiceImpl.class);
        prefs.put(BM_HOST, host);
        prefs.putInt(BM_PORT, port);
        prefs.put(BM_API_KEY, apiKey);
        prefs.putInt(BM_TIMEOUT, timeoutSeconds);
    }
}
