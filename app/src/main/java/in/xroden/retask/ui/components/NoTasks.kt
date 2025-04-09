package `in`.xroden.retask.ui.components

import android.content.res.Configuration
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.EmojiEmotions
import androidx.compose.material.icons.rounded.Spa
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import java.time.format.TextStyle as DateTextStyle
import java.util.Locale

/**
 * Empty state component shown when no tasks exist.
 * Adapts its layout based on device configuration.
 *
 * @param onAddSampleTasks Callback when user wants to add sample tasks
 * @param onAddNewTask Callback when user wants to add a new task
 * @param modifier Optional modifier for the component
 */
@Composable
fun NoTasks(
    onAddSampleTasks: () -> Unit,
    onAddNewTask: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Get current device form factor and dimensions
    val deviceConfig = rememberDeviceConfig()

    // Get theme typography for dimensions
    val typography = MaterialTheme.typography

    // Create dimensions based on device config and typography
    val dimensions = remember(deviceConfig) {
        NoTasksDimensions(
            deviceConfig = deviceConfig
        )
    }

    // Common elements across all layouts
    val scale = rememberPulseAnimation()
    val dayOfWeek = rememberCurrentDayOfWeek()
    val warmGradient = rememberWarmGradient()
    val haptic = LocalHapticFeedback.current

    // Action handlers with haptic feedback
    val handleAddNewTask: () -> Unit = {
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        onAddNewTask()
    }

    val handleAddSampleTasks: () -> Unit = {
        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        onAddSampleTasks()
    }

    Surface(
        modifier = modifier
            .fillMaxSize()
            .semantics { contentDescription = "Empty state view when no tasks exist" },
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp
    ) {
        // Choose appropriate layout based on device configuration
        when {
            deviceConfig.isMobileLandscape ->
                LandscapeLayout(
                    dimensions = dimensions,
                    typography = typography,
                    dayOfWeek = dayOfWeek,
                    scale = scale,
                    warmGradient = warmGradient,
                    onAddNewTask = handleAddNewTask,
                    onAddSampleTasks = handleAddSampleTasks,
                    isScrollable = true
                )

            deviceConfig.isLandscape && deviceConfig.isTablet ->
                TabletLandscapeLayout(
                    dimensions = dimensions,
                    typography = typography,
                    dayOfWeek = dayOfWeek,
                    scale = scale,
                    warmGradient = warmGradient,
                    onAddNewTask = handleAddNewTask,
                    onAddSampleTasks = handleAddSampleTasks
                )

            deviceConfig.isFoldable || (deviceConfig.isTablet && !deviceConfig.isLandscape) ->
                CenteredLayout(
                    dimensions = dimensions,
                    typography = typography,
                    dayOfWeek = dayOfWeek,
                    scale = scale,
                    warmGradient = warmGradient,
                    onAddNewTask = handleAddNewTask,
                    onAddSampleTasks = handleAddSampleTasks,
                    isFoldable = deviceConfig.isFoldable
                )

            else ->
                StandardLayout(
                    dimensions = dimensions,
                    typography = typography,
                    dayOfWeek = dayOfWeek,
                    scale = scale,
                    warmGradient = warmGradient,
                    onAddNewTask = handleAddNewTask,
                    onAddSampleTasks = handleAddSampleTasks
                )
        }
    }
}

/**
 * Dimensions system that adapts to device configuration
 */
@Stable
private class NoTasksDimensions(deviceConfig: DeviceConfig) {
    // Icon sizes
    val iconSize: Dp = when {
        deviceConfig.isTablet && deviceConfig.screenWidth > 840.dp -> 180.dp
        deviceConfig.isTablet -> 150.dp
        deviceConfig.isFoldable -> 130.dp
        deviceConfig.isMobileLandscape -> 90.dp
        else -> 120.dp
    }

    // Button dimensions
    val buttonHeight: Dp = when {
        deviceConfig.isTablet && deviceConfig.screenWidth > 840.dp -> 64.dp
        deviceConfig.isTablet || deviceConfig.isFoldable -> 58.dp
        else -> 54.dp
    }

    val buttonWidthFraction: Float = when {
        deviceConfig.isTablet && deviceConfig.screenWidth > 840.dp -> 0.5f
        deviceConfig.isTablet -> 0.65f
        deviceConfig.isFoldable -> 0.75f
        else -> 0.85f
    }

    // Padding values
    val horizontalPadding: Dp = when {
        deviceConfig.isTablet && deviceConfig.screenWidth > 840.dp -> 48.dp
        deviceConfig.isTablet -> 36.dp
        deviceConfig.isFoldable -> 28.dp
        else -> 24.dp
    }

    val contentSpacing: Dp = when {
        deviceConfig.isTablet -> 40.dp
        deviceConfig.isFoldable -> 32.dp
        else -> 24.dp
    }

    val buttonSpacing: Dp = when {
        deviceConfig.isTablet -> 20.dp
        else -> 16.dp
    }

    // Determine whether to use large or small headings based on device type
    val useHeadlineLarge: Boolean = deviceConfig.isTablet && deviceConfig.screenWidth > 840.dp
    val useHeadlineMedium: Boolean = deviceConfig.isTablet || deviceConfig.isFoldable
}

// ----- Device Configuration -----

/**
 * Data class for holding device configuration information
 */
@Stable
private data class DeviceConfig(
    val screenWidth: Dp,
    val screenHeight: Dp,
    val isLandscape: Boolean,
    val isTablet: Boolean,
    val isFoldable: Boolean,
    val isMobileLandscape: Boolean
)

/**
 * Calculates and provides the current device configuration
 */
@Composable
private fun rememberDeviceConfig(): DeviceConfig {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val screenHeight = configuration.screenHeightDp.dp
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    // Define device form factors
    val isTablet = screenWidth > 600.dp
    val isFoldable = screenWidth > 580.dp && screenWidth < 700.dp && !isLandscape
    val isMobileLandscape = isLandscape && !isTablet

    return remember(screenWidth, screenHeight, isLandscape) {
        DeviceConfig(
            screenWidth = screenWidth,
            screenHeight = screenHeight,
            isLandscape = isLandscape,
            isTablet = isTablet,
            isFoldable = isFoldable,
            isMobileLandscape = isMobileLandscape
        )
    }
}

// ----- Common Composable Utilities -----

/**
 * Creates a pulsing animation for the icon
 */
@Composable
private fun rememberPulseAnimation(): Float {
    val infiniteTransition = rememberInfiniteTransition(label = "EmptyStateAnimation")
    return infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Pulse"
    ).value
}

/**
 * Gets the current day of week for personalized greeting
 */
@Composable
private fun rememberCurrentDayOfWeek(): String {
    return remember {
        val today = LocalDate.now()
        today.dayOfWeek.getDisplayName(DateTextStyle.FULL, Locale.getDefault())
    }
}

/**
 * Creates a warm gradient for visual elements
 */
@Composable
private fun rememberWarmGradient(): Brush {
    val primary = MaterialTheme.colorScheme.primary
    val tertiary = MaterialTheme.colorScheme.tertiary

    return remember(primary, tertiary) {
        Brush.linearGradient(
            colors = listOf(
                primary.copy(alpha = 0.7f),
                tertiary.copy(alpha = 0.6f)
            )
        )
    }
}

// ----- Reusable UI Components -----

/**
 * Shows a personalized greeting message
 */
@Composable
private fun GreetingSection(
    dayOfWeek: String,
    style: TextStyle = MaterialTheme.typography.titleMedium,
    bodyStyle: TextStyle = MaterialTheme.typography.bodyMedium,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = "Happy $dayOfWeek!",
            style = style.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurface
        )

        Text(
            text = "Your schedule is clear today",
            style = bodyStyle,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Animated icon component with decorative elements
 */
@Composable
private fun AnimatedIcon(
    scale: Float,
    gradient: Brush,
    size: Dp,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(size)
            .scale(scale)
            .clip(CircleShape)
            .background(gradient),
        contentAlignment = Alignment.Center,
    ) {
        // Main icon
        Icon(
            imageVector = Icons.Rounded.Spa,
            contentDescription = null,
            modifier = Modifier.size(size * 0.45f),
            tint = Color.White.copy(alpha = 0.9f)
        )

        // Small decorative element
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset((-size * 0.07f), size * 0.2f)
        ) {
            Icon(
                imageVector = Icons.Rounded.EmojiEmotions,
                contentDescription = null,
                modifier = Modifier
                    .size(size * 0.27f)
                    .rotate(-15f),
                tint = Color.White.copy(alpha = 0.85f)
            )
        }
    }
}

/**
 * Heading text section for empty state
 */
@Composable
private fun HeadingSection(
    typography: Typography,
    dimensions: NoTasksDimensions,
    textAlign: TextAlign = TextAlign.Center,
    contentPadding: Dp = 0.dp,
    modifier: Modifier = Modifier
) {
    // Select the right typography style based on device dimensions
    val headlineStyle = when {
        dimensions.useHeadlineLarge -> typography.headlineLarge
        dimensions.useHeadlineMedium -> typography.headlineMedium
        else -> typography.headlineSmall
    }

    val bodyStyle = typography.bodyLarge

    Column(
        modifier = modifier,
        horizontalAlignment = if (textAlign == TextAlign.Center) Alignment.CenterHorizontally else Alignment.Start
    ) {
        Text(
            text = "Mindful Productivity Awaits",
            style = headlineStyle.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.3.sp
            ),
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = textAlign
        )

        Spacer(modifier = Modifier.height(if (dimensions.useHeadlineMedium) 16.dp else 8.dp))

        Text(
            text = "Take a deep breath and start planning your day with intention. What would you like to accomplish?",
            style = bodyStyle,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = textAlign,
            modifier = if (contentPadding > 0.dp) Modifier.padding(horizontal = contentPadding) else Modifier
        )
    }
}

/**
 * Primary action button for creating tasks
 */
@Composable
private fun PrimaryActionButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    height: Dp = 58.dp,
    cornerRadius: Dp = 20.dp,
    iconSize: Dp = 20.dp,
    textStyle: TextStyle = MaterialTheme.typography.titleMedium
) {
    ElevatedButton(
        onClick = onClick,
        modifier = modifier
            .height(height)
            .semantics { contentDescription = "Add new task" },
        shape = RoundedCornerShape(cornerRadius),
        colors = ButtonDefaults.elevatedButtonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ),
        elevation = ButtonDefaults.elevatedButtonElevation(
            defaultElevation = 6.dp,
            pressedElevation = 2.dp
        )
    ) {
        Icon(
            imageVector = Icons.Rounded.Add,
            contentDescription = null,
            modifier = Modifier.size(iconSize)
        )
        Spacer(modifier = Modifier.size(ButtonDefaults.IconSpacing))
        Text(
            "Create First Task",
            style = textStyle,
            fontWeight = FontWeight.SemiBold
        )
    }
}

/**
 * Secondary action button for inspiration
 */
@Composable
private fun SecondaryActionButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .clickable(onClick = onClick)
                .padding(vertical = 12.dp, horizontal = 24.dp)
                .semantics { contentDescription = "Add sample tasks" },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Or try some inspiration",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

/**
 * Attribution credit component
 */
@Composable
private fun Attribution(
    modifier: Modifier = Modifier,
    topPadding: Dp = 16.dp
) {
    Text(
        text = "Created with ❤️ by @abhisheksharm-3",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
        modifier = modifier.padding(top = topPadding)
    )
}

// ----- Layout Implementations -----

/**
 * Layout for mobile devices in landscape mode
 */
@Composable
private fun LandscapeLayout(
    dimensions: NoTasksDimensions,
    typography: Typography,
    dayOfWeek: String,
    scale: Float,
    warmGradient: Brush,
    onAddNewTask: () -> Unit,
    onAddSampleTasks: () -> Unit,
    isScrollable: Boolean = false
) {
    val contentModifier = if (isScrollable) {
        val scrollState = rememberScrollState()
        Modifier
            .fillMaxSize()
            .padding(horizontal = dimensions.horizontalPadding, vertical = 16.dp)
            .verticalScroll(scrollState)
    } else {
        Modifier
            .fillMaxSize()
            .padding(horizontal = dimensions.horizontalPadding, vertical = 16.dp)
    }

    Column(modifier = contentModifier) {
        // Greeting row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left side: Greeting
            GreetingSection(
                dayOfWeek = dayOfWeek,
                modifier = Modifier.weight(1f)
            )

            // Right side: Icon
            AnimatedIcon(
                scale = scale,
                gradient = warmGradient,
                size = dimensions.iconSize
            )
        }

        // Header and message
        Spacer(modifier = Modifier.height(16.dp))
        HeadingSection(
            typography = typography,
            dimensions = dimensions,
            textAlign = TextAlign.Start
        )

        // Action buttons
        Spacer(modifier = Modifier.height(24.dp))
        PrimaryActionButton(
            onClick = onAddNewTask,
            modifier = Modifier.fillMaxWidth(),
            height = dimensions.buttonHeight
        )

        Spacer(modifier = Modifier.height(dimensions.buttonSpacing))
        SecondaryActionButton(onClick = onAddSampleTasks)

        // Attribution
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Attribution(topPadding = dimensions.buttonSpacing)
        }

        // Extra space at bottom for scroll-ability if needed
        if (isScrollable) {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

/**
 * Layout for tablets in landscape mode
 */
@Composable
private fun TabletLandscapeLayout(
    dimensions: NoTasksDimensions,
    typography: Typography,
    dayOfWeek: String,
    scale: Float,
    warmGradient: Brush,
    onAddNewTask: () -> Unit,
    onAddSampleTasks: () -> Unit
) {
    // Select the appropriate headline style based on dimensions
    val headlineStyle = when {
        dimensions.useHeadlineLarge -> typography.headlineLarge
        dimensions.useHeadlineMedium -> typography.headlineMedium
        else -> typography.headlineSmall
    }

    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = dimensions.horizontalPadding, vertical = 24.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left side: Visual elements
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AnimatedIcon(
                scale = scale,
                gradient = warmGradient,
                size = dimensions.iconSize
            )

            Spacer(modifier = Modifier.height(dimensions.contentSpacing))

            Text(
                text = "Mindful Productivity Awaits",
                style = headlineStyle.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.3.sp
                ),
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
        }

        // Right side: Content and buttons
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = dimensions.contentSpacing),
            verticalArrangement = Arrangement.Center
        ) {
            // Greeting
            GreetingSection(
                dayOfWeek = dayOfWeek,
                style = typography.titleMedium
            )

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Take a deep breath and start planning your day with intention. What would you like to accomplish?",
                style = typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Actions
            Column(
                modifier = Modifier.fillMaxWidth(dimensions.buttonWidthFraction),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                PrimaryActionButton(
                    onClick = onAddNewTask,
                    modifier = Modifier.fillMaxWidth(),
                    height = dimensions.buttonHeight
                )

                Spacer(modifier = Modifier.height(dimensions.buttonSpacing))
                SecondaryActionButton(onClick = onAddSampleTasks)
            }
        }
    }
}

/**
 * Centered layout for tablets in portrait and foldable devices
 */
@Composable
private fun CenteredLayout(
    dimensions: NoTasksDimensions,
    typography: Typography,
    dayOfWeek: String,
    scale: Float,
    warmGradient: Brush,
    onAddNewTask: () -> Unit,
    onAddSampleTasks: () -> Unit,
    isFoldable: Boolean = false
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = dimensions.horizontalPadding, vertical = 24.dp)
    ) {
        // Greeting at the top
        GreetingSection(
            dayOfWeek = dayOfWeek,
            style = if (isFoldable) typography.titleMedium else typography.titleLarge,
            bodyStyle = if (isFoldable) typography.bodyMedium else typography.bodyLarge,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(top = if (isFoldable) 8.dp else 16.dp)
        )

        // Main content
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AnimatedIcon(
                scale = scale,
                gradient = warmGradient,
                size = dimensions.iconSize
            )

            Spacer(modifier = Modifier.height(dimensions.contentSpacing))

            HeadingSection(
                typography = typography,
                dimensions = dimensions,
                contentPadding = if (!isFoldable) dimensions.contentSpacing else 32.dp
            )
        }

        // Action buttons at the bottom
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(bottom = if (isFoldable) 28.dp else 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(dimensions.buttonSpacing)
        ) {
            PrimaryActionButton(
                onClick = onAddNewTask,
                modifier = Modifier.fillMaxWidth(dimensions.buttonWidthFraction),
                height = dimensions.buttonHeight,
                textStyle = if (!isFoldable) typography.titleMedium.copy(fontSize = 18.sp) else typography.titleMedium,
                iconSize = if (!isFoldable) 24.dp else 20.dp
            )

            SecondaryActionButton(onClick = onAddSampleTasks)
            Attribution(topPadding = if (isFoldable) 20.dp else 24.dp)
        }
    }
}

/**
 * Standard layout for phones in portrait mode
 */
@Composable
private fun StandardLayout(
    dimensions: NoTasksDimensions,
    typography: Typography,
    dayOfWeek: String,
    scale: Float,
    warmGradient: Brush,
    onAddNewTask: () -> Unit,
    onAddSampleTasks: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(dimensions.horizontalPadding)
    ) {
        // Personalized greeting at the top
        GreetingSection(
            dayOfWeek = dayOfWeek,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(top = 8.dp)
        )

        // Main content in the center
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AnimatedIcon(
                scale = scale,
                gradient = warmGradient,
                size = dimensions.iconSize
            )

            Spacer(modifier = Modifier.height(dimensions.contentSpacing))

            HeadingSection(
                typography = typography,
                dimensions = dimensions,
                modifier = Modifier.padding(horizontal = 24.dp)
            )
        }

        // Action buttons at the bottom
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp, top = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(dimensions.buttonSpacing)
        ) {
            PrimaryActionButton(
                onClick = onAddNewTask,
                modifier = Modifier.fillMaxWidth(dimensions.buttonWidthFraction)
            )

            SecondaryActionButton(onClick = onAddSampleTasks)
            Attribution()
        }
    }
}