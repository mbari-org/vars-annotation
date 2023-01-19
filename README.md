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

Set an the environment variable, `MAC_CODE_SIGNER` to the name of your signing identity (e.g. `Developer ID Application: foo bar (ABC123456)`). Then run `gradlew clean jpackage --info`. This will build a signed app at `org.mbari.vars.ui/build/jpackage/VARS Annotation.app`. This will then need to be notarized by Apple using:




