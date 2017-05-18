package org.mbari.m3.vars.annotation;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import java.util.Locale;
import java.util.ResourceBundle;

/**
 * @author Brian Schlining
 * @since 2017-05-15T10:52:00
 */
public class Constants {

    public static final Config CONFIG = ConfigFactory.load();

    public static final ResourceBundle uiBundle = ResourceBundle.getBundle("UIBundle",
            Locale.getDefault());

}
