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

### PR 4: hydration history

- List, edit and delete entries.
- Add daily/weekly totals and goal editing.
- Add DAO paging/bounded queries and tests.

### PR 5: complete Health Connect integration

- Aggregate all displayed period ranges correctly.
- Read exercise sessions and source metadata.
- Optionally write manual hydration after explicit permission.
- Handle revoked and update-required states.

### PR 6: onboarding and privacy controls

- Local-only onboarding.
- Permission rationale.
- Local data export/delete.
- Privacy/about screen.

Phase gate: a user can install the app, understand data origin, use it fully offline and delete local data.

## 4. Phase 2: authentication and backend

### ADR: provider and hosting selection

Compare managed Firebase/Supabase options with a custom API based on authorization, relational needs, data residency, cost, lock-in and operations. Do not implement multiple providers speculatively.

### Backend foundation PRs

- Infrastructure environments and secret management
- Database migrations and row-level authorization
- API error/idempotency conventions
- Health checks, logging and rate limiting

### Android authentication PRs

- Sign-in/sign-up UI and local-only choice
- Provider integration behind `AuthRepository`
- Secure session storage and refresh
- Sign-out/session-expiry behavior

### Sync PRs

- Local outbox and schema migration
- Idempotent hydration sync
- Pull cursor and reconciliation
- WorkManager scheduling/backoff
- Multi-device conflict tests

Phase gate: authenticated users can recover data on a second device without duplicating or losing offline records.

## 5. Phase 3: engagement

- Local reminders and quiet hours
- Goals and progress trends
- Complete Browse and metric history
- Trusted sharing design and authorization review
- Optional privacy-reviewed crash/analytics tooling

Phase gate: reminders are opt-in, sharing is explicitly authorized, and telemetry matches published disclosures.

## 6. Phase 4: production launch

- Final brand assets and screenshots
- Accessibility audit
- Performance/baseline profile work
- Threat-model and dependency review
- Privacy policy, terms and support contact
- Health Apps/Health Connect Play declarations
- Play App Signing and production environment protection
- Staged internal/closed/open testing tracks
- Monitoring, incident response and rollback runbook
- Versioning, changelog and support matrix

## 7. Known technical debt

| Item | Risk | Priority |
|---|---|---|
| Demo and real values coexist | Resolved in PR 1 | Resolved |
| Monolithic `PulseHealthApp.kt` | Resolved in PR 2 | Resolved |
| Manual dependency construction | Resolved in PR 2 | Resolved |
| Display labels used as favorite IDs | Breaks localization/migrations | High |
| No Room migration beyond v1 | Future update risk | High before schema change |
| No secure session layer | Blocks authentication | High for Phase 2 |
| Placeholder Sharing/Browse | Incomplete product flows | Medium |
| No instrumented UI suite | Regression risk | Medium |

## 8. Recommended next PR

Implement “hydration history” (PR 4). Add hydration history entry listing, editing and deletion, daily and weekly aggregation, user goal customization, and bounded DAO queries with unit tests.
