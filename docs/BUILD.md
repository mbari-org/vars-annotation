# Build Instructions

These instructions provde information on how to build vars-annotation for your in-house microservices infra-structure.

## Prerequisites

### Install the following:

- [Java 11](https://adoptopenjdk.net/) - Used to compile VARS Annotation. Any flavor of Java is fine, even [GraalVM](https://www.graalvm.org/) works.
- [Java 14](https://adoptopenjdk.net/) - You need the [jpackage](https://docs.oracle.com/en/java/javase/14/jpackage/packaging-overview.html#GUID-C1027043-587D-418D-8188-EF8F44A4C06A) tool to create an installabel app. jpackage is bundled with Java 14.

### Customization for your Infrastructure

You will need to define environment variables that define information about your in-house microservices configuration. For Linux/Mac, I recommned putting these in a file like `env-config.sh`. Here's example contents below, substitute in the actual URLs and Secrets for your configuration:

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

The looks for the environment variable `JPACKAGE_HOME`. This is the equivalent of `JAVA_HOME` but points to the JDK 14+ build. For example on a standard mac installation you could use:

```shell
export JPACKAGE_HOME="/Library/Java/JavaVirtualMachines/adoptopenjdk-14.jdk/Contents/Home"
```

On linux, it could be:

```shell
export JPACKAGE_HOME=/usr/lib/jvm/openjdk-14-jd
```

## Build

Once the prerequisites are completed you can run build as follows (Mac/Linux):

```shell
export JPACKAGE_HOME="/Path/To/adoptopenjdk-14.jdk/Contents/Home"
source "/My/Path/To/env-config.sh"
gradlew clean jpackage --info
```