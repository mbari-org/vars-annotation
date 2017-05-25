package org.mbari.m3.vars.annotation.services;

import org.mbari.m3.vars.annotation.services.annosaurus.v1.ServiceGenerator;

/**
 * @author Brian Schlining
 * @since 2017-05-25T10:32:00
 */
public class TestConfig {

    public static final String ANNO_ENDPOINT = "http://m3.shore.mbari.org/anno/v1/";
    public static final ServiceGenerator ANNO_SERVICE_GEN = new ServiceGenerator(ANNO_ENDPOINT);
}
