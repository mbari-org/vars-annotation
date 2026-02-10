# vars-annotation

![MBARI logo](docs/assets/images/mbari-logo.png)

[![Build Status](https://travis-ci.org/mbari-media-management/vars-annotation.svg?branch=master)](https://travis-ci.org/mbari-media-management/vars-annotation)  [![DOI](https://zenodo.org/badge/90881605.svg)](https://zenodo.org/badge/latestdoi/90881605)

MBARI's Video Annotation and Reference System's user interface for creating and editing video annotations. For more information, visit <https://docs.mbari.org/vars-annotation/>

![VARS Annotation](docs/assets/images/vars-annotation.png)

## Developers

VARS Uses libraries in [mbari-org/maven](https://github.com/mbari-org/maven). You can follow the instructions there to allow gradle to fetch the needed libraries or incuded you github user hame and access token in the example below.

```bash
.\gradlew clean jpackage --info -P"gpr.user"=my_github_username -P"gpr.key"=my_github_token
```

### Code signing for Macs

```bash
# You need a Developer ID Application cert from developer.apple.com. A fake id used below
export MAC_CODE_SIGNER="Developer ID Application: blah blah blah (ABC123456)"

cd vars-annotation

# The [BaseHttpClient.java](org.mbari.vars.services/src/main/java/org/mbari/vars/services/impl/BaseHttpClient.java)build will correctly sign everything if your MAC_CODE_SIGNER is correct
./gradlew clean jpackage --info

cd  org.mbari.vars.ui/build/jpackage

# App must be packaged/zipped to be notarized
ditto -c -k --keepParent "VARS Annotation.app" "VARS Annotation.zip"

xcrun notarytool submit "VARS Annotation.zip" \
    --wait \
    --team-id ABC123456 \                      # Found in your Developer ID cert name
    --apple-id <your apple login> \            # Your email you log in to developer.apple.com with
    --password "<your app specific password>"  # You have to use an app password for your account from appleid.apple.com

# We staple to the original app, NOT the zip
xcrun stapler staple "VARS Annotation.app"

# Remove the old zip file
rm "VARS Annotation.zip"

# Rezip the app and use that zip to distribute it.
ditto -c -k --keepParent "VARS Annotation.app" "VARS Annotation.zip"
```



