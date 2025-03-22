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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import `in`.xroden.retask.R

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40

    /* Other default colors to override
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    */
)

// Set up Google Fonts Provider
private val provider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs
)

private val fontName = GoogleFont("Montserrat")

private val fontFamily = FontFamily(
    Font(
        googleFont = fontName,
        fontProvider = provider,
        weight = FontWeight.Light
    ),
    Font(
        googleFont = fontName,
        fontProvider = provider,
        weight = FontWeight.Normal
    ),
    Font(
        googleFont = fontName,
        fontProvider = provider,
        weight = FontWeight.Medium
    ),
    Font(
        googleFont = fontName,
        fontProvider = provider,
        weight = FontWeight.SemiBold
    ),
    Font(
        googleFont = fontName,
        fontProvider = provider,
        weight = FontWeight.Bold
    )
)

// Create a custom Typography with our font family
private val CustomTypography = Typography.copy(
    displayLarge = Typography.displayLarge.copy(fontFamily = fontFamily),
    displayMedium = Typography.displayMedium.copy(fontFamily = fontFamily),
    displaySmall = Typography.displaySmall.copy(fontFamily = fontFamily),

    headlineLarge = Typography.headlineLarge.copy(fontFamily = fontFamily),
    headlineMedium = Typography.headlineMedium.copy(fontFamily = fontFamily),
    headlineSmall = Typography.headlineSmall.copy(fontFamily = fontFamily),

    titleLarge = Typography.titleLarge.copy(fontFamily = fontFamily),
    titleMedium = Typography.titleMedium.copy(fontFamily = fontFamily),
    titleSmall = Typography.titleSmall.copy(fontFamily = fontFamily),

    bodyLarge = Typography.bodyLarge.copy(fontFamily = fontFamily),
    bodyMedium = Typography.bodyMedium.copy(fontFamily = fontFamily),
    bodySmall = Typography.bodySmall.copy(fontFamily = fontFamily),

    labelLarge = Typography.labelLarge.copy(fontFamily = fontFamily),
    labelMedium = Typography.labelMedium.copy(fontFamily = fontFamily),
    labelSmall = Typography.labelSmall.copy(fontFamily = fontFamily)
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

    MaterialTheme(
        colorScheme = colorScheme,
        typography = CustomTypography,
        content = content
    )
}