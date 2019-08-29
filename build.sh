#!/usr/bin/env bash

# build script used internally at MBARI

export JPACKAGE_HOME="$HOME/Applications/jdk-14.jdk/Contents/Home"
source "$HOME/workspace/m3-deployspace/m3.shore.mbari.org/vars-annotation/env-config.sh"
gradlew clean jpackage
