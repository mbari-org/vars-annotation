module org.mbari.vars.annotation {

    requires com.auth0.jwt;
    requires com.fasterxml.jackson.databind; // required by java-jwt
    requires com.github.benmanes.caffeine;
    requires com.google.gson;
    requires io.reactivex.rxjava3;
    requires java.desktop;
    requires java.logging;
    requires java.net.http;
    requires java.prefs;
    requires java.scripting;
    requires java.sql;
    requires java.xml;
    requires javafx.base;
    requires javafx.controls;
    requires javafx.graphics;
    requires javafx.fxml;
    requires javafx.media;
    requires javafx.swing;
    requires methanol;
    requires okhttp3.logging;
    requires okhttp3;
    requires okio;
    requires org.controlsfx.controls;
    requires org.kordamp.ikonli.core;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.ikonli.material2;
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

    uses org.kordamp.ikonli.IkonHandler;

    uses org.mbari.vars.annotation.ui.mediaplayers.MediaControlsFactory;

    // We also have a META-INF/service/...MediaControlsFactory file for running it in IDEs
    // That file is not used in the jlink/jpackage version
    provides org.mbari.vars.annotation.ui.mediaplayers.MediaControlsFactory
            with
                    org.mbari.vars.annotation.ui.mediaplayers.macos.bm.MediaControlsFactoryImpl,
                    org.mbari.vars.annotation.ui.mediaplayers.sharktopoda2.MediaControlsFactoryImpl,
                    org.mbari.vars.annotation.ui.mediaplayers.ships.MediaControlsFactoryImpl;

    opens org.mbari.vars.annotation to javafx.graphics, com.google.gson;
    opens org.mbari.vars.annotation.model to com.google.gson;
    opens org.mbari.vars.annotation.services.ml to com.google.gson;
    opens org.mbari.vars.annotation.ui to javafx.graphics;
    opens org.mbari.vars.annotation.ui.javafx to javafx.fxml;
    opens org.mbari.vars.annotation.ui.javafx.abpanel to javafx.fxml;
    opens org.mbari.vars.annotation.ui.javafx.imagestage to com.google.gson, javafx.fxml;
    opens org.mbari.vars.annotation.ui.javafx.imgfx to javafx.fxml;
    opens org.mbari.vars.annotation.ui.javafx.mediadialog to javafx.fxml;
    opens org.mbari.vars.annotation.ui.javafx.mlstage to javafx.fxml;
    opens org.mbari.vars.annotation.ui.javafx.raziel to javafx.fxml;
    opens org.mbari.vars.annotation.ui.javafx.rectlabel to javafx.fxml;
    opens org.mbari.vars.annotation.ui.javafx.roweditor to javafx.fxml;
    opens org.mbari.vars.annotation.ui.javafx.shared to javafx.fxml;
    opens org.mbari.vars.annotation.ui.javafx.userdialog to javafx.fxml;
    opens org.mbari.vars.annotation.ui.mediaplayers.macos.bm to javafx.fxml;
    opens org.mbari.vars.annotation.ui.mediaplayers.sharktopoda to javafx.fxml;
    opens org.mbari.vars.annotation.ui.mediaplayers.sharktopoda2 to com.google.gson, javafx.fxml;
    opens org.mbari.vars.annotation.ui.mediaplayers.ships to javafx.fxml;
    opens org.mbari.vars.annotation.ui.mediaplayers.vcr to javafx.fxml;

    exports org.mbari.vars.annotation.etc.gson;
    exports org.mbari.vars.annotation.etc.jdk.crypto;
    exports org.mbari.vars.annotation.etc.jdk;
    exports org.mbari.vars.annotation.etc.rxjava;
    exports org.mbari.vars.annotation.etc.vcr4j;
    exports org.mbari.vars.annotation.model;
    exports org.mbari.vars.annotation.services.annosaurus;
    exports org.mbari.vars.annotation.services.noop;
    exports org.mbari.vars.annotation.services.oni;
    exports org.mbari.vars.annotation.services.panopes;
    exports org.mbari.vars.annotation.services.vampiresquid;
    exports org.mbari.vars.annotation.services;
    exports org.mbari.vars.annotation.ui.mediaplayers.sharktopoda2;
    exports org.mbari.vars.annotation.ui.mediaplayers;
    exports org.mbari.vars.annotation.ui;
    exports org.mbari.vars.annotation.util;
    exports org.mbari.vars.annotation.services.ml;
    exports org.mbari.vars.annotation.services.raziel;

}