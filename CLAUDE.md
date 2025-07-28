# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Overview

This is a Hotwire Native Android app that wraps the Cluster Headache Tracker web application. It provides a native Android shell for the web app at https://clusterheadachetracker.com, making it easier for users to access through the Google Play Store rather than installing a PWA.

## Build and Development Commands

### Building the App
```bash
# Build debug APK
./gradlew assembleDebug

# Build release APK
./gradlew assembleRelease

# Install debug build on connected device
./gradlew installDebug

# Clean build artifacts
./gradlew clean
```

### Running Tests
```bash
# Run unit tests
./gradlew test

# Run instrumented tests (requires device/emulator)
./gradlew connectedAndroidTest

# Run all tests
./gradlew check
```

### Linting and Code Quality
```bash
# Run Android Lint
./gradlew lint

# View lint results after running
# HTML report: app/build/reports/lint-results-debug.html
```

## Architecture

### Technology Stack
- **Hotwire Native 1.2.0**: Web-to-native bridge framework
- **Kotlin**: Primary development language
- **Android SDK 34**: Target Android 14 (API 34), minimum Android 9 (API 28)
- **Gradle 8.9 with Kotlin DSL**: Build system

### Key Components

1. **MainActivity** (`app/src/main/java/me/paolino/clusterheadachetracker/MainActivity.kt`):
   - Extends `HotwireActivity`
   - Configures Hotwire with local path configuration
   - Sets start location to `https://clusterheadachetracker.com/headache_logs`

2. **Path Configuration** (`app/src/main/assets/json/android_v1.json`):
   - Defines navigation rules for different URL patterns
   - Modal presentation for create/edit actions
   - Fragment navigation for main sections

3. **App Structure**:
   - Single activity architecture with Hotwire managing navigation
   - No local data storage - all data handled by web app
   - Minimal permissions (only INTERNET)

### Important Files
- `app/build.gradle.kts`: App module configuration with dependencies
- `gradle/libs.versions.toml`: Centralized version catalog
- `app/src/main/AndroidManifest.xml`: App manifest with permissions and activity configuration

## Development Notes

1. **Signing**: Release builds currently use debug signing config - update for production release
2. **ProGuard**: Minification is disabled - consider enabling for production
3. **Web URL**: 
   - Debug builds use local server: `http://192.168.8.220:3000`
   - Release builds use production: `https://clusterheadachetracker.com`
   - Update IP in `AppConfig.kt` to match your local development server
4. **Navigation**: All navigation is handled by Hotwire based on web app URLs
5. **Testing**: Basic test setup exists but no actual tests implemented
6. **Network Security**: Debug builds allow cleartext HTTP traffic for local development

## Common Tasks

### Updating Hotwire Native
1. Update versions in `app/build.gradle.kts`
2. Check for breaking changes in Hotwire Native changelog
3. Test navigation thoroughly, especially modals

### Changing Web App URL
1. Update `BASE_URL` in `MainActivity.kt`
2. Update `startLocation` if the initial route changes

### Adding Native Features
Since this is a Hotwire wrapper, native features should be minimal. Any native functionality should:
1. Be added as a bridge component if needed by the web app
2. Follow Hotwire Native patterns for JavaScript-to-native communication


## Development tips
- Check the hotwire-native-android code and demo in ~/Code/External/hotwire-native-android/
- Check the rails app code in ~/Code/Plenty/cluster-headache-tracker/
- Don't overcomplicate things.
