# Product Requirements Document: Pulse Health

## 1. Document control

- Status: living draft
- Product stage: local-first MVP foundation
- Audience: product, design, Android, backend, QA, security and coding agents
- Last verified against code: 2026-11-13

## 2. Product summary

Pulse Health is an Android health dashboard that helps users understand daily activity, heart rate, sleep and hydration in one calm, privacy-conscious interface. It combines data the user explicitly authorizes through Android Health Connect with manual entries stored locally. The long-term product adds authenticated multi-device backup and synchronization without making diagnostic or treatment claims.

Pulse Health is a wellness product, not a medical device. It must not diagnose conditions, recommend medication changes or replace professional medical advice.

## 3. Problem statement

Health information is fragmented across device sensors, fitness applications and manual habits. Users need a fast daily summary that:

- makes progress understandable without overwhelming detail;
- keeps manual tracking, especially hydration, quick;
- works when offline;
- clearly explains where data came from;
- gives users control over permissions, storage and deletion.

## 4. Product principles

1. **Private by default**: no cloud upload until a user signs in and explicitly enables backup.
2. **Local first**: core logging and reading continue without network access.
3. **Honest data**: distinguish demo, manual, Health Connect and cloud-synced values.
4. **Fast daily use**: the dashboard should become useful within seconds.
5. **Accessible and adaptive**: support font scaling, screen readers, dark mode and varied Android form factors.
6. **No medical overreach**: show measurements and trends, not diagnoses.

## 5. Target users

### Persona A: daily wellness tracker

Wants a quick view of steps, sleep, resting heart rate and water. Values visual progress and reminders but does not want complex fitness coaching.

### Persona B: fitness-focused user

Uses multiple wearable or fitness apps and wants Health Connect data summarized consistently across day, week and month.

### Persona C: privacy-conscious user

Wants full local functionality, clear permissions and the ability to avoid accounts or cloud storage entirely.

## 6. Success measures

Metrics require a future privacy-reviewed analytics implementation. Until then, do not add tracking SDKs merely to measure these targets.

- Dashboard usable after first launch in under 60 seconds.
- Health Connect permission completion rate above 65% among users who start connection.
- Manual hydration entry completes in at most two taps from Summary.
- Crash-free sessions at or above 99.5% after production monitoring exists.
- At least 70% of active users return in week two.
- Account deletion/export requests complete successfully at 99.9% when cloud accounts launch.

## 7. Functional scope and status

| Area | Requirement | Status |
|---|---|---|
| Summary | Show daily activity, steps, resting heart rate, sleep and hydration | Implemented with demo fallback |
| Time range | Switch Today, Week and Month | Partial; selector works, not all cards aggregate by range |
| Activity | Show move, exercise and stand progress | Partial; UI exists, some values remain demo data |
| Favorites | Add/remove metric cards | Implemented locally |
| Hydration | Add a 250 mL manual entry and persist it | Implemented locally in Room |
| Health Connect | Check availability, request permissions and read steps/calories/heart/sleep | Implemented foundation |
| Health Connect | Read exercises and write hydration | Planned |
| Detail screens | Show historical charts, goals and source attribution | Partial placeholders |
| Sharing | Share selected metrics with trusted people | Planned; placeholder screen only |
| Browse | Browse health categories and records | Partial |
| Onboarding | Explain privacy, permissions, goals and optional account | Planned |
| Authentication | Email/Google sign-in, session recovery and sign-out | Planned |
| Cloud sync | Encrypted transport, offline queue and multi-device reconciliation | Planned |
| Reminders | Local hydration/sleep/activity reminders | Planned |
| Export/delete | Export local/cloud data and delete account | Planned |
| Observability | Crash reporting and privacy-safe operational metrics | Planned |

## 8. Detailed requirements

### 8.1 Onboarding

- Explain that Health Connect access is optional.
- Explain which record types are requested and why before launching system permissions.
- Allow “Continue locally” without creating an account.
- Allow goals to be accepted as defaults or edited.
- Never block the app because Health Connect is absent.

Acceptance criteria:

- A user can reach Summary without account or Health Connect permission.
- Permission denial produces an actionable, non-alarming state.
- Consent copy is versioned when cloud processing is introduced.

### 8.2 Dashboard

- Display the selected period and data freshness.
- Display source attribution when a metric is opened.
- Show loading skeletons only when existing local data cannot be shown.
- Keep last known values visible during sync failures.
- Provide empty states instead of misleading zeros when data is unknown.

Acceptance criteria:

- Today values use the user’s local timezone.
- Week/month boundaries are documented and tested.
- No demo value is presented as real user data.
- Font scale 200% remains usable without clipped primary values.

### 8.3 Hydration

- Default quick-add amount is 250 mL.
- Validate manual entries from 1–5000 mL.
- Store timestamp, source and sync state.
- Support edit/delete and history in a future increment.
- Optionally write to Health Connect only after write permission.

Acceptance criteria:

- Entries survive process death and device restart.
- Totals roll over at local midnight without deleting history.
- Duplicate cloud sync retries do not create duplicate records.

### 8.4 Health Connect

- Request only permissions required by enabled features.
- Re-check grants before every sensitive operation.
- Handle unavailable, update-required, denied and revoked states.
- Record source and last successful refresh.
- Avoid background reads unless the user enabled the related feature and platform policy permits it.

### 8.5 Accounts and synchronization

- Local-only remains a supported mode.
- Initial authentication target: email link/password plus Google sign-in; final provider choice requires an architecture decision record.
- Tokens must use Android Keystore-backed secure storage.
- Cloud sync must be idempotent and offline-first.
- Sign-out must explain which local data remains.
- Account deletion must remove cloud data and offer local-data deletion.

Acceptance criteria:

- Network loss never prevents local hydration logging.
- The same logical record is not duplicated across devices.
- Expired sessions recover or return to signed-out state without data loss.
- Deletion status is visible and retryable.

### 8.6 Notifications

- Notifications are opt-in and configurable by type and quiet hours.
- Use exact alarms only when a product requirement justifies them.
- Reminders must not reveal sensitive values on the lock screen by default.
- Users can disable reminders in-app and through Android settings.

## 9. Non-functional requirements

### Performance

- Cold start target: under 2 seconds on a representative mid-range supported device.
- Dashboard local render target: under 500 ms after database availability.
- No database or Health Connect operation on the main thread.
- Avoid unbounded queries and retain pagination for record history.

### Reliability

- Local writes are transactional.
- Schema migrations preserve user records.
- Sync operations are idempotent and safely retryable.
- WorkManager jobs use constraints and exponential backoff.

### Accessibility

- TalkBack labels for interactive and visual-only metrics.
- Minimum 48 dp touch targets.
- Do not encode meaning only with color.
- Support dark mode, RTL and dynamic font size.

### Security and privacy

- Follow [SECURITY_PRIVACY.md](SECURITY_PRIVACY.md).
- Encrypt transport with TLS and validate server identity.
- Never log tokens, passwords, raw health records or user identifiers.
- Backup stays disabled until a reviewed encrypted backup design exists.

## 10. Out of scope for the initial production release

- Clinical diagnosis or emergency detection
- Medication dosage recommendations
- Insurance or employer reporting
- Public social feeds
- Marketplace for health products
- iOS, web or wearable applications

## 11. Release gates

Before claiming production readiness:

- Critical onboarding, permission, local logging and failure flows are complete.
- Authentication and sync have security review if enabled.
- Privacy policy and in-app disclosures match actual data processing.
- Health Connect and Play Console declarations are approved.
- Release signing and Play App Signing are configured.
- Crash monitoring, rollback procedure and support contact exist.
- Automated tests cover migrations and critical user journeys.
