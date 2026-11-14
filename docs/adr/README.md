# Architecture Decision Records

Use an ADR for decisions that are expensive to reverse or affect security, privacy, data ownership or multiple features.

Naming:

```text
0001-auth-and-backend-provider.md
0002-dependency-injection.md
```

## Recorded ADRs

- [0001: Manual Dependency Injection via AppContainer](0001-dependency-injection.md) (Accepted)
- [0002: Supabase as Authentication and Backend Provider for Hybrid Sync](0002-auth-and-backend-provider.md) (Accepted)

Template:

```markdown
# NNNN: Decision title

- Status: proposed | accepted | superseded
- Date: YYYY-MM-DD
- Owners: names/roles

## Context

What problem and constraints require a decision?

## Decision

What is being chosen?

## Alternatives considered

What credible options were rejected and why?

## Consequences

Positive and negative operational/engineering effects.

## Security and privacy impact

Data, permission, threat and compliance implications.

## Migration and rollback

How to adopt, measure and reverse the decision.
```
