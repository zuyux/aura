package io.aura.android.navigation

import android.net.Uri

sealed class AuraRoute(val route: String) {
    data object Home : AuraRoute("main")
    data object Report : AuraRoute("report")
    data object AddEvidence : AuraRoute("report/{$REPORT_ID_ARG}/evidence")
    data object Alerts : AuraRoute("alerts")
    data object AlertsMap : AuraRoute("alerts/map")
    data object AlertDetail : AuraRoute("alerts/{$ALERT_ID_ARG}")
    data object Guardian : AuraRoute("guardian")
    data object Profile : AuraRoute("profile")

    companion object {
        const val LEGACY_HOME_ROUTE = "home"
        const val ALERT_ID_ARG = "alertId"
        const val REPORT_ID_ARG = "reportId"

        fun alertDetailRoute(alertId: String): String = "alerts/${Uri.encode(alertId)}"
        fun addEvidenceRoute(reportId: String): String = "report/${Uri.encode(reportId)}/evidence"
    }
}
