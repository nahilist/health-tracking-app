# Backend API Contract (Planned)

## 1. Status

No backend or API client is implemented. This document is a proposed contract that keeps Android and backend work aligned. Provider and hosting choices require an accepted architecture decision record before implementation.

## 2. Protocol conventions

- Base path: `/v1`
- Transport: HTTPS only
- Content type: `application/json`
- Authentication: `Authorization: Bearer <access-token>`
- Timestamps: UTC RFC 3339 strings
- IDs: UUID strings
- Mutation idempotency: `Idempotency-Key` header containing a client operation UUID
- Pagination: opaque cursor with `limit` capped server-side
- API version changes must remain backward compatible within `/v1`

Do not put tokens or health values in URLs, query logs or analytics.

## 3. Standard error

```json
{
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "The request could not be processed.",
    "requestId": "req_uuid",
    "fieldErrors": {
      "amountMilliliters": "Must be between 1 and 5000."
    }
  }
}
```

Stable error codes:

- `UNAUTHENTICATED`
- `FORBIDDEN`
- `VALIDATION_ERROR`
- `NOT_FOUND`
- `CONFLICT`
- `RATE_LIMITED`
- `SYNC_CURSOR_EXPIRED`
- `INTERNAL_ERROR`

Clients display localized friendly messages and retain the code/request ID for diagnostics.

## 4. Session endpoints

If a managed identity provider supplies tokens directly, these endpoints may be replaced by the provider flow. The app still requires a backend session/exchange contract.

### `POST /v1/auth/session`

Exchange a provider credential for an application session.

Request:

```json
{
  "provider": "google",
  "providerToken": "opaque",
  "installationId": "uuid"
}
```

Response:

```json
{
  "accessToken": "opaque",
  "expiresInSeconds": 900,
  "refreshToken": "opaque",
  "user": {
    "id": "uuid",
    "displayName": "Manish"
  }
}
```

### `POST /v1/auth/refresh`

Rotates a refresh token. Reuse of an already-rotated token should revoke the token family.

### `DELETE /v1/auth/session`

Revokes the current session/device refresh credential.

## 5. Profile endpoints

### `GET /v1/me`

Returns the authenticated profile, settings revision and deletion state.

### `PATCH /v1/me`

Updates allowlisted fields such as display name, timezone and unit system. The server ignores or rejects ownership/security fields.

### `POST /v1/me/deletion-requests`

Starts account deletion and returns a request ID/status. A separate cancellation policy may apply during a documented grace period.

### `GET /v1/me/export`

Returns export status or a short-lived authenticated download reference. Never place a long-lived public export URL in logs.

## 6. Hydration endpoints

### `PUT /v1/hydration/{id}`

Idempotent upsert using a client-generated UUID.

```json
{
  "amountMilliliters": 250,
  "recordedAt": "2026-11-13T08:00:00Z",
  "updatedAt": "2026-11-13T08:00:05Z",
  "clientRevision": 1,
  "source": "manual"
}
```

Response:

```json
{
  "id": "uuid",
  "serverRevision": 104,
  "serverUpdatedAt": "2026-11-13T08:00:06Z"
}
```

### `DELETE /v1/hydration/{id}`

Creates a server tombstone. Repeating the same delete is successful.

### `GET /v1/hydration?from=...&to=...&cursor=...&limit=...`

Returns records within a bounded interval and an optional next cursor.

## 7. Synchronization endpoint

### `POST /v1/sync`

Preferred batch endpoint for efficient offline synchronization.

Request:

```json
{
  "cursor": "opaque-or-null",
  "operations": [
    {
      "operationId": "uuid",
      "entityType": "hydration",
      "operation": "upsert",
      "entityId": "uuid",
      "payloadVersion": 1,
      "payload": {
        "amountMilliliters": 250,
        "recordedAt": "2026-11-13T08:00:00Z"
      }
    }
  ]
}
```

Response:

```json
{
  "cursor": "next-opaque-cursor",
  "acknowledgedOperationIds": ["uuid"],
  "rejectedOperations": [],
  "changes": [],
  "hasMore": false
}
```

Contract requirements:

- Repeated `operationId` returns the original result.
- Changes are ordered by monotonically increasing server revision.
- A cursor is scoped to the authenticated user.
- Expired cursors return `SYNC_CURSOR_EXPIRED`; the client performs a bounded full reconciliation.
- One invalid operation does not silently discard unrelated valid operations.

## 8. Goals and settings

- `GET /v1/goals`
- `PUT /v1/goals/{metricType}`
- `GET /v1/notification-preferences`
- `PUT /v1/notification-preferences`

Validate safe ranges server-side. Notification quiet hours include timezone behavior.

## 9. API testing requirements

- Contract tests generated from the agreed schema/OpenAPI document.
- Authorization tests for cross-user access.
- Idempotency/replay tests.
- Pagination and cursor-expiry tests.
- Range/size/fuzz validation.
- Deletion and retention tests.
- Backward-compatibility test against the oldest supported Android app API version.
