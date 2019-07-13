package org.mbari.vars.services.impl.annosaurus.v2;

import org.mbari.vars.services.model.Annotation;
import org.mbari.vars.services.AuthService;
import org.mbari.vars.services.RetrofitWebService;

import javax.inject.Inject;
import javax.inject.Named;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * @author Brian Schlining
 * @since 2019-05-09T10:43:00
 */
public class AnnoServiceV2 implements RetrofitWebService {

    private final AnnoWebServiceV2 annoService;

    @Inject
    public AnnoServiceV2(AnnoWebServiceFactoryV2 serviceFactory, @Named("ANNO_AUTH") AuthService authService) {
        annoService = serviceFactory.create(AnnoWebServiceV2.class, authService);
    }

    public CompletableFuture<List<Annotation>> findAnnotations(UUID videoReferenceUuid, Long limit, Long offset) {
        return sendRequest(annoService.findByVideoReferenceUuid(videoReferenceUuid, limit, offset));
    }

    public CompletableFuture<List<Annotation>> findAnnotations(UUID videoReferenceUuid,
                                                               Instant start,
                                                               Instant end,
                                                               Long limit,
                                                               Long offset) {
        return sendRequest(annoService.findByVideoReferenceUuidAndTimestamps(videoReferenceUuid, start, end, limit, offset));
    }

}
