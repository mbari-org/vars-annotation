module org.mbari.vars.services {
  requires com.github.benmanes.caffeine;
  requires com.google.common;
  requires com.google.guice;
  requires gson.javatime.serialisers;
  requires gson;
  requires io.reactivex.rxjava2;
  requires java.desktop;
  requires java.jwt;
  requires java.prefs;
  requires java.sql;
  requires javax.inject;
  requires jsr305;
  requires okhttp3.logging;
  requires okhttp3;
  requires org.mbari.vars.core;
  requires retrofit2.converter.gson;
  requires retrofit2;
  requires slf4j.api;
  requires typesafe.config;
  requires vcr4j.core;

  exports org.mbari.vars.services.gson;
  exports org.mbari.vars.services.impl.annosaurus.v1;
  exports org.mbari.vars.services.impl.annosaurus.v2;
  exports org.mbari.vars.services.impl.panoptes.v1;
  exports org.mbari.vars.services.impl.vampiresquid.v1;
  exports org.mbari.vars.services.impl.varskbserver.v1;
  exports org.mbari.vars.services.impl.varsuserserver.v1;
  exports org.mbari.vars.services.model;
  exports org.mbari.vars.services.util;
  exports org.mbari.vars.services;
}