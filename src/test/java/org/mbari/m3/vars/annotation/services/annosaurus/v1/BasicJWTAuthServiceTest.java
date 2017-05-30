package org.mbari.m3.vars.annotation.services.annosaurus.v1;

import static org.junit.Assert.*;
import org.junit.Test;
import org.mbari.m3.vars.annotation.model.Authorization;
import org.mbari.m3.vars.annotation.services.AuthService;
import org.mbari.m3.vars.annotation.services.BasicJWTAuthService;
import org.mbari.m3.vars.annotation.services.TestConfig;

import java.time.Duration;
import java.util.Optional;

/**
 * @author Brian Schlining
 * @since 2017-05-25T09:00:00
 */
public class BasicJWTAuthServiceTest {

    AnnoWebServiceFactory serviceGenerator = TestConfig.ANNO_SERVICE_GEN;
    AuthService authService = new BasicJWTAuthService(serviceGenerator,
            new Authorization("APIKEY", ""));
    Duration timeout = Duration.ofMillis(15000);

    @Test
    public void testAuth() {
        Optional<Authorization> authorize = authService.authorize();
        assertTrue("Authorization was null", authorize.isPresent());
        Authorization a = authorize.get();
        assertTrue("Token type was not Bearer", a.getTokenType().equalsIgnoreCase("Bearer"));
    }

}
