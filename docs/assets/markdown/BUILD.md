# Building from Source Code

![MBARI logo](../images/mbari-logo.png)

This project can be built on macOS, Linux, and Windows. We primarily develop on _macOS_ and these build instructions reflect that. They should be essentially identical on Linux.

## tl;dr

```bash
./gradlew clean jpackage
# built app is in vars-annotation/build/jpackage
```

## Prerequisites

### Install the following:

- [Java 25](https://adoptium.net/) — Used to compile and package VARS Annotation. Java 25 includes the `jpackage` tool needed to create an installable app. The built app bundles its own JVM, so users do not need Java installed to run it.

### Customization for your Infrastructure

VARS is configured at runtime via the configuration server ([raziel](https://github.com/mbari-org/raziel)). At build time, you can bake in default values for service endpoints by defining environment variables _before_ you run the build. Refer to [reference.conf](https://github.com/mbari-org/vars-annotation/blob/main/vars-annotation/src/main/resources/reference.conf) for the full list of available settings.

If any of these variables are not explicitly defined, they will default to values appropriate for working with the development setup provided by [vars-quickstart-public](https://github.com/mbari-org/vars-quickstart-public).

For Linux/macOS, we recommend putting these in a file like `env-config.sh`. Here's an example — substitute in the actual URLs and secrets for your configuration:

__env-config.sh__

```shell
export ANNOTATION_SERVICE_URL=http://annosaurus.org/anno/v1
export ANNOTATION_SERVICE_TIMEOUT="240seconds"
export ANNOTATION_SERVICE_CLIENT_SECRET="myAnnosaurusSecret"
export ANNOTATION_SERVICE_PAGING=parallel
export ANNOTATION_SERVICE_PAGE_COUNT=2
export ANNOTATION_SERVICE_V2_URL=http://annosaurus.org/anno/v2
export APP_IMAGE_COPYRIGHT_OWNER="The Name of Your Organization Here"
export CONCEPT_SERVICE_URL=http://oni.org/kb/v1
export CONCEPT_SERVICE_TIMEOUT="5seconds"
export MEDIA_SERVICE_URL=http://vampire-squid.org/vam/v1
export MEDIA_SERVICE_TIMEOUT="5seconds"
export MEDIA_SERVICE_CLIENT_SECRET="myVampireSquidSecret"
export PANOPTES_SERVICE_URL=http://panoptes.org/panoptes/v1
export PANOPTES_SERVICE_TIMEOUT="60seconds"
export PANOPTES_SERVICE_CLIENT_SECRET="myPanoptesSecret"
export SHARKTOPODA_DEFAULTS_CONTROL_PORT=8800
export SHARKTOPODA_DEFAULTS_FRAMEGRAB_PORT=5000

# Optional. If these are provided and you are building on a Mac, these values
# will be used to code sign your app. These values are from your Apple Developer
# signing certificate.
export MAC_PACKAGE_SIGNING_PREFIX=1AB2C345V6
export MAC_SIGNING_KEY_USER_NAME="Your Signing User Name"
```

## Build

1. Optional: Run `./gradlew test` to run unit tests and verify the environment
2. Source your env config: `source "/My/Path/To/env-config.sh"`
3. Run `./gradlew clean jpackage`
4. The built package will be found in `vars-annotation/build/jpackage`. The app is packaged for whatever OS you run the build on, and includes its own bundled JVM so it can be distributed without requiring users to install Java.

## Useful commands

Show application dependencies:

```
./gradlew vars-annotation:dependencies --configuration implementation
```

Check for available dependency updates:

```
./gradlew dependencyUpdates
```
