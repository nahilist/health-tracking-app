# Coding Agent Guide

This file is the entry point for any coding agent working in this repository. Read it completely before changing code, then read the task-specific documents linked below.

## Project identity

- Product: Pulse Health
- Repository: `nahilist/health-tracking-app`
- Platform: Android only
- Package: `com.pulsehealth.app`; debug package: `com.pulsehealth.app.debug`
- Minimum Android: API 26
- Target/compile Android: API 37
- Primary language/UI: Kotlin and Jetpack Compose
- Default branch: `main`

## Source of truth

Read documents in this order:

1. [Product requirements](docs/PRD.md)
2. [Architecture](docs/ARCHITECTURE.md)
3. [Data model](docs/DATA_MODEL.md)
4. [Security and privacy](docs/SECURITY_PRIVACY.md)
5. [Development workflow](docs/DEVELOPMENT.md)
6. [Backend API contract](docs/API_CONTRACT.md) when working on auth or sync
7. [Roadmap](docs/ROADMAP.md) when selecting the next feature

## Reality check

Do not describe the app as fully production-ready. The repository currently contains a functional local-first MVP foundation.

Implemented:

- Compose dashboard, activity rings, metric cards and detail placeholders
- Summary, Sharing and Browse top-level destinations
- Today/week/month selection
- Health Connect availability, permission request and read aggregation
- Room database version 1 with hydration and daily-summary entities
- DataStore-backed hidden favorites
- Local hydration entry creation
- Unit/Robolectric tests, lint, debug APK and release bundle builds
- GitHub Actions CI and tag-based signed release workflow

Partial or placeholder:

- Sharing screen, Browse details and metric charts
- Health Connect history and hydration write-back
- Error, empty and onboarding experiences
- Accessibility and adaptive-layout coverage

Not implemented:

- Authentication, accounts or sessions
- Backend, cloud database or sync
- Account deletion/export
- Push notifications or reminders
- Food, medicine, workout or symptom logging
- Admin tools, subscriptions or payments
- Analytics, crash reporting or remote configuration
- Play Console declarations, privacy-policy hosting or production signing secrets

## Engineering rules

- Never commit secrets, keystores, `local.properties`, tokens or health data.
- Health data is sensitive. Collect the minimum required fields and require explicit consent.
- Do not add medical diagnosis, treatment advice or unsupported health claims.
- Keep UI state immutable and expose asynchronous state with `StateFlow`.
- Keep database access behind repositories; composables must never call DAOs directly.
- Every Room schema change must increment the database version, add a migration and commit the exported schema.
- Prefer stable AndroidX releases. Document any alpha/beta dependency and why it is necessary.
- Preserve offline behavior. Network failure must not corrupt or hide locally saved records.
- Use locale-aware formatting and accessible content descriptions.
- Do not silently discard user data or use destructive Room migrations.

## Branch and PR workflow

One feature per branch and pull request:

- `feature/<short-name>` for product work
- `fix/<short-name>` for bug fixes
- `docs/<short-name>` for documentation
- `chore/<short-name>` for tooling

Before commit, run:

```powershell
.\gradlew.bat testDebugUnitTest lintDebug assembleDebug
```

For release-sensitive changes, also run:

```powershell
.\gradlew.bat bundleRelease
```

PR descriptions must state scope, screenshots for UI changes, tests run, migration impact, security/privacy impact and rollback considerations.

## Definition of done

A feature is done only when:

- Requirements and acceptance criteria are satisfied.
- Loading, empty, success and failure states are handled where applicable.
- Tests cover core business rules and persistence changes.
- Lint and the debug build pass.
- Documentation is updated when behavior, schema, architecture or setup changes.
- No secret or personal health information is present in the diff or test fixtures.
