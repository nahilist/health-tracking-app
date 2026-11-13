# 0002: Supabase as Authentication and Backend Provider for Hybrid Sync

- Status: accepted
- Date: 2026-11-14
- Owners: Architecture / Core Engineering

## Context

Pulse Health is a local-first Android application currently persisting health metrics and user preferences in Room (SQLite) and Jetpack DataStore. To satisfy Phase 2 of the production roadmap, the app requires:
1. User identity management (Sign-Up, Sign-In, Session Refresh, Sign-Out, and anonymous/guest mode).
2. A cloud persistence backend to enable multi-device sync and backup for authenticated users without breaking offline functionality.
3. Strict row-level authorization and health data privacy guarantees in accordance with `docs/SECURITY_PRIVACY.md`.

We need to choose a backend and authentication provider that integrates cleanly with our existing architecture, preserves local-first behavior, and minimizes operational complexity.

## Decision

We adopt **Supabase** (PostgreSQL + Gotrue Auth) as our managed identity and backend provider:
1. **Authentication Layer**:
   - Integrate `AuthRepository` contract behind `AppContainer`.
   - Support Email/Password authentication, OAuth (Google), and a zero-friction "Continue as Guest (Local-Only)" mode.
   - Encrypt session refresh tokens and authentication state locally using Android Keystore-backed storage (`EncryptedSharedPreferences` / Jetpack Security).
2. **Relational Backend Alignment**:
   - Supabase is powered by PostgreSQL. Because Pulse Health uses Room (SQLite) locally, schemas, foreign keys, timestamps, and aggregate queries (`SUM`, `AVG`, date truncations) translate 1:1 between client and server.
3. **Data Protection & Authorization**:
   - Enforce Row-Level Security (RLS) on all user-scoped tables with policies requiring `auth.uid() = user_id`.
   - Derive user ownership strictly on the server from validated JWT tokens; client payloads cannot specify or override user IDs.
4. **Offline-First Hybrid Sync (Planned)**:
   - UI continues to treat local Room as the Single Source of Truth (SSOT).
   - An outbox sync queue backed by Android `WorkManager` synchronizes mutations when connectivity is available.

## Alternatives considered

1. **Firebase (Auth + Cloud Firestore)**:
   - *Rejected for Database*: Cloud Firestore is a NoSQL document database. Mapping relational Room tables and performing time-window aggregations across steps, sleep, and hydration requires cumbersome document modeling and expensive read operations compared to relational SQL.
   - *Considered for Auth only*: Adding Firebase Auth while maintaining an external PostgreSQL backend would introduce dual vendor dependencies and JWT validation bridges.
2. **Custom Self-Hosted REST/gRPC API**:
   - *Rejected for initial phase*: High initial engineering overhead requiring dedicated infrastructure, secret management, server maintenance, authentication token lifecycle endpoints, and database ops for a small engineering team.

## Consequences

- **Positive**:
  - Relational parity between Room (SQLite) and PostgreSQL avoids object-relational impedance mismatch.
  - PostgreSQL Row-Level Security directly satisfies least-privilege requirements for health data.
  - Open-source ecosystem enables local Docker emulation and prevents hard vendor lock-in.
  - Native Kotlin support via official Supabase Kotlin client libraries (`supabase-kt`).
- **Negative**:
  - Requires managing `SUPABASE_URL` and `SUPABASE_ANON_KEY` configuration via build configs or secure runtime properties.
  - Network error handling and offline queueing logic must be maintained in the Android application.

## Security and privacy impact

- Sensitive access and refresh tokens are encrypted at rest using Android Keystore.
- No health data is synchronized until the user explicitly signs in and grants synchronization consent.
- Signing out clears all session credentials and unsynced outbox queue entries.
- Server-side RLS guarantees that no user can read or mutate records belonging to another identity.

## Migration and rollback

The architecture keeps Supabase encapsulated behind `AuthRepository` and future `SyncRepository` interfaces. The presentation layer and Room local database remain completely provider-agnostic. If a migration to another provider or custom API is required in the future, only the repository implementations need replacement; UI components and local persistence remain unaffected.
