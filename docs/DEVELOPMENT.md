# Development and Delivery Guide

## 1. Prerequisites

- Windows, macOS or Linux supported by Android Studio
- Current stable Android Studio
- JDK 21 for local/CI consistency
- Android SDK 37 and build tools
- Git
- API 26+ device or emulator

The repository includes Gradle Wrapper 9.6. Do not rely on a globally installed Gradle.

## 2. Clone and open

```powershell
git clone https://github.com/nahilist/health-tracking-app.git
cd health-tracking-app
git switch main
git pull --ff-only origin main
```

Open the root directory in Android Studio and allow Gradle sync. `local.properties` is machine-specific and must not be committed.

## 3. Build commands

Windows PowerShell:

```powershell
.\gradlew.bat assembleDebug
.\gradlew.bat testDebugUnitTest
.\gradlew.bat lintDebug
.\gradlew.bat bundleRelease
```

macOS/Linux:

```bash
./gradlew assembleDebug
./gradlew testDebugUnitTest
./gradlew lintDebug
./gradlew bundleRelease
```

Debug APK:

```text
app/build/outputs/apk/debug/app-debug.apk
```

Release bundle:

```text
app/build/outputs/bundle/release/app-release.aab
```

An unsigned local release bundle is suitable only for build verification, not distribution.

### Code Quality and Coverage (SonarQube & JaCoCo)

Run unit tests and generate JaCoCo coverage reports:

```powershell
.\gradlew.bat testDebugUnitTest jacocoTestReport
```

HTML report output: `app/build/reports/jacoco/jacocoTestReport/html/index.html`  
XML report output: `app/build/reports/jacoco/jacocoTestReport/jacocoTestReport.xml`

Run full SonarQube quality analysis (tests, coverage, lint, and Sonar scanner):

```powershell
.\gradlew.bat sonar "-Dsonar.token=<your-token>"
```

Or provide `SONAR_TOKEN` and `SONAR_HOST_URL` via environment variables.

## 4. Install on a device

Preferred during development: connect an Android device with USB debugging and run the `app` configuration from Android Studio.

CLI install/update:

```powershell
adb install -r app\build\outputs\apk\debug\app-debug.apk
```

If the existing app was signed with a different debug key, Android rejects the update. Uninstalling fixes the signature mismatch but deletes local app data.

## 5. Health Connect testing

- Android 14+ includes Health Connect as a system component.
- Older supported Android versions require the Health Connect application.
- Test available, unavailable, denied, partially granted and revoked states.
- Use synthetic/test records; never commit a real user’s exported health data.
- Verify manifest permissions match runtime permission requests.

## 6. Database workflow

Current schema is exported under `app/schemas/`.

For schema changes:

1. Create a feature branch.
2. Update entities/DAOs.
3. Increment database version.
4. Add and register a migration.
5. Build to generate the schema.
6. Commit the schema JSON.
7. Add migration tests.

Never use destructive migration as a shortcut.

## 7. Testing strategy

### Unit tests

- Validation and time-boundary logic
- Reducers/state transformations
- Repository behavior with fakes
- Conflict and sync policies

### Robolectric tests

- Room DAO and repository behavior
- Migrations where device instrumentation is unnecessary

### Instrumented/Compose tests

- Permission and ActivityResult integration
- Navigation and critical journeys
- Accessibility semantics
- Real Room integration on supported API levels

### Manual release matrix

- Minimum API 26 device
- Latest Android/API 37 device
- Phone and larger/adaptive display
- Light/dark mode
- Font scale 100% and 200%
- RTL locale
- Offline mode
- Health Connect missing/denied/granted/revoked

## 8. Branch and pull-request process

Start from updated main:

```powershell
git switch main
git pull --ff-only origin main
git switch -c feature/example
```

Before push:

```powershell
.\gradlew.bat testDebugUnitTest lintDebug assembleDebug
git diff --check
```

PR checklist:

- Clear scope and linked requirement
- No unrelated changes
- UI screenshots when applicable
- Tests and commands listed
- Room migration impact stated
- Security/privacy impact stated
- Rollback or compatibility note
- Documentation updated

Merge only after required CI checks pass.

## 9. GitHub Actions

### Android CI

Triggers on pull requests to `main`, pushes to `main` and manual dispatch. It runs unit tests, lint and debug APK assembly, then uploads APK and reports.

### Android Release

Triggers on semantic tags such as `v1.0.0` or manually. It uses the `production` GitHub environment and requires:

- `ANDROID_KEYSTORE_BASE64`
- `ANDROID_STORE_PASSWORD`
- `ANDROID_KEY_ALIAS`
- `ANDROID_KEY_PASSWORD`

After configuration and approval:

```powershell
git switch main
git pull --ff-only origin main
git tag v1.0.0
git push origin v1.0.0
```

Do not create release tags until version code/name, changelog and production checks are ready.

## 10. Coding conventions

- Kotlin formatting follows official style.
- Composable functions use PascalCase; event callbacks use `on<Event>`.
- Prefer immutable models and explicit nullable/unknown state.
- Use stable internal metric IDs, not localized display strings.
- Keep business rules out of composables and DAOs out of ViewModels.
- Inject clocks/dispatchers into time- or concurrency-sensitive code.
- Add comments for decisions, not obvious syntax.

## 11. Troubleshooting

### Dubious Git ownership

On a trusted local checkout only:

```powershell
git config --global --add safe.directory C:/health-track
```

### SDK location not found

Install the required Android SDK through Android Studio or set a local `sdk.dir` in `local.properties`.

### App not installed/signature mismatch

Build with the same debug keystore as the installed copy, or uninstall the old debug app knowing that local data will be removed.

### Health Connect shows demo values

Confirm availability, permissions and actual records. The current app intentionally falls back to demo metrics for several cards; see the roadmap to remove this ambiguity.
