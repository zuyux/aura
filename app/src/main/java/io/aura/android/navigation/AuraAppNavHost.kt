package io.aura.android.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddAlert
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import io.aura.android.feature.home.AlertsPlaceholderScreen
import io.aura.android.feature.home.GuardianPlaceholderScreen
import io.aura.android.feature.home.HomeScreen
import io.aura.android.feature.report.ReportIncidentScreen

private data class BottomDestination(
    val route: AuraRoute,
    val label: String,
    val icon: ImageVector,
)

private val bottomDestinations = listOf(
    BottomDestination(AuraRoute.Home, "Inicio", Icons.Outlined.Home),
    BottomDestination(AuraRoute.Report, "Reportar", Icons.Outlined.AddAlert),
    BottomDestination(AuraRoute.Alerts, "Alertas", Icons.Outlined.Map),
    BottomDestination(AuraRoute.Guardian, "Guardián", Icons.Outlined.Shield),
)

@Composable
fun AuraAppNavHost() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route ?: AuraRoute.Home.route

    Scaffold(
        bottomBar = {
            NavigationBar {
                bottomDestinations.forEach { destination ->
                    NavigationBarItem(
                        selected = currentRoute == destination.route.route,
                        onClick = {
                            navController.navigate(destination.route.route) {
                                popUpTo(AuraRoute.Home.route) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
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
                    onReportClick = { navController.navigate(AuraRoute.Report.route) },
                    onAlertsClick = { navController.navigate(AuraRoute.Alerts.route) },
                    onGuardianClick = { navController.navigate(AuraRoute.Guardian.route) },
                )
            }
            composable(AuraRoute.Report.route) {
                ReportIncidentScreen()
            }
            composable(AuraRoute.Alerts.route) {
                AlertsPlaceholderScreen()
            }
            composable(AuraRoute.Guardian.route) {
                GuardianPlaceholderScreen()
            }
        }
    }
}
