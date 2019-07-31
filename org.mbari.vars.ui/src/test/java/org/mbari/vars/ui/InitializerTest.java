package org.mbari.vars.ui;

import static org.junit.Assert.*;

import org.junit.Test;
import org.mbari.vars.services.Services;
import org.mbari.vars.services.UserService;
import org.mbari.vars.services.model.User;


import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * @author Brian Schlining
 * @since 2017-06-15T09:35:00
 */
public class InitializerTest {

    @Test
    public void getSettingsDirectoryTest() {
        Path path = Initializer.getSettingsDirectory();
        assertNotNull("The Settings directory was null", path);
        assertTrue("The settings directory does not exist", Files.exists(path));
    }

    @Test
    public void getToolBoxTest() throws Exception {
        UIToolBox toolBox = Initializer.getToolBox();
        assertNotNull("UIToolBox was null", toolBox);
        Data data = toolBox.getData();
        assertNotNull("Data in toolbox was null", data);
        Services services = toolBox.getServices();
        assertNotNull("Services in toolbox was null", data);
    }



//    @Test
//    public void getToolBoxTest() throws Exception {
//        UIToolBox toolBox = Initializer.getToolBox();
//        assertNotNull("UIToolBox was null", toolBox);
//        Data data = toolBox.getData();
//        assertNotNull("Data in toolbox was null", data);
//        Services services = toolBox.getServices();
//        assertNotNull("Services in toolbox was null", data);
//
//        // -- Users
//        UserService userService = services.getUserService();
//        assertNotNull("UserServices from toolbox was null", userService);
//        CompletableFuture<List<User>> users = userService.findAllUsers();
//        Duration userTimeout = toolBox.getConfig().getDuration("accounts.service.timeout");
//        List<User> us = users.get(userTimeout.toMillis(), TimeUnit.MILLISECONDS);
//        assertFalse("Users was empty", us.isEmpty());
//    }
}
