#!/usr/bin/env bash

# build script used internally at MBARI for building app against testing/development servers and databases

export JPACKAGE_HOME="/Library/Java/JavaVirtualMachines/adoptopenjdk-14.jdk/Contents/Home"
source "$HOME/workspace/M3/m3-deployspace/vars-annotation/env-config-dev.sh"
gradlew clean jpackage --info
