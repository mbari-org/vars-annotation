module org.mbari.vars.annotation {

    requires transitive com.auth0.jwt;
    requires transitive com.fasterxml.jackson.databind; // required by java-jwt
    requires transitive com.github.benmanes.caffeine;
    requires transitive com.google.gson;
    requires transitive io.reactivex.rxjava3;
    requires transitive jakarta.inject;
    requires transitive java.desktop;
    requires transitive java.net.http;
    requires transitive java.prefs;
    requires transitive java.scripting;
    requires transitive java.sql;
    requires transitive java.xml;
    requires transitive javafx.base;
    requires transitive javafx.controls;
    requires transitive javafx.fxml;
    requires transitive javafx.media;
    requires transitive javafx.swing;
    requires transitive methanol;
    requires okhttp3.logging;
    requires okhttp3;
    requires transitive okio;
    requires transitive org.controlsfx.controls;
    requires transitive org.kordamp.ikonli.javafx;
    requires transitive org.kordamp.ikonli.material;
    requires transitive org.kordamp.ikonli.swing;
    requires transitive org.mbari.imgfx;
    requires org.mbari.vars.annosaurus.sdk;
    requires org.mbari.vars.oni.sdk;
    requires org.mbari.vars.raziel.sdk;
    requires org.mbari.vars.vampiresquid.sdk;
    requires transitive org.slf4j;
    requires swingx.all;
    requires typesafe.config;
    requires transitive vcr4j.core;
    requires transitive vcr4j.remote;

    uses org.mbari.vars.annotation.ui.mediaplayers.MediaControlsFactory;

    provides org.mbari.vars.annotation.ui.mediaplayers.MediaControlsFactory
            with
                    org.mbari.vars.annotation.ui.mediaplayers.macos.bm.MediaControlsFactoryImpl,
                    org.mbari.vars.annotation.ui.mediaplayers.sharktopoda2.MediaControlsFactoryImpl,
                    org.mbari.vars.annotation.ui.mediaplayers.ships.MediaControlsFactoryImpl;

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