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

// Refined font selection - Montserrat for headings and Work Sans for body
private val montserrat = GoogleFont("Montserrat")
private val workSans = GoogleFont("Work Sans")

private val montserratFamily = FontFamily(
    Font(googleFont = montserrat, fontProvider = provider, weight = FontWeight.Light),
    Font(googleFont = montserrat, fontProvider = provider, weight = FontWeight.Normal),
    Font(googleFont = montserrat, fontProvider = provider, weight = FontWeight.Medium),
    Font(googleFont = montserrat, fontProvider = provider, weight = FontWeight.SemiBold),
    Font(googleFont = montserrat, fontProvider = provider, weight = FontWeight.Bold)
)

private val workSansFamily = FontFamily(
    Font(googleFont = workSans, fontProvider = provider, weight = FontWeight.Light),
    Font(googleFont = workSans, fontProvider = provider, weight = FontWeight.Normal),
    Font(googleFont = workSans, fontProvider = provider, weight = FontWeight.Medium),
    Font(googleFont = workSans, fontProvider = provider, weight = FontWeight.SemiBold)
)

// Create a custom Typography with our font families
private val CustomTypography = Typography.copy(
    // Display styles
    displayLarge = Typography.displayLarge.copy(
        fontFamily = montserratFamily,
        fontWeight = FontWeight.Bold,
        letterSpacing = (-0.25).sp
    ),
    displayMedium = Typography.displayMedium.copy(
        fontFamily = montserratFamily,
        fontWeight = FontWeight.Bold,
        letterSpacing = (-0.25).sp
    ),
    displaySmall = Typography.displaySmall.copy(
        fontFamily = montserratFamily,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = (-0.25).sp
    ),

    // Headline styles
    headlineLarge = Typography.headlineLarge.copy(
        fontFamily = montserratFamily,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = (-0.25).sp
    ),
    headlineMedium = Typography.headlineMedium.copy(
        fontFamily = montserratFamily,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = (-0.25).sp
    ),
    headlineSmall = Typography.headlineSmall.copy(
        fontFamily = montserratFamily,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = (-0.25).sp
    ),

    // Title styles
    titleLarge = Typography.titleLarge.copy(
        fontFamily = montserratFamily,
        fontWeight = FontWeight.SemiBold
    ),
    titleMedium = Typography.titleMedium.copy(
        fontFamily = montserratFamily,
        fontWeight = FontWeight.Medium
    ),
    titleSmall = Typography.titleSmall.copy(
        fontFamily = montserratFamily,
        fontWeight = FontWeight.Medium
    ),

    // Body styles - using Work Sans for better readability
    bodyLarge = Typography.bodyLarge.copy(
        fontFamily = workSansFamily,
        fontWeight = FontWeight.Normal,
        letterSpacing = 0.sp
    ),
    bodyMedium = Typography.bodyMedium.copy(
        fontFamily = workSansFamily,
        fontWeight = FontWeight.Normal,
        letterSpacing = 0.sp
    ),
    bodySmall = Typography.bodySmall.copy(
        fontFamily = workSansFamily,
        fontWeight = FontWeight.Normal,
        letterSpacing = 0.sp
    ),

    // Label styles
    labelLarge = Typography.labelLarge.copy(
        fontFamily = workSansFamily,
        fontWeight = FontWeight.Medium
    ),
    labelMedium = Typography.labelMedium.copy(
        fontFamily = workSansFamily,
        fontWeight = FontWeight.Medium
    ),
    labelSmall = Typography.labelSmall.copy(
        fontFamily = workSansFamily,
        fontWeight = FontWeight.Medium
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