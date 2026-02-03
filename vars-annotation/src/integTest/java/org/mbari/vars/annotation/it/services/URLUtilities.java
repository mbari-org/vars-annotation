package org.mbari.vars.annotation.it.services;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;

public class URLUtilities {

    public static File toFile(URL url) {
        File f;
        try {
            f = new File(url.toURI());
        }
        catch (URISyntaxException e) {
            f = new File(url.getPath());
        }
        return f;
    }

    /**
     * Extract just the filename that the URL refers to
     * @param  url [description]
     * @return     [description]
     */
    public static String toFilename(URL url) {
        String urlString = url.getFile();
        return urlString.substring(urlString.lastIndexOf('/') + 1).split("\\?")[0].split("#")[0];
    }

}
