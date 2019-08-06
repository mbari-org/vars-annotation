# How to build vars-annotation

__Note__: We primarily develop on _macOS_ and these build instructions reflect that. They should be essentially identical on Linux.

## Set up

1. This build is for [Java 11 LTS](https://openjdk.java.net/projects/jdk/11/). Any flavor of Java 11+ should work, but we are developing against [AdoptOpenJDK 11](https://adoptopenjdk.net).
2. Run `gradlew check` to run tests and verify environment.
3. Download [jpackage](https://jdk.java.net/jpackage/). 
4. Define `JPACKAGE_HOME` environment variable that points to the location of the jpackage JVM. e.g. 
```
# bash
export JPACKAGE_HOME="/Users/brian/Applications/jdk-14.jdk/Contents/Home"

# fish
set -x JPACKAGE_HOME "/Users/brian/Applications/jdk-14.jdk/Contents/Home"
```

## Build

The build can be customized for your deployment by setting environment variables _before_ you run a build. Refer to [reference.conf](../org.mbari.vars.ui/src/main/resources/reference.conf) for more details. Currently, the following variables can be defined:

```
ACCOUNTS_SERVICE_URL,
ACCOUNTS_SERVICE_TIMEOUT,
ACCOUNTS_SERVICE_CLIENT_SECRET,
ANNOTATION_SERVICE_URL,
ANNOTATION_SERVICE_TIMEOUT,
ANNOTATION_SERVICE_CLIENT_SECRET,
ANNOTATION_SERVICE_PAGING,
ANNOTATION_SERVICE_PAGE_COUNT,
ANNOTATION_SERVICE_V2_URL,
APP_IMAGE_COPYRIGHT_OWNER,
CONCEPT_SERVICE_URL,
CONCEPT_SERVICE_TIMEOUT,
CONCEPT_SERVICE_TEMPLATE_FILTERS,
MEDIA_SERVICE_URL,
MEDIA_SERVICE_TIMEOUT,
MEDIA_SERVICE_CLIENT_SECRET,
PANOPTES_SERVICE_URL,
PANOPTES_SERVICE_TIMEOUT,
PANOPTES_SERVICE_CLIENT_SECRET,
PREFERENCES_SERVICE_URL,
PREFERENCES_SERVICE_TIMEOUT,
PREFERENCES_SERVICE_CLIENT_SECRET,
SHARKTOPODA_DEFAULTS_CONTROL_PORT,
SHARKTOPODA_DEFAULTS_FRAMEGRAB_PORT
```

To build a standalone distribution: `gradlew jpackage`