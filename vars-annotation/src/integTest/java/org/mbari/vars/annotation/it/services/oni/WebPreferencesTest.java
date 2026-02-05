package org.mbari.vars.annotation.it.services.oni;


import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.mbari.vars.annotation.it.services.TestToolbox;
import org.mbari.vars.annotation.services.oni.WebPreferencesFactory;
import org.mbari.vars.oni.sdk.r1.PreferencesService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.prefs.Preferences;


/**
 * @author Brian Schlining
 * @since 2017-10-18T10:05:00
 */
public class WebPreferencesTest {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private PreferencesService service = TestToolbox.getPreferencesService();

    WebPreferencesFactory factory = new WebPreferencesFactory(service, 10 * 1000L);

    @Test
    public void testNode() {
        Preferences userRoot = factory.remoteUserRoot("brian");
        log.debug("userRoot = " + userRoot.absolutePath());
        userRoot.put("trash", "foo");
        String s0 = userRoot.get("trash", "bar");
        assertEquals("foo", s0, "Returned value was not what we expected");
        userRoot.put("trash", "bar");
        String s1 = userRoot.get("trash", "bar");
        assertEquals("bar", s1, "Returned value was not what we expected");
        userRoot.remove("trash");

    }

}
