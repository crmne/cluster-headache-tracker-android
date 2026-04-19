# Cluster Headache Tracker Android Clean-Room Architecture

## Goal

Rebuild the Android shell around the current Hotwire Native stack and Joe Masilotti's Bridge Components libraries while preserving the existing product contract:

- Bottom tab bar with `Logs`, `Charts`, centered `New`, `Account`, and `Feedback`
- Modal authentication
- Shell-wide reset after authentication changes
- Native bridge support from Joe's public and PRO libraries

This rewrite intentionally avoids carrying forward the current app's custom navigation/auth implementation. The new shell should be small, explicit, and aligned with upstream APIs.

## Version Anchors

As verified on April 17, 2026:

- Hotwire Native Android latest release: `1.2.7`
- `joemasilotti/bridge-components` latest tag: `v0.13.2`
- `Masilotti-com/bridge-components-pro` latest tag visible from the local parent repo: `v0.13.0`

The Android app should consume the local parent checkouts via Gradle composite builds instead of JitPack/Maven artifacts. That gives us the exact code the user asked to build against and avoids release lag between tags and published artifacts.

## Source of Truth

The native Android shell is driven by:

- Rails routes and web screens in `../cluster-headache-tracker`
- Tab IA and iconography already present in the current Android/iOS apps
- Official Hotwire Native navigation and bridge behavior
- Joe Masilotti's Kotlin bridge components from the local parent repos

## App Shape

### Application

`ClusterHeadacheTrackerApplication` is responsible for:

- Hotwire configuration
- Path configuration loading from local asset plus remote server override
- Default fragment destination registration
- Bridge component registration
- User agent prefix setup for Rails-side native detection
- Debug toggles for Hotwire/WebView

### Activity Shell

`MainActivity` owns:

- `HotwireBottomNavigationController`
- The selected real tab index
- Intercepting the centered `New` action tab
- Presenting the sign-in flow
- Resetting the shell after auth changes

The activity should not own fragment-level web concerns. It should coordinate app state, not inspect `WebView` internals.

### Fragments

Two fragment classes are sufficient:

- `AppWebFragment`
  - Extends the PRO `WebFragment` so PRO components that require fragment hooks keep working
  - Handles auth success detection and 401 handling for normal screens
- `AppWebModalFragment`
  - Extends `HotwireWebBottomSheetFragment`
  - Handles the same auth logic for modal screens

Both fragments should share the same auth detection rules through a small helper instead of duplicating string checks inline.

## Navigation Model

### Tabs

The tab model remains:

1. `Logs` -> `/headache_logs`
2. `Charts` -> `/charts`
3. `New` -> action tab, not a persistent destination
4. `Account` -> `/settings`
5. `Feedback` -> `/feedback`

The `New` tab still requires a hidden navigator host because `HotwireBottomNavigationController` expects a host per tab. Its configured start URL should stay non-destructive, and the activity should immediately reselect the previous real tab and route `/headache_logs/new` on the active navigator.

### Modal Routing

Authentication and form-style routes should continue to be modal via path configuration. The app should rely on path configuration for presentation context instead of passing manual presentation bundles from Kotlin.

### Reset Strategy

On successful authentication, the Android shell should:

1. Dismiss the modal if the success was detected from within a modal auth flow
2. Reset all navigators
3. Restore the previously selected real tab

This keeps all tabs coherent and avoids per-tab refresh bookkeeping.

On authentication failure or logout, the shell should:

1. Reset the shell to clear stale navigation state
2. Present the sign-in modal on the active navigator

## Authentication Contract

### Native-side Detection

The Android app should treat any of the following as authentication success:

- Visiting `/recede_historical_location`
- Navigating from an auth route to a non-auth route

The app should treat HTTP 401 as an authentication-required signal.

### Current Rails State

The Rails app currently uses a legacy `/recede_historical_location` controller action that renders a transitional page for native apps. That works with the current application contract, but it is not the cleanest modern Hotwire Native approach.

### Preferred Future Rails State

The preferred long-term server behavior is to use Hotwire Native historical location helpers such as:

- `resume_or_redirect_to`
- `refresh_or_redirect_to`
- `recede_or_redirect_to`

Hotwire Native Android `1.2.x` supports those routes automatically. The Android rewrite should remain compatible with the current Rails implementation while keeping the auth handling isolated enough that the server can later switch to the newer helpers without another native refactor.

## Bridge Components Strategy

### Use Joe's Libraries for Native Components

Register Joe's public and PRO components from the parent repos.

Public:

- button
- share
- menu
- form
- search
- alert
- toast
- theme
- review-prompt
- haptic

PRO:

- barcode-scanner
- biometrics-lock
- document-scanner
- location
- nfc
- permissions
- notification-token only when Firebase is configured

### App-Specific Native Component

Keep one app-specific bridge component: `print`.

Reason:

- The current app relies on native Android printing from the report screens
- Joe's generic `button` component intentionally clicks the HTML element
- That is correct for sign-out and ordinary links, but printing is better treated as an app-specific native affordance

## Web Bridge Contract

The current Rails app ships custom `bridge--button` and `bridge--share` Stimulus controllers.

To use Joe's Kotlin `button` component cleanly, the Rails-side `bridge--button` controller must speak Joe's event contract:

- send `"right"` instead of `"connect"`
- send `"disconnect"` on teardown
- still preserve the current `requestSubmit()` behavior for `button_to` forms

That is a small compatibility patch, not a change in product behavior.

`share` can remain compatible with Joe's Kotlin component because the Android component only requires the URL and safely ignores extra keys.

## Build Integration

The Android project should use Gradle composite builds for:

- `../hotwire-native-android`
- `../bridge-components/android`
- `../bridge-components-pro/android`

This keeps the app pinned to the local sources and removes the need for JitPack credentials in this repo.

## Permissions and Platform Setup

Because PRO components are registered, the app manifest should include the platform permissions and metadata required by the libraries:

- Internet
- Fine/coarse location
- Post notifications
- NFC
- ML Kit barcode dependency metadata

Firebase-backed notification token support should be conditional. If no Firebase app configuration exists, the app should not register the `notification-token` component.

## Non-Goals

- Rebuilding the Rails web UI
- Reworking iOS
- Introducing native-first screens where web screens already work
- Preserving the old LocalBroadcastManager-based auth flow

## Acceptance Criteria

- Project builds against the local parent Hotwire/Bridge repos
- Bottom tabs match the current app IA and iconography
- Center `New` tab opens the log form modally and does not remain selected
- 401s present sign-in modally
- Successful sign-in resets the tab shell cleanly
- Joe's bridge components are the default native bridge layer
- Native printing still works
