# Incident Response and Rollback Runbook

This document defines the operational procedures, severity classifications, and response protocols for **Pulse Health** (`com.pulsehealth.app`).

---

## 1. Incident Severity Matrix

| Severity | Definition | Target Response | Target Resolution | Examples |
|---|---|---|---|---|
| **P0 (Critical)** | Broad data loss, security/credential breach, app crash on launch affecting >1% of users, or unauthenticated health data exposure. | **< 30 minutes** | **< 4 hours** | App crashes immediately on cold start; Keystore decryption fails universally; unauthenticated RLS bypass. |
| **P1 (High)** | Core user flow blocked without workaround, repeated Health Connect sync failure, or persistent cloud sync retry loop draining battery. | **< 2 hours** | **< 24 hours** | Hydration cannot be logged; Health Connect permissions revoked causes infinite loop; cloud sync fails for all users. |
| **P2 (Medium)** | Non-blocking feature degraded with available workaround, minor UI rendering bug, or isolated sync timeout. | **< 8 hours** | **< 3 business days** | Streak calculation off by 1 day; export report text formatting glitch; dark mode contrast issue. |
| **P3 (Low)** | Cosmetic imperfection, localization typo, or non-impacting log warning. | **Next sprint** | **Next release cycle** | Minor typo in About description; redundant warning in lint output. |

---

## 2. Release Rollback & Roll-Forward Policy

> **Important**: The Android platform and Google Play Store **do not permit downgrading** an installed app to a lower `versionCode`. Once a release is installed on a user's device, any remediation must be published as a **roll-forward build** with an incremented `versionCode`.

### Staged Rollout Protocol
When publishing updates to Google Play:
1. **Internal Track**: Automated smoke test and developer validation.
2. **Closed Testing**: 48-hour observation window with test group.
3. **Open Production Staged Rollout**:
   - **Day 1**: 10% rollout. Monitor Google Play Console Vitals (Crash rate target: <0.2%, ANR rate target: <0.1%).
   - **Day 2**: 25% rollout.
   - **Day 3**: 50% rollout.
   - **Day 4**: 100% full production rollout.

### Emergency Rollout Halt
If a P0 or P1 incident is detected during a staged rollout:
1. Navigate to **Google Play Console** > **Production** > **Releases**.
2. Click **Halt rollout** immediately. This freezes distribution to new devices while the fix is prepared.
3. Users who have already downloaded the problematic build will receive the roll-forward patch upon release.

---

## 3. Roll-Forward Hotfix Procedure

When a hotfix is required:
1. Branch from the release tag or `main`:
   ```powershell
   git checkout -b hotfix/v1.0.1
   ```
2. Increment `versionCode` and update `versionName` in `app/build.gradle.kts`:
   ```kotlin
   versionCode = 2
   versionName = "1.0.1"
   ```
3. Apply the targeted fix with a minimal diff.
4. Run full local verification:
   ```powershell
   .\gradlew.bat testDebugUnitTest lintDebug assembleDebug bundleRelease
   ```
5. Merge hotfix PR into `main`.
6. Tag the release:
   ```powershell
   git tag v1.0.1
   git push origin v1.0.1
   ```
7. GitHub Actions will build the signed release bundle (`app-release.aab`) and attach it to the GitHub Release.
8. Upload the hotfix AAB to Google Play Console and promote to 100% rollout.

---

## 4. Backend & Cloud Outage Handling

Because Pulse Health is engineered as **local-first**:
- An outage of Supabase, network loss, or backend latency **never prevents local logging or dashboard reading**.
- Room SQLite holds records and queues unsynced entries in the outbox (`syncState: pending`).
- When backend connectivity is restored, WorkManager automatically resumes idempotent synchronization using exponential backoff without duplicating or losing records.

### Manual Backend Recovery Steps:
1. Check Supabase service status: [https://status.supabase.com](https://status.supabase.com).
2. If RLS or API authentication fails:
   - Verify `SUPABASE_URL` and `SUPABASE_KEY` configuration.
   - Verify PostgreSQL database health and connection pooling in Supabase dashboard.
3. The app displays a non-blocking error banner (`ErrorBanner`) on Summary with a manual "Retry" action.

---

## 5. Database Schema Incident Protocol

- **Zero Data Loss Rule**: Never execute destructive schema migrations (`DROP TABLE`, `TRUNCATE`, `fallbackToDestructiveMigration`).
- Every schema change must increment the database version and provide an explicit `Migration(from, to)`.
- If an unhandled migration crash occurs:
  - Users' raw SQLite database file (`pulse_health.db`) remains intact in private app storage.
  - Hotfix must deliver an explicit migration path covering the problematic version transition.
  - The in-app JSON export function (`exportDataJson`) allows emergency data extraction prior to any device reset.

---

## 6. Security Incident & Credential Revocation

If credentials, signing keys, or API tokens are suspected of compromise:
1. **Supabase Secrets**:
   - Rotate the publishable anon key in the Supabase Dashboard.
   - Update repository secrets `SUPABASE_KEY` in GitHub Actions.
   - Trigger hotfix release with updated build configuration.
2. **Session Revocation**:
   - Invalidate existing user JWTs and refresh tokens via Supabase Auth administrative controls.
   - The app's `SupabaseAuthRepository` detects `401 Unauthorized` responses and returns the user to a signed-out state while preserving their offline local records.
3. **Keystore Reset**:
   - If device keystore integrity is compromised, the user can tap "Clear All Local Data" to purge Keystore-encrypted preferences and reset the local encryption container.

---

## 7. Post-Incident Review Template

Following resolution of any P0 or P1 incident, complete this post-mortem within 48 hours:

```markdown
# Post-Incident Review: [Incident Title]
- Date: [YYYY-MM-DD]
- Severity: [P0 / P1]
- Duration: [Start time to resolution]
- Impacted Users: [Estimated % or count]

## Root Cause
[Technical explanation of what triggered the failure]

## Timeline
- [HH:MM] Detection
- [HH:MM] Triage & Rollout halted
- [HH:MM] Hotfix identified
- [HH:MM] Hotfix deployed & verified

## Corrective Actions
1. [Preventive code change / test case addition]
2. [CI/CD validation rule addition]
3. [Documentation / runbook update]
```
