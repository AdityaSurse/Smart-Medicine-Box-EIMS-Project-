# Data Contract & Firestore Schema Specification: Smart Medicine Box

> **Version**: 2.0.0-PROPOSAL (Transition to Pure-Lid Adherence & NTP Internal Clock)  
> **Status**: **PENDING USER APPROVAL (Step 0 Audit & Contract Proposal)**  
> **Target Platforms**: ESP32 Firmware (FreeRTOS / C++ / Arduino), Android Client (Jetpack Compose / Kotlin Clean Architecture), Cloud Firestore  
> **Security Model**: User-scoped document partitioning (`users/{uid}/...`) with strict role-based Firestore Rules.

---

## 0. Amendment Proposal: Removal of Load Cell & Hardware Streamlining

### 0.1 Context & Hardware Evolution
The initial prototype specification included an HX711 4-wire strain-gauge load cell and an external DS3231 I2C real-time clock. In the production hardware design (ESP32 DevKit 38-pin), the hardware bill of materials (BOM) has been streamlined:
- **Removed**: HX711 load cell and DS3231 hardware RTC.
- **Retained & Active Hardware**:
  - **Magnetic Reed Switch** (`GPIO 27`, `INPUT_PULLUP`): Senses box lid opening and closure. Magnet near = lid CLOSED (reading LOW with default polarity); lid OPEN = reading HIGH.
  - **Chamber Environmental Sensor** (`DHT22` on `GPIO 14`): Measures ambient temperature (-40°C to 80°C) and relative humidity (0% to 100%).
  - **Audio Annunciator** (`Active Buzzer` on `GPIO 26`, HIGH = on): Emits frequency-fixed cadences for reminders, alerts, and chirps.
  - **Color Display** (`1.8" SPI TFT ST7735`, 128x160 portrait): Displays 12-hour clock, next dose countdown, adherence status, and chamber vitals.
  - **ESP32 Internal Clock & NTP**: Time synchronization managed over NTP via multi-tier fallback servers (`pool.ntp.org`, `time.google.com`, `time.cloudflare.com`) with local timezone offset (default IST UTC+5:30) and 6-hour resync cadence.

---

### 0.2 Audit: Weight Dependencies Across the Full Stack

A comprehensive audit of the repository identified the following touchpoints reliant on load-cell weight:

| Layer / File | Component / Field | Role in Legacy Architecture | Proposed Minimal Resolution |
| :--- | :--- | :--- | :--- |
| **Firestore Contract** | `telemetry/latest.weightGrams` | Monitored current total weight (g) of medication tray | Retain field as optional. Firmware reports `0.0` to preserve backwards compatibility with existing `firestore.rules` validation (`data.weightGrams >= 0.0`). |
| **Firestore Contract** | `events/{eventId}.eventType = "WEIGHT_DROP"` | Discrete event fired when weight reduction was detected | Superseded by debounced lid open/close detection during active dose window. Firmware emits `BOX_OPENED` / `BOX_CLOSED` and `DOSE_TAKEN` / `MISSED_DOSE`. |
| **Firestore Contract** | `events/{eventId}.deltaWeightGrams`, `currentWeightGrams` | Tracked gram delta during pill extraction | Retain fields in schema with default `0.0`. |
| **Firestore Contract** | `medicines/{id}.estimatedUnitWeightGrams` | Used to compute expected weight delta per pill | Retained as optional metadata or legacy tare calculation; ignored by adherence logic. |
| **Firestore Contract** | `doseLogs/{id}.recordedWeightGrams` | Recorded gram drop when dose was logged | Retained as nullable (`null` or `0.0`). |
| **Domain Engine** | `DoseEvaluator.kt` | Enforced rule: `hasBoxOpen && hasWeightDrop -> TAKEN`; `hasBoxOpen && !hasWeightDrop -> MISSED/PENDING` | Update rule: `hasBoxOpen` within `[windowStart, windowEnd]` resolves directly to `TAKEN`. `WEIGHT_DROP` check bypassed. |
| **Domain Model** | `DeviceTelemetry.weightGrams`, `DeviceStatus.weightGrams` | Exposed weight reading to UI state | Retain field with fallback `0.0f` to avoid breaking downstream composables. |
| **Android UI** | `DashboardScreen.kt` | Metric card labeled "Load Cell" displaying `tabletsRemaining pills` and `Gross: 42.6g` | Re-label card to "Pill Supply" / "Stock Level"; remove gross weight subtext; display `tabletsRemaining`. |
| **Inventory / Stock** | `Medicine.currentStock` & `totalStock` | Tracked physical pill inventory in app | Pill count is already decremented in `LogDoseUseCase` when dose status transitions to `TAKEN`. Stock count is maintained as a pure inventory ledger. |
| **Security Rules** | `firestore.rules` `isValidTelemetry()` | Enforced `data.weightGrams is number && data.weightGrams >= 0.0` | Retain `weightGrams: 0.0` from firmware to satisfy existing rule without breaking deployments. |

---

### 0.3 Proposed Minimal Contract & Adherence Rules

1. **Adherence Definition (TAKEN)**:
   - A scheduled dose opens at its programmed wall-clock time (`scheduledTime`).
   - The active dose window extends from `scheduledTime` until `scheduledTime + gracePeriodMinutes` (default 30 minutes, configurable per schedule).
   - A dose is marked **`TAKEN`** when the lid is opened and closed during the active window, with a debounced open duration of at least **2.0 seconds** (preventing accidental knocks or bumps from registering).
   - Once marked `TAKEN`, visual cues switch to a confirming status, audible reminder beeps cease immediately, and an acknowledgement chirp sounds.
2. **Adherence Definition (MISSED)**:
   - If the active dose window expires (`now > scheduledTime + gracePeriodMinutes`) without a valid debounced lid opening, the dose is marked **`MISSED`**.
   - The device logs a missed dose event (`eventType = "MISSED_DOSE"` or event document with `status = "MISSED"`), triggering the server-side Cloud Function (`functions/index.js`) to dispatch an immediate high-priority FCM push notification to the patient/caregiver.
   - The TFT switches to the vermilion missed banner and sounds the missed buzzer pattern.
3. **Out-of-Window Lid Activity**:
   - Opening the lid outside any active dose window does not alter any prescription or dose schedule status. It is logged locally and emitted as a `BOX_OPENED` / `BOX_CLOSED` telemetry audit event.
4. **Pills-Left / Stock Management**:
   - Pill inventory is tracked via `currentStock` and `totalStock` in `users/{uid}/medicines/{medicineId}`.
   - When a dose transitions to `TAKEN` (whether initiated by the physical box or via the "mark taken" command in the app), `currentStock` is decremented by 1.
   - Low-stock warning thresholds (`lowStockThreshold`, default 5) trigger notifications independently of load cell weight.

---

## 1. System Architecture & End-to-End Flowchart

```mermaid
flowchart TD
    subgraph ESP32["ESP32 Microcontroller (Firmware)"]
        Sensors["Peripherals: DHT22 (GPIO 14), Reed Switch (GPIO 27)"]
        TimeSync["NTP Time Engine (UTC+5:30 / Multi-Server)"]
        DoseEngine["Pure DoseEngine (State Machine & Local NVS Cache)"]
        Alerts["Buzzer (GPIO 26) & ST7735 1.8' TFT (SPI)"]
        WiFi_Client["Firestore REST API over HTTPS (WiFiClientSecure)"]

        Sensors --> DoseEngine
        TimeSync --> DoseEngine
        DoseEngine --> Alerts
        DoseEngine --> WiFi_Client
    end

    subgraph Firebase["Firebase Cloud Services"]
        FB_Auth["Firebase Auth (Device Identity / Token)"]
        FS_Telemetry["users/{uid}/devices/{deviceId}/telemetry/latest"]
        FS_Events["users/{uid}/devices/{deviceId}/events/{eventId}"]
        FS_Medicines["users/{uid}/medicines/{medId}"]
        FS_Schedules["users/{uid}/schedules/{scheduleId}"]
        FS_DoseLogs["users/{uid}/doseLogs/{logId}"]
        FS_Commands["users/{uid}/devices/{deviceId}/commands/{cmdId}"]
        Functions["Cloud Functions (Missed Dose & Env FCM Triggers)"]
    end

    subgraph Android["Android Application (Offline-First)"]
        Remote_DS["Firestore Remote DataSources (Snapshot Flow)"]
        Sync_Engine["Bi-directional Sync Manager"]
        Room_DB[("Room Local Database (SSOT)")]
        DataStore_Pref[("Jetpack DataStore Preferences")]
        Domain_UC["Domain Use Cases (DoseEvaluator & LogDoseUseCase)"]
        UI_Screens["Jetpack Compose UI (M3 Apothecary Theme)"]
        
        Remote_DS <--> Sync_Engine
        Sync_Engine <--> Room_DB
        Room_DB --> Domain_UC
        DataStore_Pref --> Domain_UC
        Domain_UC --> UI_Screens
    end

    WiFi_Client -- "1. Write Telemetry (15s heartbeat + on state change)" --> FS_Telemetry
    WiFi_Client -- "2. Write Hardware & Dose Events" --> FS_Events
    WiFi_Client -- "3. Write Resolved Dose Logs" --> FS_DoseLogs
    WiFi_Client <-- "4. Poll Remote Commands (SILENCE_BUZZER, MARK_TAKEN)" -- FS_Commands
    WiFi_Client <-- "5. Pull Prescription & Regimen Schedules" -- FS_Schedules

    FS_Events --> Functions
    FS_Telemetry --> Functions
    Functions -. "Push FCM Alert" .-> UI_Screens

    Android -- "Authenticate" --> FB_Auth
    FS_Telemetry -. "Snapshot Flow" .-> Remote_DS
    FS_Events -. "Snapshot Flow" .-> Remote_DS
    FS_Medicines <--> Remote_DS
    FS_Schedules <--> Remote_DS
    FS_DoseLogs <--> Remote_DS
    UI_Screens -- "Dispatch Remote Commands" --> FS_Commands
```

---

## 2. Firestore Document Paths & Schemas

### 2.1 Device Metadata
- **Path**: `users/{uid}/devices/{deviceId}`
- **Purpose**: Tracks paired hardware box specifications, registration date, network address, and pairing status.

| Field Name | Type | Constraints / Format | Description |
| :--- | :--- | :--- | :--- |
| `deviceId` | `string` | e.g. `"DG-4081"` | Hardware Serial Number / Unique Identifier |
| `modelName` | `string` | e.g. `"Smart Medicine Box"` | Commercial product model string |
| `serialNumber` | `string` | e.g. `"#DG-4081"` | Manufacturer production serial |
| `firmwareVersion`| `string` | e.g. `"v2.0.0"` | Embedded firmware build tag |
| `hardwareRev` | `string` | e.g. `"ESP32-38Pin-TFT"` | PCB / circuit revision identifier |
| `macAddress` | `string` | `XX:XX:XX:XX:XX:XX` | ESP32 Wi-Fi MAC address |
| `baysCount` | `integer`| Min: `1`, Max: `8` (Default `4`) | Number of monitored physical compartments |
| `isOnline` | `boolean`| `true` / `false` | Heartbeat presence flag |
| `batteryPercent`| `integer`| Range: `0` to `100` | Battery voltage percentage (or 100 on USB 5V) |
| `wifiSsid` | `string` | e.g. `"Home_WiFi_2G"` | Connected wireless access point |
| `ipAddress` | `string` | IPv4 format | Local LAN IP for debugging / mDNS |
| `lastSeen` | `timestamp`| Firestore server timestamp | Heartbeat timestamp |
| `pairedAt` | `timestamp`| Client/server timestamp | Registration / pairing time |
| `pairingToken` | `string`| e.g. `"8820"` | 4-6 digit proof-of-possession pairing token |

---

### 2.2 Live Telemetry (`latest`)
- **Path**: `users/{uid}/devices/{deviceId}/telemetry/latest`
- **Purpose**: Fixed singleton document overwritten by the ESP32 every 15 seconds or immediately upon state changes.

| Field Name | Type | Units / Range | Description |
| :--- | :--- | :--- | :--- |
| `temp` | `number` | Celsius (`-20.0` to `70.0`) | DHT22 chamber temperature |
| `humidity` | `number` | Percent (`0.0` to `100.0`) | DHT22 relative ambient humidity |
| `weightGrams`| `number` | Float (`0.0`) | Set to `0.0` (weight sensor omitted) |
| `boxOpen` | `boolean`| `true` (open), `false` (shut)| Magnetic reed switch state |
| `rtcTime` | `string` | ISO 8601 (`YYYY-MM-DDTHH:mm:ssZ`)| NTP-synced internal clock time |
| `ts` | `timestamp`| Firestore server timestamp | Epoch timestamp of telemetry write |
| `batteryPercent`| `integer`| `0` to `100` | Battery / USB 5V status |
| `wifiRssi` | `integer`| dBm (`-100` to `0`) | Wi-Fi RSSI signal strength |

**Example Document JSON**:
```json
{
  "temp": 24.2,
  "humidity": 48.5,
  "weightGrams": 0.0,
  "boxOpen": false,
  "rtcTime": "2026-11-04T10:15:30Z",
  "ts": "2026-11-04T10:15:30Z",
  "batteryPercent": 100,
  "wifiRssi": -58
}
```

---

### 2.3 Hardware & Adherence Event Stream
- **Path**: `users/{uid}/devices/{deviceId}/events/{eventId}`
- **Purpose**: Append-only log of physical interactions and adherence milestones.

| Field Name | Type | Valid Values | Description |
| :--- | :--- | :--- | :--- |
| `eventId` | `string` | e.g. `"evt_1729000120000"` | Unique event transaction ID |
| `eventType` | `string` | `BOX_OPENED`, `BOX_CLOSED`, `MISSED_DOSE`, `DOSE_TAKEN`, `DOSE_DISPENSED`, `ENV_ALERT`, `LOW_BATTERY` | Discrete hardware state change |
| `timestamp` | `timestamp`| Firestore server timestamp | Event occurrence timestamp |
| `rtcTime` | `string` | ISO 8601 string | NTP-synced internal clock reading |
| `deltaWeightGrams`| `number` | Float (`0.0`) | Zeroed (no load cell) |
| `currentWeightGrams`|`number`| Float (`0.0`) | Zeroed (no load cell) |
| `trayBay` | `string` | e.g. `"Bay 01"` | Associated compartment if applicable |
| `status` | `string` | `DUE`, `TAKEN`, `MISSED`, `PENDING` | Adherence status flag |
| `scheduleId` | `string` | Schedule ID reference | Reference to the triggering schedule |
| `medicineName` | `string` | e.g. `"Amoxicillin"` | Denormalized name for FCM push |
| `dosage` | `string` | e.g. `"500 mg"` | Denormalized dosage description |
| `details` | `string` | Human-readable note | Diagnostic details (e.g. `"Lid opened for 4.2s inside dose window"`) |

---

### 2.4 Remote Commands Collection
- **Path**: `users/{uid}/devices/{deviceId}/commands/{commandId}`
- **Purpose**: Remote commands dispatched from the Android app to the ESP32.

| Field Name | Type | Valid Values | Description |
| :--- | :--- | :--- | :--- |
| `commandId` | `string` | e.g. `"cmd_1729000120000"` | Unique command ID |
| `type` | `string` | `SILENCE_BUZZER`, `MARK_TAKEN`, `DISPENSE_DOSE` | Command action type |
| `status` | `string` | `PENDING`, `EXECUTED`, `EXPIRED` | Command execution lifecycle |
| `createdAt` | `timestamp`| Firestore server timestamp | Creation timestamp |
| `executedAt` | `timestamp`| Firestore server timestamp | ESP32 execution timestamp |
| `payload` | `map` | e.g. `{"scheduleId": "sch_amox"}` | Command arguments |

---

### 2.5 Medicines Collection (Read by Firmware)
- **Path**: `users/{uid}/medicines/{medicineId}`
- **Purpose**: Pharmaceutical inventory, dosages, and compartment assignments.

| Field Name | Type | Description |
| :--- | :--- | :--- |
| `id` | `string` | Unique medicine ID |
| `name` | `string` | Medicine name |
| `dosageDescription` | `string` | Dosage instructions |
| `strengthMg` | `integer` | Active ingredient strength (mg) |
| `formFactor` | `string` | Capsule, Tablet, Coated Pill, Softgel |
| `compartmentIndex` | `integer` | Compartment tray index (1 to 4) |
| `compartmentLabel` | `string` | UI label (e.g. `"Bay 01"`) |
| `currentStock` | `integer` | Current remaining pill count |
| `totalStock` | `integer` | Container total supply capacity |
| `lowStockThreshold`| `integer` | Warning trigger limit |
| `isActive` | `boolean` | Course active toggle |

---

### 2.6 Dose Schedules Collection (Read by Firmware)
- **Path**: `users/{uid}/schedules/{scheduleId}`
- **Purpose**: Prescription alarms, grace periods, and active recurrence days.

| Field Name | Type | Description |
| :--- | :--- | :--- |
| `id` | `string` | Unique schedule ID |
| `medicineId` | `string` | Foreign key referencing medicine |
| `medicineName` | `string` | Denormalized medicine name |
| `times` | `array<string>` | Alarm times (e.g. `["08:00 AM", "08:00 PM"]`) |
| `daysOfWeek` | `array<integer>` | Days active (1 = Monday, ..., 7 = Sunday) |
| `gracePeriodMinutes` | `integer` | Window duration before marking MISSED (default `30`) |
| `compartmentBay` | `string` | Bay label (e.g. `"Bay 01"`) |
| `isActive` | `boolean` | Schedule toggle switch |

---

### 2.7 Dose Intake Logs Collection (Written by Firmware / App)
- **Path**: `users/{uid}/doseLogs/{logId}`
- **Purpose**: Persistent audit log of every resolved dose instance.

| Field Name | Type | Description |
| :--- | :--- | :--- |
| `id` | `string` | Unique log entry ID |
| `scheduleId` | `string` | Associated schedule ID |
| `medicineId` | `string` | Associated medicine ID |
| `medicineName` | `string` | Medicine title |
| `dosage` | `string` | Dose instructions |
| `scheduledTimeEpochMillis` | `integer` | Scheduled target timestamp (ms) |
| `status` | `string` | Status: `TAKEN`, `PENDING`, `MISSED` |
| `loggedTimeEpochMillis` | `integer?` | Actual intake timestamp (ms) |
| `bayLabel` | `string` | Associated tray (e.g. `"Bay 01"`) |
| `source` | `string` | `DEVICE_SENSOR` (lid switch) or `MANUAL_APP` |
| `updatedAt` | `timestamp` | Sync modification timestamp |

---

## 3. Firmware HTTP/REST Communication Architecture

### 3.1 Library Choice: Direct Firestore REST API over `HTTPClient`
- **Selected**: Direct Google Cloud Firestore REST API over standard ESP32 `HTTPClient` + `WiFiClientSecure` using `ArduinoJson` (v7.x).
- **Justification**:
  - **Flash Footprint**: Mobizt Firebase ESP Client occupies ~1.2 MB to 1.5 MB flash, often requiring non-standard partition schemes (`huge_app`) and risking OOM with ST7735 graphics buffers. Direct REST API uses under 450 KB flash, fitting comfortably within the default 1.3 MB app partition (`default.csv`).
  - **RAM Overhead**: Direct REST API requires zero persistent WebSocket or streaming JSON heap allocations. RAM footprint is deterministic and predictable (<25 KB dynamic heap).
  - **Parity with Test Harness**: Exactly mirrors the Python device simulator (`tools/device_simulator.py`), which successfully operates against the Firestore REST API.
