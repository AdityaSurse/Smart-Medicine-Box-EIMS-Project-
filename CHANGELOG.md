# Changelog

All notable changes to the **Smart Medicine Box** Android application will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Conventional Commits](https://www.conventionalcommits.org/en/v1.0.0/) and [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

---

## [1.0.0] - 2026-10-31

### 🚀 Core Features & UI Presentation
- **Jetpack Compose & Material 3 UI**: Built with 100% Kotlin Jetpack Compose following Stitch design tokens (Apothecary palette: Obsidian slate `#1A1A17`, Forest Sage `#3E683A`, Bone Paper `#F7F7F4`, Terracotta Alert `#BA1A1A`).
- **Onboarding & Authentication**:
  - Interactive onboarding walkthrough introducing smart dispenser hardware capabilities (`5c94542`).
  - Firebase Authentication with email/password validation and Google Sign-In support (`832c8ba`, `ff8acfa`).
  - Auth-gated navigation routing protecting sensitive medical views.
- **Live Telemetry Dashboard**:
  - Real-time chamber temperature, relative humidity, load-cell weight, battery state, and Wi-Fi connection indicators (`14b0217`, `f6da72a`).
  - Dynamic "Next Dose" countdown and device staleness warnings with pull-to-refresh.
- **Prescription & Schedule Management**:
  - Weekly horizontal date strip with interactive day filters and regimen overview (`3ee5a73`).
  - Medication inventory management with low-stock warning threshold badges (`73542dd`).
  - Add and Edit Medicine flow with dosage form selector and 4-bay compartment allocation (`5fdcee1`).
- **Intake Log & Clinical Adherence Ledger**:
  - Adherence summary ledger grouped into today and past historical audits (`e3fcb04`).
  - Clinical adherence calculations (taken vs missed counts, percentage adherence, and streaks).
- **Missed-Dose Alerts Screen**:
  - Critical notification center displaying timestamped missed-dose warnings, chamber environmental warnings (DHT22), and offline device alarms (`28abf9d`).
- **Device Pairing & Hardware Configuration**:
  - Camera-based QR code scanner and manual fallback pairing for ESP32 dispenser provisioning (`b9c4890`, `f6da72a`).
  - SoftAP and BLE Wi-Fi provisioning architecture (`docs/PROVISIONING.md`).
  - Remote hardware controls: buzzer silencing command doc (`DG_BOX_4081/commands`).
- **Comprehensive Settings & Preferences**:
  - Caregiver contact roster with direct phone dialer actions (`41fc61e`).
  - Measurement unit toggle: Metric (°C, g) vs Imperial (°F, oz).
  - Configurable dose grace periods (15m, 30m, 45m, 60m).
  - Reminder repeat loops (1x, 2x, 3x, 5x) and quiet hours chime muting (10:00 PM – 07:00 AM).
  - Export intake history as CSV or printable clinical summary (`fe9afd2`).

---

### 🧠 Dose Evaluation Engine & Offline Architecture
- **Hardware-Verified Dose Evaluation**:
  - Pure Kotlin `DoseEvaluator` engine verifying physical intake lifecycle:
    $$\text{Prescription Schedule} \longrightarrow \text{Dose Time} \longrightarrow \text{Reed Switch Open} \longrightarrow \text{HX711 Weight Drop} \longrightarrow \text{TAKEN / MISSED}$$
  - Full tolerance for edge cases: box opened without weight change, unscheduled weight drops, multiple doses in close proximity, timezone shifts, and offline queueing (`356c59d`).
- **Exact Alarms & Doze Survival**:
  - Jetpack WorkManager integration combined with `AlarmManager.setExactAndAllowWhileIdle()` to guarantee timely reminders through Android Doze mode and device reboots (`356c59d`, `06f76f5`).
  - Android 12+ `SCHEDULE_EXACT_ALARM` user consent permission flow with settings fallback.
- **Offline-First Persistence**:
  - Room SQLite database (`SmartMedBoxDatabase`) as the local single source of truth for prescriptions, schedules, intake events, and hardware status (`0ff9010`, `325f3d0`).
  - Encrypted/private DataStore preferences for persistent user preferences and reminder loop thresholds (`6a9c315`).

---

### ☁️ Cloud Sync, Firebase & Data Contracts
- **Shared ESP32-Android Firestore Schema**:
  - Formal data contract specification in [`docs/DATA_CONTRACT.md`](docs/DATA_CONTRACT.md) with collection schemas:
    - `users/{uid}/devices/{deviceId}` (Metadata & last seen)
    - `.../devices/{deviceId}/telemetry/latest` (Live sensor readings)
    - `.../devices/{deviceId}/events/{eventId}` (Hardware reed switch & load cell events)
    - `users/{uid}/medicines`, `schedules`, `doseLogs` (`ff8acfa`).
- **Firestore Security Rules**:
  - Strict user-scoped document partitioning in [`firestore.rules`](firestore.rules).
  - 27-test automated verification suite (`tools/test_firestore_rules.js`).
- **Hardware Device Simulator**:
  - Python test simulator script in [`tools/device_simulator.py`](tools/device_simulator.py) emitting simulated telemetry and physical box opening events for hardware-free development (`ff8acfa`).

---

### 🔔 Notifications & Remote Messaging
- **Targeted Notification Channels**:
  - `Dose Reminder` (High Importance, audible chime)
  - `Missed Dose` (High Importance, persistent alert)
  - `Device Alerts` (Default Importance, environmental & battery warnings)
  - `Low Stock` (Low Importance, inventory warnings) (`06f76f5`).
- **Interactive Notification Actions**:
  - "Mark as Taken" and "Snooze 10 min" action buttons handled by `NotificationActionReceiver`.
- **Firebase Cloud Messaging (FCM)**:
  - Background and killed-state push message listener (`SmartMedBoxFirebaseMessagingService`).
  - Token registration stored under `users/{uid}/fcmTokens/`.
  - Cloud Functions push trigger architecture documented in [`functions/README.md`](functions/README.md).

---

### 🛡️ Security, Privacy & Compliance (HIPAA / GDPR)
- **Zero Secrets & Portability Policy**:
  - Keystore passwords, service account credentials, and `google-services.json` strictly excluded from source control.
  - Zero hardcoded machine paths (`local.properties` untracked, CI/CD portable) (`1f0d7b2`, `b69e0df`).
- **Protected Health Information (PHI) Hardening**:
  - Android cloud backup disabled (`android:allowBackup="false"` in manifest).
  - `backup_rules.xml` and `data_extraction_rules.xml` configured to block ADB and cloud extraction of database tables and shared preferences (`fe9afd2`).
- **Patient Data Sovereignty & Right to Erasure**:
  - Dedicated in-app [Privacy Policy Screen](docs/README.md) detailing GDPR and HIPAA protections.
  - One-tap "Delete All Account & Medical Data" with confirmation dialog to purge Room database tables and reset preferences (`fe9afd2`).
- **R8 / ProGuard Optimization**:
  - Enabled code minification and resource shrinking in release builds.
  - Robust keep rules for Navigation routes, Room converters, Firestore DTOs, and Workers in `proguard-rules.pro`.

---

### 🧪 Testing, Quality & Accessibility Pass
- **Unit Testing**:
  - **155 automated unit tests** passing with 100% green status across all 11 Domain UseCases, 10 ViewModels, Data Mappers, and Exporter utilities.
  - Asynchronous StateFlow testing utilizing `Turbine` and `kotlinx-coroutines-test` (`f878da6`).
- **Compose UI & Instrumented Tests**:
  - End-to-end Compose UI test suites for `AuthScreenTest`, `AddEditMedicineScreenTest`, and `DashboardScreenTest`.
  - Instrumented test suite for `NotificationActionReceiverInstrumentedTest`.
- **Quality Assurance & Memory Safety**:
  - Integration of Square's **LeakCanary 2.14** in debug builds.
  - Android **StrictMode** enabled in debug mode for early thread-policy and VM-leak detection.
  - Formal QA report published in [`docs/QA_REPORT.md`](docs/QA_REPORT.md).
- **Accessibility & Contrast**:
  - Minimum 48dp touch targets verified across all buttons, icon toggles, and sliders.
  - Full TalkBack content descriptions and semantic labels on all interactive controls.

---

### ⚙️ CI/CD & Release Infrastructure
- **Continuous Integration (`.github/workflows/ci.yml`)**:
  - Automated PR and push validation running KtLint, Detekt static analysis, unit tests, and debug assembly on JDK 21.
- **Automated Release Pipeline (`.github/workflows/release.yml`)**:
  - Automated pipeline on `v*` tag triggers.
  - Decodes base64 release keystore from GitHub Secrets, builds signed release APK and Android App Bundle (AAB), and publishes a GitHub Release with release binaries.

---

### 🐛 Bug Fixes & Refactoring
- Removed machine-specific JDK paths (`org.gradle.java.home`) from `gradle.properties` (`1f0d7b2`, `b69e0df`).
- Standardized Detekt and KtLint configuration files (`e56abb2`).
- Resolved Compose UI parameter signatures in `DashboardScreen` and `AlertsScreen` (`f878da6`).
- Replaced deprecated icon and outlined button border APIs with Material 3 standard implementations (`f878da6`).
