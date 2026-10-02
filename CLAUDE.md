# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Overview

Hotwire Native Android shell for the Cluster Headache Tracker web app (https://clusterheadachetracker.com). The web app renders every screen; the shell adds native tabs, modals, bridge components, a home-screen widget, app shortcuts and file handling.

## Toolchain

- JDK: use Android Studio's bundled JBR (`export JAVA_HOME=/opt/android-studio/jbr`). AGP doesn't run on newer system JDKs.
- Android SDK at `~/Android/Sdk` (`local.properties`), compile/target SDK 37, build-tools 37.
- Gradle 9.8 wrapper, AGP 9.4 (built-in Kotlin), Kotlin 2.4, Hotwire Native 1.3.1, Joe Masilotti's bridge-components v0.14.0 (JitPack).
- minSdk 28 (Android 9).

## Commands

```bash
./gradlew assembleDebug                         # Debug APK against production
./gradlew installDebug -PbaseUrl=http://localhost:3078   # Point a build at a local Rails server
./gradlew testDebugUnitTest                     # JVM unit tests
./gradlew connectedDebugAndroidTest             # Espresso + UI Automator tests on a device/emulator
./gradlew lintDebug detekt spotlessCheck        # Static checks (CI runs these)
./gradlew spotlessApply                         # Format
```

Local server on the emulator: run Rails on a free port, then `adb reverse tcp:3078 tcp:3078` and build with `-PbaseUrl=http://localhost:3078` (debug builds allow cleartext). The WebView can be inspected/driven over CDP: `adb forward tcp:9333 localabstract:webview_devtools_remote_<pid>`.

## Architecture

- `ClusterHeadacheTrackerApplication`: Hotwire config (logger, user agent prefix `ClusterHeadacheTracker; platform=android; version=…`), path configuration (bundled `assets/json/path-configuration.json`, remote `/configurations/android_v2.json`), fragment destinations, route decision handlers, bridge components, dynamic color.
- `MainActivity`: splash screen, edge-to-edge, `HotwireBottomNavigationController` with lazy tabs (Logs, Charts, New action tab, Account, Feedback), auth reset, shortcut/widget deep links (`DeepLinks`, action `OPEN_PATH` + `path` extra, resolved by `AppRoutes.urlForPath`).
- Fragments: `WebFragment` (`hotwire://fragment/web`) and `WebModalFragment` (`hotwire://fragment/web/modal`, full-screen in the modal context so library bridge components find a `HotwireFragment` toolbar). Both handle 401 → sign-in and attach the download listener.
- Bridge components: Joe's core set (alert, form, haptic, menu, review-prompt, search, share, theme, toast), our `button` (`AppButtonComponent`: native print, sign-out and sponsor, decided by `nativeAction`, falling back to `androidImage` and the English title for older servers), `download` (PDF reports), `widget-status` (stores the payload for the widget/shortcuts).
- Downloads (`downloads/`): same-host `.pdf`/`.csv` and WebView downloads are fetched with the WebView cookies into `cacheDir/downloads` and opened via FileProvider with a Share option.
- Widget and shortcuts (`widget/`): Glance `AttackWidget` (live timer while ongoing, days attack-free otherwise), static shortcuts in `res/xml/shortcuts.xml`, dynamic "End attack" shortcut while ongoing. Data only comes from the `widget-status` bridge payload in SharedPreferences (`widget_status`, excluded from backups); widgets never call the server.
- File uploads (camera + gallery) are handled by Hotwire Native's built-in file chooser.

## Path configuration

Rules merge in order, later rules win, and patterns match path plus query string. Keep `.*` first. The bundled copy mirrors the server's `public/configurations/android_v2.json`; `PathConfigurationTest` checks the important outcomes.

## Strings

User-facing strings live in `res/values{,-de,-it,-es}/strings.xml`; add all four languages.

## Development tips
- Hotwire Native Android source: https://github.com/hotwired/hotwire-native-android
- Rails app: ../cluster-headache-tracker
- Don't overcomplicate things.
