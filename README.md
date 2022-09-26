# vars-annotation

![MBARI logo](docs/assets/images/mbari-logo.png)

[![Build Status](https://travis-ci.org/mbari-media-management/vars-annotation.svg?branch=master)](https://travis-ci.org/mbari-media-management/vars-annotation)  [![DOI](https://zenodo.org/badge/90881605.svg)](https://zenodo.org/badge/latestdoi/90881605)

MBARI's Video Annotation and Reference System's user interface for creating and editing video annotations. For more information, visit <https://docs.mbari.org/vars-annotation/>

![VARS Annotation](docs/assets/images/vars-annotation.png)

## Developers

```bash
.\gradlew clean jpackage --info -P"gpr.user"=my_github_username -P"gpr.key"=my_github_token
```

```bash
# Hacked in better debugging using Maven. Note it may be out of sync with gradlew build
mvn install
mvn javafx:run -pl org.mbari.vars.ui
```
