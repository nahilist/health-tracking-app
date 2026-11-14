# Google Play Console & Health Connect Declarations

This document provides the canonical responses, justifications, and declarations required for publishing **Pulse Health** (`com.pulsehealth.app`) on Google Play in compliance with the **Google Play Health Apps Policy** and **Health Connect Permissions Policy**.

---

## 1. Google Play Health Apps Declaration

### A. App Categorization
- **Primary Category**: Health & Fitness
- **App Nature**: Non-medical wellness and physical activity companion.
- **Medical Device Status**: **No**. Pulse Health is NOT a medical device under the EU MDR, US FDA, or relevant national regulations.
- **Diagnostic / Treatment Disclaimer**: Pulse Health does not diagnose, treat, cure, or prevent any disease, condition, or physiological abnormality. It does not provide medical advice or recommend drug/treatment changes.

### B. Health Connect Disclosures
- Pulse Health uses Health Connect to aggregate sensor data for personal wellness visibility.
- Data read from Health Connect is processed strictly on-device for displaying dashboard rings and historical trends.
- Data read from Health Connect is **never transferred or sold** to advertising networks, third-party data brokers, or information resellers.

---

## 2. Play Console Data Safety Form

| Question | Value | Explanation |
|---|---|---|
| **Does your app collect or share user data?** | **Yes** | Collects health & fitness data for app functionality. |
| **Is data encrypted in transit?** | **Yes** | All network communications use TLS 1.3. |
| **Do you provide a way for users to request data deletion?** | **Yes** | Users can delete all local data with one tap, or delete their cloud account and associated backups. |

### Data Types Collected

#### 1. Health and Fitness
- **Physical Activity**: Steps, active calories, exercise duration, stand hours.
  - *Collected*: Yes
  - *Shared*: No (unless user explicitly exports/shares with a trusted contact).
  - *Ephemeral*: No (persisted locally).
  - *Required or Optional*: Optional (requires user permission).
  - *Purpose*: App functionality, personalization.
- **Health Info**: Resting heart rate, sleep duration, hydration volume.
  - *Collected*: Yes
  - *Shared*: No.
  - *Ephemeral*: No.
  - *Purpose*: App functionality.

#### 2. Personal Info (Optional - Cloud Accounts Only)
- **Email Address**:
  - *Collected*: Yes (only if user signs up for cloud backup).
  - *Shared*: No.
  - *Purpose*: Account management, multi-device recovery.

#### 3. App Info and Performance
- *Crash Logs / Diagnostics*: Standard platform-level Play Console vitals only; zero third-party telemetry SDKs.

---

## 3. Health Connect Permission Justifications

These justifications must be submitted verbatim in the Google Play Console Health Connect declaration form:

### 1. `android.permission.health.READ_STEPS`
> **Justification**: Pulse Health requires read access to steps to display daily walking progress against user goals on the primary dashboard and provide weekly/monthly historical trend visualizations.

### 2. `android.permission.health.READ_ACTIVE_CALORIES_BURNED`
> **Justification**: Read access to active calories burned is required to compute and render the Move activity ring, enabling users to track daily energy expenditure.

### 3. `android.permission.health.READ_EXERCISE`
> **Justification**: Read access to exercise sessions is required to calculate total daily exercise minutes and provide duration metrics on the Exercise activity ring.

### 4. `android.permission.health.READ_HEART_RATE`
> **Justification**: Read access to heart rate records is required to calculate and display the user's daily resting heart rate on the Vitals card and historical trend views.

### 5. `android.permission.health.READ_SLEEP`
> **Justification**: Read access to sleep session records is required to display nightly sleep duration and restorative rest trends on the Sleep card and detail view.

### 6. `android.permission.health.WRITE_HYDRATION`
> **Justification**: Write access to hydration records is an optional setting enabling users who log water intake in Pulse Health to automatically record those entries into Health Connect for visibility across other health apps.

---

## 4. App Store Listing Metadata

- **App Name**: Pulse Health
- **Short Description**: Calm, private health dashboard for activity, heart rate, sleep and hydration.
- **Full Description**:
  > Pulse Health is an open-source, privacy-first wellness dashboard that brings together your daily activity rings, resting heart rate, sleep duration, and hydration in one elegant, distraction-free interface.
  >
  > Key features:
  > • Daily Activity Rings: Track Move, Exercise, and Stand targets.
  > • Vitals & Rest: Monitor resting heart rate and sleep duration.
  > • Fast Hydration Logging: One-tap quick-add with streak milestones.
  > • Health Connect Integration: Seamless aggregation with your favorite wearables.
  > • Offline First: Fully functional without an account or internet access.
  > • Trusted Sharing: Share selected trends with doctors, trainers, or family with granular permissions and instant revocation.
  > • Zero Ads or Trackers: We never sell or monetize your health data.
- **Privacy Policy URL**: `https://github.com/nahilist/health-tracking-app/blob/main/docs/PRIVACY_POLICY.md`
