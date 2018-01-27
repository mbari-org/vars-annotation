#!/usr/bin/env bash

BUILD_HOME=`dirname "$0"`
APP_CONF=$BUILD_HOME/src/main/resources/application.conf

#scp brian@m3.shore.mbari.org:/u/brian/deployspace/m3.shore.mbari.org/vars-annotation/conf/application.conf \
cp $HOME/workspace/m3-deployspace/m3.shore.mbari.org/vars-annotation/conf/application.conf \
    $APP_CONF &&
    mvnw clean package -Dmaven.test.skip=true -X

if [ -f $APP_CONF ]; then
  rm $APP_CONF
fi
