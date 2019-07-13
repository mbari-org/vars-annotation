package org.mbari.vars.services;

import org.mbari.vars.services.model.Authorization;

import java.util.Optional;

/**
 * @author Brian Schlining
 * @since 2017-05-24T10:21:00
 */
public class NoopAuthService implements AuthService {


    @Override
    public Optional<Authorization> authorize() {
        return Optional.empty();
    }
}
