# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

VARS Annotation is MBARI's Video Annotation and Reference System - a JavaFX desktop application for creating and editing video annotations. It's part of the M3 (MBARI Media Management) ecosystem and communicates with several microservices for annotation storage, media management, knowledge base access, and user authentication.

## Build Commands

```bash
# Build the application (requires Java 25+)
./gradlew build

# Build native installer (dmg on macOS, deb on Linux, msi on Windows)
./gradlew clean jpackage --info

# Run the application
./gradlew run

# Run with debug enabled on port 5005
./gradlew runDebug

# Run unit tests
./gradlew test

# Run a single test
./gradlew test --tests "org.mbari.vars.core.crypto.AESTest"

# Run integration tests (requires running backend services)
./gradlew integrationTest

# Check for dependency updates
./gradlew dependencyUpdates
```

## Architecture

### Core Components

- **`App`** (`org.mbari.vars.annotation.App`) - JavaFX Application entry point
- **`UIToolBox`** - Singleton container for shared resources: services, event bus, config, i18n, executor service
- **`Initializer`** - Static factory that wires together services and configuration (replaced Guice DI)
- **`Services`** - Aggregates all backend service interfaces for annotations, concepts, media, users, preferences

### Event-Driven Architecture

The application uses an RxJava-based `EventBus` for loose coupling:
- Components publish events via `eventBus.send(event)`
- Subscribers filter with `eventBus.toObserverable().ofType(EventClass.class)`
- Events are in `org.mbari.vars.annotation.ui.events` package

### Command Pattern

All user actions that modify state use the Command pattern for undo/redo support:
- Commands implement `Command` interface with `apply()` and `unapply()` methods
- `CommandManager` executes commands and maintains undo/redo stacks
- Commands are in `org.mbari.vars.annotation.ui.commands` package

### Backend Services

The app connects to several microservices configured via Raziel (configuration service):
- **Annosaurus** - Annotation storage and retrieval
- **Vampire Squid** - Media/video management
- **ONI (vars-kb-server)** - Knowledge base (concept hierarchy)
- **Panoptes** - Image archive service
- **Vars User Server** - User accounts and preferences

Service implementations use Retrofit for HTTP communication and are in `org.mbari.vars.services.impl.*` packages.

### Media Players

Multiple media player backends in `org.mbari.vars.annotation.ui.mediaplayers`:
- **sharktopoda/sharktopoda2** - Native macOS video player with UDP control
- **ships** - Shipboard annotation system integration
- **vcr** - RS-422 VCR control (legacy hardware)
- **macos/avf** - AVFoundation-based capture

### UI Components

JavaFX UI organized by feature in `org.mbari.vars.annotation.ui.javafx`:
- **annotable** - Annotation table display
- **cbpanel** - Concept button panels for quick annotation
- **abpanel** - Association button panels
- **concepttree** - Hierarchical concept browser
- **imgfx** - Image annotation with bounding boxes/localizations
- **roweditor** - Single annotation editing
- **mediadialog** - Media selection dialogs

## Configuration

Configuration uses Typesafe Config with this precedence:
1. `~/.vars/vars-annotation.conf` (user overrides)
2. `reference.conf` in resources (defaults)
3. Environment variables (see `reference.conf` for names)

Key settings:
- Service URLs and timeouts for all backend services
- Sharktopoda video player ports
- Annotation defaults (camera ID, group, activity)

Connection credentials are stored encrypted in `~/.vars/raziel.txt`.

## Project Structure

This is a single-module Gradle project (Kotlin DSL) at `vars-annotation/`:
- `src/main/java` - Application source code
- `src/main/resources` - Config, CSS, FXML, i18n properties
- `src/test/java` - Unit tests
- `src/integTest/java` - Integration tests (require running services)

## Java Module System

The project uses JPMS. The module is `org.mbari.vars.annotation` defined in `module-info.java`. JLink is used to create a custom runtime for distribution.

## Key Dependencies

- JavaFX 25 (via org.openjfx.javafxplugin)
- RxJava 3 for reactive event handling
- Retrofit + Gson for REST client communication
- MBARI's vcr4j libraries for video control
- imgfx for image annotation overlays
- ControlsFX for enhanced JavaFX controls
