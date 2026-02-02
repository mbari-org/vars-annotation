module org.mbari.vars.annotation {

    requires com.auth0.jwt;
    requires com.fasterxml.jackson.databind; // required by java-jwt
    requires com.github.benmanes.caffeine;
    requires com.google.gson;
    requires io.reactivex.rxjava3;
    requires jakarta.inject;
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
    requires javafx.swing;
    requires methanol;
    requires okhttp3.logging;
    requires okhttp3;
    requires okio;
    requires org.controlsfx.controls;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.ikonli.material;
    requires org.kordamp.ikonli.swing;
    requires org.mbari.imgfx;
    requires org.mbari.vars.annosaurus.sdk;
    requires org.mbari.vars.oni.sdk;
    requires org.mbari.vars.raziel.sdk;
    requires org.mbari.vars.vampiresquid.sdk;
    requires org.slf4j;
    requires swingx.all;
    requires typesafe.config;
    requires vcr4j.core;
    requires vcr4j.remote;

    opens org.mbari.vars.annotation.services.ml to com.google.gson;

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