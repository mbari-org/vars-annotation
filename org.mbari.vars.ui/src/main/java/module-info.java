module org.mbari.vars.ui {
  requires com.google.common;
  requires com.google.guice;
  requires com.jfoenix;
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
  requires org.kordamp.iconli.core;
  requires org.kordamp.ikonli.javafx;
  requires org.kordamp.ikonli.material;
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

  uses org.mbari.m3.vars.annotation.mediaplayers.MediaControlsFactory;

  provides org.mbari.m3.vars.annotation.mediaplayers.MediaControlsFactory with
          org.mbari.m3.vars.annotation.mediaplayers.macos.MediaControlsFactoryImpl,
          org.mbari.m3.vars.annotation.mediaplayers.sharktopoda.MediaControlsFactoryImpl,
          org.mbari.m3.vars.annotation.mediaplayers.ships.MediaControlsFactoryImpl,
          org.mbari.m3.vars.annotation.mediaplayers.vcr.MediaControlsFactoryImpl;

  opens org.mbari.m3.vars.annotation to javafx.graphics;
  opens org.mbari.m3.vars.annotation.mediaplayers.macos to javafx.fxml;
  opens org.mbari.m3.vars.annotation.mediaplayers.sharktopoda to javafx.fxml;
  opens org.mbari.m3.vars.annotation.mediaplayers.ships to javafx.fxml;
  opens org.mbari.m3.vars.annotation.mediaplayers.vcr to javafx.fxml;
  opens org.mbari.m3.vars.annotation.ui to javafx.fxml;
  opens org.mbari.m3.vars.annotation.ui.mediadialog to javafx.fxml;
  opens org.mbari.m3.vars.annotation.ui.rectlabel to javafx.fxml;
  opens org.mbari.m3.vars.annotation.ui.roweditor to javafx.fxml;
  opens org.mbari.vars.ui to javafx.graphics;
}