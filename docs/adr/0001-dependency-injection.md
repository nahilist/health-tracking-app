# 0001: Manual Dependency Injection via AppContainer

- Status: accepted
- Date: 2026-11-13
- Owners: Architecture / Core Engineering

## Context

Pulse Health requires clean boundaries between presentation, domain, and data layers to satisfy PR 2 and decouple ViewModels from concrete database and platform storage singletons. Currently:
- `DashboardViewModel` directly instantiates `PulseDatabase`, `UserPreferencesRepository`, and `HealthConnectRepository`.
- Concrete implementations cannot be swapped for lightweight fakes in unit tests without running under Robolectric.
- Clock and dispatchers are hardcoded in several components.

We need a dependency injection approach that introduces clear repository contracts and injected dispatchers/clocks without introducing premature build complexity.

## Decision

We adopt an **Application Container (`AppContainer`) pattern** with interface-based repositories for Phase 1:
1. Define repository interfaces (`HydrationRepository`, `PreferencesRepository`, `HealthConnectRepository`).
2. Provide a top-level `AppContainer` interface instantiated by `PulseHealthApplication`.
3. Inject `Clock` and `CoroutineDispatcher` (e.g. `Dispatchers.IO`) through repository constructors.
4. Supply ViewModels through `ViewModelProvider.Factory` using `viewModelFactory { initializer { ... } }`.

## Alternatives considered

1. **Hilt / Dagger**:
   - *Rejected for Phase 1*: Adds significant annotation processing (`kapt`/`ksp`), bytecode transformation, and build time overhead for a single-module app with 3 repositories. Can be seamlessly adopted in Phase 2 if multi-module boundaries or dynamic feature requirements arise.
2. **Koin**:
   - *Rejected*: Adds third-party runtime reflection/service locator dependency outside AndroidX.
3. **Leaving singletons in ViewModels**:
   - *Rejected*: Violates core architectural rules and blocks fast JVM unit testing.

## Consequences

- **Positive**: Zero annotation-processing build latency; 100% pure Kotlin interfaces; ViewModels become unit-testable on standard JVM with simple fakes; dispatchers and clocks are easily substituted.
- **Negative**: Factory boilerplate in `viewModelFactory` initializers (minimal, ~6 lines per ViewModel).

## Security and privacy impact

Centralizing data sources in `AppContainer` prevents unauthorized, direct database queries and ensures all data access flows through verified repository boundaries where health permission checks and validation rules are enforced.

## Migration and rollback

Repository interfaces are standard Kotlin interfaces matching the target Hilt `@Inject` architecture. If migrating to Hilt later, repository implementations simply gain `@Inject constructor(...)` annotations without changing presentation consumers.
