# Production Roadmap

## 1. Delivery policy

Each item below should normally be one focused pull request. Do not begin a dependent phase until its prerequisite is merged and stable. Priorities may change, but security/privacy gates do not.

## 2. Completed foundations

- [x] Native Compose dashboard and visual system
- [x] Health Connect availability/permission/read foundation
- [x] DataStore preferences
- [x] Room version-1 database and exported schema
- [x] Database-backed hydration quick add
- [x] Unit/Robolectric test foundation
- [x] PR CI and signed-tag release workflow
- [x] Product/architecture/agent documentation pack

## 3. Phase 1: honest local MVP

### PR 1: remove demo/real ambiguity (Completed)

- [x] Represent unknown metrics explicitly.
- [x] Label demo mode in debug/previews only.
- [x] Add source and freshness to UI state.
- [x] Acceptance: production build never presents demo values as user records.

### PR 2: repository and dependency boundaries (Completed)

- [x] Introduce repository interfaces and an application container through ADR 0001.
- [x] Inject clock and dispatchers.
- [x] Split the oversized UI file by feature/component.
- [x] Acceptance: ViewModels do not instantiate concrete data stores.

### PR 3: typed navigation and screen state (Completed)

- [x] Add a real navigation graph and typed routes.
- [x] Preserve state across process recreation.
- [x] Add loading/empty/error states.

### PR 4: hydration history (Completed)

- [x] List, edit and delete entries.
- [x] Add daily/weekly totals and goal editing.
- [x] Add DAO paging/bounded queries and tests.

### PR 5: complete Health Connect integration (Completed)

- [x] Aggregate all displayed period ranges correctly.
- [x] Read exercise sessions and source metadata.
- [x] Optionally write manual hydration after explicit permission.
- [x] Handle revoked and update-required states.

### PR 6: onboarding and privacy controls (Completed)

- [x] Local-only onboarding.
- [x] Permission rationale.
- [x] Local data export/delete.
- [x] Privacy/about screen.

Phase gate: a user can install the app, understand data origin, use it fully offline and delete local data.

## 4. Phase 2: authentication and backend

### ADR: provider and hosting selection (Completed)

- [x] Compare managed Firebase/Supabase options with a custom API. (Completed in [ADR 0002](adr/0002-auth-and-backend-provider.md)).

### Android authentication foundation PR (Completed)

- [x] Sign-in/sign-up UI and local-only choice
- [x] Provider integration behind `AuthRepository`
- [x] Secure session storage and refresh
- [x] Sign-out/session-expiry behavior

### Backend & Cloud Sync PRs (Completed)

- [x] Supabase PostgreSQL database schema migrations and row-level authorization (RLS)
- [x] Local outbox sync pattern (`syncState: pending` / `synced`)
- [x] Idempotent hydration two-way synchronization
- [x] WorkManager scheduling with network constraint and immediate/periodic triggers
- [x] Account & Cloud Sync controls in user profile with live status and manual sync trigger

Phase gate: authenticated users can recover data on a second device without duplicating or losing offline records.

## 5. Phase 3: engagement

### PR 12: local reminders and quiet hours (Completed)

- [x] Opt-in local hydration reminders with configurable intervals (1h, 2h, 3h, 4h)
- [x] Automatic quiet hours window silencing notifications overnight (e.g. 22:00 – 08:00)
- [x] Android Notification Channel (`pulse_hydration_reminders`) and `POST_NOTIFICATIONS` permission flow
- [x] Interactive "+250 ml" quick-add notification action via `HydrationActionReceiver`
- [x] WorkManager `ReminderWorker` and `WorkManagerReminderScheduler`

### PR 13: goals, progress trends and achievements (Completed)

- [x] Goals and progress trends calculation
- [x] Consecutive days goal streak calculations and all-time best streaks
- [x] Dynamic 7-day weekly hydration bar visualization on Dashboard
- [x] Milestone celebration banner on reaching daily hydration goal

### PR 14: complete Browse and metric history (Completed)

- [x] Complete Browse screen with categorized navigation (Activity, Vitals, Sleep, Nutrition & Hydration)
- [x] Real-time metric search filtering with empty-state handling
- [x] Full metric detail history views with TimeRange switching (Today, Week, Month)
- [x] Interactive trend visualization (`MetricTrendChart`) across Steps, Heart Rate, Sleep, Water, and Activity
- [x] Metric summary statistics (averages, totals, highs, lows, completion status)
- [x] Non-diagnostic wellness descriptions and data source attribution

### PR 15: trusted sharing design and privacy export review (Completed)

- [x] Trusted sharing invitation and recipient authorization review
- [x] Granular metric authorization toggles and expiration windows (7 days, 30 days, until revoked)
- [x] Export health package sanitization (`ExportedHealthPackage`) omitting unselected metrics
- [x] Android system share sheet integration (`Intent.ACTION_SEND`) and report preview
- [x] Full `SharingScreen` replacing placeholder UI with active recipient cards, status badges, and revocation controls

Phase gate: reminders are opt-in, sharing is explicitly authorized, and telemetry matches published disclosures.

## 6. Phase 4: production launch

### PR 16: accessibility audit, release bundle validation and Play store declarations (Completed)

- [x] Complete TalkBack accessibility labeling and semantic roles (`Role.Button`, `Role.Tab`) across dashboard cards, navigation bars, and dialogs
- [x] Adaptive layout height auditing and 200% dynamic font scaling support (replacing fixed height bounds with `defaultMinSize`)
- [x] Full minimum 48dp touch target compliance on all interactive buttons and icons
- [x] Production Privacy Policy documentation (`docs/PRIVACY_POLICY.md`)
- [x] Google Play Console Health Apps & Health Connect declarations (`docs/PLAY_STORE_DECLARATIONS.md`)
- [x] Production R8 minification and `bundleRelease` signed bundle verification

### PR 17: release tagging, changelog and production monitoring runbook (Completed)

- [x] Production versioning and release notes (`docs/CHANGELOG.md`)
- [x] Threat model and dependency license compliance review (`docs/DEPENDENCY_COMPLIANCE.md`)
- [x] Rollback and incident response runbook verification (`docs/INCIDENT_RESPONSE_RUNBOOK.md`)
- [x] Play App Signing and staged rollout track plan

## 7. Known technical debt

| Item | Risk | Priority |
|---|---|---|
| Demo and real values coexist | Resolved in PR 1 | Resolved |
| Monolithic `PulseHealthApp.kt` | Resolved in PR 2 | Resolved |
| Manual dependency construction | Resolved in PR 2 | Resolved |
| Display labels used as favorite IDs | Breaks localization/migrations | High |
| No Room migration beyond v1 | Future update risk | High before schema change |
| No secure session layer | Blocks authentication | Resolved in PR 10 |
| Placeholder Sharing/Browse | Incomplete product flows | Resolved in PR 14 & 15 |
| No instrumented UI suite | Regression risk | Medium |

## 8. Recommended next step

Phase 4 roadmap milestones completed! The codebase is production-ready for tagging `v1.0.0` and distribution via Google Play Store tracks.
