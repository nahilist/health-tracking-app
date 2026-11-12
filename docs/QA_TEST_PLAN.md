# QA Test Plan

## 1. Test levels

| Level | Purpose | Required cadence |
|---|---|---|
| Unit | Business rules, validation, state reducers | Every PR |
| Robolectric | Room/repository Android behavior | Every relevant PR |
| Instrumented | Framework integration, permissions, navigation | Every relevant PR/nightly when suite grows |
| Manual smoke | Critical user journeys on real device | Before merge for risky UI/platform changes |
| Release regression | Supported-device and privacy/security matrix | Before production tag |

## 2. Automated CI suite

Current required command:

```powershell
.\gradlew.bat testDebugUnitTest lintDebug assembleDebug
```

Release-sensitive command:

```powershell
.\gradlew.bat bundleRelease
```

Future required gates:

- Compose UI tests for onboarding, Summary and hydration
- Room migration tests for every schema version
- Backend contract and authorization tests
- Sync idempotency/conflict tests
- Dependency/security scan
- Baseline profile generation and performance checks

## 3. Critical manual scenarios

### Installation and launch

- Fresh install launches without crash.
- Upgrade preserves Room/DataStore data.
- Process death and relaunch restore meaningful state.
- App works without network.

### Health Connect

- Health Connect unavailable.
- Available but no permissions.
- All requested permissions granted.
- Partial grant.
- Permission revoked while app is backgrounded.
- No records, one record and large record history.
- Records from multiple sources.

### Hydration

- Add default 250 mL.
- Add minimum and maximum allowed values when custom entry exists.
- Reject zero, negative and over-limit values.
- Cross local midnight and verify correct day totals.
- Change timezone and verify documented behavior.
- Restart device/app and verify persistence.

### UI/accessibility

- Light and dark themes.
- 200% font scale.
- TalkBack traversal and action labels.
- RTL locale.
- Small phone and large/adaptive screen.
- Gesture and three-button navigation insets.

### Failure states

- Database operation failure is surfaced safely.
- Health Connect read failure retains previous data.
- Network timeout/retry once backend exists.
- Expired/revoked session once auth exists.
- Insufficient storage during write/build update.

## 4. Release device matrix

Minimum matrix:

| Dimension | Coverage |
|---|---|
| Android version | API 26, a representative middle version, latest target |
| Hardware | Mid-range physical phone and emulator/reference device |
| Screen | Compact and expanded width |
| Theme | Light and dark |
| Locale | English, Hindi and one RTL locale |
| Font | Default and 200% |
| Health Connect | Missing/denied/granted/revoked |
| Connectivity | Online, offline and unstable |

## 5. Bug severity

- **S0 blocker**: data loss, security/privacy exposure, cannot launch/install, incorrect signing.
- **S1 critical**: primary flow unavailable, misleading health data, repeatable crash.
- **S2 major**: feature substantially impaired with workaround.
- **S3 minor**: visual/copy issue with no material functional impact.

No open S0/S1 issue is allowed in a production release.

## 6. Test-data policy

- Use synthetic records and dedicated test accounts.
- Never paste real health exports into issues, fixtures or screenshots.
- Redact email, tokens, device identifiers and Health Connect source IDs.
- Test cleanup/deletion for every environment.
