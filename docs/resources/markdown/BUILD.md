# Building from Source Code

![MBARI logo](../images/mbari-logo.png)

This project can be built on macOS, Linux, and Windows (We actually tested all of them). We primarily develop on _macOS_ and these build instructions reflect that. They should be essentially identical on Linux.

## tl;dr

```bash
export JPACKAGE_HOME="/Users/brian/Applications/jdk-14.jdk/Contents/Home"
source env-config.sh
gradle jpackage
# built app is in org.mbari.vars.ui/build/jpackage
```

## Prerequisites

### Install the following:

- [Java 11](https://adoptopenjdk.net/) - Used to compile VARS Annotation. Any flavor of Java 11 is fine, even [GraalVM](https://www.graalvm.org/) works.
- [Java 14](https://adoptopenjdk.net/) - You need the [jpackage](https://docs.oracle.com/en/java/javase/14/jpackage/packaging-overview.html#GUID-C1027043-587D-418D-8188-EF8F44A4C06A) tool to create an installable app. jpackage is bundled with Java 14.

### Customization for your Infrastructure

The build can be customized for your deployment by setting environment variables _before_ you run a build. Refer to [reference.conf](https://github.com/mbari-media-management/vars-annotation/blob/master/org.mbari.vars.ui/src/main/resources/reference.conf) for more details. If any of these variables are not explicitly defined, they will default to values appropriate for working with the development setup provided by [m3-microservices](https://github.com/mbari-media-management/m3-microservices). This project includes an example [env-config.sh](https://github.com/mbari-media-management/vars-annotation/blob/master/env-config.sh)

These environment variables define information about your in-house microservices configuration. For Linux/Mac, I recommend putting these in a file like `env-config.sh`. Here's example contents below, substitute in the actual URLs and Secrets for your configuration:

__env-config.sh__

```shell
export ACCOUNTS_SERVICE_URL=http://vars-user-server.org/accounts/v1
export ACCOUNTS_SERVICE_TIMEOUT="5seconds"
export ACCOUNTS_SERVICE_CLIENT_SECRET="myAccountServerSecret"
export ANNOTATION_SERVICE_URL=http://annosaurus.org/anno/v1
export ANNOTATION_SERVICE_TIMEOUT="240seconds"
export ANNOTATION_SERVICE_CLIENT_SECRET="myAnnosaurusSecret"
export ANNOTATION_SERVICE_PAGING=parallel
export ANNOTATION_SERVICE_PAGE_COUNT=2
export ANNOTATION_SERVICE_V2_URL=http://annosaurus.org/anno/v2
export APP_IMAGE_COPYRIGHT_OWNER="The Name of Your Organization Here"
export CONCEPT_SERVICE_URL=http://vars-kb-server.org/kb/v1
export CONCEPT_SERVICE_TIMEOUT="5seconds"
export MEDIA_SERVICE_URL=http://vampire-squid.org/vam/v1
export MEDIA_SERVICE_TIMEOUT="5seconds"
export MEDIA_SERVICE_CLIENT_SECRET="myVampireSquidSecret"
export PANOPTES_SERVICE_URL=http://panoptes.org/panoptes/v1
export PANOPTES_SERVICE_TIMEOUT="60seconds"
export PANOPTES_SERVICE_CLIENT_SECRET="myPanoptesSecret="
export PREFERENCES_SERVICE_URL=http://vars-user-service.org/accounts/v1
export PREFERENCES_SERVICE_TIMEOUT="5seconds"
export PREFERENCES_SERVICE_CLIENT_SECRET="myAccountServerSecret"
export SHARKTOPODA_DEFAULTS_CONTROL_PORT=8800
export SHARKTOPODA_DEFAULTS_FRAMEGRAB_PORT=5000


# Optional. If these are provided and you are building on a mac, these value
# will be used to code sign your app. These values are from your Apple Develop
# Signing certificate
export MAC_PACKAGE_SIGNING_PREFIX=1AB2C345V6
export MAC_SIGNING_KEY_USER_NAME="Your Sigining User Name"
```

### Define JPACKAGE_HOME

The looks for the environment variable `JPACKAGE_HOME`. This is the equivalent of `JAVA_HOME` but points to the JDK 14+ build. Here's examples

__macOS__: E

```bash
# bash
export JPACKAGE_HOME="/Library/Java/JavaVirtualMachines/jdk-14.jdk/Contents/Home"

# fish
set -x JPACKAGE_HOME "/Library/Java/JavaVirtualMachines/jdk-14.jdk/Contents/Home"
```

__Windows__

```shell
# cmd
set JPACKAGE_HOME="C:\Users\brian\Applications\jdk-14"
```

__Linux__

```shell
export JPACKAGE_HOME=/usr/lib/jvm/openjdk-14-jdk
```

## Build

1. Run `gradlew check` to run tests and verify environment
2. Set your `JPACKAGE_HOME` variable. e.g. `export JPACKAGE_HOME="/Path/To/adoptopenjdk-14.jdk/Contents/Home"`
3. Set other required env variables: `source "/My/Path/To/env-config.sh"`
4. Run `gradlew clean jpackage --info`
5. The build package will be found in `vars-annotation/org.mbari.vars.ui/build/jpackage`. The app will be packaged for whatever OS you run the build on. Note that the built app includes it's own packaged JVM, so it can be distrubuted without requiring users to install Java.

## Useful commands

Show application dependencies:

```
gradle org.mbari.vars.ui:dependencies --configuration implementation
```
