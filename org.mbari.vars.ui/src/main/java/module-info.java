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
  requires org.mbari.vars.core;
  requires org.mbari.vars.javafx;
  requires org.mbari.vars.services;
  requires org.reactivestreams;
  requires rxjavafx;
  requires slf4j.api;
  requires typesafe.config;
  requires vars.avfoundation;
  requires vars.blackmagic;
  requires vcr4j.core;
  requires vcr4j.jserialcomm;
  requires vcr4j.rs422;
  requires vcr4j.sharktopoda;

  uses org.mbari.vars.ui.demos.mediaplayers.MediaControlsFactory;

  provides org.mbari.vars.ui.demos.mediaplayers.MediaControlsFactory with
          org.mbari.vars.ui.demos.mediaplayers.macos.MediaControlsFactoryImpl,
          org.mbari.vars.ui.demos.mediaplayers.sharktopoda.MediaControlsFactoryImpl,
          org.mbari.vars.ui.demos.mediaplayers.ships.MediaControlsFactoryImpl,
          org.mbari.vars.ui.demos.mediaplayers.vcr.MediaControlsFactoryImpl;

  opens org.mbari.vars.ui to javafx.graphics;
  opens org.mbari.vars.ui.demos.javafx to javafx.fxml;
  opens org.mbari.vars.ui.demos.javafx.mediadialog to javafx.fxml;
  opens org.mbari.vars.ui.demos.javafx.rectlabel to javafx.fxml;
  opens org.mbari.vars.ui.demos.javafx.roweditor to javafx.fxml;
  opens org.mbari.vars.ui.demos.mediaplayers.macos to javafx.fxml;
  opens org.mbari.vars.ui.demos.mediaplayers.sharktopoda to javafx.fxml;
  opens org.mbari.vars.ui.demos.mediaplayers.ships to javafx.fxml;
  opens org.mbari.vars.ui.demos.mediaplayers.vcr to javafx.fxml;

  exports org.mbari.vars.ui;
}