package org.mbari.m3.vars.annotation.services.annosaurus.v1;

import static org.junit.Assert.*;
import org.junit.Test;
import org.mbari.m3.vars.annotation.model.Authorization;
import org.mbari.m3.vars.annotation.services.AuthService;
import org.mbari.m3.vars.annotation.services.BasicJWTAuthService;
import org.mbari.m3.vars.annotation.ui.DemoConstants;

import java.util.Optional;

/**
 * @author Brian Schlining
 * @since 2017-05-25T09:00:00
 */
public class BasicJWTAuthServiceTest {

    AnnoWebServiceFactory serviceFactory = DemoConstants.newAnnoWebServiceFactory();
    String clientSecret = DemoConstants.getToolBox().getConfig().getString("annotation.service.client.secret");
    AuthService authService = new BasicJWTAuthService(serviceFactory,
            new Authorization("APIKEY", clientSecret));

    @Test
    public void testAuth() {
        Optional<Authorization> authorize = authService.authorize();
        assertTrue("Authorization was null", authorize.isPresent());
        Authorization a = authorize.get();
        assertTrue("Token type was not Bearer", a.getTokenType().equalsIgnoreCase("Bearer"));
    }

}
