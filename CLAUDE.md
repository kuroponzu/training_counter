# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is an Android application called "training_counter" built with Kotlin. It's a standard Android project using Gradle with version catalogs for dependency management.

## Development Commands

### Build and Run
- `./gradlew build` - Build the entire project
- `./gradlew app:build` - Build only the app module
- `./gradlew assembleDebug` - Build debug APK
- `./gradlew assembleRelease` - Build release APK
- `./gradlew installDebug` - Install debug APK to connected device

### Testing
- `./gradlew test` - Run unit tests
- `./gradlew connectedAndroidTest` - Run instrumented tests on connected device
- `./gradlew app:testDebugUnitTest` - Run unit tests for debug build

### Code Quality
- `./gradlew lint` - Run Android lint checks
- `./gradlew lintDebug` - Run lint on debug build only

### Cleaning
- `./gradlew clean` - Clean build artifacts

## Project Structure

- **Package**: `com.example.training_counter`
- **Target SDK**: 35 (Android 15)
- **Min SDK**: 35
- **Language**: Kotlin with JVM target 11
- **Dependencies**: Standard Android libraries (AndroidX, Material Design)

## Configuration

- Uses Gradle version catalogs (`gradle/libs.versions.toml`) for dependency management
- Kotlin version: 2.0.21
- Android Gradle Plugin: 8.10.0
- No activity is currently defined in the manifest - this appears to be a fresh project template

## Testing Setup

- Unit tests: JUnit 4
- Instrumented tests: AndroidX Test with Espresso
- Test runner: AndroidJUnitRunner