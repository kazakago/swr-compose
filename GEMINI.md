# GEMINI.md

This file provides guidance to AI agent when working with code in this repository.

## Project Overview

This is a Kotlin Multiplatform library called "SWR for Compose" - a port of React SWR for Jetpack Compose and Compose Multiplatform. The project implements the "stale-while-revalidate" HTTP cache invalidation strategy for data fetching in Compose applications.

## Architecture

The project is structured as a multi-module Kotlin Multiplatform project:

- **swr-store**: Core data storage and caching logic
- **swr-runtime**: Core SWR runtime implementation with platform-specific network monitoring
- **swr-compose**: Compose-specific implementations with `rememberSWR` functions
- **exampleApp**: Comprehensive example application demonstrating all features
- **build-logic**: Custom Gradle build logic for publishing

### Key Components

- **SWRStore**: Main data store with state management (`swr-store/src/commonMain/kotlin/com/kazakago/swr/store/SWRStore.kt`)
- **SWRConfig**: Global configuration (`swr-runtime/src/commonMain/kotlin/com/kazakago/swr/runtime/SWRConfig.kt`)
- **RememberSWR**: Compose integration (`swr-compose/src/commonMain/kotlin/com/kazakago/swr/compose/RememberSWR.kt`)

## Common Development Commands

### Build
```bash
./gradlew swr-compose:build swr-runtime:build swr-store:build                 # Build all modules
```

### Test
```bash
./gradlew swr-compose:jvmTest swr-runtime:jvmTest swr-store:jvmTest           # Run jvm tests (fast)
./gradlew swr-compose:allTests swr-runtime:allTests swr-store:allTests        # Run all tests (slow)
```

### Example App
```bash
./gradlew :exampleApp:run                    # Run desktop app
./gradlew :exampleApp:wasmJsBrowserRun       # Run web app
./gradlew :exampleApp:installDebug           # Install Android app
```

## Platform Support

The library supports:
- **Android**: Full Android support with Activity integration
- **Desktop**: JVM desktop applications
- **iOS**: iOS applications via Kotlin/Native
- **Web**: WASM-JS web applications

## Key Technologies

- **Kotlin Multiplatform**: Core language and multiplatform support
- **Compose Multiplatform**: UI framework for all platforms
- **Kotlinx Coroutines**: Asynchronous programming

## Testing

Tests are located in `commonTest` directories within each module. The project uses:
- Kotlin Test framework
- Kotlinx Coroutines Test
- Turbine for testing flows

## Configuration

- **gradle.properties**: Gradle and Android configuration
- **libs.versions.toml**: Centralized version management
- **settings.gradle.kts**: Project structure and repository configuration
