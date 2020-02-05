#!/usr/bin/env bash

# build script used internally at MBARI

export JPACKAGE_HOME="$HOME/Applications/jdk-14.jdk/Contents/Home"
source "$HOME/workspace/m3-deployspace/vars-annotation/env-config.sh"
gradlew clean jpackage --info

cat << EOF
Next Steps:
  - Open VARS Annotation/Contents/Info.plist in XCode
  - Add the following key/value to enable access to cameras. Note that in XCode
    this appears as a "Privacy - ..." setting

<key>NSCameraUsageDescription</key>
<string>VARS Annotation uses camera access for framecapture from hardware</string>
EOF




# /Users/brian/Applications/jdk-14.jdk/Contents/Home/bin/jpackage --type dmg --dest /Users/brian/workspace/vars-annotation/org.mbari.vars.ui/build/jpackage --name "VARS Annotation" --module-path /Users/brian/workspace/vars-annotation/org.mbari.vars.ui/build/jlinkbase/jlinkjars --module org.mbari.vars.ui/org.mbari.vars.ui.App --app-version 0.3.15 --runtime-image /Users/brian/workspace/vars-annotation/org.mbari.vars.ui/build/image --java-options -Xms1g --java-options --add-exports --java-options javafx.controls/com.sun.javafx.scene.control.behavior=com.jfoenix --java-options --add-exports --java-options javafx.controls/com.sun.javafx.scene.control=com.jfoenix --java-options --add-exports --java-options javafx.base/com.sun.javafx.binding=com.jfoenix --java-options --add-exports --java-options javafx.graphics/com.sun.javafx.stage=com.jfoenix --java-options --add-exports --java-options javafx.base/com.sun.javafx.event=com.jfoenix --java-options --add-reads --java-options vars.annotation.merged.module=org.slf4j