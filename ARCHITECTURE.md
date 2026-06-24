# AURA-ANDROID Architecture

**AURA-ANDROID** es una aplicación móvil Android desarrollada en **Kotlin** para seguridad ciudadana, reportes comunitarios, alertas geolocalizadas y sesiones privadas de emergencia mediante **Red Guardián**.

Este documento define la arquitectura técnica del **MVP 0.1**, sus módulos principales, flujo de datos, modelo offline-first, sincronización, seguridad, privacidad, estructura del repositorio y responsabilidades por capa.

---

## 1. Resumen del MVP 0.1

El MVP 0.1 de AURA se concentra en tres capacidades principales:

1. **Reportar incidentes**
2. **Ver alertas cercanas**
3. **Activar Red Guardián / SOS**

La app está pensada como una solución **local-first**, donde las acciones críticas pueden guardarse primero en el dispositivo y sincronizarse cuando exista conexión.

---

## 2. Objetivos de arquitectura

### Objetivos principales

* Permitir reportes rápidos y simples.
* Guardar datos localmente con Room.
* Sincronizar acciones pendientes con backend.
* Mostrar alertas cercanas por ubicación aproximada.
* Soportar modo offline y semi-offline.
* Proteger privacidad del usuario por defecto.
* Separar claramente UI, dominio y datos.
* Mantener una arquitectura modular y testeable.
* Preparar el proyecto para futuras integraciones con IA, moderación y autoridades.

### No objetivos del MVP 0.1

El MVP 0.1 no incluye:

* Chat público.
* Feed social.
* Reconocimiento facial.
* Identificación pública de sospechosos.
* Integración directa con sistemas policiales.
* Panel web administrativo.
* Blockchain.
* IA generativa en producción.
* Reputación avanzada.
* Moderación automática compleja.

---

## 3. Stack técnico

### Plataforma

```txt
Platform: Android
Language: Kotlin
UI: Jetpack Compose
Architecture: MVVM + Clean Architecture
Database: Room / SQLite
Background jobs: WorkManager
State: Kotlin Flow / StateFlow
DI: Hilt
Networking: Retrofit or Ktor Client
Persistence preferences: DataStore
Maps: Google Maps SDK or MapLibre
Image loading: Coil
Serialization: Kotlinx Serialization
Testing: JUnit, MockK, Turbine, Espresso / Compose UI Test
```

### Backend sugerido

Para MVP 0.1:

```txt
Backend: Supabase or custom API
Database: PostgreSQL + PostGIS
Storage: Supabase Storage or S3-compatible storage
Auth: Optional anonymous/device-based auth
Push: Firebase Cloud Messaging
```

---

## 4. Principio arquitectónico principal

AURA usa una arquitectura **offline-first / local-first**.

Esto significa que las acciones importantes se guardan primero en el dispositivo y luego se sincronizan con el servidor.

```txt
User Action
  ↓
Save locally in Room
  ↓
Create SyncQueue item
  ↓
WorkManager attempts sync
  ↓
Remote API receives data
  ↓
Local state updates as synced
```

Este modelo evita pérdida de información en escenarios de baja conectividad, algo crítico para una app de seguridad ciudadana.

---

## 5. Capas de arquitectura

AURA se divide en tres capas principales:

```txt
UI Layer
Domain Layer
Data Layer
```

---

## 6. UI Layer

La capa UI contiene pantallas, componentes visuales y ViewModels.

### Responsabilidades

* Renderizar pantallas con Jetpack Compose.
* Exponer estado mediante `StateFlow`.
* Enviar eventos de usuario al ViewModel.
* Mostrar errores, loading states y estados offline.
* No contener lógica de negocio compleja.
* No acceder directamente a Room ni a APIs remotas.

### Componentes

```txt
feature/home
feature/report
feature/alerts
feature/guardian
feature/profile
feature/onboarding
core/ui
```

### Ejemplo de patrón UI

```kotlin
data class ReportIncidentUiState(
    val selectedType: IncidentType? = null,
    val severity: SeverityLevel = SeverityLevel.MEDIUM,
    val description: String = "",
    val location: AuraLocation? = null,
    val evidence: List<LocalEvidenceDraft> = emptyList(),
    val isAnonymous: Boolean = true,
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null
)
```

```kotlin
sealed interface ReportIncidentEvent {
    data class SelectType(val type: IncidentType) : ReportIncidentEvent
    data class UpdateDescription(val value: String) : ReportIncidentEvent
    data class AddEvidence(val evidence: LocalEvidenceDraft) : ReportIncidentEvent
    data class ToggleAnonymous(val enabled: Boolean) : ReportIncidentEvent
    data object Submit : ReportIncidentEvent
}
```

---

## 7. Domain Layer

La capa de dominio contiene modelos, casos de uso y contratos de repositorio.

### Responsabilidades

* Definir reglas de negocio.
* Validar acciones.
* Coordinar operaciones entre repositorios.
* Mantener lógica independiente de Android cuando sea posible.
* Permitir testing unitario sin framework Android.

### Estructura

```txt
domain/
├── model/
├── repository/
└── usecase/
```

### Casos de uso principales

```txt
CreateIncidentReportUseCase
AttachEvidenceUseCase
GetNearbyAlertsUseCase
VerifyReportUseCase
StartSafetySessionUseCase
EndSafetySessionUseCase
ShareSafetyStatusUseCase
GetGuardianContactsUseCase
AddGuardianContactUseCase
SyncPendingItemsUseCase
```

### Ejemplo

```kotlin
class CreateIncidentReportUseCase(
    private val reportRepository: IncidentReportRepository,
    private val locationPrivacyService: LocationPrivacyService
) {
    suspend operator fun invoke(input: CreateIncidentReportInput): Result<IncidentReport> {
        val safeLocation = locationPrivacyService.applyPrecision(
            location = input.location,
            precision = input.locationPrecision
        )

        val report = IncidentReport.create(
            type = input.type,
            severity = input.severity,
            description = input.description,
            location = safeLocation,
            visibility = input.visibility,
            isAnonymous = input.isAnonymous
        )

        return reportRepository.createLocalReport(report)
    }
}
```

---

## 8. Data Layer

La capa de datos conecta la app con almacenamiento local, API remota, media storage y sistema de sincronización.

### Responsabilidades

* Persistir entidades en Room.
* Leer y escribir preferencias en DataStore.
* Ejecutar llamadas remotas.
* Mapear DTOs, entidades locales y modelos de dominio.
* Crear items en `SyncQueue`.
* Manejar cache de alertas y reportes.
* Guardar evidencia local.
* Subir evidencia cuando haya conexión.

### Estructura

```txt
data/
├── local/
│   ├── dao/
│   ├── entity/
│   └── database/
├── remote/
│   ├── api/
│   ├── dto/
│   └── service/
├── repository/
└── mapper/
```

---

## 9. Estructura recomendada del proyecto

```txt
aura-android/
│
├── app/
│   ├── build.gradle.kts
│   └── src/main/
│       ├── AndroidManifest.xml
│       └── java/io/aura/android/
│           │
│           ├── AuraApp.kt
│           ├── MainActivity.kt
│           │
│           ├── core/
│           │   ├── common/
│           │   ├── config/
│           │   ├── crypto/
│           │   ├── location/
│           │   ├── network/
│           │   ├── permissions/
│           │   ├── sync/
│           │   └── ui/
│           │
│           ├── data/
│           │   ├── local/
│           │   ├── remote/
│           │   ├── mapper/
│           │   └── repository/
│           │
│           ├── domain/
│           │   ├── model/
│           │   ├── repository/
│           │   └── usecase/
│           │
│           ├── feature/
│           │   ├── home/
│           │   ├── report/
│           │   ├── alerts/
│           │   ├── guardian/
│           │   ├── profile/
│           │   └── onboarding/
│           │
│           └── worker/
│               ├── ReportSyncWorker.kt
│               ├── EvidenceUploadWorker.kt
│               ├── AlertFetchWorker.kt
│               └── SafetySessionWorker.kt
│
├── docs/
│   ├── ARCHITECTURE.md
│   ├── DATA_MODEL.md
│   ├── OFFLINE_MODE.md
│   ├── PRIVACY.md
│   └── ROADMAP.md
│
├── assets/
│   ├── mockups/
│   └── screenshots/
│
├── README.md
├── LICENSE
└── CONTRIBUTING.md
```

---

## 10. Feature modules

Para MVP 0.1, los módulos funcionales pueden mantenerse dentro de `app/feature/*`. Si el proyecto crece, pueden migrarse a módulos Gradle separados.

---

## 10.1 Home

### Responsabilidad

Pantalla principal de acceso rápido.

### Funciones

* Mostrar estado de seguridad.
* Acceso a reportar incidente.
* Acceso a alertas cercanas.
* Acceso a Red Guardián.
* Botón SOS visible.

### Archivos sugeridos

```txt
feature/home/
├── HomeScreen.kt
├── HomeViewModel.kt
├── HomeUiState.kt
└── HomeActions.kt
```

---

## 10.2 Report

### Responsabilidad

Crear reportes ciudadanos.

### Funciones

* Selección de tipo de incidente.
* Selección de gravedad.
* Confirmación de ubicación.
* Descripción opcional.
* Adjuntar evidencia.
* Selección de privacidad.
* Guardado local.
* Sincronización diferida.

### Archivos sugeridos

```txt
feature/report/
├── ReportIncidentScreen.kt
├── AddEvidenceScreen.kt
├── ReportIncidentViewModel.kt
├── ReportIncidentUiState.kt
└── components/
```

---

## 10.3 Alerts

### Responsabilidad

Mostrar alertas cercanas en mapa y lista.

### Funciones

* Ver mapa de alertas.
* Ver lista de alertas.
* Filtrar por tipo.
* Ver detalle de alerta.
* Confirmar reporte.
* Marcar como falso.
* Marcar como resuelto.

### Archivos sugeridos

```txt
feature/alerts/
├── AlertsMapScreen.kt
├── AlertsListScreen.kt
├── AlertDetailScreen.kt
├── AlertsViewModel.kt
├── AlertDetailViewModel.kt
└── components/
```

---

## 10.4 Red Guardián

### Responsabilidad

Manejar sesiones privadas de seguridad y contactos de confianza.

### Funciones

* Crear contactos de emergencia.
* Activar sesión de seguridad.
* Compartir ubicación.
* Enviar SOS.
* Marcar “Estoy bien”.
* Llamar a contacto.
* Finalizar sesión.

### Archivos sugeridos

```txt
feature/guardian/
├── GuardianHomeScreen.kt
├── GuardianContactsScreen.kt
├── ActiveSafetySessionScreen.kt
├── GuardianViewModel.kt
├── SafetySessionViewModel.kt
└── components/
```

---

## 10.5 Profile

### Responsabilidad

Preferencias del usuario, privacidad y configuración.

### Funciones

* Editar alias local.
* Activar modo anónimo por defecto.
* Configurar modo offline.
* Configurar notificaciones.
* Ver contactos de emergencia.
* Seleccionar tema.

### Archivos sugeridos

```txt
feature/profile/
├── ProfileScreen.kt
├── PrivacySettingsScreen.kt
├── ProfileViewModel.kt
└── components/
```

---

## 11. Navegación

AURA usa Navigation Compose.

### Rutas iniciales

```kotlin
sealed class AuraRoute(val route: String) {
    data object Home : AuraRoute("home")
    data object ReportIncident : AuraRoute("report/incident")
    data object AddEvidence : AuraRoute("report/evidence/{reportDraftId}")
    data object AlertsMap : AuraRoute("alerts/map")
    data object AlertsList : AuraRoute("alerts/list")
    data object AlertDetail : AuraRoute("alerts/detail/{alertId}")
    data object GuardianHome : AuraRoute("guardian")
    data object ActiveSafetySession : AuraRoute("guardian/session/{sessionId}")
    data object Profile : AuraRoute("profile")
}
```

### Bottom navigation

```txt
Inicio
Alertas
SOS
Perfil
```

---

## 12. Modelo de datos principal

## 12.1 UserProfile

```kotlin
data class UserProfile(
    val id: String,
    val localAlias: String?,
    val publicDisplayName: String?,
    val phoneHash: String?,
    val avatarUri: String?,
    val trustScore: Int,
    val isAnonymousModeDefault: Boolean,
    val createdAt: Long,
    val updatedAt: Long
)
```

---

## 12.2 DeviceIdentity

```kotlin
data class DeviceIdentity(
    val id: String,
    val userId: String,
    val devicePublicKey: String,
    val deviceName: String?,
    val createdAt: Long,
    val lastSeenAt: Long,
    val revokedAt: Long?
)
```

---

## 12.3 IncidentReport

```kotlin
data class IncidentReport(
    val id: String,
    val authorUserId: String?,
    val anonymousId: String?,
    val type: IncidentType,
    val severity: SeverityLevel,
    val title: String?,
    val description: String?,
    val latitude: Double?,
    val longitude: Double?,
    val geohash: String,
    val locationPrecision: LocationPrecision,
    val addressLabel: String?,
    val occurredAt: Long,
    val createdAt: Long,
    val status: ReportStatus,
    val visibility: ReportVisibility,
    val verificationScore: Int,
    val falseReportScore: Int,
    val syncedAt: Long?,
    val deletedAt: Long?
)
```

---

## 12.4 IncidentEvidence

```kotlin
data class IncidentEvidence(
    val id: String,
    val reportId: String,
    val type: EvidenceType,
    val localUri: String?,
    val remoteUrl: String?,
    val sha256Hash: String,
    val encrypted: Boolean,
    val visibility: EvidenceVisibility,
    val createdAt: Long,
    val syncedAt: Long?
)
```

---

## 12.5 Alert

```kotlin
data class Alert(
    val id: String,
    val reportId: String,
    val title: String,
    val body: String,
    val geohash: String,
    val radiusMeters: Int,
    val severity: SeverityLevel,
    val createdAt: Long,
    val expiresAt: Long,
    val status: AlertStatus
)
```

---

## 12.6 ReportVerification

```kotlin
data class ReportVerification(
    val id: String,
    val reportId: String,
    val userId: String?,
    val deviceId: String?,
    val action: VerificationAction,
    val comment: String?,
    val latitude: Double?,
    val longitude: Double?,
    val geohash: String?,
    val createdAt: Long,
    val syncedAt: Long?
)
```

---

## 12.7 GuardianContact

```kotlin
data class GuardianContact(
    val id: String,
    val userId: String,
    val name: String,
    val phone: String?,
    val email: String?,
    val appUserId: String?,
    val priority: Int,
    val canReceiveLocation: Boolean,
    val canReceiveMedia: Boolean,
    val createdAt: Long
)
```

---

## 12.8 SafetySession

```kotlin
data class SafetySession(
    val id: String,
    val userId: String,
    val startedAt: Long,
    val endedAt: Long?,
    val status: SafetySessionStatus,
    val lastLatitude: Double?,
    val lastLongitude: Double?,
    val lastGeohash: String?,
    val batteryLevel: Int?,
    val message: String?,
    val createdReportId: String?
)
```

---

## 12.9 SafetySessionUpdate

```kotlin
data class SafetySessionUpdate(
    val id: String,
    val sessionId: String,
    val latitude: Double?,
    val longitude: Double?,
    val geohash: String?,
    val batteryLevel: Int?,
    val note: String?,
    val createdAt: Long,
    val syncedAt: Long?
)
```

---

## 12.10 SyncQueueItem

```kotlin
data class SyncQueueItem(
    val id: String,
    val entityType: String,
    val entityId: String,
    val operation: SyncOperation,
    val payloadJson: String,
    val priority: SyncPriority,
    val attemptCount: Int,
    val lastAttemptAt: Long?,
    val nextRetryAt: Long?,
    val status: SyncStatus
)
```

---

## 13. Enums

```kotlin
enum class IncidentType {
    THEFT,
    ATTEMPTED_THEFT,
    SUSPICIOUS_ACTIVITY,
    VIOLENCE,
    HARASSMENT,
    ACCIDENT,
    DANGER_ZONE,
    OTHER
}

enum class SeverityLevel {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL
}

enum class ReportStatus {
    DRAFT,
    PENDING_SYNC,
    SUBMITTED,
    UNDER_REVIEW,
    COMMUNITY_CONFIRMED,
    AUTHORITY_CONFIRMED,
    RESOLVED,
    DISMISSED
}

enum class LocationPrecision {
    EXACT,
    APPROXIMATE,
    ZONE
}

enum class ReportVisibility {
    PRIVATE,
    GUARDIAN_NETWORK,
    COMMUNITY,
    AUTHORITY
}

enum class EvidenceType {
    PHOTO,
    VIDEO,
    AUDIO,
    TEXT,
    HASH
}

enum class EvidenceVisibility {
    PRIVATE,
    MODERATORS,
    COMMUNITY,
    AUTHORITY
}

enum class VerificationAction {
    CONFIRM,
    DENY,
    RESOLVED,
    SPAM,
    UNSAFE_CONTENT
}

enum class AlertStatus {
    ACTIVE,
    EXPIRED,
    RESOLVED,
    HIDDEN
}

enum class SafetySessionStatus {
    ACTIVE,
    ENDED,
    CANCELLED
}

enum class SyncOperation {
    CREATE,
    UPDATE,
    DELETE
}

enum class SyncPriority {
    LOW,
    NORMAL,
    HIGH,
    CRITICAL
}

enum class SyncStatus {
    PENDING,
    SYNCING,
    SYNCED,
    FAILED
}
```

---

## 14. Room Database

### Base de datos local

```kotlin
@Database(
    entities = [
        UserProfileEntity::class,
        DeviceIdentityEntity::class,
        IncidentReportEntity::class,
        IncidentEvidenceEntity::class,
        AlertEntity::class,
        ReportVerificationEntity::class,
        GuardianContactEntity::class,
        SafetySessionEntity::class,
        SafetySessionUpdateEntity::class,
        SyncQueueEntity::class
    ],
    version = 1,
    exportSchema = true
)
abstract class AuraDatabase : RoomDatabase() {
    abstract fun userProfileDao(): UserProfileDao
    abstract fun deviceIdentityDao(): DeviceIdentityDao
    abstract fun incidentReportDao(): IncidentReportDao
    abstract fun incidentEvidenceDao(): IncidentEvidenceDao
    abstract fun alertDao(): AlertDao
    abstract fun reportVerificationDao(): ReportVerificationDao
    abstract fun guardianContactDao(): GuardianContactDao
    abstract fun safetySessionDao(): SafetySessionDao
    abstract fun syncQueueDao(): SyncQueueDao
}
```

### DAOs mínimos

```txt
UserProfileDao
DeviceIdentityDao
IncidentReportDao
IncidentEvidenceDao
AlertDao
ReportVerificationDao
GuardianContactDao
SafetySessionDao
SafetySessionUpdateDao
SyncQueueDao
```

---

## 15. Sync Architecture

La sincronización se ejecuta con WorkManager.

### Workers principales

```txt
ReportSyncWorker
EvidenceUploadWorker
AlertFetchWorker
SafetySessionWorker
```

---

## 15.1 ReportSyncWorker

### Responsabilidad

Sincronizar reportes pendientes.

```txt
SyncQueueItem(entityType = "IncidentReport", operation = CREATE)
  ↓
ReportSyncWorker
  ↓
POST /reports
  ↓
Update IncidentReport.syncedAt
  ↓
Update SyncQueue.status = SYNCED
```

### Reglas

* Si no hay conexión, mantener `PENDING`.
* Si falla el servidor, reintentar con backoff.
* Si el reporte contiene ubicación exacta pero el usuario eligió modo zona, aplicar anonimización antes de enviar.
* Si el backend rechaza el reporte, marcar como `FAILED` y mostrar error recuperable.

---

## 15.2 EvidenceUploadWorker

### Responsabilidad

Subir evidencia asociada a reportes ya sincronizados.

```txt
IncidentEvidence(localUri != null, remoteUrl == null)
  ↓
EvidenceUploadWorker
  ↓
Encrypt / strip metadata
  ↓
Upload to remote storage
  ↓
POST /reports/{id}/evidence
  ↓
Update local remoteUrl and syncedAt
```

### Reglas

* No subir evidencia sin consentimiento.
* No subir evidencia si el reporte aún no tiene ID remoto.
* Remover metadata EXIF en fotos.
* Calcular SHA-256 antes del upload.
* Cifrar si la evidencia es privada.

---

## 15.3 AlertFetchWorker

### Responsabilidad

Descargar alertas cercanas para cache offline.

```txt
Current approximate location
  ↓
GET /alerts/nearby
  ↓
Save in Room
  ↓
Expire old alerts
```

### Reglas

* Descargar solo alertas dentro del radio configurado.
* No mostrar alertas expiradas como activas.
* Priorizar alertas recientes.
* Mantener cache local limitada.

---

## 15.4 SafetySessionWorker

### Responsabilidad

Sincronizar actualizaciones de sesión activa de Red Guardián.

```txt
SafetySession ACTIVE
  ↓
Location update
  ↓
Save SafetySessionUpdate locally
  ↓
Sync when possible
  ↓
Notify trusted contacts
```

### Reglas

* Prioridad crítica.
* Si no hay internet, preparar SMS.
* Si el usuario finaliza sesión, detener actualizaciones.
* No compartir ubicación después de `ENDED` o `CANCELLED`.

---

## 16. Offline mode

AURA debe poder ejecutar acciones críticas sin internet.

### Acciones totalmente offline

```txt
Create incident draft
Save incident locally
Save evidence locally
Compute evidence hash
View cached alerts
View emergency contacts
Open SOS screen
Prepare emergency SMS
Access safety tips
```

### Acciones semi-offline

```txt
Queue incident report
Queue verification action
Queue safety session updates
Send SMS fallback if cellular network exists
Upload evidence later
Fetch alerts when temporary connection exists
```

---

## 17. Estados de conectividad

La app debe observar conectividad mediante un `ConnectivityObserver`.

```kotlin
enum class ConnectivityStatus {
    ONLINE,
    OFFLINE,
    LIMITED
}
```

### Uso

* Mostrar banner “Modo sin conexión”.
* Desactivar acciones que requieren red inmediata.
* Mantener activas acciones locales.
* Reintentar sincronización cuando vuelva conexión.

---

## 18. Location Architecture

La ubicación es crítica, pero sensible.

### Componentes

```txt
LocationProvider
LocationPrivacyService
GeohashService
LocationPermissionManager
```

### Reglas

* Solicitar ubicación solo cuando sea necesario.
* Permitir ubicación aproximada.
* Convertir coordenadas exactas a zona para reportes públicos.
* Nunca mostrar ubicación exacta del usuario en reportes comunitarios.
* Permitir ubicación exacta solo para Red Guardián y contactos autorizados.

### Precisión por contexto

```txt
Incident public report: approximate / zone
Private report: exact allowed
Red Guardián active session: exact allowed for trusted contacts
Nearby alerts: approximate allowed
Analytics: disabled in MVP 0.1
```

---

## 19. Privacy Architecture

AURA debe diseñarse con privacidad por defecto.

### Principios

* Modo anónimo por defecto.
* Datos mínimos.
* Evidencia privada por defecto.
* Ubicación aproximada en comunidad.
* Contactos privados.
* No exponer identidad públicamente.
* No publicar información sensible de terceros.

### Datos sensibles

```txt
Exact location
Phone number
Emergency contacts
Photos
Videos
Audio
Incident descriptions
Safety sessions
Device identity
```

### Reglas de protección

* Guardar teléfonos como hash cuando sea posible.
* Usar Android Keystore para claves locales.
* Cifrar evidencia sensible.
* Eliminar EXIF de imágenes.
* No enviar logs con datos personales.
* No mostrar evidencia sensible sin moderación.
* Permitir eliminar reportes locales.
* Permitir eliminar evidencia local.

---

## 20. Security Architecture

### Componentes de seguridad

```txt
Android Keystore
Encrypted local storage
Evidence hashing
HTTPS-only API
Token storage protection
Input validation
Rate limiting backend-side
```

### Firmas de dispositivo

Para reducir spam sin exigir identidad real, cada instalación puede crear una identidad de dispositivo:

```txt
Device private key: Android Keystore
Device public key: shared with backend
Actions: signed locally
Backend: verifies source consistency
```

Esto no debe usarse para revelar identidad pública del usuario.

---

## 21. Evidence pipeline

La evidencia debe tratarse como dato sensible.

### Flujo

```txt
User captures/selects evidence
  ↓
Save local copy
  ↓
Strip metadata when applicable
  ↓
Compute SHA-256 hash
  ↓
Attach to IncidentReport
  ↓
Encrypt if private
  ↓
Queue upload if user confirms
  ↓
Upload after report sync
```

### Metadata

Para fotos y videos:

* Remover EXIF.
* Evitar subir coordenadas embebidas.
* Evitar timestamp sensible si no es necesario.
* Guardar timestamp interno controlado por la app.

---

## 22. API Contracts

### Endpoints mínimos

```txt
POST   /reports
GET    /reports/nearby
GET    /reports/{id}
POST   /reports/{id}/evidence
POST   /reports/{id}/verifications

GET    /alerts/nearby
GET    /alerts/{id}

POST   /safety-sessions
PATCH  /safety-sessions/{id}
POST   /safety-sessions/{id}/updates

GET    /guardian-contacts
POST   /guardian-contacts
DELETE /guardian-contacts/{id}
```

---

## 23. API DTO examples

### Create report request

```json
{
  "clientId": "local-report-uuid",
  "type": "SUSPICIOUS_ACTIVITY",
  "severity": "MEDIUM",
  "description": "Persona merodeando vehículos estacionados.",
  "geohash": "6mc5r",
  "locationPrecision": "ZONE",
  "occurredAt": 1720000000,
  "visibility": "COMMUNITY",
  "anonymous": true
}
```

### Nearby alerts response

```json
{
  "items": [
    {
      "id": "alert-001",
      "reportId": "report-001",
      "title": "Persona sospechosa",
      "body": "Reporte cerca de Av. Arequipa.",
      "geohash": "6mc5r",
      "radiusMeters": 400,
      "severity": "MEDIUM",
      "status": "ACTIVE",
      "createdAt": 1720000000,
      "expiresAt": 1720007200
    }
  ]
}
```

### Verification request

```json
{
  "reportId": "report-001",
  "action": "CONFIRM",
  "geohash": "6mc5r",
  "createdAt": 1720000300
}
```

### Safety session update

```json
{
  "sessionId": "session-001",
  "geohash": "6mc5rx",
  "latitude": -12.0464,
  "longitude": -77.0428,
  "batteryLevel": 18,
  "createdAt": 1720000400
}
```

---

## 24. Error handling

### Tipos de error

```kotlin
sealed interface AuraError {
    data object NoConnection : AuraError
    data object PermissionDenied : AuraError
    data object LocationUnavailable : AuraError
    data object InvalidReport : AuraError
    data object SyncFailed : AuraError
    data object Unauthorized : AuraError
    data object ServerError : AuraError
    data class Unknown(val message: String?) : AuraError
}
```

### Reglas

* Los errores de red no deben borrar datos locales.
* Los errores de permisos deben explicar qué función se limita.
* Los errores de sync deben permitir reintento.
* Los errores críticos de SOS deben ofrecer alternativa: llamada o SMS.
* La UI debe evitar mensajes técnicos innecesarios.

---

## 25. Permissions

Permisos posibles:

```xml
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
<uses-permission android:name="android.permission.CAMERA" />
<uses-permission android:name="android.permission.RECORD_AUDIO" />
<uses-permission android:name="android.permission.READ_MEDIA_IMAGES" />
<uses-permission android:name="android.permission.READ_MEDIA_VIDEO" />
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
<uses-permission android:name="android.permission.SEND_SMS" />
<uses-permission android:name="android.permission.CALL_PHONE" />
```

### Regla de UX

Los permisos se solicitan en contexto, no al iniciar la app.

Ejemplos:

```txt
Location: al reportar o abrir alertas cercanas
Camera: al adjuntar foto
Microphone: al adjuntar audio
Notifications: al activar alertas
SMS: al configurar fallback SOS
Call phone: al usar botón llamar
```

---

## 26. Notification Architecture

Para MVP 0.1, las notificaciones pueden manejarse en dos modos:

### Local notifications

* Reporte guardado.
* Reporte pendiente de sincronizar.
* Sesión Red Guardián activa.
* Falló sincronización.
* Alerta cacheada cercana.

### Push notifications

* Nueva alerta cercana.
* Contacto inició sesión de seguridad.
* Contacto marcó “Estoy bien”.
* Reporte fue confirmado o resuelto.

---

## 27. LLM Architecture Future Extension

La **generación automática de denuncias con LLM** queda fuera del MVP 0.1, pero la arquitectura debe dejar espacio para integrarla luego.

### Futuro flujo previsto

```txt
IncidentReport
  ↓
Evidence metadata
  ↓
SafetySession logs, optional
  ↓
User confirmation
  ↓
LLM draft generation
  ↓
Editable complaint/report document
  ↓
Export PDF/JSON
```

### Reglas futuras

* Nunca enviar evidencia sensible al LLM sin consentimiento.
* Permitir edición manual.
* Marcar el resultado como borrador.
* No reemplazar asesoría legal.
* No enviar automáticamente a autoridades.
* Guardar trazabilidad del contenido usado.

---

## 28. Testing Strategy

### Unit tests

```txt
CreateIncidentReportUseCaseTest
LocationPrivacyServiceTest
SyncQueueManagerTest
EvidenceHashingServiceTest
AlertExpirationUseCaseTest
SafetySessionUseCaseTest
```

### Repository tests

```txt
IncidentReportRepositoryTest
AlertRepositoryTest
GuardianContactRepositoryTest
SafetySessionRepositoryTest
```

### Room tests

```txt
IncidentReportDaoTest
AlertDaoTest
SyncQueueDaoTest
SafetySessionDaoTest
```

### Worker tests

```txt
ReportSyncWorkerTest
EvidenceUploadWorkerTest
AlertFetchWorkerTest
SafetySessionWorkerTest
```

### UI tests

```txt
HomeScreenTest
ReportIncidentFlowTest
AlertsListScreenTest
AlertDetailScreenTest
RedGuardianScreenTest
ProfileSettingsScreenTest
```

---

## 29. Build variants

Se recomiendan tres variantes:

```txt
debug
staging
release
```

### debug

* Logs habilitados.
* API local/staging.
* Datos mock opcionales.
* Menor restricción de seguridad.

### staging

* Backend staging.
* Logs moderados.
* Testing de integración.

### release

* R8 / ProGuard.
* Sin logs sensibles.
* HTTPS obligatorio.
* API producción.
* Crash reporting sin datos personales.

---

## 30. Logging

### Permitido

```txt
Sync status
Worker result
Non-sensitive error type
Screen lifecycle
Feature flags
```

### Prohibido

```txt
Exact location
Phone numbers
Names of contacts
Incident descriptions
Evidence URIs
Remote media URLs
Tokens
Private keys
User identifiers raw
```

---

## 31. Accessibility

AURA debe ser usable en situaciones de estrés.

### Reglas

* Botones grandes.
* Alto contraste.
* Textos breves.
* Soporte dark mode.
* Labels accesibles en iconos.
* Feedback claro después de acciones.
* Botón SOS visible y difícil de tocar por error.
* Confirmación rápida en acciones destructivas.
* No depender solo de color para estados.

---

## 32. Performance

### Reglas

* Mantener pantallas ligeras.
* Paginar listas de alertas.
* Limitar cache local de alertas antiguas.
* Comprimir evidencia antes de subir.
* Diferir uploads pesados.
* Usar WorkManager para tareas no inmediatas.
* Evitar polling agresivo.
* Reducir uso de GPS continuo salvo en sesión activa.

---

## 33. Data retention

### Reglas iniciales

```txt
Draft reports: stored until user deletes or submits
Pending reports: stored until synced or deleted
Cached alerts: expire automatically
Evidence local: user-controlled
Safety sessions: private by default
SyncQueue: clear synced items periodically
```

### Expiración sugerida

```txt
Cached alerts: 24-72 hours
Resolved alerts: hidden after expiration
Failed sync items: retry with limit and keep user-visible
Safety session updates: user can delete
```

---

## 34. Moderation-ready design

Aunque la moderación avanzada no está en MVP 0.1, los datos deben estar preparados.

### Campos importantes

```txt
status
verificationScore
falseReportScore
visibility
evidenceVisibility
createdAt
syncedAt
deletedAt
```

### Estados de moderación

```txt
UNDER_REVIEW
COMMUNITY_CONFIRMED
AUTHORITY_CONFIRMED
RESOLVED
DISMISSED
```

---

## 35. Main flows

## 35.1 Report incident flow

```txt
Home
  ↓
Reportar incidente
  ↓
Seleccionar tipo
  ↓
Confirmar ubicación
  ↓
Añadir detalles/evidencia
  ↓
Elegir privacidad
  ↓
Guardar localmente
  ↓
Crear SyncQueueItem
  ↓
Sincronizar
```

---

## 35.2 Nearby alerts flow

```txt
Home
  ↓
Alertas cercanas
  ↓
Leer cache local
  ↓
Solicitar alertas remotas si hay conexión
  ↓
Actualizar Room
  ↓
Mostrar mapa/lista
  ↓
Abrir detalle
  ↓
Confirmar / falso / resuelto
```

---

## 35.3 Red Guardián SOS flow

```txt
Home
  ↓
Red Guardián
  ↓
Iniciar sesión
  ↓
Guardar SafetySession local
  ↓
Compartir ubicación si hay conexión
  ↓
Fallback SMS si no hay internet
  ↓
Actualizar estado
  ↓
Estoy bien / Llamar / Finalizar
```

---

## 36. Suggested implementation order

### Phase 1: Foundation

```txt
Project setup
Navigation
Theme
Room database
DataStore
Hilt
Base UI components
```

### Phase 2: Local report flow

```txt
Incident type screen
Location capture
Description
Evidence local attachment
Local save
SyncQueue creation
```

### Phase 3: Alerts

```txt
Alert local model
Cached list
Map screen
Alert detail
Verification actions
```

### Phase 4: Red Guardián

```txt
Guardian contacts
Safety session model
Active session screen
Location updates
Call/SMS fallback
```

### Phase 5: Sync

```txt
ReportSyncWorker
EvidenceUploadWorker
AlertFetchWorker
SafetySessionWorker
Retry policies
```

### Phase 6: Privacy hardening

```txt
Location approximation
Evidence hashing
EXIF cleanup
Encrypted local storage
Sensitive logs cleanup
```

---

## 37. MVP 0.1 acceptance criteria

### Report incident

* User can create report.
* Report is stored locally.
* Report can be marked anonymous.
* Report can be queued for sync.
* Report has status `PENDING_SYNC` when offline.
* Report becomes `SUBMITTED` when synced.

### Alerts

* User can view cached alerts.
* User can fetch nearby alerts.
* User can open alert detail.
* User can submit verification action.
* Expired alerts are hidden or marked expired.

### Red Guardián

* User can add trusted contacts.
* User can start safety session.
* User can see active session state.
* User can mark “Estoy bien”.
* User can call emergency/contact.
* Session can work semi-offline.

### Privacy

* Public reports do not expose exact location by default.
* Evidence is private by default.
* Sensitive logs are avoided.
* Permissions are requested contextually.

---

## 38. Future architecture extensions

Después del MVP 0.1:

```txt
Moderation dashboard
LLM complaint generation
PDF/JSON export
Community trust score
Authority integrations
Neighborhood groups
Encrypted reputation
Push notification targeting
Advanced geospatial heatmaps
On-device safety assistant
```

---

## 39. Architecture decision records

Se recomienda crear una carpeta:

```txt
docs/adr/
```

Ejemplos:

```txt
0001-use-jetpack-compose.md
0002-use-room-local-first.md
0003-use-workmanager-for-sync.md
0004-use-approximate-location-for-public-reports.md
0005-use-red-guardian-private-by-default.md
```

---

## 40. Final notes

AURA-ANDROID debe priorizar confiabilidad, privacidad y claridad antes que complejidad. En una app de seguridad ciudadana, una mala decisión de diseño puede exponer personas, amplificar rumores o incentivar confrontaciones.

La arquitectura del MVP 0.1 debe mantener una regla simple:

```txt
Guardar primero.
Proteger por defecto.
Sincronizar después.
Mostrar solo lo necesario.
```

---
