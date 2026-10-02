# 🧠 Cluster Headache Tracker Android

Cluster Headache Tracker Android is a Hotwire Native wrapper for the [Cluster Headache Tracker](https://github.com/crmne/cluster-headache-tracker) web application. This app uses [Hotwire Native for Android](https://github.com/hotwired/hotwire-native-android) to provide the web app experience within a native Android app shell.

## 🎯 Purpose

The main reason for creating this Android app is to increase accessibility and visibility of the Cluster Headache Tracker. While the web application functions as a Progressive Web App (PWA), many users are unfamiliar with PWAs or how to add them to their home screens. This native Android wrapper provides a familiar way for users to download and access the app through the App Store.

## 🚀 Features

- 📱 Access the Cluster Headache Tracker web app through a familiar Android app interface
- 📊 View and interact with headache logs and charts
- 🔒 Secure authentication and data storage (handled by the web app)
- 🏠 Home-screen widget with a live timer for an ongoing attack or your days attack-free
- ⚡ App shortcuts to log an attack or open the current attack in one tap
- 📄 PDF reports open in your viewer or share sheet; photos can be attached from the camera or gallery

## 🔢 Versioning

- Local builds fall back to the checked-in app version in `app/build.gradle.kts`
- Tag releases derive `versionName` from the Git tag and `versionCode` from semver
- Example: `v2.1.1` builds as `versionName=2.1.1` and `versionCode=20101`

## 🛠 Requirements

- Android 9 (API level 28) or newer on the device
- Android Studio (its bundled JBR is the JDK the build expects)

## 🔏 Release signing

Release builds are signed with a release key when one is configured, otherwise with the debug key (so local and CI builds keep working).

Locally, set Gradle properties (e.g. in `~/.gradle/gradle.properties`) or environment variables:

```
RELEASE_KEYSTORE_PATH=/path/to/release.keystore
RELEASE_KEYSTORE_PASSWORD=…
RELEASE_KEY_ALIAS=…
RELEASE_KEY_PASSWORD=…
```

The release workflow reads these GitHub secrets: `RELEASE_KEYSTORE_BASE64` (the keystore, base64-encoded), `RELEASE_KEYSTORE_PASSWORD`, `RELEASE_KEY_ALIAS`, `RELEASE_KEY_PASSWORD`.

## 🧪 Development

```bash
export JAVA_HOME=/opt/android-studio/jbr
./gradlew testDebugUnitTest lintDebug detekt spotlessCheck
./gradlew connectedDebugAndroidTest   # needs an emulator or device
```

To run against a local Rails server, start it on a free port, run `adb reverse tcp:3078 tcp:3078` and install with `./gradlew installDebug -PbaseUrl=http://localhost:3078`.

## 📲 Installation

1. Clone the repository:
   ```
   git clone https://github.com/crmne/cluster-headache-tracker-android.git
   ```

2. Open the folder in Android Studio

3. Build and run the project on your Android device or simulator.

## 🔗 Related Projects

- [Cluster Headache Tracker (Web)](https://github.com/crmne/cluster-headache-tracker): The main web application that this Android app wraps.

## 🤝 Contributing

We welcome contributions! Please see our [Contributing Guide](CONTRIBUTING.md) for more details on how to get started.

## 📄 License

Cluster Headache Tracker Android is released under the [GNU General Public License v3.0 (GPL-3.0)](LICENSE).

## 🔒 Privacy

This Android app provides access to the same web application as the browser version, maintaining the same privacy principles. No additional personally identifiable information is collected or stored. All data handling occurs on the EU-based servers of the main Cluster Headache Tracker application.

## 🍕 Support the Project

If you find this tool valuable, please consider making a donation:

[![Buy me a pizza](https://img.shields.io/badge/Buy%20me%20a%20pizza-%2410-orange?style=for-the-badge&logo=buy-me-a-coffee&logoColor=white)](https://buymeacoffee.com/crmne)

## 🆘 Support

If you encounter any issues or have questions, please [open an issue](https://github.com/crmne/cluster-headache-tracker-android/issues) on GitHub.

## 🙏 Acknowledgements

- [Hotwire Native for Android](https://github.com/hotwired/hotwire-native-android) for enabling this web-to-native wrapper.
- All contributors and users who help improve this project.
