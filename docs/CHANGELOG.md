# Changelog

All notable changes to **Pulse Health** are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

---

## [1.0.0] - 2026-11-14

### Added
- **Dedicated Modular Profile & Settings Architecture**:
  - Re-architected Profile & Settings into an executive, modular multi-screen experience with distinct, dedicated destinations for each domain:
    - **Account & Cloud Sync** (`AccountSectionScreen.kt`): User identity, sync freshness, Supabase PostgreSQL RLS security badge, instantaneous manual sync trigger, and secure auth actions.
    - **Health Connect** (`HealthConnectSectionScreen.kt`): Standalone permission rationale hub, availability badge, 2-way water write-back toggle, and deep-link system settings shortcut.
    - **Hydration Reminders** (`RemindersSectionScreen.kt`): Master reminder toggle with Android 13+ `POST_NOTIFICATIONS` runtime permission launcher, interval selection chips (1h, 2h, 3h, 4h), quiet hours schedule (22:00–08:00), and immediate diagnostic reminder trigger.
    - **Data Vault & Privacy** (`DataPrivacySectionScreen.kt`): 100% user-owned declaration, standardized JSON export triggering system share sheet, local data erasure with double-confirmation dialog, and privacy policy viewer.
    - **Preferences & About** (`PreferencesSectionScreen.kt`): Sample Demo Mode simulator toggle, build metadata, and architecture specifications.
  - Native `BackHandler` integration ensuring predictable system back gesture/button behavior from any sub-screen back to the main Profile Hub.
  - Executive minimalist styling: OLED-optimized high-contrast cards, hairline borders, live status dots, and fluid `AnimatedContent` cross-fading.
- **Native Health Dashboard**:
  - Native Jetpack Compose interface with Material 3 theming supporting both dynamic light and dark color schemes.
  - Interactive 3-ring Activity visualizer representing daily Move (active calories), Exercise (minutes), and Stand (hours) progress.
  - Quick-glance metric cards for Steps, Resting Heart Rate, Sleep Duration, and Hydration.
  - Interactive Time Range selector switching seamlessly across Today, Week, and Month aggregations.
  - Customizable Favorites management allowing users to hide, reorder, and personalize metric cards.
- **Android Health Connect Integration**:
  - Transparent availability detection (`AVAILABLE`, `UPDATE_REQUIRED`, `NOT_SUPPORTED`).
  - Pre-permission educational rationale dialog explaining data usage and boundaries before system prompts.
  - Granular permission requests for Steps, Active Calories, Exercise Sessions, Heart Rate, and Sleep.
  - Optional write-back synchronization for manual water intake records with explicit consent.
  - Graceful degradation when Health Connect is missing, ungranted, or permissions are revoked.
- **Local-First SQLite Database**:
  - Room version 1 database with exported schema and transactional safety.
  - `HydrationEntryEntity` with milliliter volume, timestamps, and two-way sync state tracking.
  - `DailyHealthSummaryEntity` caching daily rollup aggregates.
  - Jetpack DataStore preferences storing hidden card IDs, daily water targets, and quiet hours settings.
- **Supabase Cloud Authentication & Keystore Security**:
  - Optional email and password authentication with instant local-only bypass choice.
  - Hardware-backed Android Keystore credential encryption (`EncryptedSharedPreferences`) protecting refresh tokens and session keys.
  - Automatic session recovery, silent refresh rotation, and clean sign-out clearing account-scoped jobs.
- **Offline-to-Cloud Sync Engine**:
  - Idempotent Room outbox pattern (`syncState: pending / synced`).
  - WorkManager background synchronization constrained to connected unmetered networks with exponential backoff.
  - Supabase PostgreSQL cloud tables guarded by Row-Level Security (RLS) policies enforcing user isolation.
  - Manual sync trigger in user profile with real-time status and error banner feedback.
- **Opt-in Local Notifications & Reminders**:
  - Notification channel (`pulse_hydration_reminders`) and runtime permission handling for Android 13+ (`POST_NOTIFICATIONS`).
  - Configurable reminder frequency (1h, 2h, 3h, 4h).
  - Quiet hours window (default: 22:00 to 08:00) automatically silencing reminders overnight.
  - Interactive notification quick action receiver (`HydrationActionReceiver`) logging "+250 mL" directly from the notification tray.
- **Hydration Trends, Streaks & Milestones**:
  - Pure domain streak calculation engine (`HydrationTrends.kt`) tracking consecutive goal days, all-time best streaks, and weekly milestones.
  - Dynamic 7-day weekly hydration progress bar card on Dashboard with goal achievement indicators.
  - Celebratory milestone banner on reaching daily hydration targets.
- **Categorized Browse & Historical Metric Views**:
  - Comprehensive `BrowseScreen` categorizing all 8 metrics: Activity (Overview, Steps, Calories, Exercise, Stand), Vitals (Resting Heart Rate), Sleep (Sleep Duration), and Nutrition & Hydration (Water Intake).
  - Real-time search filter with query highlighting and empty-state messaging.
  - Interactive `MetricTrendChart` component rendering 7-day trend bars with target threshold lines and current-day highlighting.
  - Summary statistics card displaying daily averages, range totals, peak highs, and lows.
  - Non-diagnostic educational metric guidance adhering to wellness and medical device standards.
- **Trusted Health Sharing**:
  - 100% opt-in trusted sharing architecture (`SharingModels.kt` & `SharingRepository.kt`).
  - Role-based recipient authorizations (Healthcare Provider, Family, Partner, Coach, Friend).
  - Granular metric selection checkboxes omitting unselected data from exported payloads.
  - Time-bounded access windows (7 Days, 30 Days, Until Revoked) with instant one-tap revocation.
  - Sanitized health summary export (`ExportedHealthPackage`) integrated with Android system share sheet (`Intent.ACTION_SEND`).
- **Accessibility & Compliance**:
  - Full TalkBack accessibility traversal, semantic roles (`Role.Button`, `Role.Tab`), and state descriptions.
  - Minimum 48dp touch targets on all interactive buttons, icons, and dialogs.
  - Adaptive layout height constraints (`defaultMinSize(minHeight = 154.dp)`) resilient to 200% font scaling without text clipping.
  - Complete production `PRIVACY_POLICY.md` and Google Play Console `PLAY_STORE_DECLARATIONS.md`.

### Fixed
- Decoupled favorite metric card configuration from localized English display labels by introducing canonical identifiers (`FavoriteCardId`), automated transparent in-flight DataStore migration, and TalkBack button semantics.
- **Auth & Sync Error Sanitization & Leak Prevention**: Completely sanitized all authentication and synchronization error handling in `SupabaseAuthRepository`, `SupabaseSyncRepository`, `AuthDialog`, `SummaryScreen`, and `ProfileScreen`. Strips internal URLs, authorization headers, and API keys, preventing sensitive credential leakage and rendering human-friendly error messages.
- **Minimalist Frontend Redesign & Decluttering**:
  - Eliminated visual overcrowding across dashboard and secondary screens by consolidating multi-level stacked cards and redundant banners.
  - Redesigned Apple/Oura-grade minimalist theme with OLED black `#000000` dark mode, crisp `#F8F9FA` light mode, delicate 0.5dp hairline borders, and 0dp elevation.
  - Unified grouped card architecture (inset grouped style) for `BrowseScreen` category metrics and `DetailScreen` hydration log history.
  - Integrated sync freshness timestamp into dashboard top row, eliminating bulky standalone status banner.
  - Streamlined `ProfileScreen` into 3 concise grouped preference cards (Data Sources, Preferences, Privacy) and integrated privacy badge in `SharingScreen`.
  - Refined floating flat bottom navigation bar with clean 0dp shadow and delicate hairline divider.

### Security & Privacy
- Zero advertising SDKs, tracking pixels, or third-party behavioral profiling libraries.
- TLS 1.3 encrypted transport for all network requests.
- Hardware-backed Android Keystore credential protection.
- Row-Level Security on cloud PostgreSQL tables ensuring strict tenant isolation.
- Automatic local midnight hydration rollover without destructive history loss.
- Full local data export (JSON) and one-tap local data deletion in user profile.

---

[1.0.0]: https://github.com/nahilist/health-tracking-app/releases/tag/v1.0.0
