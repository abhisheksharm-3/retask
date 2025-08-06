package `in`.xroden.retask.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// This file now correctly brings together your colors, typography, and shapes.

// Define the complete ColorScheme objects using your colors imported from Color.kt
private val LightColorScheme = lightColorScheme(
    primary = PrimaryLight,
    onPrimary = White,
    primaryContainer = PrimaryContainerLight,
    onPrimaryContainer = PrimaryDark,
    secondary = SecondaryLight,
    onSecondary = White,
    secondaryContainer = SecondaryContainerLight,
    onSecondaryContainer = SecondaryDark,
    tertiary = TertiaryLight,
    onTertiary = White,
    tertiaryContainer = TertiaryContainerLight,
    onTertiaryContainer = TertiaryDark,
    background = BackgroundLight,
    onBackground = TextPrimaryLight,
    surface = SurfaceLight,
    onSurface = TextPrimaryLight,
    error = ErrorLight,
    onError = White,
    errorContainer = ErrorContainerLight,
    onErrorContainer = ErrorDark,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = TextSecondaryLight,
    outline = OutlineLight,
    outlineVariant = OutlineVariantLight
)

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryDark,
    onPrimary = White,
    primaryContainer = PrimaryContainerDark,
    onPrimaryContainer = PrimaryLight,
    secondary = SecondaryDark,
    onSecondary = White,
    secondaryContainer = SecondaryContainerDark,
    onSecondaryContainer = SecondaryLight,
    tertiary = TertiaryDark,
    onTertiary = White,
    tertiaryContainer = TertiaryContainerDark,
    onTertiaryContainer = TertiaryLight,
    background = BackgroundDark,
    onBackground = TextPrimaryDark,
    surface = SurfaceDark,
    onSurface = TextPrimaryDark,
    error = ErrorDark,
    onError = White,
    errorContainer = ErrorContainerDark,
    onErrorContainer = ErrorLight,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = TextSecondaryDark,
    outline = OutlineDark,
    outlineVariant = OutlineVariantDark
)


@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalSharedTransitionApi::class)
@Composable
fun RetaskTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    // The content lambda is updated to accept a SharedTransitionScope
    // This makes implementing shared element transitions much cleaner
    content: @Composable (SharedTransitionScope) -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            // Set status and navigation bar colors to be transparent for edge-to-edge
            window.statusBarColor = Color.Transparent.toArgb()
            window.navigationBarColor = Color.Transparent.toArgb()
            // Ensure content is drawn behind the system bars
            WindowCompat.setDecorFitsSystemWindows(window, false)
            // Set the icons on the system bars to be light or dark based on the theme
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !darkTheme
        }
    }

    // Use MaterialExpressiveTheme and enable the new features
    MaterialExpressiveTheme(
        colorScheme = colorScheme,
        typography = AppTypography, // From Type.kt
        shapes = AppShapes,         // From Type.kt
        motionScheme = MotionScheme.expressive(), // Enable the new physics-based motion
        content = {
            // Wrap the entire app in SharedTransitionLayout to enable
            // expressive screen transitions automatically.
            SharedTransitionLayout {
                content(this)
            }
        }
    )
}
