# Smart Medicine Box

> IoT-enabled companion Android app for automated medication scheduling, real-time load-cell weight telemetry, and proactive adherence alerts.

[![Kotlin](https://img.shields.io/badge/Kotlin-2.1.0-blue.svg?logo=kotlin)](https://kotlinlang.org)
[![Android](https://img.shields.io/badge/Android-API%2026%2B-brightgreen.svg?logo=android)](https://developer.android.com)
[![Compose](https://img.shields.io/badge/Jetpack%20Compose-Material%203-4285F4.svg?logo=jetpackcompose)](https://developer.android.com/jetpack/compose)
[![Firebase](https://img.shields.io/badge/Firebase-Firestore%20%7C%20FCM-FFCA28.svg?logo=firebase)](https://firebase.google.com)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

---

### System Overview

```mermaid
flowchart TD
    classDef device fill:#FFE0B2,stroke:#E65100,stroke-width:2px,color:#BF360C;
    classDef cloud fill:#BBDEFB,stroke:#1976D2,stroke-width:2px,color:#0D47A1;
    classDef app fill:#C8E6C9,stroke:#388E3C,stroke-width:2px,color:#1B5E20;
    classDef alert fill:#FFCDD2,stroke:#D32F2F,stroke-width:2px,color:#B71C1C;

    S1["HX711 Load Cell"] --> ESP["ESP32 Controller"]
    S2["Reed Switch"] --> ESP
    S3["DHT22 Sensor"] --> ESP
    S4["DS1307 RTC"] --> ESP
    ESP --> ACT1["OLED Display"]
    ESP --> ACT2["Buzzer Alarm"]
    ESP -->|"Wi-Fi Telemetry"| FB["Firebase Cloud"]
    FB -->|"Auth / Firestore"| APP["Android Companion App"]
    FB -->|"FCM Push Alerts"| NOTIF["Missed-Dose Notification"]
    APP -->|"Sync Prescriptions"| FB

    class S1,S2,S3,S4,ESP,ACT1,ACT2 device;
    class FB cloud;
    class APP app;
    class NOTIF alert;
```

---

### Dose Lifecycle

```mermaid
flowchart TD
    classDef device fill:#FFE0B2,stroke:#E65100,stroke-width:2px,color:#BF360C;
    classDef cloud fill:#BBDEFB,stroke:#1976D2,stroke-width:2px,color:#0D47A1;
    classDef app fill:#C8E6C9,stroke:#388E3C,stroke-width:2px,color:#1B5E20;
    classDef alert fill:#FFCDD2,stroke:#D32F2F,stroke-width:2px,color:#B71C1C;

    P["Prescription Configured"] --> WAIT["Wait for Scheduled Time"]
    WAIT --> TIME["Dose Time Reached"]
    TIME --> ALERT_DEV["Box Beeps and Displays Dose"]
    ALERT_DEV --> OPEN{"Box Opened?"}
    OPEN -- "Yes" --> WEIGHT{"Weight Decreased?"}
    OPEN -- "No" --> MISSED["Mark MISSED"]
    WEIGHT -- "Yes" --> TAKEN["Mark TAKEN"]
    WEIGHT -- "No" --> MISSED
    TAKEN --> LOG["Log Intake Event to Firestore"]
    MISSED --> PUSH["Trigger FCM Missed-Dose Alert"]
    PUSH --> LOOP["Reminder Loop Until Acknowledged"]

    class ALERT_DEV,OPEN,WEIGHT device;
    class LOG cloud;
    class P,WAIT,TIME,TAKEN app;
    class MISSED,PUSH,LOOP alert;
```

---

### Missed-Dose Detection Logic

```mermaid
flowchart TD
    classDef device fill:#FFE0B2,stroke:#E65100,stroke-width:2px,color:#BF360C;
    classDef cloud fill:#BBDEFB,stroke:#1976D2,stroke-width:2px,color:#0D47A1;
    classDef app fill:#C8E6C9,stroke:#388E3C,stroke-width:2px,color:#1B5E20;
    classDef alert fill:#FFCDD2,stroke:#D32F2F,stroke-width:2px,color:#B71C1C;

    START["Scheduled Dose Window"] --> GRACE["Start Grace Period Timer"]
    GRACE --> REED{"Reed Switch Triggered?"}
    REED -- "No" --> EXPIRED{"Grace Period Expired?"}
    EXPIRED -- "No" --> GRACE
    EXPIRED -- "Yes" --> FIRE_MISSED["Trigger MISSED State"]
    REED -- "Yes" --> READ_SCALE["Read HX711 Load Cell"]
    READ_SCALE --> DELTA{"Weight Drop Matches Dose?"}
    DELTA -- "Yes" --> CONFIRM_TAKEN["Confirm Dose TAKEN"]
    DELTA -- "No" --> WARN["Flag Quantity Anomaly"]
    WARN --> EXPIRED
    FIRE_MISSED --> DISPATCH["Send Urgent FCM Push"]
    DISPATCH --> ESCALATE["Escalate Reminder Notifications"]

    class REED,READ_SCALE,DELTA device;
    class DISPATCH cloud;
    class START,GRACE,CONFIRM_TAKEN app;
    class EXPIRED,FIRE_MISSED,WARN,ESCALATE alert;
```

---

### App Architecture Layers

```mermaid
flowchart TD
    classDef device fill:#FFE0B2,stroke:#E65100,stroke-width:2px,color:#BF360C;
    classDef cloud fill:#BBDEFB,stroke:#1976D2,stroke-width:2px,color:#0D47A1;
    classDef app fill:#C8E6C9,stroke:#388E3C,stroke-width:2px,color:#1B5E20;
    classDef alert fill:#FFCDD2,stroke:#D32F2F,stroke-width:2px,color:#B71C1C;

    UI["UI: Stateless Jetpack Compose Screens"] -->|"Events"| VM["Presentation: ViewModel with StateFlow"]
    VM -->|"State Flow"| UI
    VM -->|"Executes"| UC["Domain: UseCases operator invoke"]
    UC -->|"Queries / Mutates"| REPO["Domain: Repository Interfaces"]
    REPO_IMPL["Data: Repository Implementation"] -->|"Implements"| REPO
    REPO_IMPL -->|"Cache / Read"| ROOM["Local: Room Database and DataStore"]
    REPO_IMPL -->|"Realtime Sync"| FS["Remote: Cloud Firestore and FCM"]
    CORE["Core: Common, DesignSystem, DI, Util"] -.->|"Shared Utilities"| UI
    CORE -.->|"Shared Utilities"| VM
    CORE -.->|"Shared Utilities"| REPO_IMPL

    class FS cloud;
    class UI,VM,UC,REPO,REPO_IMPL,ROOM,CORE app;
```

---

### App Navigation Map

```mermaid
flowchart TD
    classDef device fill:#FFE0B2,stroke:#E65100,stroke-width:2px,color:#BF360C;
    classDef cloud fill:#BBDEFB,stroke:#1976D2,stroke-width:2px,color:#0D47A1;
    classDef app fill:#C8E6C9,stroke:#388E3C,stroke-width:2px,color:#1B5E20;
    classDef alert fill:#FFCDD2,stroke:#D32F2F,stroke-width:2px,color:#B71C1C;

    AUTH["Screen.Auth: Sign In & Device Link"] --> DASH["Screen.Dashboard: Live Telemetry & Next Dose"]
    DASH <--> SCHEDULE["Screen.Schedule: Prescription Schedules"]
    DASH <--> MEDS["Screen.Medicines: Medication Inventory"]
    DASH <--> HISTORY["Screen.History: Intake History & Load Cell Logs"]
    DASH <--> ALERTS["Screen.Alerts: Missed-Dose Alerts"]
    DASH <--> SETTINGS["Screen.Settings: Calibration & Preferences"]

    class AUTH,DASH,SCHEDULE,MEDS,HISTORY,SETTINGS app;
    class ALERTS alert;
```

---

### Real-Time Telemetry Data Flow

```mermaid
flowchart TD
    classDef device fill:#FFE0B2,stroke:#E65100,stroke-width:2px,color:#BF360C;
    classDef cloud fill:#BBDEFB,stroke:#1976D2,stroke-width:2px,color:#0D47A1;
    classDef app fill:#C8E6C9,stroke:#388E3C,stroke-width:2px,color:#1B5E20;
    classDef alert fill:#FFCDD2,stroke:#D32F2F,stroke-width:2px,color:#B71C1C;

    SENSORS["ESP32 Sensors: HX711, DHT22, Reed"] -->|"JSON Over HTTPS"| FS_DOC["Firestore: devices/id/telemetry"]
    FS_DOC -->|"Snapshot Listener Flow"| REMOTE_DS["FirestoreDataSource"]
    REMOTE_DS -->|"TelemetryDto"| MAPPER["TelemetryMapper"]
    MAPPER -->|"Domain Telemetry"| REPO["TelemetryRepositoryImpl"]
    REPO -->|"Cache Snapshot"| ROOM_DAO["Room TelemetryDao"]
    REPO -->|"Emit Flow"| USECASE["ObserveLiveTelemetryUseCase"]
    USECASE -->|"Flow Telemetry"| VM["DashboardViewModel"]
    VM -->|"DashboardUiState"| SCREEN["DashboardScreen Recompose"]

    class SENSORS device;
    class FS_DOC cloud;
    class REMOTE_DS,MAPPER,REPO,ROOM_DAO,USECASE,VM,SCREEN app;
```

---

### Git Branching & Release Workflow

```mermaid
flowchart TD
    classDef device fill:#FFE0B2,stroke:#E65100,stroke-width:2px,color:#BF360C;
    classDef cloud fill:#BBDEFB,stroke:#1976D2,stroke-width:2px,color:#0D47A1;
    classDef app fill:#C8E6C9,stroke:#388E3C,stroke-width:2px,color:#1B5E20;
    classDef alert fill:#FFCDD2,stroke:#D32F2F,stroke-width:2px,color:#B71C1C;

    DEV["develop: Integration Branch"] -->|"Branch out"| FEAT["feature/*: Granular Phase Work"]
    FEAT -->|"PR and Review"| DEV
    DEV -->|"Release Cut"| REL["release/vX.Y.Z"]
    REL -->|"Tested and Tagged"| MAIN["main: Stable Production"]
    MAIN -->|"Tag"| TAG["vX.Y.Z Release Tag"]
    MAIN -->|"Urgent Patch"| HOTFIX["hotfix/*"]
    HOTFIX -->|"Backport Fix"| MAIN
    HOTFIX -->|"Backport Fix"| DEV

    class MAIN,TAG device;
    class DEV,REL cloud;
    class FEAT app;
    class HOTFIX alert;
```

---

### Project Roadmap by Phase

```mermaid
flowchart TD
    classDef device fill:#FFE0B2,stroke:#E65100,stroke-width:2px,color:#BF360C;
    classDef cloud fill:#BBDEFB,stroke:#1976D2,stroke-width:2px,color:#0D47A1;
    classDef app fill:#C8E6C9,stroke:#388E3C,stroke-width:2px,color:#1B5E20;
    classDef alert fill:#FFCDD2,stroke:#D32F2F,stroke-width:2px,color:#B71C1C;

    P1["Phase 1: Repository Setup and Architecture Standards"] --> P2["Phase 2: Design Tokens and Stitch UI System"]
    P2 --> P3["Phase 3: Domain and Offline-First Data Layer"]
    P3 --> P4["Phase 4: Firebase Sync and WorkManager Jobs"]
    P4 --> P5["Phase 5: Presentation Layer, Compose Screens and E2E Tests"]

    class P1 app;
    class P2 device;
    class P3 app;
    class P4 cloud;
    class P5 alert;
```

---

### Tech Stack

- **Core & UI**: Kotlin 2.1.0, Jetpack Compose, Material 3, Navigation Compose.
- **Architecture & DI**: MVVM, Clean Architecture (UDF), Dagger Hilt, Coroutines & StateFlow.
- **Data & Backend**: Room Database, Preferences DataStore, Cloud Firestore, Firebase Auth & FCM.
- **Build & Quality**: Gradle Kotlin DSL with Version Catalog (`libs.versions.toml`), WorkManager.

---

### Getting Started

- Open project in Android Studio (Ladybug or newer) with JDK 17+.
- Add `google-services.json` to the `app/` directory (obtain from Firebase console).
- Configure Stitch MCP server in Antigravity or provide API credentials for design tokens.
- Run `./gradlew test` for unit tests or `./gradlew assembleDebug` to build the APK.
