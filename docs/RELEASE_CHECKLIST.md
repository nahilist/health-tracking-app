# Release Checklist and Runbook

## 1. Preconditions

- [x] Intended commits are verified against CI suite.
- [x] Version code and version name (`1.0.0`) match production roadmap.
- [x] Changelog (`docs/CHANGELOG.md`) documents all features, fixes, and security posture.
- [x] Privacy disclosures (`docs/PRIVACY_POLICY.md`) match actual permissions/data flows.
- [x] Health Connect and Play Console declarations (`docs/PLAY_STORE_DECLARATIONS.md`) are current.
- [x] Incident response and rollback runbook (`docs/INCIDENT_RESPONSE_RUNBOOK.md`) is documented.
- [x] Third-party dependency licenses audited and verified compliant (`docs/DEPENDENCY_COMPLIANCE.md`).
- [x] No S0/S1 bugs remain open.
- [ ] Production signing secrets configured in repository secrets environment.

## 2. Validation

```powershell
git switch main
git pull --ff-only origin main
.\gradlew.bat clean testDebugUnitTest lintDebug assembleDebug bundleRelease
```

- [ ] Install a release candidate on a clean device.
- [ ] Upgrade over the previous production version.
- [ ] Run the critical scenarios in [QA Test Plan](QA_TEST_PLAN.md).
- [ ] Verify R8 build startup and Health Connect integration.
- [ ] Verify package name, icon, label and version.

## 3. Create release

Use semantic tags:

```powershell
git tag v1.0.0
git push origin v1.0.0
```

The `Android Release` workflow builds signed APK/AAB artifacts and a GitHub release. Confirm:

- [ ] Workflow used the expected commit/tag.
- [ ] Signing step succeeded.
- [ ] APK/AAB and mapping file were retained.
- [ ] Artifact version and certificate are correct.
- [ ] Release notes exclude secrets and personal data.

## 4. Play deployment

- [ ] Upload AAB to internal testing first.
- [ ] Review automated pre-launch report.
- [ ] Verify Data safety form and permissions declaration.
- [ ] Promote progressively; do not jump directly to 100% production.
- [ ] Monitor crash/ANR, authentication and sync signals when available.

## 5. Rollback/incident response

Android releases cannot be removed from already updated devices. Roll forward with a higher version code.

For a critical issue:

1. Stop staged rollout.
2. Classify security/privacy and data-integrity impact.
3. Disable affected remote feature when a reviewed kill switch exists.
4. Prepare a minimal fix from the production tag.
5. Run focused regression plus full release gates.
6. Release with a higher version code.
7. Document timeline, scope and corrective actions.

Never solve migration failures by deleting user data unless the user explicitly chooses a documented reset flow.

## 6. Post-release

- [ ] Confirm production artifact availability.
- [ ] Confirm key user journeys on a production install.
- [ ] Review crash/ANR and support feedback.
- [ ] Record release commit, tag, version code and rollout dates.
- [ ] Update roadmap and known issues.
