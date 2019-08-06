# Building from Source Code

__Note__: This project can be built on macOS, Linux, and Windows (We actually tested all of them). We primarily develop on _macOS_ and these build instructions reflect that. They should be essentially identical on Linux.

## tl;dr

```bash
export JPACKAGE_HOME="/Users/brian/Applications/jdk-14.jdk/Contents/Home"
source my-env-conf.sh
gradle jpackage
# built app is in org.mbari.vars.ui/build/jpackage
```

## Instructions

### Set up

1. This build is for [Java 11 LTS](https://openjdk.java.net/projects/jdk/11/), so install that before building. Any flavor of Java 11+ should work, but we are developing against [AdoptOpenJDK 11](https://adoptopenjdk.net).
2. Run `gradlew check` to run tests and verify environment.
3. Download [jpackage](https://jdk.java.net/jpackage/). 
4. Define `JPACKAGE_HOME` environment variable that points to the location of the jpackage JVM:

```bash
# bash
export JPACKAGE_HOME="/Users/brian/Applications/jdk-14.jdk/Contents/Home"

# fish
set -x JPACKAGE_HOME "/Users/brian/Applications/jdk-14.jdk/Contents/Home"

# cmd
set JPACKAGE_HOME=C:\Users\brian\Applications\jdk-14
```

### Build

The build can be customized for your deployment by setting environment variables _before_ you run a build. Refer to [reference.conf](https://github.com/mbari-media-management/vars-annotation/blob/master/org.mbari.vars.ui/src/main/resources/reference.conf) for more details. If these are not explicitly defined, they will default to values appropriate for working with the development setup provided by [m3-microservices](https://github.com/mbari-media-management/m3-microservices). Currently, the following variables can be defined:

```bash
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

This project includes an example [env-config.sh](https://github.com/mbari-media-management/vars-annotation/blob/master/env-config.sh) that sets the environment variables to ones used for testing with [m3-microservices](https://github.com/mbari-media-management/m3-microservices). You can create you own to define variables you use for your in-house configuration. Just remember to run `source env-config.sh` before your build.

Once your enviroment is configured, run: `gradlew jpackage` to build the application. The built application will be in `org.mbari.vars.ui/build/jpackage`. Note that the build will include it's own JVM, so it can be distrubuted without requiring users to install Java.
