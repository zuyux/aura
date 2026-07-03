package io.aura.android.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddAlert
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import io.aura.android.feature.alerts.AlertDetailScreen
import io.aura.android.feature.alerts.AlertsListScreen
import io.aura.android.feature.home.HomeScreen
import io.aura.android.feature.home.ProfilePlaceholderScreen
import io.aura.android.feature.guardian.GuardianScreen
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
fun AuraAppNavHost() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route ?: AuraRoute.Home.route
    val currentTopLevelRoute = when (currentRoute) {
        AuraRoute.LEGACY_HOME_ROUTE -> AuraRoute.Home.route
        AuraRoute.AlertDetail.route -> AuraRoute.Alerts.route
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
                )
            }
            composable(AuraRoute.LEGACY_HOME_ROUTE) {
                HomeScreen(
                    onReportClick = { navController.navigateToTopLevelRoute(AuraRoute.Report.route) },
                    onAlertsClick = { navController.navigateToTopLevelRoute(AuraRoute.Alerts.route) },
                    onGuardianClick = { navController.navigateToTopLevelRoute(AuraRoute.Guardian.route) },
                )
            }
            composable(AuraRoute.Report.route) {
                ReportIncidentScreen()
            }
            composable(AuraRoute.Alerts.route) {
                AlertsListScreen(
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
                ProfilePlaceholderScreen()
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
