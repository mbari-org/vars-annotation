package org.mbari.m3.vars.annotation.services;

import org.mbari.m3.vars.annotation.model.Authorization;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

import java.io.IOException;
import java.util.Optional;

/**
 * This service handles the handshake to get the authorization from Annosaurus and vampire-squid
 * that implement Basic JWT handshake (i.e. we send a client secret and get back a JWT token to
 * use for authentication.)
 *
 * @author Brian Schlining
 * @since 2017-05-24T09:14:00 
 */
public class BasicJWTAuthService implements AuthService {

    private final BasicJWTAuthWebService service;
    private final Authorization clientSecret;
    private final Logger log = LoggerFactory.getLogger(getClass());

    @Inject
    public BasicJWTAuthService(RetrofitServiceFactory serviceFactory, Authorization clientSecret) {
        service = serviceFactory.create(BasicJWTAuthWebService.class);
        this.clientSecret = clientSecret;
    }


    @Override
    public Optional<Authorization> authorize() {
        Authorization a = null;
        try {
            // Block! We need to wait for authorization before we can move on
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
