package io.aura.android.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddAlert
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import io.aura.android.BuildConfig
import io.aura.android.feature.alerts.AlertDetailScreen
import io.aura.android.feature.alerts.AlertsDisplayMode
import io.aura.android.feature.alerts.AlertsListScreen
import io.aura.android.feature.evidence.AddEvidenceScreen
import io.aura.android.feature.guardian.GuardianScreen
import io.aura.android.feature.home.HomeScreen
import io.aura.android.feature.profile.OnboardingScreen
import io.aura.android.feature.profile.PrivacyDisclaimerScreen
import io.aura.android.feature.profile.ProfileScreen
import io.aura.android.feature.profile.ProfileViewModel
import io.aura.android.feature.report.ReportIncidentScreen

private data class BottomDestination(
    val route: AuraRoute,
    val label: String,
    val icon: ImageVector,
)

private val bottomDestinations = listOf(
    BottomDestination(AuraRoute.Home, "Inicio", Icons.Outlined.Home),
    BottomDestination(AuraRoute.Alerts, "Alertas", Icons.Outlined.Map),
    BottomDestination(AuraRoute.Guardian, "SOS", Icons.Outlined.AddAlert),
    BottomDestination(AuraRoute.Profile, "Perfil", Icons.Outlined.Person),
)

@Composable
fun AuraAppNavHost(profileViewModel: ProfileViewModel = hiltViewModel()) {
    val profileUiState by profileViewModel.uiState.collectAsStateWithLifecycle()

    when {
        profileUiState.isLoading -> {
            Surface(modifier = Modifier.fillMaxSize()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            return
        }
        !profileUiState.privacyDisclaimerAccepted -> {
            PrivacyDisclaimerScreen(
                onAcceptClick = profileViewModel::acceptPrivacyDisclaimer,
            )
            return
        }
        !profileUiState.isProfileComplete -> {
            OnboardingScreen(
                uiState = profileUiState,
                onNameChanged = profileViewModel::onNameChanged,
                onPhoneNumberChanged = profileViewModel::onPhoneNumberChanged,
                onSendSmsCode = profileViewModel::sendSmsCode,
                onSmsCodeChanged = profileViewModel::onSmsCodeChanged,
                onSmsCodeDetected = profileViewModel::onSmsCodeDetected,
                onSmsPermissionDenied = profileViewModel::onSmsPermissionDenied,
                onComplete = profileViewModel::completeOnboarding,
            )
            return
        }
    }

    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route ?: AuraRoute.Home.route
    val currentTopLevelRoute = when (currentRoute) {
        AuraRoute.LEGACY_HOME_ROUTE -> AuraRoute.Home.route
        AuraRoute.AlertDetail.route -> AuraRoute.Alerts.route
        AuraRoute.AlertsMap.route -> AuraRoute.Alerts.route
        AuraRoute.AddEvidence.route -> AuraRoute.Report.route
        else -> currentRoute
    }

    Scaffold(
        bottomBar = {
            NavigationBar {
                bottomDestinations.forEach { destination ->
                    NavigationBarItem(
                        selected = currentTopLevelRoute == destination.route.route,
                        onClick = {
                            if (destination.route == AuraRoute.Home) {
                                navController.navigateHome()
                            } else {
                                navController.navigateToTopLevelRoute(destination.route.route)
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = destination.icon,
                                contentDescription = destination.label,
                            )
                        },
                        label = { Text(destination.label) },
                    )
                }
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = AuraRoute.Home.route,
            modifier = Modifier.padding(innerPadding),
        ) {
            composable(AuraRoute.Home.route) {
                HomeScreen(
                    onReportClick = { navController.navigateToTopLevelRoute(AuraRoute.Report.route) },
                    onAlertsClick = { navController.navigateToTopLevelRoute(AuraRoute.Alerts.route) },
                    onGuardianClick = { navController.navigateToTopLevelRoute(AuraRoute.Guardian.route) },
                    username = profileUiState.profile?.displayName.orEmpty().ifBlank { "AURA" },
                )
            }
            composable(AuraRoute.LEGACY_HOME_ROUTE) {
                HomeScreen(
                    onReportClick = { navController.navigateToTopLevelRoute(AuraRoute.Report.route) },
                    onAlertsClick = { navController.navigateToTopLevelRoute(AuraRoute.Alerts.route) },
                    onGuardianClick = { navController.navigateToTopLevelRoute(AuraRoute.Guardian.route) },
                    username = profileUiState.profile?.displayName.orEmpty().ifBlank { "AURA" },
                )
            }
            composable(AuraRoute.Report.route) {
                ReportIncidentScreen(
                    onAddEvidenceClick = { reportId ->
                        navController.navigate(AuraRoute.addEvidenceRoute(reportId))
                    },
                    onReportSubmitted = {
                        navController.navigate(AuraRoute.AlertsMap.route) {
                            popUpTo(AuraRoute.Report.route) {
                                inclusive = true
                            }
                            launchSingleTop = true
                        }
                    },
                )
            }
            composable(
                route = AuraRoute.AddEvidence.route,
                arguments = listOf(navArgument(AuraRoute.REPORT_ID_ARG) { type = NavType.StringType }),
            ) {
                AddEvidenceScreen(onBackClick = { navController.popBackStack() })
            }
            composable(AuraRoute.Alerts.route) {
                AlertsListScreen(
                    onAlertClick = { alertId ->
                        navController.navigate(AuraRoute.alertDetailRoute(alertId))
                    },
                )
            }
            composable(AuraRoute.AlertsMap.route) {
                AlertsListScreen(
                    initialDisplayMode = AlertsDisplayMode.MAP,
                    onAlertClick = { alertId ->
                        navController.navigate(AuraRoute.alertDetailRoute(alertId))
                    },
                )
            }
            composable(
                route = AuraRoute.AlertDetail.route,
                arguments = listOf(navArgument(AuraRoute.ALERT_ID_ARG) { type = NavType.StringType }),
            ) {
                AlertDetailScreen(onBackClick = { navController.popBackStack() })
            }
            composable(AuraRoute.Guardian.route) {
                GuardianScreen()
            }
            composable(AuraRoute.Profile.route) {
                ProfileScreen(
                    profile = profileUiState.profile,
                    anonymousModeDefault = profileUiState.anonymousModeDefault,
                    onAnonymousModeDefaultChanged = profileViewModel::onAnonymousModeDefaultChanged,
                    offlineModeEnabled = profileUiState.offlineModeEnabled,
                    onOfflineModeChanged = profileViewModel::onOfflineModeChanged,
                    notificationsEnabled = profileUiState.notificationsEnabled,
                    onNotificationsEnabledChanged = profileViewModel::onNotificationsEnabledChanged,
                    guardianInviteNotificationsEnabled = profileUiState.guardianInviteNotificationsEnabled,
                    onGuardianInviteNotificationsChanged = profileViewModel::onGuardianInviteNotificationsChanged,
                    sosAlertNotificationsEnabled = profileUiState.sosAlertNotificationsEnabled,
                    onSosAlertNotificationsChanged = profileViewModel::onSosAlertNotificationsChanged,
                    privacyDisclaimerAccepted = profileUiState.privacyDisclaimerAccepted,
                    appVersion = BuildConfig.VERSION_NAME,
                )
            }
        }
    }
}

private fun NavController.navigateHome() {
    navigate(AuraRoute.Home.route) {
        popUpTo(graph.findStartDestination().id) {
            inclusive = true
        }
        launchSingleTop = true
    }
}

private fun NavController.navigateToTopLevelRoute(route: String) {
    navigate(route) {
        popUpTo(graph.findStartDestination().id) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }
}
