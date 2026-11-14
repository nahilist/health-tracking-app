# Privacy Policy for Pulse Health

**Last updated**: November 14, 2026  
**Effective date**: November 14, 2026  
**Application**: Pulse Health (`com.pulsehealth.app`)  
**Developer**: Pulse Health Open Source Team (`nahilist/health-tracking-app`)

---

## 1. Introduction and Core Principles

Pulse Health is an Android wellness tracking application designed to help individuals monitor daily activity, resting heart rate, sleep duration, and hydration in a calm, clear, and privacy-respecting environment.

Our architectural and product commitment is built upon four fundamental principles:

1. **Private by Default**: Health records remain stored locally on your device unless you explicitly authenticate and enable cloud synchronization.
2. **User-Controlled Data**: You maintain full ownership and control over your health metrics. You can view, export in standard formats, or permanently delete your records at any time.
3. **Least Privilege & Transparency**: We only request permissions necessary to display the features you choose to use. We explain the purpose of each permission before requesting it.
4. **No Commercial Data Exploitation**: We never sell, rent, monetize, or disclose your health data to third-party data brokers, advertising networks, or analytics tracking services.

> **Medical Disclaimer**: Pulse Health is a personal wellness tool and is **not a medical device**. It does not diagnose medical conditions, recommend medication alterations, or substitute for professional medical consultation, diagnosis, or clinical treatment.

---

## 2. Information We Collect and Process

### A. Health and Fitness Data (Local First)
- **Activity & Movement**: Daily step count, active calories burned, exercise duration in minutes, and stand hours.
- **Cardiovascular Vitals**: Resting heart rate in beats per minute (bpm).
- **Sleep Metrics**: Nightly sleep duration and session intervals.
- **Hydration**: Manually recorded water intake logs (in milliliters), daily intake targets, and streak records.

All manual records and derived daily aggregations are stored locally on your device in an encrypted/sandboxed SQLite database via Android Room.

### B. Android Health Connect Integration
Pulse Health integrates with Android Health Connect to aggregate measurements from your connected fitness trackers and sensors upon your explicit authorization:
- `android.permission.health.READ_STEPS`
- `android.permission.health.READ_ACTIVE_CALORIES_BURNED`
- `android.permission.health.READ_EXERCISE`
- `android.permission.health.READ_HEART_RATE`
- `android.permission.health.READ_SLEEP`
- `android.permission.health.WRITE_HYDRATION` (Optional: used only if you explicitly choose to sync manual hydration records back to Health Connect).

Health Connect data read by Pulse Health is processed locally on your device to generate your daily summaries and trends. Pulse Health complies strictly with Google's Health Connect Permissions Policy.

### C. Optional Account & Cloud Synchronization
If you choose to create an account for cross-device backup:
- **Authentication**: Email address and password or federated sign-in credentials managed via our authenticated backend service (Supabase).
- **Session Credentials**: Stored securely on your device using hardware-backed Android Keystore cryptography (`EncryptedSharedPreferences`).
- **Cloud Database**: Records are synchronized using TLS-encrypted transport to PostgreSQL tables protected by Row-Level Security (RLS) policies ensuring only your authenticated account can access your data.
- **Local-Only Choice**: You can decline account creation and use Pulse Health entirely in local-first offline mode without limitation.

### D. Local Notifications & Reminders
- Pulse Health provides optional, local-only hydration reminders scheduled via Android WorkManager.
- Notifications are scheduled and displayed locally on your device without cloud telemetry.
- Configurable Quiet Hours (default: 22:00 to 08:00) silence notifications overnight.

### E. Trusted Health Sharing
- Sharing is strictly opt-in and recipient-authorized.
- You select the specific recipient (e.g., healthcare provider, family member, coach), the exact metrics to share, and the duration window (7 days, 30 days, or until revoked).
- Export packages generated through the app omit unselected metrics to prevent data leakage.
- You can revoke access immediately with a single tap.

---

## 3. How We Protect Your Information

- **Local Sandboxing**: Data is stored in private internal application storage inaccessible to other apps.
- **Encrypted Transport**: All network operations use TLS 1.3 with strict modern cipher suites.
- **Credential Protection**: Auth tokens and refresh keys are encrypted using AES-256-GCM via the Android Keystore system.
- **No Third-Party Analytics / Ads**: Pulse Health contains no advertising SDKs, tracking pixels, or third-party behavioral profiling libraries.

---

## 4. User Rights and Data Control

Under applicable privacy legislation (including GDPR, CCPA, and Android platform policies), you have the right to:

1. **Access and Export**: You can export your full recorded hydration and summary data in portable JSON format at any time directly from the app's Profile screen.
2. **Erase / Delete**: You can permanently delete all locally stored records and preferences with one tap ("Delete All Local Data") in the Profile screen.
3. **Revoke Permissions**: You can revoke Health Connect permissions or notification permissions at any time via Android System Settings without breaking local habit tracking.
4. **Cloud Account Deletion**: When signed into a cloud account, you can request immediate account deletion and removal of cloud backups.

---

## 5. Children's Privacy

Pulse Health is not directed to individuals under the age of 13 (or the minimum legal age in your jurisdiction). We do not knowingly collect personal health information from children.

---

## 6. Changes to this Privacy Policy

If we modify this Privacy Policy, we will update the "Last updated" date at the top of this document. Material changes regarding cloud processing or data sharing will be accompanied by an explicit in-app notification before taking effect.

---

## 7. Contact Us

If you have questions, feedback, or requests regarding this Privacy Policy or your data privacy rights, please open an issue on our GitHub repository:

- **Repository**: [https://github.com/nahilist/health-tracking-app](https://github.com/nahilist/health-tracking-app)
- **Support Contact**: privacy@pulsehealth.app (or GitHub issue tracker)
