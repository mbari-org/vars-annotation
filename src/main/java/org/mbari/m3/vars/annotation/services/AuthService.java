package org.mbari.m3.vars.annotation.services;

import org.mbari.m3.vars.annotation.model.Authorization;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * @author Brian Schlining
 * @since 2017-05-24T08:49:00
 */
public interface AuthService {

    Optional<Authorization> authorize();

}
