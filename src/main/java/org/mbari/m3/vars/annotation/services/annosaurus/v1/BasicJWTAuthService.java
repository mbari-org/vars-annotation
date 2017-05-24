package org.mbari.m3.vars.annotation.services.annosaurus.v1;

import org.mbari.m3.vars.annotation.model.Authorization;
import org.mbari.m3.vars.annotation.services.AuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Named;
import java.io.IOException;
import java.util.Optional;

/**
 * @author Brian Schlining
 * @since 2017-05-24T09:14:00
 */
public class BasicJWTAuthService implements AuthService {

    private final BasicJWTAuthWebService service;
    private final Authorization clientSecret;
    private final Logger log = LoggerFactory.getLogger(getClass());

    @Named
    public BasicJWTAuthService(ServiceGenerator serviceGenerator, Authorization clientSecret) {
        service = serviceGenerator.create(BasicJWTAuthWebService.class);
        this.clientSecret = clientSecret;
    }


    @Override
    public Optional<Authorization> authorize() {
        Authorization a = null;
        try {
            a = service.authorize(clientSecret.toString())
                    .execute()
                    .body();
        }
        catch (IOException e) {
            log.warn("Failed to authorize", e);
        }
        return Optional.ofNullable(a);
    }
}
