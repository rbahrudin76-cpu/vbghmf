package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

private val BloodlinesColorScheme = darkColorScheme(
    primary = CrimsonPrimary,
    onPrimary = ParchmentText,
    primaryContainer = CrimsonContainer,
    onPrimaryContainer = WarmGoldBright,
    secondary = AntiqueGold,
    onSecondary = ObsidianBg,
    secondaryContainer = GoldContainer,
    onSecondaryContainer = WarmGoldBright,
    tertiary = AnimusCyan,
    onTertiary = ObsidianBg,
    background = ObsidianBg,
    onBackground = ParchmentText,
    surface = BasaltSurface,
    onSurface = ParchmentText,
    surfaceVariant = ElevatedBasalt,
    onSurfaceVariant = AshMuted,
    outline = CardBorder,
    outlineVariant = SubtleText
)

private val BloodlinesShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun BloodlinesTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = BloodlinesColorScheme,
        typography = AppTypography,
        shapes = BloodlinesShapes,
        content = content
    )
}
