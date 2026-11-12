# Pulse Health for Android

A production-oriented Jetpack Compose health dashboard inspired by the supplied visual reference. The UI is original native Android code: no screenshot or embedded web view is used.

## Included

- Responsive Summary dashboard with day, week, and month ranges
- Animated activity rings and metric visualizations
- Persistent hydration history in Room and editable favorites via DataStore
- Activity, steps, heart-rate, sleep, and hydration detail views
- Summary, Sharing, and Browse navigation
- Health Connect availability, permission, and data-reading flow
- Dark theme support, accessibility labels, edge-to-edge layout
- Release resource shrinking and R8 configuration
- Local unit-test foundation

## Stack

- Kotlin 2.4 / Android Gradle Plugin 9.4
- Jetpack Compose + Material 3
- Lifecycle ViewModel and StateFlow
- Preferences DataStore
- Room database with exported schemas
- Android Health Connect 1.1 stable

## Run

1. Open this directory in the latest stable Android Studio.
2. Install Android SDK 37 when prompted.
3. Let Gradle sync, then run the `app` configuration on an API 26+ device.
4. For real metrics, open Profile and connect Health Connect. Without permission, the app intentionally shows polished demo data.

Health Connect permissions must also be declared in the Google Play Console before a production release. Replace the debug/demo identity, configure a private release keystore outside source control, add a privacy policy, and complete the Health Apps declaration before publishing.

## Data handling

The app stores only presentation preferences and water progress locally. Health records are read through Health Connect after explicit user consent; they are not uploaded by this project.

## CI/CD

`Android CI` runs unit tests, Android lint, and a debug APK build for every pull request and every push to `main`. APKs and reports are retained as workflow artifacts.

`Android Release` runs for semantic version tags such as `v1.0.0`. It builds signed APK and AAB files, uploads them as workflow artifacts, and creates a GitHub release. Configure these encrypted secrets in the GitHub `production` environment before creating a release tag:

- `ANDROID_KEYSTORE_BASE64`
- `ANDROID_STORE_PASSWORD`
- `ANDROID_KEY_ALIAS`
- `ANDROID_KEY_PASSWORD`

On PowerShell, create the base64 value without line breaks with:

```powershell
[Convert]::ToBase64String([IO.File]::ReadAllBytes("C:\path\to\release.jks"))
```
