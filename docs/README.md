# Pulse Health Documentation

This directory is the operational source of truth for product and engineering decisions.

| Document | Purpose | Read when |
|---|---|---|
| [PRD](PRD.md) | Product goals, users, requirements and acceptance criteria | Planning any user-facing feature |
| [Architecture](ARCHITECTURE.md) | Current and target system design | Changing modules, dependencies or data flow |
| [Data model](DATA_MODEL.md) | Local schema, target cloud schema and migration rules | Editing Room or backend persistence |
| [API contract](API_CONTRACT.md) | Planned authentication and sync API | Building backend, auth or cloud sync |
| [Security and privacy](SECURITY_PRIVACY.md) | Sensitive-data controls and threat model | Handling identity, health data or releases |
| [Development](DEVELOPMENT.md) | Setup, commands, testing, CI/CD and PR process | Starting work or diagnosing builds |
| [Roadmap](ROADMAP.md) | Delivery sequence and PR boundaries | Choosing what to implement next |
| [QA test plan](QA_TEST_PLAN.md) | Automated/manual coverage and release matrix | Testing a change or planning regression |
| [Release checklist](RELEASE_CHECKLIST.md) | Signing, tagging, rollout and rollback | Preparing a production build |
| [ADRs](adr/README.md) | Durable technical decisions | Choosing providers or architecture boundaries |

Status vocabulary used throughout:

- **Implemented**: present in `main` and covered by the current build.
- **Partial**: some code exists, but production acceptance criteria are not met.
- **Planned**: approved target behavior with no complete implementation.
- **Out of scope**: intentionally excluded from the current product direction.

When code and documentation conflict, verify the code, then update the documentation in the same PR.
