# Git Hooks for Android Project

This directory contains git hooks that enforce code quality standards for the Android project, similar to how overcommit works for Ruby/Rails projects.

## Setup

Git hooks are automatically installed when you run any Gradle task. They can also be manually installed by running:

```bash
./gradlew installGitHooks
```

## Pre-commit Hook

The pre-commit hook runs the following checks before allowing a commit:

1. **Spotless** - Checks code formatting using ktlint rules
2. **Detekt** - Performs static code analysis for Kotlin
3. **Android Lint** - Runs Android-specific lint checks

## Bypassing Hooks

If you need to bypass the hooks temporarily (not recommended), you can use:

```bash
git commit --no-verify
```

## Fixing Issues

- **Formatting issues**: Run `./gradlew spotlessApply` to automatically fix
- **Detekt issues**: Check the report at `build/reports/detekt/detekt.html`
- **Lint issues**: Check the report at `app/build/reports/lint-results-debug.html`

## Configuration

- **Spotless**: Configured in the root `build.gradle.kts`
- **Detekt**: Configured in `detekt-config.yml`
- **Android Lint**: Uses default Android configuration