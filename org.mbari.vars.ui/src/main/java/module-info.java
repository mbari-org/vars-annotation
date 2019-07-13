module org.mbari.vars.ui {
  requires com.google.common;
  requires com.google.guice;
  requires com.jfoenix;
  requires de.jensd.fx.fontawesomefx.commons;
  requires de.jensd.fx.fontawesomefx.materialicons;
  requires gson;
  requires io.reactivex.rxjava2;
  requires java.desktop;
  requires java.prefs;
  requires javafx.controls;
  requires javafx.fxml;
  requires javafx.graphics;
  requires javax.inject;
  requires jsr305;
  requires mbarix4j;
  requires org.controlsfx.controls;
  requires org.mbari.vars.javafx;
  requires org.mbari.vars.services;
  requires org.reactivestreams;
  requires slf4j.api;
  requires rxjavafx;
  requires typesafe.config;
  requires vars.avfoundation;
  requires vars.blackmagic;
  requires vcr4j.core;
  requires vcr4j.jserialcomm;
  requires vcr4j.rs422;
  requires vcr4j.sharktopoda;
  requires vcr4j.ui;

  opens org.mbari.vars.ui to javafx.graphics;
}