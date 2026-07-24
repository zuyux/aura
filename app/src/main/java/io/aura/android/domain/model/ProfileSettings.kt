package io.aura.android.domain.model

data class ProfileSettings(
    val privacyDisclaimerAccepted: Boolean = false,
    val anonymousModeDefault: Boolean = true,
    val offlineModeEnabled: Boolean = false,
    val notificationsEnabled: Boolean = true,
    val guardianInviteNotificationsEnabled: Boolean = true,
    val sosAlertNotificationsEnabled: Boolean = true,
    val themeMode: ThemeMode = ThemeMode.DARK,
)

enum class ThemeMode {
    DARK,
    LIGHT,
    SYSTEM,
}
