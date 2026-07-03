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
* [ ] Add Maps SDK or MapLibre
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

* [ ] Define color palette:

```txt
Primary Blue
Dark Blue
Alert Red
Safe Green
Background White
Light Gray
Muted Text
```

* [ ] Define typography
* [ ] Define spacing scale
* [ ] Define rounded card style
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

* [ ] Create entities:

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

* [ ] Create DAOs:

  * [X] `IncidentReportDao`
  * [X] `IncidentEvidenceDao`
  * [X] `AlertDao`
  * [X] `ReportVerificationDao`
  * [X] `GuardianContactDao`
  * [X] `SafetySessionDao`
  * [X] `SyncQueueDao`

* [ ] Add mappers:

  * [X] Entity → Domain
  * [X] Domain → Entity
  * [ ] Domain → DTO
  * [ ] DTO → Domain

---

### 8. Feature: Home

* [ ] Create `HomeScreen`
* [ ] Create `HomeViewModel`
* [ ] Show AURA logo
* [ ] Show greeting
* [ ] Add large SOS button
* [ ] Add shortcut to “Reportar incidente”
* [ ] Add shortcut to “Alertas cercanas”
* [ ] Add shortcut to “Red Guardián”
* [ ] Add bottom navigation
* [ ] Show offline status banner when needed

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

* [ ] Create `AddEvidenceScreen`
* [ ] Add photo attachment
* [ ] Add video attachment
* [ ] Add audio attachment
* [ ] Save evidence locally
* [ ] Generate SHA-256 hash
* [ ] Strip EXIF metadata from images
* [ ] Mark evidence as private by default
* [ ] Attach evidence to report
* [ ] Queue evidence upload after report sync

---

### 11. Feature: Alertas cercanas

* [ ] Create `AlertsMapScreen`

* [ ] Create `AlertsListScreen`

* [ ] Create `AlertDetailScreen`

* [ ] Create `AlertsViewModel`

* [ ] Load cached alerts from Room

* [ ] Fetch nearby alerts when online

* [ ] Display alert cards

* [ ] Display alert status:

  * [ ] No verificado
  * [ ] Confirmado
  * [ ] Resuelto

* [ ] Add filters:

  * [ ] Todas
  * [ ] Robo
  * [ ] Violencia
  * [ ] Acoso
  * [ ] Accidente

* [ ] Add map pins or zones

* [ ] Add alert detail page

* [ ] Add verification actions:

  * [ ] “Yo también lo vi”
  * [ ] “Parece falso”
  * [ ] “Está resuelto”

---

### 12. Feature: Red Guardián

* [ ] Create `GuardianHomeScreen`
* [ ] Create `GuardianContactsScreen`
* [ ] Create `ActiveSafetySessionScreen`
* [ ] Create `GuardianViewModel`
* [ ] Add trusted contacts locally
* [ ] Start safety session
* [ ] Save `SafetySession`
* [ ] Track location during active session
* [ ] Show “Compartiendo ubicación”
* [ ] Add “Estoy bien” button
* [ ] Add “Llamar” button
* [ ] Add “Finalizar sesión”
* [ ] Add SMS fallback draft
* [ ] Stop location updates when session ends

---

### 13. Location module

* [X] Create `LocationProvider`
* [ ] Create `LocationPermissionManager`
* [X] Create `LocationPrivacyService`
* [ ] Create `GeohashService`
* [X] Request precise or approximate location contextually
* [X] Convert exact location to approximate zone for public reports
* [ ] Store last known location
* [X] Handle location unavailable state

---

### 14. Offline-first sync

* [ ] Create `SyncQueueManager`

* [ ] Create `ReportSyncWorker`

* [ ] Create `EvidenceUploadWorker`

* [ ] Create `AlertFetchWorker`

* [ ] Create `SafetySessionWorker`

* [ ] Add retry with exponential backoff

* [ ] Add sync priority:

  * [ ] Critical: SOS / Red Guardián
  * [ ] High: incident reports
  * [ ] Normal: verification actions
  * [ ] Low: evidence upload

* [ ] Show pending sync state in UI

* [ ] Retry sync when connection returns

---

### 15. Network layer

* [ ] Define API client
* [ ] Define DTOs
* [ ] Add endpoints:

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

* [ ] Add error handling
* [ ] Add timeout handling
* [ ] Add offline detection

---

### 16. Privacy and security

* [ ] Store sensitive preferences in DataStore
* [ ] Use Android Keystore for local keys
* [ ] Avoid logging sensitive data
* [ ] Strip image metadata
* [ ] Hash evidence files
* [ ] Keep evidence private by default
* [ ] Avoid exposing exact public location
* [ ] Add delete local evidence option
* [ ] Add privacy disclaimer screen
* [X] Add permission rationale dialogs

---

### 17. Profile and settings

* [ ] Create `ProfileScreen`
* [ ] Add local alias
* [ ] Add anonymous mode default toggle
* [ ] Add offline mode setting
* [ ] Add notification preferences
* [ ] Add emergency contacts entry point
* [ ] Add theme selector
* [ ] Add app version
* [ ] Add privacy policy link placeholder

---

### 18. Notifications

* [ ] Add local notification for active Red Guardián session
* [ ] Add notification for pending sync
* [ ] Add notification for nearby alerts
* [ ] Add notification channel setup
* [ ] Prepare Firebase Cloud Messaging integration for later

---

### 19. Testing

* [ ] Unit test domain use cases
* [ ] Unit test mappers
* [ ] Unit test location privacy logic
* [ ] Unit test sync queue logic
* [ ] Room DAO tests
* [ ] Worker tests
* [ ] Compose UI tests for:

  * [ ] Home
  * [ ] Report incident flow
  * [ ] Alerts list
  * [ ] Alert detail
  * [ ] Red Guardián session
  * [ ] Profile settings

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

## Suggested first sprint

### Sprint 1: Foundation

* [ ] Create Android project
* [ ] Add Compose + Material 3
* [ ] Add Navigation
* [ ] Add Hilt
* [ ] Add Room
* [ ] Build Home screen
* [ ] Build base theme
* [ ] Add logo assets
* [ ] Add initial README and architecture docs

### Sprint 2: Local reporting

* [ ] Build report incident UI
* [ ] Add domain models
* [ ] Add Room entities
* [ ] Save report locally
* [ ] Add pending sync queue
* [ ] Build evidence attachment placeholder

### Sprint 3: Alerts + Red Guardián

* [ ] Build alerts list
* [ ] Build alert detail
* [ ] Build Red Guardián screen
* [ ] Add trusted contacts
* [ ] Add safety session state
* [ ] Add offline banner

Best first milestone:

> User can open AURA, create a local incident report, see it saved as pending sync, and start a basic Red Guardián session.
