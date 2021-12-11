module org.mbari.vars.core {

  requires java.scripting;
  requires io.reactivex.rxjava2;
  requires org.slf4j;
  requires typesafe.config;

  exports org.mbari.vars.core;
  exports org.mbari.vars.core.util;
    exports org.mbari.vars.core.crypto;
}