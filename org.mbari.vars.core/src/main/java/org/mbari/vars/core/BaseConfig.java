package org.mbari.vars.core;

import com.typesafe.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.util.function.Function;

/**
 * BaseConfig - In general, we strongly prefer not to scatter config strings
 * throughout the codebase. Instead, for vars-annotation apps, extend this
 * class and add methoed that reads teh config and returns the value of interest.
 * Some helper methods are provided.
 */
public abstract class BaseConfig {

  protected final Logger log = LoggerFactory.getLogger(getClass());
  protected final Config config;
  protected final URL defaultUrl;
  protected Duration defaultTimeout = Duration.ofSeconds(20);

  public BaseConfig(Config config) {
    this.config = config;
    try {
      defaultUrl = new URL("http://localhost");
    }
    catch (MalformedURLException e) {
      throw new RuntimeException("Unable to create a default URL for config values", e);
    }
  }

  public <T> T read(String path, Function<String, T> fn, T defaultValue) {
    try {
      return fn.apply(path);
    }
    catch (Exception e) {
      log.warn("Unable to find a config value at path {}", path);
      return defaultValue;
    }
  }

  public  URL readUrl(String path) {
    String url = null;
    try {
      url = config.getString(path);
      return new URL(url);
    }
    catch (MalformedURLException e) {
      log.warn("The URL {} defined in the config at {} is malformed", url, path);
      return defaultUrl;
    }
    catch (Exception e) {
      log.warn("Unable to find a config value at path {}", path);
      return defaultUrl;
    }
  }

  public Config getConfig() {
    return config;
  }


}