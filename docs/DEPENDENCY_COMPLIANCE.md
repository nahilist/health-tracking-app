# Dependency License and Compliance Audit

This document summarizes the third-party dependencies used in **Pulse Health** (`com.pulsehealth.app`), their respective open-source licenses, and our vulnerability management posture.

---

## 1. Direct Production Dependencies

| Group & Artifact | Version | License | Compatibility & Usage |
|---|---|---|---|
| `androidx.core:core-ktx` | 1.17.0 | Apache 2.0 | Android Jetpack core extensions |
| `androidx.activity:activity-compose` | 1.13.0 | Apache 2.0 | Activity host for Jetpack Compose |
| `androidx.lifecycle:*` | 2.11.0 | Apache 2.0 | Compose runtime lifecycle & ViewModel integration |
| `androidx.navigation:navigation-compose` | 2.10.2 | Apache 2.0 | Typed navigation graph |
| `androidx.compose.ui:*` | BOM 2026.09.00 | Apache 2.0 | Declarative UI framework |
| `androidx.compose.material3:material3` | BOM 2026.09.00 | Apache 2.0 | Material Design 3 design system |
| `androidx.compose.material:material-icons-extended` | BOM 2026.09.00 | Apache 2.0 | Iconography |
| `androidx.room:room-runtime` & `room-ktx` | 2.8.5 | Apache 2.0 | Embedded SQLite ORM |
| `androidx.datastore:datastore-preferences` | 1.2.0 | Apache 2.0 | Key-value reactive preferences storage |
| `androidx.security:security-crypto` | 1.0.0 | Apache 2.0 | Android Keystore encryption (`EncryptedSharedPreferences`) |
| `androidx.work:work-runtime-ktx` | 2.10.0 | Apache 2.0 | Deferrable background work (sync & reminders) |
| `androidx.health.connect:connect-client` | 1.1.0 | Apache 2.0 | Official Android Health Connect API client |
| `io.github.jan-tennert.supabase:*` | BOM 3.1.1 | Apache 2.0 | Supabase Kotlin SDK (Auth & Postgrest) |
| `io.ktor:ktor-client-android` | 3.0.3 | Apache 2.0 | HTTP engine for Supabase client |
| `org.jetbrains.kotlinx:kotlinx-coroutines-android` | 1.10.2 | Apache 2.0 | Asynchronous flow & coroutine engine |
| `org.jetbrains.kotlinx:kotlinx-serialization-json` | 1.7.3 | Apache 2.0 | High-performance JSON serialization |

---

## 2. Testing Dependencies

| Group & Artifact | Version | License | Usage |
|---|---|---|---|
| `junit:junit` | 4.13.2 | EPL 1.0 | Unit test runner |
| `org.jetbrains.kotlinx:kotlinx-coroutines-test` | 1.10.2 | Apache 2.0 | Coroutine test dispatchers (`runTest`) |
| `androidx.room:room-testing` | 2.8.5 | Apache 2.0 | In-memory Room database testing |
| `androidx.work:work-testing` | 2.10.0 | Apache 2.0 | TestWorkManager driver |
| `org.robolectric:robolectric` | 4.16.1 | MIT | Shadow Android runtime for unit tests |
| `androidx.test:core-ktx` | 1.7.0 | Apache 2.0 | AndroidX test core utilities |

---

## 3. Compliance Assessment

- **Permissive Licensing**: 100% of production runtime dependencies are distributed under the **Apache License, Version 2.0**.
- **No Copyleft Contamination**: The codebase contains zero GPL, AGPL, or restrictive copyleft dependencies.
- **Commercial & Store Distribution**: Fully compatible with distribution on the Google Play Store and open-source distribution on GitHub.
- **Notice Requirements**: The Apache 2.0 license allows modification and distribution provided that copyright notices and disclaimers are preserved.

---

## 4. Vulnerability Management

- **GitHub Dependabot**: Configured to alert on known CVEs in Gradle dependencies.
- **Automated CI Security**: Code scanning and static analysis run on every pull request via GitHub Actions.
- **Review Cadence**: Dependencies are reviewed prior to every minor and major release milestone.
