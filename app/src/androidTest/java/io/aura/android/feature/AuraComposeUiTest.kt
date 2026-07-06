package io.aura.android.feature

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.isToggleable
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.lifecycle.SavedStateHandle
import androidx.test.platform.app.InstrumentationRegistry
import io.aura.android.core.ui.theme.AuraTheme
import io.aura.android.data.guardian.GuardianSosNotifier
import io.aura.android.domain.location.LocationProvider
import io.aura.android.domain.model.Alert
import io.aura.android.domain.model.AlertStatus
import io.aura.android.domain.model.AuraLocation
import io.aura.android.domain.model.GuardianContact
import io.aura.android.domain.model.GuardianNotification
import io.aura.android.domain.model.IncidentType
import io.aura.android.domain.model.LocationPrecision
import io.aura.android.domain.model.ProfileSettings
import io.aura.android.domain.model.SafetySession
import io.aura.android.domain.model.SafetySessionStatus
import io.aura.android.domain.model.SafetySessionUpdate
import io.aura.android.domain.model.SeverityLevel
import io.aura.android.domain.model.UserProfile
import io.aura.android.domain.model.VerificationAction
import io.aura.android.domain.repository.AlertRepository
import io.aura.android.domain.repository.GuardianRepository
import io.aura.android.domain.repository.ProfileSettingsRepository
import io.aura.android.feature.alerts.AlertDetailScreen
import io.aura.android.feature.alerts.AlertDetailViewModel
import io.aura.android.feature.alerts.AlertsListScreen
import io.aura.android.feature.alerts.AlertsViewModel
import io.aura.android.feature.guardian.GuardianScreen
import io.aura.android.feature.guardian.GuardianViewModel
import io.aura.android.feature.home.HomeNotificationsViewModel
import io.aura.android.feature.home.HomeScreen
import io.aura.android.feature.profile.ProfileScreen
import io.aura.android.feature.report.ReportIncidentContent
import io.aura.android.feature.report.ReportIncidentUiState
import io.aura.android.navigation.AuraRoute
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class AuraComposeUiTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun homeShowsPrimaryActionsAndRoutesToSelectedFlows() {
        val guardianRepository = FakeGuardianRepository()
        val profileSettingsRepository = FakeProfileSettingsRepository()
        val guardianViewModel = GuardianViewModel(
            guardianRepository = guardianRepository,
            locationProvider = FakeLocationProvider(),
            guardianSosNotifier = GuardianSosNotifier(appContext()),
        )
        val notificationsViewModel = HomeNotificationsViewModel(
            guardianRepository = guardianRepository,
            profileSettingsRepository = profileSettingsRepository,
        )
        var reportClicks = 0
        var alertsClicks = 0
        var guardianClicks = 0

        composeRule.setContent {
            AuraTheme {
                HomeScreen(
                    onReportClick = { reportClicks++ },
                    onAlertsClick = { alertsClicks++ },
                    onGuardianClick = { guardianClicks++ },
                    username = "Valeria",
                    guardianViewModel = guardianViewModel,
                    notificationsViewModel = notificationsViewModel,
                )
            }
        }

        composeRule.onNodeWithText("Hola, Valeria").assertIsDisplayed()
        composeRule.onNodeWithText("Reportar incidente").performClick()
        composeRule.onNodeWithText("Ver alertas cercanas").performClick()
        composeRule.onNodeWithText("Red Guardián").performClick()

        composeRule.runOnIdle {
            assertEquals(1, reportClicks)
            assertEquals(1, alertsClicks)
            assertEquals(1, guardianClicks)
        }
    }

    @Test
    fun reportIncidentFlowCollectsLocationDescriptionAndSubmits() {
        var uiState by mutableStateOf(ReportIncidentUiState())
        var submitted = false
        var draftSaved = false
        var evidenceReportId: String? = null

        composeRule.setContent {
            AuraTheme {
                ReportIncidentContent(
                    uiState = uiState,
                    onTypeSelected = { uiState = uiState.copy(selectedType = it) },
                    onSeveritySelected = { uiState = uiState.copy(severity = it) },
                    onLocationPrecisionSelected = {
                        uiState = uiState.copy(locationPrecision = it, isLocationConfirmed = false)
                    },
                    onUseGpsClick = {
                        uiState = uiState.copy(
                            location = testLocation,
                            locationStatus = "Ubicacion actual detectada",
                        )
                    },
                    onConfirmLocation = { uiState = uiState.copy(isLocationConfirmed = uiState.location != null) },
                    onDescriptionChanged = { uiState = uiState.copy(description = it) },
                    onAnonymousChanged = { uiState = uiState.copy(isAnonymous = it) },
                    onSaveDraft = {
                        draftSaved = true
                        uiState = uiState.copy(savedReportId = "draft-1", savedReportMessage = "Borrador guardado localmente.")
                    },
                    onSubmit = {
                        submitted = true
                        uiState = uiState.copy(
                            savedReportId = "report-1",
                            canAddEvidence = true,
                            savedReportMessage = "Reporte guardado localmente y agregado a sincronizacion pendiente.",
                        )
                    },
                    onAddEvidenceClick = { evidenceReportId = it },
                )
            }
        }

        composeRule.onNodeWithText("Robo").performClick()
        composeRule.onNodeWithText("Alta").performClick()
        composeRule.onNodeWithText("Usar GPS actual").performClick()
        composeRule.onNodeWithText("Ubicacion actual detectada").assertIsDisplayed()
        composeRule.onNodeWithText("Confirmar ubicacion").performClick()
        composeRule.onNodeWithText("Descripción opcional").performTextInput("Auto sospechoso cerca del paradero")
        composeRule.onNodeWithText("Guardar borrador").assertIsEnabled().performClick()
        composeRule.onNodeWithText("Guardar como pendiente").assertIsEnabled().performClick()
        composeRule.onNodeWithText("Agregar evidencia privada").assertIsDisplayed().performClick()

        composeRule.runOnIdle {
            assertTrue(draftSaved)
            assertTrue(submitted)
            assertEquals("report-1", evidenceReportId)
        }
    }

    @Test
    fun alertsListShowsAlertsFiltersAndOpensDetail() {
        val alertRepository = FakeAlertRepository(listOf(testAlert))
        val viewModel = AlertsViewModel(alertRepository, FakeLocationProvider())
        var openedAlertId: String? = null

        composeRule.setContent {
            AuraTheme {
                AlertsListScreen(
                    onAlertClick = { openedAlertId = it },
                    viewModel = viewModel,
                )
            }
        }

        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithText("Bolso sustraido").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText("Alertas cercanas").assertIsDisplayed()
        composeRule.onNodeWithText("Bolso sustraido").assertIsDisplayed().performClick()

        composeRule.runOnIdle {
            assertEquals("alert-1", openedAlertId)
        }
    }

    @Test
    fun alertDetailShowsVerificationActionsAndBack() {
        val alertRepository = FakeAlertRepository(listOf(testAlert))
        val viewModel = AlertDetailViewModel(
            savedStateHandle = SavedStateHandle(mapOf(AuraRoute.ALERT_ID_ARG to "alert-1")),
            alertRepository = alertRepository,
        )
        var backClicks = 0

        composeRule.setContent {
            AuraTheme {
                AlertDetailScreen(
                    onBackClick = { backClicks++ },
                    viewModel = viewModel,
                )
            }
        }

        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithText("Bolso sustraido").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText("Acciones comunitarias").assertIsDisplayed()
        composeRule.onNodeWithText("Yo tambien lo vi").performClick()
        composeRule.onNodeWithContentDescription("Volver").performClick()

        composeRule.runOnIdle {
            assertEquals(VerificationAction.ALSO_SEEN, alertRepository.recordedAction)
            assertEquals(1, backClicks)
        }
    }

    @Test
    fun guardianSessionShowsActiveSessionAndContactControls() {
        val guardianRepository = FakeGuardianRepository(
            contacts = listOf(testContact),
            sessions = listOf(testSession),
        )
        val viewModel = GuardianViewModel(
            guardianRepository = guardianRepository,
            locationProvider = FakeLocationProvider(),
            guardianSosNotifier = GuardianSosNotifier(appContext()),
        )

        composeRule.setContent {
            AuraTheme {
                GuardianScreen(viewModel = viewModel)
            }
        }

        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithText("Compartiendo estado").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText("Compartir ubicación").assertIsEnabled()
        composeRule.onNodeWithText("Estoy bien").assertIsEnabled()
        composeRule.onNodeWithText("Finalizar").assertIsEnabled()
        composeRule.onNodeWithText("Llamar").assertIsEnabled()
        composeRule.onNodeWithText("SMS").assertIsEnabled()
        composeRule.onNodeWithText("Lucia Torres").assertIsDisplayed()
    }

    @Test
    fun profileSettingsShowsProfileAndTogglesPreferences() {
        var anonymousModeDefault = true
        var offlineModeEnabled = false
        var notificationsEnabled = true
        var guardianInviteNotificationsEnabled = true
        var sosAlertNotificationsEnabled = true

        composeRule.setContent {
            AuraTheme {
                ProfileScreen(
                    profile = UserProfile(
                        id = "user-1",
                        displayName = "Valeria Ramos",
                        phoneNumber = "+51999999999",
                        createdAtMillis = 1_700_000_000_000,
                        updatedAtMillis = 1_700_000_000_000,
                    ),
                    anonymousModeDefault = anonymousModeDefault,
                    onAnonymousModeDefaultChanged = { anonymousModeDefault = it },
                    offlineModeEnabled = offlineModeEnabled,
                    onOfflineModeChanged = { offlineModeEnabled = it },
                    notificationsEnabled = notificationsEnabled,
                    onNotificationsEnabledChanged = { notificationsEnabled = it },
                    guardianInviteNotificationsEnabled = guardianInviteNotificationsEnabled,
                    onGuardianInviteNotificationsChanged = { guardianInviteNotificationsEnabled = it },
                    sosAlertNotificationsEnabled = sosAlertNotificationsEnabled,
                    onSosAlertNotificationsChanged = { sosAlertNotificationsEnabled = it },
                    privacyDisclaimerAccepted = true,
                    appVersion = "0.1.0-test",
                )
            }
        }

        composeRule.onNodeWithText("Valeria Ramos").assertIsDisplayed()
        composeRule.onNodeWithText("Modo anonimo por defecto").assertIsDisplayed()
        composeRule.onAllNodes(isToggleable())[0].performClick()
        composeRule.onAllNodes(isToggleable())[1].performClick()
        composeRule.onAllNodes(isToggleable())[2].performClick()

        composeRule.runOnIdle {
            assertEquals(false, anonymousModeDefault)
            assertEquals(true, offlineModeEnabled)
            assertEquals(false, notificationsEnabled)
        }
    }

    private fun appContext() = InstrumentationRegistry.getInstrumentation().targetContext

    private class FakeLocationProvider(
        private val location: AuraLocation? = testLocation,
    ) : LocationProvider {
        override suspend fun getCurrentLocation(): AuraLocation? = location
    }

    private class FakeProfileSettingsRepository(
        initialSettings: ProfileSettings = ProfileSettings(),
    ) : ProfileSettingsRepository {
        private val settings = MutableStateFlow(initialSettings)

        override fun observeSettings(): Flow<ProfileSettings> = settings
        override suspend fun setPrivacyDisclaimerAccepted(accepted: Boolean) = update { copy(privacyDisclaimerAccepted = accepted) }
        override suspend fun setAnonymousModeDefault(enabled: Boolean) = update { copy(anonymousModeDefault = enabled) }
        override suspend fun setOfflineModeEnabled(enabled: Boolean) = update { copy(offlineModeEnabled = enabled) }
        override suspend fun setNotificationsEnabled(enabled: Boolean) = update { copy(notificationsEnabled = enabled) }
        override suspend fun setGuardianInviteNotificationsEnabled(enabled: Boolean) = update { copy(guardianInviteNotificationsEnabled = enabled) }
        override suspend fun setSosAlertNotificationsEnabled(enabled: Boolean) = update { copy(sosAlertNotificationsEnabled = enabled) }

        private fun update(block: ProfileSettings.() -> ProfileSettings) {
            settings.value = settings.value.block()
        }
    }

    private class FakeAlertRepository(initialAlerts: List<Alert>) : AlertRepository {
        private val alerts = MutableStateFlow(initialAlerts)
        var recordedAction: VerificationAction? = null

        override fun observeNearbyAlerts(): Flow<List<Alert>> = alerts
        override fun observeAlert(alertId: String): Flow<Alert?> = alerts.map { items -> items.firstOrNull { it.id == alertId } }
        override suspend fun recordVerification(alertId: String, action: VerificationAction) {
            recordedAction = action
        }
        override suspend fun refreshNearbyAlerts(location: AuraLocation, radiusMeters: Int) = Unit
        override suspend fun seedDemoAlertsIfEmpty() = Unit
    }

    private class FakeGuardianRepository(
        contacts: List<GuardianContact> = emptyList(),
        notifications: List<GuardianNotification> = emptyList(),
        sessions: List<SafetySession> = emptyList(),
    ) : GuardianRepository {
        private val contactsFlow = MutableStateFlow(contacts)
        private val notificationsFlow = MutableStateFlow(notifications)
        private val sessionsFlow = MutableStateFlow(sessions)
        private val updatesFlow = MutableStateFlow(emptyList<SafetySessionUpdate>())

        override fun observeContacts(): Flow<List<GuardianContact>> = contactsFlow
        override fun observeNotifications(): Flow<List<GuardianNotification>> = notificationsFlow
        override fun observeSessions(): Flow<List<SafetySession>> = sessionsFlow
        override fun observeUpdates(sessionId: String): Flow<List<SafetySessionUpdate>> = updatesFlow
        override suspend fun saveContact(contact: GuardianContact) {
            contactsFlow.value = contactsFlow.value + contact
        }
        override suspend fun removeContact(contactId: String) {
            contactsFlow.value = contactsFlow.value.filterNot { it.id == contactId }
        }
        override suspend fun saveNotification(notification: GuardianNotification) {
            notificationsFlow.value = notificationsFlow.value + notification
        }
        override suspend fun markNotificationRead(notificationId: String) = Unit
        override suspend fun acceptGuardianInvite(notificationId: String) = Unit
        override suspend fun declineGuardianInvite(notificationId: String) = Unit
        override suspend fun saveSession(session: SafetySession) {
            sessionsFlow.value = sessionsFlow.value.filterNot { it.id == session.id } + session
        }
        override suspend fun saveUpdate(update: SafetySessionUpdate) {
            updatesFlow.value = updatesFlow.value + update
        }
    }

    private companion object {
        val testLocation = AuraLocation(
            latitude = -12.0464,
            longitude = -77.0428,
            precision = LocationPrecision.APPROXIMATE,
        )

        val testAlert = Alert(
            id = "alert-1",
            reportId = "report-1",
            type = IncidentType.THEFT,
            severity = SeverityLevel.HIGH,
            status = AlertStatus.UNVERIFIED,
            location = testLocation,
            summary = "Bolso sustraido",
            distanceMeters = 240,
            reportedAtMillis = System.currentTimeMillis() - 5 * 60_000,
        )

        val testContact = GuardianContact(
            id = "contact-1",
            displayName = "Lucia Torres",
            phoneNumber = "+51911111111",
            photoUri = null,
            isPrimary = true,
            createdAtMillis = 1_700_000_000_000,
        )

        val testSession = SafetySession(
            id = "session-1",
            status = SafetySessionStatus.ACTIVE,
            startedAtMillis = 1_700_000_000_000,
            endedAtMillis = null,
            lastLocation = testLocation,
        )
    }
}
