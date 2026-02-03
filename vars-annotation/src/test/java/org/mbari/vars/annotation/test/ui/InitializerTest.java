package org.mbari.vars.annotation.test.ui;

import org.junit.jupiter.api.Test;
import org.mbari.vars.annotation.services.Services;
import org.mbari.vars.annotation.ui.Data;
import org.mbari.vars.annotation.ui.Initializer;
import org.mbari.vars.annotation.ui.UIToolBox;

import static org.junit.jupiter.api.Assertions.*;


import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author Brian Schlining
 * @since 2017-06-15T09:35:00
 */
public class InitializerTest {

    @Test
    public void getSettingsDirectoryTest() {
        Path path = Initializer.getSettingsDirectory();
        assertNotNull(path,"The Settings directory was null");
        assertTrue(Files.exists(path),"The settings directory does not exist");
    }

    @Test
    public void getToolBoxTest() throws Exception {
        UIToolBox toolBox = Initializer.getToolBox();
        assertNotNull(toolBox, "UIToolBox was null");
        Data data = toolBox.getData();
        assertNotNull(data, "Data in toolbox was null");
        Services services = toolBox.getServices();
        assertNotNull(services,"Services in toolbox was null");
    }


}
