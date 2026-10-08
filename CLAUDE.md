# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

VARS Annotation is MBARI's (Monterey Bay Aquarium Research Institute) JavaFX desktop application for creating and editing video annotations. It is **not standalone** — it requires external microservices (Annosaurus, ONI, Vampire Squid, Raziel, Panoptes) to function. Part of MBARI's Media Management software stack.

## Build & Run Commands

```bash
# Build
./gradlew clean build

# Run the application (classpath mode, no JPMS)
./gradlew run

# Run with remote debug on port 5005
./gradlew runDebug

# Unit tests only
./gradlew test

# Integration tests (requires running config server)
./gradlew integrationTest

# Run a specific test class
./gradlew test --tests "org.mbari.vars.annotation.test.etc.jdk.crypto.AESTest"

# Build native installer (DMG on macOS, DEB on Linux, MSI on Windows)
./gradlew clean jpackage

# Build with GitHub Packages authentication
./gradlew clean jpackage -P"gpr.user"=USERNAME -P"gpr.key"=TOKEN

# Check for dependency updates
./gradlew dependencyUpdates
```

## Tech Stack

- **Java 27** (Gradle toolchain) with JPMS (module `org.mbari.vars.annotation`)
- **JavaFX 27** — UI framework (via `org.openjfx.javafxplugin`)
- **Gradle Kotlin DSL** — build system (single subproject: `vars-annotation/`)
- **Typesafe Config** (HOCON) — configuration (`reference.conf` + `~/.vars/vars-annotation.conf`)
- **RxJava 3** — reactive event bus for decoupled UI communication
- **Kiota-generated SDKs** — HTTP clients for MBARI microservices
- **ControlsFX**, **imgfx** (image annotation overlays), **vcr4j** (video control)
- **JUnit Jupiter 5** — testing

## Architecture

### Key Patterns

1. **Service Locator via `UIToolBox`** — Central dependency container created once in `Initializer.getToolBox()`. Holds `Data`, `Services`, `EventBus`, `ExecutorService`, `Config`, and stylesheets. Passed to all controllers.

2. **EventBus (RxJava)** — All UI state changes broadcast via `EventBus` (`etc/rxjava/EventBus.java`). Controllers subscribe to typed events (e.g., `AnnotationsAddedEvent`, `MediaChangedEvent`). Never call UI updates directly across components — always go through the event bus.

3. **Command Pattern** — `ui/commands/` contains 30+ command classes managed by `CommandManager` for undo/redo support. All annotation operations (create, delete, duplicate, framegrab) are commands.

4. **Service Decorators** — Services wrap Kiota SDK clients with caching (`CachedConceptService`, `CachedMediaService`) and filtering (`AnnotationServiceDecorator`). The `Services` record bundles all backend clients together.

5. **Media Player SPI** — `MediaControlsFactory` is a Java `ServiceLoader` interface. Implementations: Sharktopoda / Sharktopoda 2 (UDP to external AVFoundation-based Swift video player), macOS native (BM framework), SHIPS (shipboard integration), VCR (legacy RS-422 hardware). Registered in `module-info.java` `provides` clause and `META-INF/services/`.

6. **No DI framework** — Guice was removed due to JPMS conflicts. `Initializer` is a static class that wires everything manually with a synchronized lock.

### Source Layout (`vars-annotation/src/main/java/org/mbari/vars/annotation/`)

| Package | Purpose |
|---------|---------|
| `App.java` / `AppConfig.java` | Entry point and typed config (Java records) |
| `ui/` | JavaFX controllers, `UIToolBox`, `Data` (observable state), `Initializer` |
| `ui/commands/` | Command pattern implementations for all annotation operations |
| `ui/events/` | Typed event classes for the RxJava EventBus |
| `ui/javafx/` | JavaFX components: `annotable` (annotation table), `cbpanel`/`abpanel` (concept/association button panels), `concepttree`, `imgfx`/`imageanno` (bounding boxes/localizations), `roweditor`, `mediadialog`, `prefs`, etc. |
| `ui/mediaplayers/` | Media player abstractions and implementations (Sharktopoda, Sharktopoda 2, macOS, SHIPS, VCR) |
| `services/` | Backend service wrappers: `annosaurus/`, `oni/`, `vampiresquid/`, `raziel/`, `panopes/`, `noop/`, `ml/` |
| `model/` | Domain model classes |
| `etc/` | Utilities: `gson/` (custom serializers), `jdk/` (AES, strings, IO), `rxjava/` (EventBus) |

### External Microservices

| Service | SDK | Purpose |
|---------|-----|---------|
| **Annosaurus** | `annosaurus-java-sdk` | Annotation CRUD |
| **ONI** | `oni-java-sdk` | Concept vocabulary, user preferences |
| **Vampire Squid** | `vampire-squid-java-sdk` | Media/video metadata |
| **Raziel** | `raziel-java-sdk` | Configuration server |
| **Panoptes** | (custom) | Image archiving |

### Bootstrap Sequence

`App.main()` → sets UTC timezone, logging → `App.init()` → `Initializer.getToolBox()` (creates Services, EventBus, Data, thread pool, UIToolBox singleton) → `App.start()` → shows scene, starts `ActiveAppBeacon` (UDP single-instance check on 10 ports).

### Configuration

Config loads from `reference.conf` (defaults) with overrides from `~/.vars/vars-annotation.conf` (HOCON). All values can also be overridden via environment variables (e.g., `ANNOTATION_SERVICE_TIMEOUT`, `SHARKTOPODA_DEFAULTS_CONTROL_PORT`). The `AppConfig` class provides typed access via nested Java records.

Raziel connection credentials are stored encrypted in `~/.vars/raziel.txt`.

### Threading

- UI updates: `Platform.runLater()` on JavaFX thread
- Background work: Fixed thread pool (`CPUs * 2` threads) via `toolBox.getExecutorService()`
- Service calls: Async via `CompletableFuture`

## JPMS Notes

- Module path is **disabled** for `run`, `test`, and `integrationTest` tasks because Kiota SDKs have split packages
- The `jlink`/`jpackage` build uses `mergedModule` to handle this properly
- `module-info.java` opens specific packages to `javafx.fxml`, `javafx.graphics`, and `com.google.gson` for reflection
- `extraJavaModuleInfo` plugin auto-assigns module names to legacy JARs

## Test Structure

- **Unit tests**: `vars-annotation/src/test/java/` — run with `./gradlew test`
- **Integration tests**: `vars-annotation/src/integTest/java/` — run with `./gradlew integrationTest` (requires backend services)
- Test utilities: `TestToolbox`, `TestUtils`, `ITConfig`
