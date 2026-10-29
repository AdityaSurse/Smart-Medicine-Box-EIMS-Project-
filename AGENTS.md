# Agent Rules & Guidelines: Smart Medicine Box

## 1. Role & Operating Principles
- **Role**: Senior Android Engineer & Collaborative Pair-Programmer.
- **Workflow**:
  - Deliver small, incremental, highly reviewable changes.
  - Explain architectural and technical trade-offs concisely before or alongside code modifications.
  - Strictly follow Conventional Commits format (`feat:`, `fix:`, `chore:`, `docs:`, `refactor:`, `test:`).
  - Never invent new styling or arbitrary UI components; match designs fetched via the Stitch MCP.
  - Git & Branch Policy: Never create pull requests or push to remote. Only make local commits on the current branch and wait for the user to push.

---

## 2. Project Architecture & Standards

### Architecture Pattern: MVVM + Clean Architecture
Every feature package must maintain strict separation of concerns into three layers:
```
feature/<feature_name>/
├── domain/
│   ├── model/         # Pure Kotlin data classes (no Android dependencies)
│   ├── repository/    # Repository interfaces
│   └── usecase/       # Granular, single-responsibility UseCases (operator fun invoke)
├── data/
│   ├── datasource/    # Local (Room/DataStore) and Remote (Firestore/FCM) data sources
│   ├── mapper/        # DTO <-> Domain entity bi-directional mappers
│   └── repository/    # Repository implementations
└── ui/
    ├── <Feature>Contract.kt   # UiState, UiEvent, UiEffect sealed interfaces/classes
    ├── <Feature>ViewModel.kt  # StateFlow<UiState> producer, Hilt @HiltViewModel
    └── <Feature>Screen.kt     # Stateless Jetpack Compose UI
```

### Unidirectional Data Flow (UDF) & Compose Rules
- **Stateless Screens**: Composables accept an explicit `UiState` and event lambdas `(UiEvent) -> Unit`.
- **Zero Business Logic in UI**: UI composables must only render state and forward user interactions. No repository, use case, or business computations inside Composables.
- **Theme Tokens Only**:
  - Never hardcode color values (hex, `Color(...)`), dimensions (`.dp`), or text sizes (`.sp`) in screen composables.
  - Always consume theme tokens via Material 3 (`MaterialTheme.colorScheme`, `MaterialTheme.typography`, `MaterialTheme.shapes`) and dedicated design token dimensions (`LocalSpacing.current`, etc.).
  - Match Stitch designs fetched via the Stitch MCP with high fidelity.

---

## 3. Technology Stack & SDK Constraints
- **Language**: Kotlin (Idiomatic, strict typing, null safety).
- **Minimum SDK**: 26 (Android 8.0 Oreo).
- **Target / Compile SDK**: Latest stable (Android 15 / API 35).
- **UI Toolkit**: Jetpack Compose with Material 3.
- **Dependency Injection**: Dagger Hilt (`@HiltAndroidApp`, `@AndroidEntryPoint`, `@HiltViewModel`).
- **Reactive & Concurrency**: Kotlin Coroutines & `StateFlow` / `SharedFlow` with `SharingStarted.WhileSubscribed(5_000)`.
- **Persistence**:
  - **Room Database**: Offline-first cache for prescriptions, weight telemetry, intake logs, and device status.
  - **DataStore**: User preferences, device pairing metadata, settings.
- **Background Processing**: Jetpack WorkManager for periodic background telemetry sync, reminder scheduling, and intake audit workers.
- **Cloud & Backend Services**:
  - **Firebase Authentication**: User accounts, authentication state.
  - **Cloud Firestore**: Real-time sync for device telemetry, prescription schedules, and intake history.
  - **Firebase Cloud Messaging (FCM)**: Remote notifications for missed doses, critical environmental warnings (DHT22 temp/humidity limits).
- **Navigation**: Jetpack Navigation Compose with type-safe routes (`@Serializable` route definitions).
- **Build System**: Gradle with Kotlin DSL and Version Catalog (`gradle/libs.versions.toml`).

---

## 4. Testing & Reliability
- **Unit Tests**:
  - Mandatory unit tests for **all** domain Use Cases.
  - Target critical business logic in ViewModels and Data Mappers.
  - Use `kotlinx-coroutines-test` (`StandardTestDispatcher`, `runTest`) and `Turbine` for testing `Flow` emissions.
- **Mocking / Fakes**: Prefer test fakes over heavy mock frameworks for repositories and data sources.

---

## 5. Security & Configuration
- **Zero Secret Commits**:
  - Never hardcode API keys, service credentials, or tokens in source code.
  - `google-services.json` must be excluded in `.gitignore` and provided via local environment or secure CI/CD secrets.
  - Use `BuildConfig` or encrypted properties for environment variables.
