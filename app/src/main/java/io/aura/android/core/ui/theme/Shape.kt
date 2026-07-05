package io.aura.android.core.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

object AuraShapes {
    val card = RoundedCornerShape(8.dp)
    val compact = RoundedCornerShape(6.dp)
    val control = RoundedCornerShape(8.dp)
}

val AuraMaterialShapes = Shapes(
    extraSmall = AuraShapes.compact,
    small = AuraShapes.control,
    medium = AuraShapes.card,
    large = AuraShapes.card,
    extraLarge = AuraShapes.card,
)
