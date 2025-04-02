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
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import java.time.format.TextStyle
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
    // Device configuration detection
    val deviceConfig = rememberDeviceConfiguration()

    // Animation for the icon
    val scale = rememberPulseAnimation()

    // Current day for personalized message
    val dayOfWeek = rememberCurrentDayOfWeek()

    // Warm gradient for visual elements
    val warmGradient = rememberWarmGradient()

    // Haptic feedback
    val haptic = LocalHapticFeedback.current

    Surface(
        modifier = modifier
            .fillMaxSize()
            .semantics { contentDescription = "Empty state view when no tasks exist" },
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp
    ) {
        when {
            // Mobile landscape (scrollable)
            deviceConfig.isMobileLandscape -> MobileLandscapeLayout(
                dayOfWeek = dayOfWeek,
                scale = scale,
                warmGradient = warmGradient,
                onAddNewTask = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onAddNewTask()
                },
                onAddSampleTasks = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onAddSampleTasks()
                }
            )

            // Tablet landscape
            deviceConfig.isLandscape && deviceConfig.isTablet -> TabletLandscapeLayout(
                dayOfWeek = dayOfWeek,
                scale = scale,
                warmGradient = warmGradient,
                screenWidth = deviceConfig.screenWidth,
                onAddNewTask = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onAddNewTask()
                },
                onAddSampleTasks = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onAddSampleTasks()
                }
            )

            // Foldable portrait
            deviceConfig.isFoldable -> FoldableLayout(
                dayOfWeek = dayOfWeek,
                scale = scale,
                warmGradient = warmGradient,
                onAddNewTask = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onAddNewTask()
                },
                onAddSampleTasks = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onAddSampleTasks()
                }
            )

            // Tablet portrait
            deviceConfig.isTablet && !deviceConfig.isLandscape -> TabletPortraitLayout(
                dayOfWeek = dayOfWeek,
                scale = scale,
                warmGradient = warmGradient,
                screenWidth = deviceConfig.screenWidth,
                onAddNewTask = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onAddNewTask()
                },
                onAddSampleTasks = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onAddSampleTasks()
                }
            )

            // Standard phone portrait (default)
            else -> PhonePortraitLayout(
                dayOfWeek = dayOfWeek,
                scale = scale,
                warmGradient = warmGradient,
                onAddNewTask = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onAddNewTask()
                },
                onAddSampleTasks = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onAddSampleTasks()
                }
            )
        }
    }
}

// ----- Device Configuration -----

/**
 * Data class for holding device configuration information
 */
private data class DeviceConfiguration(
    val screenWidth: androidx.compose.ui.unit.Dp,
    val screenHeight: androidx.compose.ui.unit.Dp,
    val isLandscape: Boolean,
    val isTablet: Boolean,
    val isFoldable: Boolean,
    val isMobileLandscape: Boolean
)

/**
 * Calculates and provides the current device configuration
 */
@Composable
private fun rememberDeviceConfiguration(): DeviceConfiguration {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val screenHeight = configuration.screenHeightDp.dp
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    // Define device form factors
    val isTablet = screenWidth > 600.dp
    val isFoldable = screenWidth > 580.dp && screenWidth < 700.dp && !isLandscape
    val isMobileLandscape = isLandscape && !isTablet

    return DeviceConfiguration(
        screenWidth = screenWidth,
        screenHeight = screenHeight,
        isLandscape = isLandscape,
        isTablet = isTablet,
        isFoldable = isFoldable,
        isMobileLandscape = isMobileLandscape
    )
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
    val today = LocalDate.now()
    return today.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.getDefault())
}

/**
 * Creates a warm gradient for visual elements
 */
@Composable
private fun rememberWarmGradient(): Brush {
    return Brush.linearGradient(
        colors = listOf(
            MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
            MaterialTheme.colorScheme.tertiary.copy(alpha = 0.6f)
        )
    )
}

// ----- Reusable UI Components -----

/**
 * Shows a personalized greeting message
 */
@Composable
private fun GreetingSection(
    dayOfWeek: String,
    style: androidx.compose.ui.text.TextStyle = MaterialTheme.typography.titleMedium,
    bodyStyle: androidx.compose.ui.text.TextStyle = MaterialTheme.typography.bodyMedium,
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
    size: androidx.compose.ui.unit.Dp,
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
    style: androidx.compose.ui.text.TextStyle = MaterialTheme.typography.headlineSmall,
    bodyStyle: androidx.compose.ui.text.TextStyle = MaterialTheme.typography.bodyLarge,
    textAlign: TextAlign = TextAlign.Center,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = if (textAlign == TextAlign.Center) Alignment.CenterHorizontally else Alignment.Start
    ) {
        Text(
            text = "Mindful Productivity Awaits",
            style = style.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.3.sp
            ),
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = textAlign
        )

        Spacer(modifier = Modifier.height(if (style == MaterialTheme.typography.headlineMedium) 16.dp else 8.dp))

        Text(
            text = "Take a deep breath and start planning your day with intention. What would you like to accomplish?",
            style = bodyStyle,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = textAlign
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
    height: androidx.compose.ui.unit.Dp = 58.dp,
    cornerRadius: androidx.compose.ui.unit.Dp = 20.dp,
    iconSize: androidx.compose.ui.unit.Dp = 20.dp,
    textStyle: androidx.compose.ui.text.TextStyle = MaterialTheme.typography.titleMedium
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
    topPadding: androidx.compose.ui.unit.Dp = 16.dp
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
 * Layout for mobile devices in landscape mode (scrollable)
 */
@Composable
private fun MobileLandscapeLayout(
    dayOfWeek: String,
    scale: Float,
    warmGradient: Brush,
    onAddNewTask: () -> Unit,
    onAddSampleTasks: () -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 16.dp)
            .verticalScroll(scrollState)
    ) {
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

            // Right side: Icon (smaller in landscape)
            AnimatedIcon(
                scale = scale,
                gradient = warmGradient,
                size = 90.dp
            )
        }

        // Header and message
        Spacer(modifier = Modifier.height(16.dp))
        HeadingSection(textAlign = TextAlign.Start)

        // Action buttons
        Spacer(modifier = Modifier.height(24.dp))
        PrimaryActionButton(
            onClick = onAddNewTask,
            modifier = Modifier.fillMaxWidth(),
            height = 54.dp,
            cornerRadius = 16.dp
        )

        Spacer(modifier = Modifier.height(12.dp))
        SecondaryActionButton(onClick = onAddSampleTasks)

        // Attribution
        Spacer(modifier = Modifier.height(8.dp))
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Attribution()
        }

        // Extra space at bottom for scroll-ability
        Spacer(modifier = Modifier.height(16.dp))
    }
}

/**
 * Layout for tablets in landscape mode
 */
@Composable
private fun TabletLandscapeLayout(
    dayOfWeek: String,
    scale: Float,
    warmGradient: Brush,
    screenWidth: androidx.compose.ui.unit.Dp,
    onAddNewTask: () -> Unit,
    onAddSampleTasks: () -> Unit
) {
    // Adjust values based on screen size
    val isLargeTablet = screenWidth > 840.dp
    val horizontalPadding = if (isLargeTablet) 48.dp else 32.dp
    val iconSize = if (isLargeTablet) 160.dp else 130.dp
    val headlineStyle = if (isLargeTablet) {
        MaterialTheme.typography.headlineMedium
    } else {
        MaterialTheme.typography.headlineSmall
    }
    val buttonWidth = if (isLargeTablet) 0.8f else 0.9f

    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = horizontalPadding, vertical = 24.dp),
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
                size = iconSize
            )

            Spacer(modifier = Modifier.height(24.dp))

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
                .padding(start = if (isLargeTablet) 32.dp else 24.dp),
            verticalArrangement = Arrangement.Center
        ) {
            // Greeting
            GreetingSection(
                dayOfWeek = dayOfWeek,
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Take a deep breath and start planning your day with intention. What would you like to accomplish?",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Actions
            Column(
                modifier = Modifier.fillMaxWidth(buttonWidth),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                PrimaryActionButton(
                    onClick = onAddNewTask,
                    modifier = Modifier.fillMaxWidth(),
                    height = 58.dp
                )

                Spacer(modifier = Modifier.height(16.dp))
                SecondaryActionButton(onClick = onAddSampleTasks)
            }
        }
    }
}

/**
 * Layout for foldable devices in portrait mode
 */
@Composable
private fun FoldableLayout(
    dayOfWeek: String,
    scale: Float,
    warmGradient: Brush,
    onAddNewTask: () -> Unit,
    onAddSampleTasks: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 28.dp, vertical = 24.dp)
    ) {
        // Greeting at the top
        GreetingSection(
            dayOfWeek = dayOfWeek,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(top = 8.dp)
        )

        // Main content
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AnimatedIcon(
                scale = scale,
                gradient = warmGradient,
                size = 130.dp
            )

            Spacer(modifier = Modifier.height(32.dp))

            HeadingSection(
                style = MaterialTheme.typography.headlineMedium
            )
        }

        // Action buttons at the bottom
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(bottom = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            PrimaryActionButton(
                onClick = onAddNewTask,
                modifier = Modifier.fillMaxWidth(0.75f),
                height = 58.dp
            )

            SecondaryActionButton(onClick = onAddSampleTasks)
            Attribution(topPadding = 20.dp)
        }
    }
}

/**
 * Layout for tablets in portrait mode
 */
@Composable
private fun TabletPortraitLayout(
    dayOfWeek: String,
    scale: Float,
    warmGradient: Brush,
    screenWidth: androidx.compose.ui.unit.Dp,
    onAddNewTask: () -> Unit,
    onAddSampleTasks: () -> Unit
) {
    // Adjust values based on screen size
    val isLargeTablet = screenWidth > 840.dp
    val horizontalPadding = if (isLargeTablet) 48.dp else 36.dp
    val tabletIconSize = if (isLargeTablet) 180.dp else 150.dp
    val headlineStyle = if (isLargeTablet) {
        MaterialTheme.typography.headlineLarge
    } else {
        MaterialTheme.typography.headlineMedium
    }
    val tabletButtonWidth = if (isLargeTablet) 0.5f else 0.65f
    val headerPadding = if (isLargeTablet) 64.dp else 40.dp

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = horizontalPadding, vertical = 32.dp)
    ) {
        // Greeting at the top
        GreetingSection(
            dayOfWeek = dayOfWeek,
            style = MaterialTheme.typography.titleLarge,
            bodyStyle = MaterialTheme.typography.bodyLarge,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(top = 16.dp)
        )

        // Main content
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AnimatedIcon(
                scale = scale,
                gradient = warmGradient,
                size = tabletIconSize
            )

            Spacer(modifier = Modifier.height(40.dp))

            Text(
                text = "Mindful Productivity Awaits",
                style = headlineStyle.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.3.sp
                ),
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Take a deep breath and start planning your day with intention. What would you like to accomplish?",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = headerPadding)
            )
        }

        // Action buttons at the bottom
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            PrimaryActionButton(
                onClick = onAddNewTask,
                modifier = Modifier.fillMaxWidth(tabletButtonWidth),
                height = 64.dp,
                textStyle = MaterialTheme.typography.titleMedium.copy(fontSize = 18.sp),
                iconSize = 24.dp
            )

            // Secondary action with centered text for tablets
            SecondaryActionButton(onClick = onAddSampleTasks)

            Attribution(topPadding = 24.dp)
        }
    }
}

/**
 * Layout for standard phones in portrait mode
 */
@Composable
private fun PhonePortraitLayout(
    dayOfWeek: String,
    scale: Float,
    warmGradient: Brush,
    onAddNewTask: () -> Unit,
    onAddSampleTasks: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
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
                size = 120.dp
            )

            Spacer(modifier = Modifier.height(32.dp))

            HeadingSection(
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            PrimaryActionButton(
                onClick = onAddNewTask,
                modifier = Modifier.fillMaxWidth(0.85f)
            )

            SecondaryActionButton(onClick = onAddSampleTasks)
            Attribution(topPadding = 16.dp)
        }
    }
}