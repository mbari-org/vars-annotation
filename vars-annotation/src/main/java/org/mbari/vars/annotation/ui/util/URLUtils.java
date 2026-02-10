package org.mbari.vars.annotation.ui.util;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;

public class URLUtils {
    private URLUtils() {}

    public static String extension(URL url) {
        String ext = null;
        if (url != null) {
            var s = url.toExternalForm();
            var idx = s.lastIndexOf(".");
            if (idx > -1) {
                ext = s.substring(idx + 1);
            }
        }
        return ext;
    }

    public static String filename(URL url) {
        String name = null;
        if (url != null) {
            try {
                name = new File(url.toURI().getPath()).getName();
            } catch (URISyntaxException e) {
                // Do nothing
            }
        }
        return name;
    }
}
