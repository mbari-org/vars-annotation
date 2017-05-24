package org.mbari.m3.vars.annotation.services.annosaurus.v1;

import org.junit.Test;
import org.mbari.m3.vars.annotation.model.Annotation;
import org.mbari.m3.vars.annotation.model.Authorization;
import org.mbari.m3.vars.annotation.services.AuthService;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * @author Brian Schlining
 * @since 2017-05-23T10:07:00
 */
public class AnnoServiceTest {

    String endpoint = "http://m3.shore.mbari.org/kb/v1/";
    ServiceGenerator serviceGenerator = new ServiceGenerator(endpoint);
    AuthService authService = new BasicJWTAuthService(serviceGenerator, new Authorization("", ""));
    AnnoService annoService = new AnnoService(serviceGenerator, authService);

    @Test
    public void testFindAnnotatons() {
        CompletableFuture<List<Annotation>> f = annoService.findAnnotations(UUID.fromString("ccbe1c1b-100d-41ab-87ac-7e48d57c8278"));
        int n = 0
        while (true) {


        }
    }

    private <T> T await(CompletableFuture<T> f, Duration timeout) {
        int interval = 100;
        Instant now = Instant.now();
        while (true) {
            // TODO implement wait
        }
    }





}
