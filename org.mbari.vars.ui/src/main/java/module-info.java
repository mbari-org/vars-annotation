module org.mbari.vars.ui {

  requires com.google.gson;
  requires com.jfoenix;
  requires java.desktop;
  requires java.logging;
  requires java.prefs;
  requires javafx.controls;
  requires javafx.fxml;
  requires javafx.graphics;
  requires javax.inject;
  requires javafx.media;
  requires javafx.swing;
  requires mbarix4j;
  requires org.controlsfx.controls;
  requires org.kordamp.ikonli.core;
  requires org.kordamp.ikonli.javafx;
  requires org.kordamp.ikonli.material;
  requires org.mbari.imgfx;
  requires org.mbari.vars.core;
  requires transitive org.mbari.vars.services;
  requires org.reactivestreams;
  requires org.slf4j;
  requires typesafe.config;
  requires vcr4j.core;

  requires vcr4j.remote;
  requires vcr4j.sharktopoda;
  requires transitive vcr4j.sharktopoda.client;
  requires io.reactivex.rxjava3;

  uses org.mbari.vars.ui.mediaplayers.MediaControlsFactory;

  provides org.mbari.vars.ui.mediaplayers.MediaControlsFactory
      with 
      org.mbari.vars.ui.mediaplayers.macos.bm.MediaControlsFactoryImpl,
      org.mbari.vars.ui.mediaplayers.sharktopoda.MediaControlsFactoryImpl,
      org.mbari.vars.ui.mediaplayers.sharktopoda2.MediaControlsFactoryImpl,
      org.mbari.vars.ui.mediaplayers.ships.MediaControlsFactoryImpl;

  opens org.mbari.vars.ui to javafx.graphics;
  opens org.mbari.vars.ui.javafx to javafx.fxml;
  opens org.mbari.vars.ui.javafx.abpanel to javafx.fxml;
  opens org.mbari.vars.ui.javafx.imgfx to javafx.fxml;
  opens org.mbari.vars.ui.javafx.imagestage to com.google.gson, javafx.fxml;
  opens org.mbari.vars.ui.javafx.mediadialog to javafx.fxml;
  opens org.mbari.vars.ui.javafx.mlstage to javafx.fxml;
  opens org.mbari.vars.ui.javafx.raziel to javafx.fxml;
  opens org.mbari.vars.ui.javafx.rectlabel to javafx.fxml;
  opens org.mbari.vars.ui.javafx.roweditor to javafx.fxml;
  opens org.mbari.vars.ui.javafx.shared to javafx.fxml;
  opens org.mbari.vars.ui.javafx.userdialog to javafx.fxml;
  opens org.mbari.vars.ui.mediaplayers.macos.bm to javafx.fxml;
  opens org.mbari.vars.ui.mediaplayers.sharktopoda to javafx.fxml;
  opens org.mbari.vars.ui.mediaplayers.sharktopoda.localization to com.google.gson, javafx.fxml;
  opens org.mbari.vars.ui.mediaplayers.ships to javafx.fxml;
  opens org.mbari.vars.ui.mediaplayers.vcr to javafx.fxml;

  exports org.mbari.vars.ui.commands;
  exports org.mbari.vars.ui.events;
  exports org.mbari.vars.ui.javafx.annotable;
  exports org.mbari.vars.ui.javafx.cbpanel;
  exports org.mbari.vars.ui.javafx.concepttree;
  exports org.mbari.vars.ui.javafx.mediadialog;
  exports org.mbari.vars.ui.javafx.raziel;
  exports org.mbari.vars.ui.javafx.rectlabel;
  exports org.mbari.vars.ui.javafx.roweditor;
  exports org.mbari.vars.ui.javafx.shared;
  exports org.mbari.vars.ui.javafx;
  exports org.mbari.vars.ui.mediaplayers;
  exports org.mbari.vars.ui.mediaplayers.sharktopoda.localization;
  exports org.mbari.vars.ui.mediaplayers.sharktopoda;
  exports org.mbari.vars.ui.mediaplayers.sharktopoda2;
  exports org.mbari.vars.ui.mediaplayers.ships;
  exports org.mbari.vars.ui.messages;
  exports org.mbari.vars.ui.services;
  exports org.mbari.vars.ui;
  exports org.mbari.vars.ui.javafx.imagestage;


}