package `in`.xroden.retask.ui.components

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.res.Configuration
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.EmojiEmotions
import androidx.compose.material.icons.rounded.Spa
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.platform.LocalContext
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
 * Empty state component shown when no tasks exist, adapting its layout based on device configuration.
 */
@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun NoTasks(
    onAddSampleTasks: () -> Unit,
    onAddNewTask: () -> Unit,
    modifier: Modifier = Modifier
) {
    val deviceConfig = rememberDeviceConfig()
    val dimensions = rememberNoTasksDimensions(deviceConfig)
    val content = rememberNoTasksContent()

    Surface(
        modifier = modifier
            .fillMaxSize()
            .semantics { contentDescription = "Empty state view when no tasks exist" },
        color = MaterialTheme.colorScheme.surface
    ) {
        when {
            deviceConfig.isMobileLandscape ->
                LandscapeLayout(dimensions, content, onAddNewTask, onAddSampleTasks, isScrollable = true)

            deviceConfig.isTablet && deviceConfig.isLandscape ->
                TabletLandscapeLayout(dimensions, content, onAddNewTask, onAddSampleTasks)

            else -> // Covers Portrait Mobile, Tablet, and Foldable
                StandardPortraitLayout(dimensions, content, onAddNewTask, onAddSampleTasks)
        }
    }
}

// ----- Data and Configuration -----

@Immutable
private data class NoTasksContent(
    val dayOfWeek: String,
    val scale: Float,
    val warmGradient: Brush,
    val typography: Typography
)

@Immutable
private data class DeviceConfig(
    val widthSizeClass: WindowWidthSizeClass,
    val isLandscape: Boolean
) {
    val isTablet: Boolean get() = widthSizeClass >= WindowWidthSizeClass.Medium
    val isMobileLandscape: Boolean get() = widthSizeClass == WindowWidthSizeClass.Compact && isLandscape
}

@Stable
private class NoTasksDimensions(config: DeviceConfig) {
    val iconSize: Dp = when {
        config.isTablet && config.isLandscape -> 180.dp
        config.isTablet -> 150.dp
        config.isMobileLandscape -> 90.dp
        else -> 120.dp
    }
    val buttonHeight: Dp = if (config.isTablet) 64.dp else 54.dp
    val buttonWidthFraction: Float = if (config.isTablet) 0.65f else 0.85f
    val horizontalPadding: Dp = if (config.isTablet) 48.dp else 24.dp
    val contentSpacing: Dp = if (config.isTablet) 40.dp else 24.dp
    val buttonSpacing: Dp = if (config.isTablet) 20.dp else 16.dp
}

// ----- `remember` helper functions -----

private fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
private fun rememberDeviceConfig(): DeviceConfig {
    // 1. Find the current activity from the context
    val activity = LocalContext.current.findActivity()
        ?: error("No Activity found in context. This composable must be used in an Activity.")

    // 2. Pass the activity to the function
    val windowSizeClass = calculateWindowSizeClass(activity)

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    return remember(windowSizeClass, isLandscape) {
        DeviceConfig(windowSizeClass.widthSizeClass, isLandscape)
    }
}

@Composable
private fun rememberNoTasksDimensions(deviceConfig: DeviceConfig): NoTasksDimensions {
    return remember(deviceConfig.widthSizeClass, deviceConfig.isLandscape) {
        NoTasksDimensions(deviceConfig)
    }
}

@Composable
private fun rememberNoTasksContent(): NoTasksContent {
    val infiniteTransition = rememberInfiniteTransition("EmptyStateAnimation")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(tween(2000), RepeatMode.Reverse),
        label = "Pulse"
    )

    val dayOfWeek = remember {
        LocalDate.now().dayOfWeek.getDisplayName(DateTextStyle.FULL, Locale.getDefault())
    }

    val primary = MaterialTheme.colorScheme.primary
    val tertiary = MaterialTheme.colorScheme.tertiary
    val warmGradient = remember(primary, tertiary) {
        Brush.linearGradient(colors = listOf(primary.copy(0.7f), tertiary.copy(0.6f)))
    }

    val typography = MaterialTheme.typography

    return remember(dayOfWeek, scale, warmGradient, typography) {
        NoTasksContent(dayOfWeek, scale, warmGradient, typography)
    }
}

// ----- Reusable UI Components -----

@Composable
private fun GreetingSection(dayOfWeek: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(
            text = "Happy $dayOfWeek!",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "Your schedule is clear today",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun AnimatedIcon(scale: Float, gradient: Brush, size: Dp, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.size(size).scale(scale).clip(CircleShape).background(gradient),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.Rounded.Spa,
            contentDescription = null,
            modifier = Modifier.size(size * 0.45f),
            tint = Color.White.copy(alpha = 0.9f)
        )
        Box(modifier = Modifier.align(Alignment.TopEnd).offset((-size * 0.07f), size * 0.2f)) {
            Icon(
                imageVector = Icons.Rounded.EmojiEmotions,
                contentDescription = null,
                modifier = Modifier.size(size * 0.27f).rotate(-15f),
                tint = Color.White.copy(alpha = 0.85f)
            )
        }
    }
}

@Composable
private fun HeadingSection(dimensions: NoTasksDimensions, typography: Typography, textAlign: TextAlign, modifier: Modifier = Modifier) {
    val headlineStyle = when {
        dimensions.iconSize > 140.dp -> typography.headlineLarge
        dimensions.iconSize > 100.dp -> typography.headlineMedium
        else -> typography.headlineSmall
    }
    Column(
        modifier = modifier,
        horizontalAlignment = if (textAlign == TextAlign.Center) Alignment.CenterHorizontally else Alignment.Start
    ) {
        Text(
            text = "Mindful Productivity Awaits",
            style = headlineStyle.copy(fontWeight = FontWeight.Bold, letterSpacing = 0.3.sp),
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = textAlign
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Take a deep breath and start planning your day with intention.",
            style = typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = textAlign,
            modifier = Modifier.padding(horizontal = 8.dp)
        )
    }
}

@Composable
private fun ActionButtons(
    onAddNewTask: () -> Unit,
    onAddSampleTasks: () -> Unit,
    dimensions: NoTasksDimensions,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(dimensions.buttonSpacing)
    ) {
        ElevatedButton(
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onAddNewTask()
            },
            modifier = Modifier
                .height(dimensions.buttonHeight)
                .fillMaxWidth(dimensions.buttonWidthFraction)
                .semantics { contentDescription = "Add new task" },
            shape = RoundedCornerShape(20.dp)
        ) {
            Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(Modifier.size(ButtonDefaults.IconSpacing))
            Text("Create First Task", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        }
        Text(
            text = "Or try some inspiration",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Medium,
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .clickable {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onAddSampleTasks()
                }
                .padding(vertical = 12.dp, horizontal = 24.dp)
                .semantics { contentDescription = "Add sample tasks" },
        )
    }
}

// ----- Layout Implementations -----

@Composable
private fun StandardPortraitLayout(
    dimensions: NoTasksDimensions,
    content: NoTasksContent,
    onAddNewTask: () -> Unit,
    onAddSampleTasks: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = dimensions.horizontalPadding, vertical = 16.dp)
    ) {
        GreetingSection(content.dayOfWeek, modifier = Modifier.align(Alignment.TopStart).padding(top = 8.dp))
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AnimatedIcon(content.scale, content.warmGradient, dimensions.iconSize)
            Spacer(Modifier.height(dimensions.contentSpacing))
            HeadingSection(dimensions, content.typography, TextAlign.Center, modifier = Modifier.padding(horizontal = 24.dp))
        }
        Column(
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 24.dp, top = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ActionButtons(onAddNewTask, onAddSampleTasks, dimensions)
            Text(
                text = "Created with ❤️ by @abhisheksharm-3",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                modifier = Modifier.padding(top = dimensions.buttonSpacing)
            )
        }
    }
}

@Composable
private fun LandscapeLayout(
    dimensions: NoTasksDimensions,
    content: NoTasksContent,
    onAddNewTask: () -> Unit,
    onAddSampleTasks: () -> Unit,
    isScrollable: Boolean
) {
    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = dimensions.horizontalPadding, vertical = 16.dp)
            .then(if (isScrollable) Modifier.verticalScroll(scrollState) else Modifier)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            GreetingSection(content.dayOfWeek, modifier = Modifier.weight(1f))
            AnimatedIcon(content.scale, content.warmGradient, dimensions.iconSize)
        }
        Spacer(Modifier.height(16.dp))
        HeadingSection(dimensions, content.typography, TextAlign.Start)
        Spacer(Modifier.height(24.dp))
        ActionButtons(onAddNewTask, onAddSampleTasks, dimensions, modifier = Modifier.fillMaxWidth())
        Box(modifier = Modifier.fillMaxWidth().padding(vertical = dimensions.buttonSpacing), contentAlignment = Alignment.Center) {
            Text(
                text = "Created with ❤️ by @abhisheksharm-3",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun TabletLandscapeLayout(
    dimensions: NoTasksDimensions,
    content: NoTasksContent,
    onAddNewTask: () -> Unit,
    onAddSampleTasks: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxSize().padding(horizontal = dimensions.horizontalPadding, vertical = 24.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AnimatedIcon(content.scale, content.warmGradient, dimensions.iconSize)
            Spacer(Modifier.height(dimensions.contentSpacing))
            HeadingSection(dimensions, content.typography, TextAlign.Center)
        }
        Column(
            modifier = Modifier.weight(1f).padding(start = dimensions.contentSpacing),
            verticalArrangement = Arrangement.Center
        ) {
            GreetingSection(dayOfWeek = content.dayOfWeek)
            Spacer(Modifier.height(32.dp))
            Text(
                "What would you like to accomplish today?",
                style = content.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(40.dp))
            ActionButtons(onAddNewTask, onAddSampleTasks, dimensions, modifier = Modifier.fillMaxWidth())
        }
    }
}