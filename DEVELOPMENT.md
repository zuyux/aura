## AURA Android MVP 0.1 — Development Task List

### 1. Project setup

* [X] Create GitHub repo: `aura-android`
* [X] Initialize Android project with **Kotlin**
* [X] Use **Jetpack Compose**
* [X] Configure **Material 3**
* [X] Configure Gradle Kotlin DSL
* [X] Set package name, e.g. `io.aura.android`
* [X] Add base app theme: blue / white / red
* [X] Add app icon draft using the minimal shield logo
* [X] Add `README.md`
* [X] Add `ARCHITECTURE.md`
* [X] Add `LICENSE`
* [X] Add `.gitignore`
* [X] Add basic CI with GitHub Actions

---

### 2. Core dependencies

* [X] Add Jetpack Compose
* [X] Add Navigation Compose
* [X] Add Room
* [X] Add WorkManager
* [X] Add Hilt
* [X] Add Kotlin Coroutines
* [X] Add Kotlin Flow
* [X] Add DataStore
* [X] Add Retrofit or Ktor Client
* [X] Add Coil for images
* [X] Add Maps SDK or MapLibre
* [X] Add Kotlinx Serialization
* [X] Add testing dependencies: JUnit, MockK, Turbine, Compose UI Test

---

### 3. Base architecture

* [X] Create folder structure:

```txt
core/
data/
domain/
feature/
worker/
```

* [X] Create base `AuraApp.kt`
* [X] Create `MainActivity.kt`
* [X] Configure Hilt application class
* [X] Configure Navigation graph
* [X] Configure bottom navigation
* [ ] Define base UI components:

  * [X] Primary button
  * [X] Emergency button
  * [X] Alert card
  * [X] Category card
  * [X] Status badge
  * [X] Section header
  * [X] Loading state
  * [X] Empty state
  * [X] Offline banner

---

### 4. Design system

* [X] Define color palette:

```txt
Primary Blue
Dark Blue
Alert Red
Safe Green
Background White
Light Gray
Muted Text
```

* [X] Define typography
* [X] Define spacing scale
* [X] Define rounded card style
* [ ] Define icon style
* [ ] Create light theme
* [ ] Create dark theme later
* [ ] Add AURA shield logo assets:

  * [ ] `ic_aura_logo.svg`
  * [ ] `ic_aura_logo_monochrome.svg`
  * [ ] launcher icon

---

### 5. Domain models

* [X] Create `UserProfile`
* [X] Create `DeviceIdentity`
* [X] Create `IncidentReport`
* [X] Create `IncidentEvidence`
* [X] Create `Alert`
* [X] Create `ReportVerification`
* [X] Create `GuardianContact`
* [X] Create `SafetySession`
* [X] Create `SafetySessionUpdate`
* [X] Create `SyncQueueItem`

---

### 6. Enums

* [X] Create `IncidentType`
* [X] Create `SeverityLevel`
* [X] Create `ReportStatus`
* [X] Create `LocationPrecision`
* [X] Create `ReportVisibility`
* [X] Create `EvidenceType`
* [X] Create `EvidenceVisibility`
* [X] Create `VerificationAction`
* [X] Create `AlertStatus`
* [X] Create `SafetySessionStatus`
* [X] Create `SyncOperation`
* [X] Create `SyncPriority`
* [X] Create `SyncStatus`

---

### 7. Local database

* [X] Add Room database: `AuraDatabase`

* [X] Create entities:

  * [X] `UserProfileEntity`
  * [X] `DeviceIdentityEntity`
  * [X] `IncidentReportEntity`
  * [X] `IncidentEvidenceEntity`
  * [X] `AlertEntity`
  * [X] `ReportVerificationEntity`
  * [X] `GuardianContactEntity`
  * [X] `SafetySessionEntity`
  * [X] `SafetySessionUpdateEntity`
  * [X] `SyncQueueEntity`

* [X] Create DAOs:

  * [X] `IncidentReportDao`
  * [X] `IncidentEvidenceDao`
  * [X] `AlertDao`
  * [X] `ReportVerificationDao`
  * [X] `GuardianContactDao`
  * [X] `SafetySessionDao`
  * [X] `SyncQueueDao`

* [X] Add mappers:

  * [X] Entity → Domain
  * [X] Domain → Entity
  * [X] Domain → DTO
  * [X] DTO → Domain

---

### 8. Feature: Home

* [X] Create `HomeScreen`
* [X] Create `HomeViewModel`
* [X] Show AURA logo
* [X] Show greeting
* [X] Add large SOS button
* [X] Add shortcut to “Reportar incidente”
* [X] Add shortcut to “Alertas cercanas”
* [X] Add shortcut to “Red Guardián”
* [X] Add bottom navigation
* [X] Show offline status banner when needed

---

### 9. Feature: Reportar incidente

* [X] Create `ReportIncidentScreen`

* [X] Create `ReportIncidentViewModel`

* [X] Add incident category selection:

  * [X] Robo
  * [X] Intento de robo
  * [X] Persona sospechosa
  * [X] Violencia
  * [X] Acoso
  * [X] Accidente
  * [X] Zona peligrosa
  * [X] Otro

* [X] Add severity selection

* [X] Add location confirmation

* [X] Add approximate location option

* [X] Add description field

* [X] Add anonymous mode toggle

* [X] Add submit button

* [X] Save report locally as `DRAFT`

* [X] Submit report as `PENDING_SYNC`

* [X] Add validation rules

---

### 10. Feature: Evidence

* [X] Create `AddEvidenceScreen`
* [X] Add photo attachment
* [X] Add video attachment
* [X] Add audio attachment
* [X] Save evidence locally
* [X] Generate SHA-256 hash
* [X] Strip EXIF metadata from images
* [X] Mark evidence as private by default
* [X] Attach evidence to report
* [X] Queue evidence upload after report sync

---

### 11. Feature: Alertas cercanas

* [X] Create `AlertsMapScreen`

* [X] Create `AlertsListScreen`

* [X] Create `AlertDetailScreen`

* [X] Create `AlertsViewModel`

* [X] Load cached alerts from Room

* [X] Fetch nearby alerts when online

* [X] Configure production alerts API base URL

* [X] Display alert cards

* [X] Display alert status:

  * [X] No verificado
  * [X] Confirmado
  * [X] Resuelto

* [X] Add filters:

  * [X] Todas
  * [X] Robo
  * [X] Violencia
  * [X] Acoso
  * [X] Accidente

* [X] Add map pins or zones

* [X] Add alert detail page

* [X] Add verification actions:

  * [X] “Yo también lo vi”
  * [X] “Parece falso”
  * [X] “Está resuelto”

---

### 12. Feature: Red Guardián

* [X] Create `GuardianScreen`
* [X] Create `GuardianViewModel`
* [X] Add trusted contacts locally
* [X] Start safety session
* [X] Save `SafetySession`
* [X] Track location during active session
* [X] Show “Compartiendo ubicación”
* [X] Add “Estoy bien” button
* [X] Add “Llamar” button
* [X] Add “Finalizar sesión”
* [X] Add SMS fallback draft
* [X] Stop location updates when session ends

---

### 13. Location module

* [X] Create `LocationProvider`
* [X] Create `LocationPermissionManager`
* [X] Create `LocationPrivacyService`
* [X] Create `GeohashService`
* [X] Request precise or approximate location contextually
* [X] Convert exact location to approximate zone for public reports
* [X] Store last known location
* [X] Handle location unavailable state

---

### 14. Offline-first sync

* [X] Create `SyncQueueManager`

* [X] Create `ReportSyncWorker`

* [X] Create `EvidenceUploadWorker`

* [X] Create `AlertFetchWorker`

* [X] Create `SafetySessionWorker`

* [X] Add retry with exponential backoff

* [X] Add sync priority:

  * [X] Critical: SOS / Red Guardián
  * [X] High: incident reports
  * [X] Normal: verification actions
  * [X] Low: evidence upload

* [X] Show pending sync state in UI

* [X] Retry sync when connection returns

---

### 15. Network layer

* [X] Define API client
* [X] Define DTOs
* [X] Add endpoints:

```txt
POST /reports
GET /reports/nearby
GET /reports/{id}
POST /reports/{id}/evidence
POST /reports/{id}/verifications
GET /alerts/nearby
POST /safety-sessions
PATCH /safety-sessions/{id}
POST /safety-sessions/{id}/updates
```

* [X] Add error handling
* [X] Add timeout handling
* [X] Add offline detection

---

### 16. Privacy and security

* [X] Store sensitive preferences in DataStore
* [X] Use Android Keystore for local keys
* [X] Avoid logging sensitive data
* [X] Strip image metadata
* [X] Hash evidence files
* [X] Keep evidence private by default
* [X] Avoid exposing exact public location
* [X] Add delete local evidence option
* [X] Add privacy disclaimer screen
* [X] Add permission rationale dialogs

---

### 17. Profile and settings

* [X] Create `ProfileScreen`
* [X] Add first-run phone onboarding
* [X] Add SMS code autofill permission flow
* [X] Add local display name
* [X] Add anonymous mode default toggle
* [X] Add offline mode setting
* [X] Add notification preferences
* [X] Add emergency contacts entry point
* [ ] Add theme selector
* [X] Add app version
* [X] Add privacy policy link placeholder

---

### 18. Notifications

* [X] Add local notification for active Red Guardián session
* [X] Add notification for pending sync
* [X] Add notification for nearby alerts
* [X] Add notification channel setup
* [X] Prepare Firebase Cloud Messaging integration for later

---

### 19. Testing

* [X] Unit test domain use cases
* [X] Unit test mappers
* [X] Unit test location privacy logic
* [X] Unit test sync queue logic
* [X] Room DAO tests
* [X] Worker tests
* [X] Compose UI tests for:

  * [X] Home
  * [X] Report incident flow
  * [X] Alerts list
  * [X] Alert detail
  * [X] Red Guardián session
  * [X] Profile settings

---

### 20. Documentation

* [ ] Update `README.md`
* [ ] Add `ARCHITECTURE.md`
* [ ] Add `DATA_MODEL.md`
* [ ] Add `OFFLINE_MODE.md`
* [ ] Add `PRIVACY.md`
* [ ] Add `ROADMAP.md`
* [ ] Add screenshots/mockups
* [ ] Add contribution guide
* [ ] Add issue templates

---

Best first milestone:

> User can open AURA, create a local incident report, see it saved as pending sync, and start a basic Red Guardián session.
