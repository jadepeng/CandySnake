package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val CandyLightColorScheme = lightColorScheme(
    primary = CandyPink,
    onPrimary = Color.White,
    primaryContainer = CandyPinkLight,
    onPrimaryContainer = CandyPinkDark,
    secondary = CandyMint,
    onSecondary = Color.White,
    secondaryContainer = CandyMintLight,
    onSecondaryContainer = CandyMintDark,
    tertiary = CandyLemon,
    onTertiary = CandyCocoaInk,
    tertiaryContainer = CandyLemonLight,
    onTertiaryContainer = CandyLemonDark,
    background = CandyVanillaBg,
    onBackground = CandyCocoaInk,
    surface = CandyMarshmallowSurface,
    onSurface = CandyCocoaInk,
    surfaceVariant = CandySurfaceVariant,
    onSurfaceVariant = CandyCocoaMuted,
    outline = CandyBorder,
    outlineVariant = CandyPinkLight,
)

private val CandyDarkColorScheme = darkColorScheme(
    primary = CandyPink,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF5D223C),
    onPrimaryContainer = CandyPinkLight,
    secondary = CandyMint,
    onSecondary = Color(0xFF06291D),
    secondaryContainer = Color(0xFF144B3A),
    onSecondaryContainer = CandyMintLight,
    tertiary = CandyLemon,
    onTertiary = CandyCocoaInk,
    tertiaryContainer = Color(0xFF533E14),
    onTertiaryContainer = CandyLemonLight,
    background = DarkBerryBg,
    onBackground = DarkBerryText,
    surface = DarkBerrySurface,
    onSurface = DarkBerryText,
    surfaceVariant = DarkBerrySurfaceVariant,
    onSurfaceVariant = DarkBerryMuted,
    outline = Color(0xFF63436E),
)

val CandyShapes = Shapes(
    extraSmall = RoundedCornerShape(10.dp),
    small = RoundedCornerShape(14.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(26.dp),
    extraLarge = RoundedCornerShape(32.dp),
)

@Composable
fun CandySnakeTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) CandyDarkColorScheme else CandyLightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = CandyShapes,
        content = content
    )
}
