# Building from Source Code

__Note__: This project can be built on macOS, Linux, and Windows (We actually tested all of them). We primarily develop on _macOS_ and these build instructions reflect that. They should be essentially identical on Linux.

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

# cmd
set JPACKAGE_HOME=C:\Users\brian\Applications\jdk-14
```

## Build

The build can be customized for your deployment by setting environment variables _before_ you run a build. Refer to [reference.conf](../org.mbari.vars.ui/src/main/resources/reference.conf) for more details. If these are not explicitly defined, they will default to values appropriate for working with the development setup provided by [m3-microservices](https://github.com/mbari-media-management/m3-microservices). Currently, the following variables can be defined:

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

Here's an example `env-config.sh` file you could create to define your deployment environment. Just remember to run `source env-config.sh` before your build. A default `env-config.sh` script is included that will reset the values to ones used for testing with [m3-microservices](https://github.com/mbari-media-management/m3-microservices).

```bash
export ACCOUNTS_SERVICE_URL=http://vars.server.org/accounts/v1
export ACCOUNTS_SERVICE_TIMEOUT="5seconds"
export ACCOUNTS_SERVICE_CLIENT_SECRET="mysecret4accounts"
export ANNOTATION_SERVICE_URL=http://vars.server.org/anno/v1
export ANNOTATION_SERVICE_TIMEOUT="240seconds"
export ANNOTATION_SERVICE_CLIENT_SECRET="mysecret4annotations"
export ANNOTATION_SERVICE_PAGING=parallel
export ANNOTATION_SERVICE_PAGE_COUNT=2
export ANNOTATION_SERVICE_V2_URL=http://vars.server.org/anno/v2
export APP_IMAGE_COPYRIGHT_OWNER="Monterey Bay Aquarium Research Institute"
export CONCEPT_SERVICE_URL=http://vars.server.org/kb/v1
export CONCEPT_SERVICE_TIMEOUT="5seconds"
export MEDIA_SERVICE_URL=http://vars.server.org/vam/v1
export MEDIA_SERVICE_TIMEOUT="5seconds"
export MEDIA_SERVICE_CLIENT_SECRET="mysecret4media"
export PANOPTES_SERVICE_URL=http://vars.server.org/panoptes/v1
export PANOPTES_SERVICE_TIMEOUT="60seconds"
export PANOPTES_SERVICE_CLIENT_SECRET="mysecret4panoptes"
export PREFERENCES_SERVICE_URL=http://vars.server.org/accounts/v1
export PREFERENCES_SERVICE_TIMEOUT="5seconds"
export PREFERENCES_SERVICE_CLIENT_SECRET="mysecret4preferences"
export SHARKTOPODA_DEFAULTS_CONTROL_PORT=8800
export SHARKTOPODA_DEFAULTS_FRAMEGRAB_PORT=5000
```

To build a standalone distribution run: `gradlew jpackage`. The build application will be in `org.mbari.vars.ui/build/jpackage`.