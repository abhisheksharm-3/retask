package `in`.xroden.retask.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import `in`.xroden.retask.R

// Refined color palette with better harmony
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

// Set up Google Fonts Provider
private val provider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs
)

// Plus Jakarta Sans for the entire application - a premium, versatile font
private val jakartaSans = GoogleFont("Plus Jakarta Sans")

private val fontFamily = FontFamily(
    Font(googleFont = jakartaSans, fontProvider = provider, weight = FontWeight.Light),
    Font(googleFont = jakartaSans, fontProvider = provider, weight = FontWeight.Normal),
    Font(googleFont = jakartaSans, fontProvider = provider, weight = FontWeight.Medium),
    Font(googleFont = jakartaSans, fontProvider = provider, weight = FontWeight.SemiBold),
    Font(googleFont = jakartaSans, fontProvider = provider, weight = FontWeight.Bold)
)

// Create a custom Typography with our font family
private val CustomTypography = Typography.copy(
    // Display styles
    displayLarge = Typography.displayLarge.copy(
        fontFamily = fontFamily,
        fontWeight = FontWeight.Bold,
        letterSpacing = (-0.2).sp
    ),
    displayMedium = Typography.displayMedium.copy(
        fontFamily = fontFamily,
        fontWeight = FontWeight.Bold,
        letterSpacing = (-0.15).sp
    ),
    displaySmall = Typography.displaySmall.copy(
        fontFamily = fontFamily,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = (-0.1).sp
    ),

    // Headline styles
    headlineLarge = Typography.headlineLarge.copy(
        fontFamily = fontFamily,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = (-0.1).sp
    ),
    headlineMedium = Typography.headlineMedium.copy(
        fontFamily = fontFamily,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 0.sp
    ),
    headlineSmall = Typography.headlineSmall.copy(
        fontFamily = fontFamily,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 0.sp
    ),

    // Title styles
    titleLarge = Typography.titleLarge.copy(
        fontFamily = fontFamily,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 0.sp
    ),
    titleMedium = Typography.titleMedium.copy(
        fontFamily = fontFamily,
        fontWeight = FontWeight.Medium,
        letterSpacing = 0.sp
    ),
    titleSmall = Typography.titleSmall.copy(
        fontFamily = fontFamily,
        fontWeight = FontWeight.Medium,
        letterSpacing = 0.sp
    ),

    // Body styles
    bodyLarge = Typography.bodyLarge.copy(
        fontFamily = fontFamily,
        fontWeight = FontWeight.Normal,
        letterSpacing = 0.sp
    ),
    bodyMedium = Typography.bodyMedium.copy(
        fontFamily = fontFamily,
        fontWeight = FontWeight.Normal,
        letterSpacing = 0.sp
    ),
    bodySmall = Typography.bodySmall.copy(
        fontFamily = fontFamily,
        fontWeight = FontWeight.Normal,
        letterSpacing = 0.sp
    ),

    // Label styles
    labelLarge = Typography.labelLarge.copy(
        fontFamily = fontFamily,
        fontWeight = FontWeight.Medium,
        letterSpacing = 0.sp
    ),
    labelMedium = Typography.labelMedium.copy(
        fontFamily = fontFamily,
        fontWeight = FontWeight.Medium,
        letterSpacing = 0.sp
    ),
    labelSmall = Typography.labelSmall.copy(
        fontFamily = fontFamily,
        fontWeight = FontWeight.Medium,
        letterSpacing = 0.sp
    )
)

@Composable
fun RetaskTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    // Apply status bar color and appearance
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = CustomTypography,
        content = content
    )
}