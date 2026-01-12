package org.mbari.vars.services.util;

import java.io.File;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Brian Schlining
 * @since 2013-02-15
 */
public class JPGPreviewUtilities {

    private static final Logger log = LoggerFactory.getLogger(JPGPreviewUtilities.class);
    private static final Config config = ConfigFactory.load();
    private static final String imageCopyrightOwner = config.getString("app.image.copyright.owner");


    /**
     * Creates the textual overlay for the preview image
     *
     * @param  png Description of the Parameter
     * @param  snapTime Description of the Parameter
     * @return  A string array of ext to be overlaid onto an image.
     */
    public static String[] createOverlayText(final File png, final SnapTime snapTime) {
        final String[] s = new String[4];
        s[0] = "Copyright " + snapTime.getYear() + " " + imageCopyrightOwner;
        s[1] = png.getAbsolutePath() + " (MAIN)";
        s[2] = snapTime.getFormattedGmtTime() + " GMT (local +" +
                snapTime.getGmtOffset().replaceFirst("-", "").replaceAll("0", "") + ")";
        s[3] = "";

        return s;
    }

}
