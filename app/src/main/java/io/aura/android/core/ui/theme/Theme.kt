package io.aura.android.core.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import io.aura.android.domain.model.ThemeMode

private val LightColors = lightColorScheme(
    primary = AuraBlue,
    onPrimary = AuraSurface,
    secondary = AuraDarkBlue,
    onSecondary = AuraSurface,
    tertiary = AuraGreen,
    onTertiary = AuraSurface,
    error = AuraRed,
    onError = AuraSurface,
    background = AuraBackground,
    onBackground = AuraDarkBlue,
    surface = AuraSurface,
    onSurface = AuraDarkBlue,
    surfaceVariant = AuraLightGray,
    onSurfaceVariant = AuraMutedText,
)

private val DarkColors = darkColorScheme(
    primary = AuraDarkPrimary,
    onPrimary = AuraDarkBackground,
    secondary = AuraGreen,
    onSecondary = AuraDarkBackground,
    tertiary = AuraGreen,
    onTertiary = AuraDarkBackground,
    error = Color(0xFFFF6B6B),
    onError = AuraDarkBackground,
    background = AuraDarkBackground,
    onBackground = AuraDarkOnSurface,
    surface = AuraDarkSurface,
    onSurface = AuraDarkOnSurface,
    surfaceVariant = AuraDarkSurfaceVariant,
    onSurfaceVariant = AuraDarkMutedText,
)

@Composable
fun AuraTheme(
    themeMode: ThemeMode = ThemeMode.DARK,
    content: @Composable () -> Unit,
) {
    val darkTheme = when (themeMode) {
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as android.app.Activity).window
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = AuraTypography,
        shapes = AuraMaterialShapes,
        content = content,
    )
}
