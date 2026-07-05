package io.aura.android.core.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

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

@Composable
fun AuraTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = LightColors,
        typography = AuraTypography,
        shapes = AuraMaterialShapes,
        content = content,
    )
}
