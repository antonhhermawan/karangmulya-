package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = KarangmulyaBluePrimary,
    onPrimary = SurfacePureWhite,
    primaryContainer = KarangmulyaBlueLight,
    onPrimaryContainer = KarangmulyaBlueDeep,
    secondary = KarangmulyaEmerald,
    onSecondary = SurfacePureWhite,
    secondaryContainer = KarangmulyaEmeraldLight,
    onSecondaryContainer = Color(0xFF064E3B),
    tertiary = KarangmulyaAmber,
    onTertiary = SurfacePureWhite,
    tertiaryContainer = KarangmulyaAmberLight,
    onTertiaryContainer = Color(0xFF78350F),
    error = KarangmulyaRose,
    onError = SurfacePureWhite,
    errorContainer = KarangmulyaRoseLight,
    onErrorContainer = Color(0xFF881337),
    background = BackgroundSoftBlue,
    onBackground = TextSlateDark,
    surface = SurfacePureWhite,
    onSurface = TextSlateDark,
    surfaceVariant = SurfaceCardBlueTint,
    onSurfaceVariant = TextSlateMedium,
    outline = OutlineLightBlue
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF38BDF8),
    onPrimary = Color(0xFF082F49),
    primaryContainer = Color(0xFF0369A1),
    onPrimaryContainer = Color(0xFFE0F2FE),
    secondary = Color(0xFF34D399),
    onSecondary = Color(0xFF064E3B),
    secondaryContainer = Color(0xFF065F46),
    onSecondaryContainer = Color(0xFFD1FAE5),
    tertiary = Color(0xFFFBBF24),
    onTertiary = Color(0xFF78350F),
    background = Color(0xFF0B192C),
    onBackground = Color(0xFFF1F5F9),
    surface = Color(0xFF112239),
    onSurface = Color(0xFFF1F5F9),
    surfaceVariant = Color(0xFF1E3554),
    onSurfaceVariant = Color(0xFFCBD5E1),
    outline = Color(0xFF334155)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
