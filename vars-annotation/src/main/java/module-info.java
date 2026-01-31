module org.mbari.vars.annotation {

  requires com.auth0.jwt;
  requires com.fasterxml.jackson.databind; // required by java-jwt
  requires com.github.benmanes.caffeine;
  requires com.google.gson;
  requires io.reactivex.rxjava3;
  requires java.desktop;
  requires java.net.http;
  requires java.prefs;
  requires java.scripting;
  requires java.sql;
  requires java.xml;
  requires javafx.base;
  requires javafx.controls;
  requires javafx.fxml;
  requires javafx.media;
  requires jakarta.inject;
  requires methanol;
  requires okhttp3.logging;
  requires okhttp3;
  requires okio;
  requires org.slf4j;
  requires typesafe.config;
  requires vcr4j.core;
  requires vcr4j.remote;
  requires org.mbari.imgfx;


  requires org.mbari.vars.raziel.sdk;
  requires org.mbari.vars.annosaurus.sdk;
  requires org.mbari.vars.oni.sdk;
  requires org.mbari.vars.vampiresquid.sdk;
    requires org.controlsfx.controls;
    requires javafx.swing;
    requires org.kordamp.ikonli.swing;
    requires org.kordamp.ikonli.material;
    requires org.kordamp.ikonli.javafx;


    opens org.mbari.vars.annotation.services.ml to com.google.gson;
  opens org.mbari.vars.services.model to com.google.gson;

  exports org.mbari.vars.services;
  exports org.mbari.vars.services.etc.gson;
  exports org.mbari.vars.services.impl.annosaurus.v1;
  exports org.mbari.vars.annotation.services.ml;
  exports org.mbari.vars.services.impl.panoptes.v1;
  exports org.mbari.vars.services.impl.raziel;
  exports org.mbari.vars.services.impl.vampiresquid.v1;
  exports org.mbari.vars.services.impl.varsuserserver.v1;
  exports org.mbari.vars.services.model;
  exports org.mbari.vars.services.util;

  exports org.mbari.vars.core;
  exports org.mbari.vars.core.util;
  exports org.mbari.vars.core.crypto;
  exports org.mbari.vars.annotation.services.panopes;
  exports org.mbari.vars.annotation.services.oni;
    exports org.mbari.vars.annotation.util;
  exports org.mbari.vars.annotation.etc.vcr4j;
  exports org.mbari.vars.annotation.services.vampiresquid;
    exports org.mbari.vars.annotation.etc.rxjava;
  exports org.mbari.vars.annotation.services;
  exports org.mbari.vars.annotation.model;
  opens org.mbari.vars.annotation.model to com.google.gson;
  exports org.mbari.vars.annotation.services.noop;
}