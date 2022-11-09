module org.mbari.vars.services {

  requires com.github.benmanes.caffeine;
//  requires com.google.common;
  requires com.google.gson;
//  requires com.google.guice;
  requires gson.javatime.serialisers;
  requires io.reactivex.rxjava2;
  requires com.fasterxml.jackson.databind; // required by java-jwt
  requires java.desktop;
  requires com.auth0.jwt;
  requires java.prefs;
  requires java.sql;
  requires javax.inject; 
  requires okhttp3.logging;
  requires okhttp3;
  requires org.mbari.vars.core;
  requires org.slf4j;
  requires retrofit2.converter.gson;
  requires retrofit2;
  requires typesafe.config;
  requires vcr4j.core;
  requires methanol;

  opens org.mbari.vars.services.impl.ml to com.google.gson;

  exports org.mbari.vars.services.gson;
  exports org.mbari.vars.services.impl.annosaurus.v1;
  exports org.mbari.vars.services.impl.annosaurus.v2;
  exports org.mbari.vars.services.impl.panoptes.v1;
  exports org.mbari.vars.services.impl.raziel;
  exports org.mbari.vars.services.impl.vampiresquid.v1;
  exports org.mbari.vars.services.impl.varskbserver.v1;
  exports org.mbari.vars.services.impl.varsuserserver.v1;
  exports org.mbari.vars.services.model;
  exports org.mbari.vars.services.util;
  exports org.mbari.vars.services;
  exports org.mbari.vars.services.impl.ml;
}