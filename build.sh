#!/usr/bin/env bash

# build script used internally at MBARI

export JPACKAGE_HOME="/Library/Java/JavaVirtualMachines/jdk-14.jdk/Contents/Home/"
source "$HOME/workspace/m3-deployspace/vars-annotation/env-config.sh"
gradlew clean jpackage --info

cat << EOF
Next Steps:

  1. Open VARS Annotation/Contents/Info.plist in XCode

  2. Add the following key/value to enable access to cameras. Note that in XCode
    this appears as a "Privacy - ..." setting

<key>NSCameraUsageDescription</key>
<string>VARS Annotation uses camera access for framecapture from hardware</string>

  3. Create a new folder, the name is not important

  4. Copy "VARS Annotation.app" into it

  6. Create a short cut to "Applications" and copy it into the new folder

  5. Open "Disk Utility.app"

  6. Select the menu: "File -> new Image -> Image from folder..."

  7. Rename it to the existing DMG file (e.g. "VARS Annotation-0.3.22.dmg")

EOF




# /Users/brian/Applications/jdk-14.jdk/Contents/Home/bin/jpackage --type dmg --dest /Users/brian/workspace/vars-annotation/org.mbari.vars.ui/build/jpackage --name "VARS Annotation" --module-path /Users/brian/workspace/vars-annotation/org.mbari.vars.ui/build/jlinkbase/jlinkjars --module org.mbari.vars.ui/org.mbari.vars.ui.App --app-version 0.3.15 --runtime-image /Users/brian/workspace/vars-annotation/org.mbari.vars.ui/build/image --java-options -Xms1g --java-options --add-exports --java-options javafx.controls/com.sun.javafx.scene.control.behavior=com.jfoenix --java-options --add-exports --java-options javafx.controls/com.sun.javafx.scene.control=com.jfoenix --java-options --add-exports --java-options javafx.base/com.sun.javafx.binding=com.jfoenix --java-options --add-exports --java-options javafx.graphics/com.sun.javafx.stage=com.jfoenix --java-options --add-exports --java-options javafx.base/com.sun.javafx.event=com.jfoenix --java-options --add-reads --java-options vars.annotation.merged.module=org.slf4j