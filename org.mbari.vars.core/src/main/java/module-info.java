module org.mbari.vars.core {

  requires java.scripting;
  requires io.reactivex.rxjava2;
  requires slf4j.api;
  requires typesafe.config;

  exports org.mbari.vars.core;
  exports org.mbari.vars.core.util;
}