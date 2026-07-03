package io.aura.android.navigation

sealed class AuraRoute(val route: String) {
    data object Home : AuraRoute("main")
    data object Report : AuraRoute("report")
    data object Alerts : AuraRoute("alerts")
    data object Guardian : AuraRoute("guardian")
    data object Profile : AuraRoute("profile")

    companion object {
        const val LEGACY_HOME_ROUTE = "home"
    }
}
