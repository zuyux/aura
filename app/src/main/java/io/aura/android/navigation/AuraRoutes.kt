package io.aura.android.navigation

sealed class AuraRoute(val route: String) {
    data object Home : AuraRoute("home")
    data object Report : AuraRoute("report")
    data object Alerts : AuraRoute("alerts")
    data object Guardian : AuraRoute("guardian")
}
