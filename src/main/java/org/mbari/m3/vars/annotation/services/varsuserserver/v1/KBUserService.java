package org.mbari.m3.vars.annotation.services.varsuserserver.v1;

import org.mbari.m3.vars.annotation.model.User;
import org.mbari.m3.vars.annotation.services.AuthService;
import org.mbari.m3.vars.annotation.services.RetrofitWebService;
import org.mbari.m3.vars.annotation.services.UserService;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * @author Brian Schlining
 * @since 2017-06-09T08:27:00
 */
public class KBUserService implements UserService, RetrofitWebService {

    private final UserWebService userService;
    private final Map<String, String> defaultHeaders;

    @Inject
    public KBUserService(KBMiscServiceFactory serviceFactory, @Named("USERS_AUTH") AuthService authService) {
        userService = serviceFactory.create(UserWebService.class, authService);
        defaultHeaders = new HashMap<>();
        defaultHeaders.put("Accept", "application/json");
        defaultHeaders.put("Accept-Charset", "utf-8");
    }

    @Override
    public CompletableFuture<List<User>> findAllUsers() {
        return sendRequest(userService.findAll());
    }

    @Override
    public CompletableFuture<User> create(User user) {
        return sendRequest(userService.create(user.getUserName(),
                user.getPassword(),
                user.getRold(),
                user.getFirstName(),
                user.getLastName(),
                user.getAffiliation(),
                user.getEmail(),
                defaultHeaders
                ));
    }

    @Override
    public CompletableFuture<Optional<User>> update(User user) {
        return null;
    }
}
