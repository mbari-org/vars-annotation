package org.mbari.vars.services.annosaurus.v1;

import static org.junit.Assert.*;
import org.junit.Test;
import org.mbari.vars.services.AuthService;
import org.mbari.vars.services.BasicJWTAuthService;
import org.mbari.vars.services.TestToolbox;
import org.mbari.vars.services.impl.annosaurus.v1.AnnoWebServiceFactory;
import org.mbari.vars.services.model.Authorization;

import java.util.Optional;


/**
 * @author Brian Schlining
 * @since 2017-05-25T09:00:00
 */
public class BasicJWTAuthServiceTest {

//    AnnoWebServiceFactory serviceFactory = TestToolbox.getServices().newAnnoWebServiceFactory();
//    String clientSecret = TestToolbox.getConfig().getString("annotation.service.client.secret");
//    AuthService authService = new BasicJWTAuthService(serviceFactory,
//            new Authorization("APIKEY", clientSecret));
//
//    @Test
//    public void testAuth() {
//        Optional<Authorization> authorize = authService.authorize();
//        assertTrue("Authorization was null", authorize.isPresent());
//        Authorization a = authorize.get();
//        assertTrue("Token type was not Bearer", a.getTokenType().equalsIgnoreCase("Bearer"));
//    }

}
