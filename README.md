# Smart Medicine Box

> IoT-enabled companion Android app for automated medication scheduling, real-time load-cell weight telemetry, and proactive adherence alerts.

[![Kotlin](https://img.shields.io/badge/Kotlin-2.1.0-blue.svg?logo=kotlin)](https://kotlinlang.org)
[![Android](https://img.shields.io/badge/Android-API%2026%20to%2035-brightgreen.svg?logo=android)](https://developer.android.com)
[![Compose](https://img.shields.io/badge/Jetpack%20Compose-Material%203-4285F4.svg?logo=jetpackcompose)](https://developer.android.com/jetpack/compose)
[![Tests](https://img.shields.io/badge/Tests-155%20Passing-success.svg?logo=checkmarx)](app/build/reports/tests/testDebugUnitTest/index.html)
[![Firebase](https://img.shields.io/badge/Firebase-Auth%20%7C%20Firestore%20%7C%20FCM-FFCA28.svg?logo=firebase)](https://firebase.google.com)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

---

## Quick Setup & Developer Getting Started

```mermaid
flowchart LR
    classDef step fill:#E8F5E9,stroke:#2E7D32,stroke-width:2px,color:#1B5E20;
    classDef cloud fill:#E1F5FE,stroke:#0277BD,stroke-width:2px,color:#01579B;
    classDef run fill:#FFF3E0,stroke:#E65100,stroke-width:2px,color:#BF360C;

    STEP1["1. Clone Repo<br/><code>git clone</code>"] --> STEP2["2. Firebase Config<br/>Add <code>google-services.json</code>"]
    STEP2 --> STEP3["3. Run Simulator<br/><code>python tools/device_simulator.py</code>"]
    STEP3 --> STEP4["4. Run Android App<br/><code>./gradlew installDebug</code>"]

    class STEP1,STEP2 step;
    class STEP3 cloud;
    class STEP4 run;
```

### Setup Instructions

1. **Clone the repository**:
   ```bash
   git clone https://github.com/AdityaSurse/Smart-Medicine-Box.git
   cd Smart-Medicine-Box
   ```
2. **Add Firebase configuration**:
   - Download `google-services.json` from your Firebase Console project.
   - Place `google-services.json` into the `app/` folder:
     ```bash
     cp /path/to/downloaded/google-services.json app/
     ```
3. **Launch the Hardware Device Simulator**:
   - If physical ESP32 hardware is not connected, use the provided Python simulator:
     ```bash
     cd tools
     pip install firebase-admin
     python device_simulator.py --device-id DG_BOX_4081
     ```
   - The simulator emits realistic load cell weight deltas, reed switch open/close events, and DHT22 temperature/humidity readings directly to Firestore.
4. **Compile & Run the Android App**:
   - Open in **Android Studio (Ladybug 2024.2+ or newer)** with JDK 21.
   - Run tests: `./gradlew testDebugUnitTest`
   - Install debug build: `./gradlew installDebug`

---

## System Overview & Hardware Architecture

```mermaid
flowchart TD
    classDef device fill:#FFE0B2,stroke:#E65100,stroke-width:2px,color:#BF360C;
    classDef cloud fill:#BBDEFB,stroke:#1976D2,stroke-width:2px,color:#0D47A1;
    classDef app fill:#C8E6C9,stroke:#388E3C,stroke-width:2px,color:#1B5E20;
    classDef alert fill:#FFCDD2,stroke:#D32F2F,stroke-width:2px,color:#B71C1C;

    subgraph Hardware ["Smart Dispenser (ESP32)"]
        S1["HX711 Load Cell<br/>Weight Delta Sensor"] --> ESP["ESP32 Microcontroller"]
        S2["Reed Switch<br/>Box Lid Detection"] --> ESP
        S3["DHT22 Sensor<br/>Chamber Temp & Humidity"] --> ESP
        S4["DS1307 RTC<br/>Hardware Timekeeper"] --> ESP
        ESP --> ACT1["OLED Display<br/>Dose Instructions"]
        ESP --> ACT2["Audible Buzzer / Chime"]
    end

    subgraph Cloud ["Firebase Cloud Platform"]
        ESP -->|"HTTPS / TLS 1.3 Telemetry"| FS["Cloud Firestore<br/>users/uid/devices/deviceId"]
        FS -->|"Snapshot Listener Flow"| APP["Android Companion App"]
        FS -->|"Cloud Functions"| FCM["Firebase Cloud Messaging (FCM)"]
    end

    subgraph Mobile ["Android Application (Offline-First)"]
        APP -->|"Cache Snapshot"| ROOM[("Room SQLite DB<br/>Single Source of Truth")]
        APP -->|"WorkManager + Exact Alarms"| ALARM["AlarmManager<br/>Doze & Reboot Safe"]
        ALARM -->|"Audible Alert"| NOTIF["Dose Reminder Notification"]
        FCM -->|"Remote Push"| NOTIF
    end

    class S1,S2,S3,S4,ESP,ACT1,ACT2 device;
    class FS,FCM cloud;
    class APP,ROOM,ALARM app;
    class NOTIF alert;
```

---

## Dose Evaluation Engine

The physical medication intake lifecycle is verified using a pure-Kotlin deterministic engine:

```mermaid
flowchart TD
    classDef device fill:#FFE0B2,stroke:#E65100,stroke-width:2px,color:#BF360C;
    classDef cloud fill:#BBDEFB,stroke:#1976D2,stroke-width:2px,color:#0D47A1;
    classDef app fill:#C8E6C9,stroke:#388E3C,stroke-width:2px,color:#1B5E20;
    classDef alert fill:#FFCDD2,stroke:#D32F2F,stroke-width:2px,color:#B71C1C;

    PRE["Prescription Scheduled<br/>e.g., 08:00 AM"] --> ALARM_TRIG["Dose Reminder Alarm"]
    ALARM_TRIG --> GRACE["Start Grace Period Timer<br/>Configurable 15m - 60m"]
    GRACE --> REED{"Box Opened?<br/>(Reed Switch)"}
    
    REED -- "No" --> TIMEOUT{"Grace Period Expired?"}
    TIMEOUT -- "No" --> REPEAT["Reminder Repeat Loop<br/>Audible Chime / Banner"]
    REPEAT --> GRACE
    TIMEOUT -- "Yes" --> MARK_MISSED["Mark Dose MISSED"]

    REED -- "Yes" --> READ_LOAD["Sample HX711 Load Cell"]
    READ_LOAD --> DELTA{"Weight Decreased?<br/>(Pill removed)"}
    DELTA -- "Yes" --> MARK_TAKEN["Mark Dose TAKEN"]
    DELTA -- "No" --> TAMPER["Quantity Anomaly<br/>Box open, no pills taken"]
    TAMPER --> TIMEOUT

    MARK_TAKEN --> PERSIST["Update Room DB & Sync Firestore"]
    MARK_MISSED --> PERSIST
    MARK_MISSED --> DISPATCH_FCM["Dispatch Urgent FCM Alert"]

    class REED,READ_LOAD,DELTA device;
    class PERSIST,DISPATCH_FCM cloud;
    class PRE,ALARM_TRIG,GRACE,MARK_TAKEN app;
    class TIMEOUT,REPEAT,MARK_MISSED,TAMPER alert;
```

---

## App Navigation & Architecture Map

```mermaid
flowchart TD
    classDef nav fill:#E8F5E9,stroke:#2E7D32,stroke-width:2px,color:#1B5E20;
    classDef screen fill:#F1F8E9,stroke:#558B2F,stroke-width:1.5px,color:#33691E;

    START((Launch)) --> SPLASH["AndroidX SplashScreen"]
    SPLASH --> AUTH_CHECK{"Authenticated?"}
    AUTH_CHECK -- "No" --> ONBOARD["Screen.Onboarding"]
    ONBOARD --> AUTH["Screen.Auth (Login & Sign-Up)"]
    AUTH_CHECK -- "Yes" --> DASH["Screen.Dashboard (Live Telemetry & Next Dose)"]
    AUTH --> DASH

    subgraph NavigationBar ["Bottom Navigation Bar"]
        DASH <--> SCHED["Screen.Schedule (Weekly Calendar & Regimens)"]
        DASH <--> MEDS["Screen.Medicines (Inventory Tracking)"]
        DASH <--> HIST["Screen.History (Intake Ledger & CSV Export)"]
        DASH <--> SETT["Screen.Settings (Protocols & Hardware Config)"]
    end

    MEDS --> ADD_MED["Screen.AddEditMedicine (Bay Allocation)"]
    DASH --> ALERTS["Screen.Alerts (Missed-Dose Log)"]
    SETT --> DEVICE["Screen.Device (QR Scan & Wi-Fi Provisioning)"]
    SETT --> PRIVACY["Screen.PrivacyPolicy (GDPR / HIPAA Sovereignty)"]
    DASH --> NOTIF_RAT["Screen.NotificationRationale (Android 13+)"]

    class START,SPLASH,AUTH_CHECK nav;
    class ONBOARD,AUTH,DASH,SCHED,MEDS,HIST,SETT,ADD_MED,ALERTS,DEVICE,PRIVACY,NOTIF_RAT screen;
```

---

## App Screens & UI Gallery

The UI was crafted from Figma designs via the Stitch MCP with an apothecary aesthetic:

| 📱 Live Telemetry Dashboard | ⏰ Schedule & Regimens | 💊 Medicines Inventory | 📋 Clinical History |
| :---: | :---: | :---: | :---: |
| ![Dashboard Screen Placeholder](docs/images/dashboard_preview.png)<br/>*Real-time weight, temp, humidity & countdown* | ![Schedule Screen Placeholder](docs/images/schedule_preview.png)<br/>*Weekly calendar strip with active dosage times* | ![Medicines Screen Placeholder](docs/images/medicines_preview.png)<br/>*Inventory stocks, pill pill-count & bay assignments* | ![History Screen Placeholder](docs/images/history_preview.png)<br/>*Intake ledger with clinical adherence scores* |

| 🚨 Alerts & Emergency Warnings | 🎛️ QR Device Pairing | ⚙️ Clinical Settings & Export | 🛡️ Patient Privacy Policy |
| :---: | :---: | :---: | :---: |
| ![Alerts Screen Placeholder](docs/images/alerts_preview.png)<br/>*Missed dose logs & environmental threshold alerts* | ![Device Screen Placeholder](docs/images/device_preview.png)<br/>*Camera QR scanning & Wi-Fi provisioning* | ![Settings Screen Placeholder](docs/images/settings_preview.png)<br/>*Grace period, quiet hours & CSV/PDF exporter* | ![Privacy Screen Placeholder](docs/images/privacy_preview.png)<br/>*GDPR/HIPAA data sovereignty & wipe options* |

---

## Technology Stack

- **Language & Runtime**: Kotlin 2.1.0 (Strict null safety, Coroutines, Flow).
- **UI Toolkit**: Jetpack Compose with Material 3 (UDF architecture, stateless screens, zero hardcoded dimensions).
- **Dependency Injection**: Dagger Hilt 2.51 (`@HiltAndroidApp`, `@HiltViewModel`, `@AndroidEntryPoint`).
- **Persistence & Offline-First**:
  - **Room SQLite Database**: Offline cache for medicines, schedules, intake logs, and device status.
  - **Preferences DataStore**: User configurations, snooze intervals, and measurement units.
- **Cloud Infrastructure**:
  - **Firebase Authentication**: Email/Password and Google Sign-In.
  - **Cloud Firestore**: Real-time sync with user-scoped security boundaries (`firestore.rules`).
  - **Firebase Cloud Messaging (FCM)**: Push notifications for missed doses and chamber anomalies.
- **Background Tasks & Alarms**: Jetpack WorkManager 2.10 + `AlarmManager.setExactAndAllowWhileIdle()`.
- **Quality & Profiling**:
  - **Testing**: JUnit 4, MockK, `kotlinx-coroutines-test`, `Turbine` (155 automated unit tests passing).
  - **Static Analysis**: KtLint 12.1 + Detekt 1.23.8 (0 violations).
  - **Memory Safety**: Square LeakCanary 2.14 + Android StrictMode in debug builds.
  - **Release Optimization**: R8 minification, bytecode obfuscation, and resource shrinking.

---

## Project Roadmap (Phases 1–12 Completed)

```mermaid
flowchart TD
    classDef done fill:#C8E6C9,stroke:#388E3C,stroke-width:2px,color:#1B5E20;

    P1["Phase 1: Architecture & Project Scaffolding"] --> P2["Phase 2: Stitch Design Tokens & Composables"]
    P2 --> P3["Phase 3: Screen Presentation Layer"]
    P3 --> P4["Phase 4: Navigation Flow & Routing"]
    P4 --> P5["Phase 5: Domain & Room Persistence Layer"]
    P5 --> P6["Phase 6: Firebase Auth & Cloud Firestore Sync"]
    P6 --> P7["Phase 7: Pure-Kotlin Dose Evaluation Engine"]
    P7 --> P8["Phase 8: High-Priority Notifications & FCM"]
    P8 --> P9["Phase 9: ESP32 Hardware Pairing & Live Telemetry"]
    P9 --> P10["Phase 10: QA Pass, Turbine Tests & LeakCanary"]
    P10 --> P11["Phase 11: Production Hardening, R8 & Privacy"]
    P11 --> P12["Phase 12: Release Readiness & v1.0.0 Tag"]

    class P1,P2,P3,P4,P5,P6,P7,P8,P9,P10,P11,P12 done;
```

---

## Technical Documentation Index

Detailed specifications and architectural guides are maintained in the [`docs/`](docs/README.md) directory:

- 📑 **[Documentation Index (`docs/README.md`)](docs/README.md)**: Master directory and summaries.
- 📡 **[Cloud Data Contract (`docs/DATA_CONTRACT.md`)](docs/DATA_CONTRACT.md)**: Shared ESP32 firmware & Android Firestore schemas.
- 📶 **[Wi-Fi Provisioning Protocol (`docs/PROVISIONING.md`)](docs/PROVISIONING.md)**: SoftAP and BLE provisioning handshake and security schemes.
- 🧪 **[QA & Verification Audit (`docs/QA_REPORT.md`)](docs/QA_REPORT.md)**: Formal testing audit, coverage analysis, and accessibility results.
- 📜 **[Changelog (`CHANGELOG.md`)](CHANGELOG.md)**: Version release notes and commit history.
