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
  requires org.mbari.vars.services;
  requires org.reactivestreams;
  requires rxjavafx;
  requires org.slf4j;
  requires typesafe.config;
  requires vars.avfoundation;
  requires vcr4j.core;
  requires vcr4j.jserialcomm;
  requires vcr4j.rs422;
  requires vcr4j.sharktopoda;

  uses org.mbari.vars.ui.mediaplayers.MediaControlsFactory;

  provides org.mbari.vars.ui.mediaplayers.MediaControlsFactory with
          org.mbari.vars.ui.mediaplayers.macos.MediaControlsFactoryImpl,
          org.mbari.vars.ui.mediaplayers.sharktopoda.MediaControlsFactoryImpl,
          org.mbari.vars.ui.mediaplayers.ships.MediaControlsFactoryImpl,
          org.mbari.vars.ui.mediaplayers.vcr.MediaControlsFactoryImpl;

  opens org.mbari.vars.ui to javafx.graphics;
  opens org.mbari.vars.ui.javafx to javafx.fxml;
  opens org.mbari.vars.ui.javafx.abpanel to javafx.fxml;
  opens org.mbari.vars.ui.javafx.mediadialog to javafx.fxml;
  opens org.mbari.vars.ui.javafx.rectlabel to javafx.fxml;
  opens org.mbari.vars.ui.javafx.roweditor to javafx.fxml;
  opens org.mbari.vars.ui.mediaplayers.macos to javafx.fxml;
  opens org.mbari.vars.ui.mediaplayers.sharktopoda to javafx.fxml;
  opens org.mbari.vars.ui.mediaplayers.ships to javafx.fxml;
  opens org.mbari.vars.ui.mediaplayers.vcr to javafx.fxml;

  exports org.mbari.vars.ui;
  exports org.mbari.vars.ui.commands;
  exports org.mbari.vars.ui.events;
  exports org.mbari.vars.ui.javafx;
  exports org.mbari.vars.ui.javafx.annotable;
  exports org.mbari.vars.ui.javafx.cbpanel;
  exports org.mbari.vars.ui.javafx.concepttree;
  exports org.mbari.vars.ui.javafx.mediadialog;
  exports org.mbari.vars.ui.javafx.rectlabel;
  exports org.mbari.vars.ui.javafx.roweditor;
  exports org.mbari.vars.ui.javafx.shared;
  exports org.mbari.vars.ui.messages;
  exports org.mbari.vars.ui.services;
  exports org.mbari.vars.ui.mediaplayers.sharktopoda;
  exports org.mbari.vars.ui.mediaplayers.ships;
}