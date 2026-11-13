# Data Model

## 1. Principles

- Room is the Android source of truth for durable application records.
- DataStore is only for small preference values, not health-record history.
- IDs are client-generated UUID strings to support offline creation and idempotent sync.
- Store instants as epoch milliseconds and calendar summaries as ISO/local epoch-day values.
- Keep measurement units explicit in names or unit fields.
- Never use destructive migrations in production.

## 2. Current Room database

- Database file: `pulse_health.db`
- Class: `PulseDatabase`
- Current version: `1`
- Exported schema: `app/schemas/com.pulsehealth.app.data.local.PulseDatabase/1.json`

### `hydration_entries`

| Column | Type | Constraints | Meaning |
|---|---|---|---|
| `id` | TEXT | Primary key | Client-generated UUID |
| `amountMilliliters` | INTEGER | App validates 1–5000 | Water amount |
| `recordedAtEpochMillis` | INTEGER | Indexed | Event time |
| `source` | TEXT | Current value `manual` | Origin of record |
| `syncState` | TEXT | Current value `pending` | Future cloud-sync state |

Current operations:

- Upsert by ID
- Delete entity
- Observe total for a half-open time interval
- Observe entries for a half-open time interval
- Delete entries older than a cutoff

### `daily_health_summaries`

| Column | Type | Constraints | Meaning |
|---|---|---|---|
| `epochDay` | INTEGER | Primary key | Calendar day |
| `steps` | INTEGER | Required | Daily steps |
| `restingHeartRate` | INTEGER | Nullable | Resting BPM when known |
| `sleepMinutes` | INTEGER | Required | Sleep duration |
| `activeCalories` | INTEGER | Required | Active kcal |
| `exerciseMinutes` | INTEGER | Required | Exercise duration |
| `standHours` | INTEGER | Required | Stand-hour count |
| `source` | TEXT | Required | Summary origin |
| `updatedAtEpochMillis` | INTEGER | Required | Freshness timestamp |

This table is currently not wired into the dashboard. Treat it as a cache target for a future Health Connect normalization job, not as complete functionality.

## 3. Current DataStore

Store name: `pulse_user_preferences`

| Key | Type | Meaning |
|---|---|---|
| `hidden_cards` | Set of strings | Summary cards hidden by the user |

Avoid using display labels as long-term identifiers. Migrate to stable enum/string IDs such as `steps`, `heart_rate`, `sleep` and `hydration` before localization.

## 4. Planned local schema

The exact schema requires migrations and may evolve through ADRs.

### `user_profile`

- local profile ID
- optional remote user ID
- display name
- timezone
- unit system
- created/updated timestamps

### `health_records`

Normalized records when raw detail is required:

- ID and type
- start/end/event timestamps
- numeric value and unit
- source package/device
- Health Connect record ID where available
- client/server revision metadata

Do not duplicate sensitive raw records unless the feature needs offline history. Prefer daily summaries when sufficient.

### `sync_outbox`

- operation ID
- entity type and entity ID
- operation type (`upsert` or `delete`)
- payload version
- attempt count
- next-attempt timestamp
- last error category
- created timestamp

### Additional entities

- goals
- notification preferences
- consent receipts
- sync cursor/checkpoint
- account-scoped app settings

## 5. Planned backend model

Recommended relational boundaries:

```text
users
user_profiles
devices
hydration_entries
daily_health_summaries
goals
notification_preferences
consent_receipts
sync_revisions
account_deletion_requests
```

Every user-owned table must include an authenticated owner ID and enforce row-level authorization. Public IDs must be unguessable. Do not store access tokens in application tables.

## 6. Sync metadata

Target record envelope:

```json
{
  "id": "uuid",
  "recordedAt": "2026-11-13T08:00:00Z",
  "updatedAt": "2026-11-13T08:00:05Z",
  "deletedAt": null,
  "clientId": "installation-uuid",
  "clientRevision": 3,
  "serverRevision": 42
}
```

Use versioned payloads. Servers must ignore unknown optional fields and reject incompatible required versions with a documented error.

## 7. Migration policy

For every Room change:

1. Update entities and DAOs.
2. Increment `PulseDatabase.version`.
3. Add an explicit `Migration(old, new)`.
4. Register the migration in the database builder.
5. Generate and commit the new schema JSON.
6. Add a migration test from every supported previous version.
7. Verify values, indices and constraints after migration.

Prohibited:

- `fallbackToDestructiveMigration()` in production
- dropping data without a product-approved retention/deletion requirement
- changing unit meaning without a migration
- storing secrets or authentication tokens in Room

## 8. Retention and deletion

- Manual local records remain until the user deletes them or a documented retention policy applies.
- Cloud-account deletion must tombstone/schedule server deletion and provide status.
- Local “clear data” must remove Room, DataStore, secure session data and scheduled work.
- Logs and analytics must not contain raw health values.
