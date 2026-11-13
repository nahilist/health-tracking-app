# Security and Privacy

## 1. Data classification

| Classification | Examples | Required handling |
|---|---|---|
| Restricted health data | steps, heart rate, sleep, exercise, hydration | Explicit permission/consent, least access, no logs, encrypted transport |
| Authentication secret | passwords, refresh tokens, signing keys | Never in repository or logs; secure platform/server storage |
| Personal data | email, profile name, device ID | Purpose limitation, deletion/export support |
| Preferences | hidden cards, units, theme | Local storage; account sync only with disclosure |
| Public operational data | app version, non-sensitive build metadata | Standard telemetry controls |

## 2. Current privacy posture

Implemented:

- Android backup disabled.
- Health Connect uses Android permission APIs.
- Health records are not uploaded by current code.
- Hydration and summaries are stored locally in Room.
- Release secrets are read from GitHub environment secrets.

Gaps before production:

- No privacy policy or consent screens.
- No local export/delete UI.
- No secure authenticated-session implementation.
- No formal data retention schedule.
- No penetration test or threat-model review.
- No verified Play Console Health Apps declaration.

## 3. Threat model

### Assets

- Health records and derived summaries
- Account identity and session credentials
- Signing keystore and CI secrets
- Integrity of app releases and synchronization

### Threats and mitigations

| Threat | Required mitigation |
|---|---|
| Lost or compromised device | Rely on device lock/encryption; minimize local data; offer clear-data controls |
| Token theft | Keystore-backed credential encryption, short-lived access tokens, refresh rotation |
| Backend authorization bypass | Server derives owner from token; row-level authorization; negative authorization tests |
| Man-in-the-middle | TLS, modern cipher policy, no cleartext traffic |
| Malicious/compromised dependency | Dependency review, pinned action majors/SHAs where policy requires, automated updates and SBOM |
| CI secret exposure | Environment secrets, protected environment, no secret echo, least GitHub permissions |
| Log leakage | Structured redaction and prohibition on raw health/token logging |
| Sync replay/duplication | Idempotency keys and server revisions |
| Database migration loss | Exported schemas, migration tests and backups only under reviewed design |
| Deep-link abuse | Allowlisted routes, validated parameters and no sensitive URL payloads |

## 4. Authentication requirements (planned)

- Prefer standards-based OAuth/OIDC or a reviewed managed auth provider.
- Passwords, if supported, are processed only by the auth service and never stored by the app/backend in plaintext.
- Access tokens are short-lived; refresh tokens rotate and can be revoked.
- Android storage uses Keystore-backed encryption or provider-supported secure storage.
- Sign-out clears credentials and account-scoped scheduled work.
- Sensitive changes require recent authentication where supported.
- Rate-limit login, password reset and verification endpoints.

## 5. Health Connect requirements

- Request only enabled record types.
- Provide a purpose explanation before the Android permission dialog.
- Check permission state immediately before reads/writes.
- Handle revocation without crashes or misleading stale values.
- Do not combine demo data with real Health Connect data.
- Match manifest permissions, runtime requests and Play Console declarations.

## 6. Backend requirements (planned)

- TLS only; cleartext traffic disabled.
- Authorization on every user-resource operation.
- Encrypt managed database volumes and backups.
- Maintain audit events for account/security changes, excluding raw health payloads.
- Separate production, staging and development credentials/data.
- Define retention, backup restoration, incident response and deletion SLAs.
- Validate payload size, range, timestamp and ownership server-side.

## 7. Logging and monitoring

Allowed examples:

- operation name, duration and success/error category;
- app version, OS version and anonymous installation/session diagnostics after consent review;
- sync counts without record values.

Never log:

- passwords, access/refresh tokens or API keys;
- email addresses or stable user identifiers in general logs;
- heart-rate values, sleep sessions, hydration entries or other raw health records;
- Health Connect record IDs if they can be linked to a person;
- keystore content or signing passwords.

## 8. CI/CD secret management

Required GitHub `production` environment secrets:

- `ANDROID_KEYSTORE_BASE64`
- `ANDROID_STORE_PASSWORD`
- `ANDROID_KEY_ALIAS`
- `ANDROID_KEY_PASSWORD`

Rules:

- Never commit the `.jks` file.
- Limit environment deployment to version tags and approved maintainers when repository settings allow.
- Store an offline encrypted backup of the upload key and recovery instructions.
- Rotate upload keys through Play Console procedures when compromised.
- Keep GitHub workflow permissions minimal; CI uses read-only contents permission.

## 9. User rights and controls (planned)

- View data sources and freshness.
- Revoke Health Connect access through system settings.
- Export local and cloud data in a documented format.
- Delete individual manual records.
- Clear all local application data.
- Delete the cloud account and observe deletion progress.
- Opt out of optional analytics and notifications.

## 10. Security review checklist

- [ ] Threat model updated for the feature
- [ ] Permissions and data collection minimized
- [ ] No secrets or personal data in diff/tests/logs
- [ ] Authentication and authorization tested negatively
- [ ] Network and storage handling reviewed
- [ ] Deletion/export behavior updated
- [ ] Dependency and CI permissions reviewed
- [ ] Privacy disclosures match actual processing
