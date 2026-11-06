# Building from Source Code

![MBARI logo](../images/mbari-logo.png)

This project can be built on macOS, Linux, and Windows. We primarily develop on _macOS_ and these build instructions reflect that. They should be essentially identical on Linux.

## tl;dr

```bash
./gradlew clean jpackage
# built app is in vars-annotation/build/jpackage
```

## Prerequisites

### Install the following

- [Java 27](https://adoptium.net/): used to compile and package VARS Annotation. Java 27 includes the `jpackage` tool needed to create an installable app. The built app bundles its own JVM, so users do not need Java installed to run it.

### Customization for your Infrastructure

Service endpoints (Annosaurus, Vampire Squid, ONI, Panoptes) and their credentials are **not** set at build time. VARS fetches them at runtime from the configuration server ([raziel](https://github.com/mbari-org/raziel)) that users enter in the **Settings** dialog.

Other defaults come from [reference.conf](https://github.com/mbari-org/vars-annotation/blob/main/vars-annotation/src/main/resources/reference.conf), which is the definitive list of available settings. These values are resolved each time the app **starts**, not when it is built. You can override them in either of two ways:

1. **A user config file.** Create `~/.vars/vars-annotation.conf` (HOCON format). Any keys in it take precedence over `reference.conf`. For example:

    ```hocon
    annotation.service.timeout = "240 seconds"
    app.defaults.group = "ROV"
    app.image.copyright.owner = "The Name of Your Organization Here"
    sharktopoda.defaults.control.port = 8800
    ```

2. **Environment variables** set in the environment that launches VARS. Commonly used ones:

    | Variable | Default | Purpose |
    |----------|---------|---------|
    | `ANNOTATION_SERVICE_TIMEOUT` | `30 seconds` | Annosaurus request timeout |
    | `ANNOTATION_SERVICE_PAGE_SIZE` | `1000` | Annotations fetched per page |
    | `ANNOTATION_SERVICE_PAGING` | `parallel` | `parallel` or `sequential` paging |
    | `ANNOTATION_SERVICE_PAGE_COUNT` | `2` | Simultaneous page requests when paging is `parallel` |
    | `CONCEPT_SERVICE_TIMEOUT` | `5 seconds` | ONI request timeout |
    | `CONCEPT_SERVICE_TEMPLATE_FILTERS` | `["^dsg.*"]` | Regexes for link templates hidden in the UI |
    | `MEDIA_SERVICE_TIMEOUT` | `10 seconds` | Vampire Squid request timeout |
    | `PANOPTES_SERVICE_TIMEOUT` | `60 seconds` | Panoptes request timeout |
    | `PREFERENCES_SERVICE_TIMEOUT` | `5 seconds` | User preferences request timeout |
    | `RAZIEL_TIMEOUT` | `10 seconds` | How long to wait for the configuration server at startup before telling the user it can't be reached |
    | `APP_DEFAULTS_GROUP` | `ROV` | Initial group for new annotations |
    | `APP_DEFAULTS_ACTIVITY` | `descend` | Initial activity for new annotations |
    | `APP_DEFAULTS_CAMERAID` | _(empty)_ | Default camera ID |
    | `APP_IMAGE_COPYRIGHT_OWNER` | _(empty)_ | Copyright owner shown in the text overlay on framegrabs |
    | `SHARKTOPODA_DEFAULTS_CONTROL_PORT` | `8800` | UDP port for controlling Sharktopoda |
    | `SHARKTOPODA_DEFAULTS_FRAMEGRAB_PORT` | `5000` | Port for frame captures from Sharktopoda |
    | `LOCALIZATION_DEFAULTS_INCOMING_PORT` | `5561` | Incoming localization port |
    | `LOCALIZATION_DEFAULTS_OUTGOING_PORT` | `5562` | Outgoing localization port |

    See `reference.conf` for the rest, including the `APP_ANNOTATION_*` association names.

### Code signing on macOS (optional)

If `MAC_CODE_SIGNER` is set when you build on a Mac, the build signs the packaged app with `codesign` using that identity, for example:

```bash
export MAC_CODE_SIGNER="Developer ID Application: Your Name (1AB2C345V6)"
```

## Build

1. Optional: Run `./gradlew test` to run unit tests and verify the environment
2. Optional (macOS): `export MAC_CODE_SIGNER=...` to sign the app
3. Run `./gradlew clean jpackage`
4. The built package will be found in `vars-annotation/build/jpackage`. The app is packaged for whatever OS you run the build on, and includes its own bundled JVM so it can be distributed without requiring users to install Java.

## Versioning and releases

The version comes from the most recent git tag; there is no version number to edit in the build files. To make a release:

```bash
git tag 2.7.0
git push origin 2.7.0
./gradlew clean jpackage
```

- **Tagged builds** show the tag, for example `2.7.0`, on the splash screen and in the log.
- **Builds after a tag** show the number of commits since the tag and the commit, for example `2.7.0-3-g1a2b3c4d`. Uncommitted changes add `-dirty`.
- **The installer version** (`jpackage --app-version`) is always the numeric `x.y.z` part, because macOS and Windows installers only accept numbers.
- **The copyright year** on the installer and splash screen is the year of the commit being built.
- **Building without git history**, for example from a source download or a shallow clone without tags, falls back to `0.0.0-SNAPSHOT`. Pass the version explicitly instead: `./gradlew clean jpackage -PappVersion=2.7.0`.

## Useful commands

Show application dependencies:

```bash
./gradlew vars-annotation:dependencies --configuration implementation
```

Check for available dependency updates:

```bash
./gradlew dependencyUpdates
```
