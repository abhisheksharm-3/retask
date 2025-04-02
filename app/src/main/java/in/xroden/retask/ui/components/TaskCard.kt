package `in`.xroden.retask.ui.components

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.MoreTime
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import `in`.xroden.retask.data.model.Task
import kotlinx.coroutines.delay

@Composable
fun TaskCard(
    task: Task,
    totalTasks: Int,
    onCompleteClick: () -> Unit,
    onSnoozeClick: () -> Unit,
    onEditClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current

    // Add configuration for landscape detection
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    // State for card appearance animation
    var isVisible by remember { mutableStateOf(false) }

    // Animate entrance for a more delightful experience
    LaunchedEffect(key1 = Unit) {
        delay(150)
        isVisible = true
    }

    // Animate color changes for smooth transitions
    val backgroundColor by animateColorAsState(
        targetValue = task.getBackgroundColor(),
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
        label = "backgroundColor"
    )

    // Add subtle elevation animation with springy effect
    val cardElevation by animateFloatAsState(
        targetValue = if (isVisible) 4f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "cardElevation"
    )

    // Scale animation for entrance
    val cardScale by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0.92f,
        animationSpec = tween(
            durationMillis = 350,
            easing = FastOutSlowInEasing
        ),
        label = "cardScale"
    )

    // Calculate optimal text color based on background luminance
    val luminance = backgroundColor.luminance()
    val textColor = if (luminance < 0.5f) Color.White else Color.Black
    val secondaryTextColor = if (luminance < 0.5f)
        Color.White.copy(alpha = 0.75f) else Color.Black.copy(alpha = 0.65f)

    // Determine if task is urgent (due soon)
    val now = System.currentTimeMillis()
    val timeRemaining = task.dueDate - now
    val isUrgent = timeRemaining in 1..900000 // Due within 15 minutes

    // Calculate task gradient based on urgency
    val gradientColors = when {
        isUrgent -> {
            listOf(
                backgroundColor,
                backgroundColor.copy(red = minOf(1f, backgroundColor.red + 0.1f))
            )
        }
        else -> {
            listOf(
                backgroundColor,
                backgroundColor.copy(alpha = 0.95f)
            )
        }
    }

    // Properly handle singular/plural for task count
    val taskCountText = if (totalTasks == 1) "1 Task Remaining" else "$totalTasks Tasks Remaining"

    // Create a semantic description for the entire card including urgency
    val urgencyText = if (isUrgent) ", urgent" else ""
    val cardDescription =
        "Task: ${task.title}, due ${task.getDueText()}$urgencyText, $taskCountText"

    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(animationSpec = tween(durationMillis = 400)),
        exit = fadeOut(animationSpec = tween(durationMillis = 200))
    ) {
        ElevatedCard(
            modifier = modifier
                .fillMaxSize()
                .padding(12.dp)
                .scale(cardScale)
                .shadow(
                    elevation = cardElevation.dp,
                    shape = RoundedCornerShape(28.dp),
                    spotColor = backgroundColor.copy(alpha = 0.25f)
                )
                .semantics {
                    contentDescription = cardDescription
                },
            shape = RoundedCornerShape(28.dp),
            elevation = CardDefaults.elevatedCardElevation(
                defaultElevation = cardElevation.dp,
                pressedElevation = 2.dp
            ),
            colors = CardDefaults.elevatedCardColors(
                containerColor = backgroundColor
            )
        ) {
            // Improved background with radial highlight for subtle depth
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        if (isLandscape) {
                            Brush.horizontalGradient(colors = gradientColors)
                        } else {
                            Brush.verticalGradient(colors = gradientColors)
                        }
                    )
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                Color.White.copy(alpha = if (luminance < 0.5f) 0.07f else 0.12f),
                                Color.Transparent
                            ),
                            radius = 500f
                        )
                    )
                    .padding(24.dp)
            ) {
                if (isLandscape) {
                    // LANDSCAPE LAYOUT: Row-based to avoid overlap
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Left side: Task counter and content
                        Column(
                            modifier = Modifier
                                .weight(0.65f)
                                .padding(end = 16.dp)
                        ) {
                            // Task counter row
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0x1A000000).copy(alpha = 0.2f))
                                    .padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (isUrgent) {
                                    Icon(
                                        imageVector = Icons.Rounded.Timer,
                                        contentDescription = null,
                                        tint = if (luminance < 0.5f) Color.White else Color.Black,
                                        modifier = Modifier
                                            .size(14.dp)
                                            .alpha(0.9f)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                }

                                Text(
                                    text = taskCountText,
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontWeight = FontWeight.Medium,
                                        letterSpacing = 0.2.sp
                                    ),
                                    color = secondaryTextColor
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Task title
                            Text(
                                text = task.title,
                                style = MaterialTheme.typography.headlineSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = (-0.5).sp,
                                    lineHeight = 32.sp
                                ),
                                color = textColor,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // Due time
                            Surface(
                                color = if (isUrgent)
                                    Color(0x22FF0000).copy(alpha = if (luminance < 0.5f) 0.3f else 0.1f)
                                else
                                    Color.Transparent,
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.padding(vertical = 4.dp)
                            ) {
                                Text(
                                    text = task.getDueText(),
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = if (isUrgent) FontWeight.Bold else FontWeight.Medium
                                    ),
                                    color = if (luminance < 0.5f) Color.White.copy(alpha = 0.9f) else Color.Black.copy(
                                        alpha = 0.8f
                                    ),
                                    modifier = Modifier.padding(
                                        horizontal = if (isUrgent) 12.dp else 6.dp,
                                        vertical = if (isUrgent) 4.dp else 2.dp
                                    )
                                )
                            }
                        }

                        // Right side: Action buttons in a vertical column
                        Column(
                            modifier = Modifier
                                .weight(0.35f)
                                .fillMaxHeight(),
                            verticalArrangement = Arrangement.SpaceEvenly,
                            horizontalAlignment = Alignment.End
                        ) {
                            ActionButton(
                                icon = Icons.Rounded.Check,
                                description = "Complete",
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    onCompleteClick()
                                },
                                tint = MaterialTheme.colorScheme.primary,
                                textColor = textColor,
                                isPrimary = true,
                                isUrgent = isUrgent,
                                isLandscape = true
                            )

                            ActionButton(
                                icon = Icons.Rounded.Edit,
                                description = "Edit",
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    onEditClick()
                                },
                                tint = MaterialTheme.colorScheme.secondary,
                                textColor = textColor,
                                isPrimary = false,
                                isLandscape = true
                            )

                            ActionButton(
                                icon = Icons.Rounded.MoreTime,
                                description = "Snooze",
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    onSnoozeClick()
                                },
                                tint = MaterialTheme.colorScheme.tertiary,
                                textColor = textColor,
                                isPrimary = false,
                                isUrgent = isUrgent && timeRemaining < 300000, // Extra emphasis if < 5 min
                                isLandscape = true
                            )
                        }
                    }
                } else {
                    // PORTRAIT LAYOUT (original flow but with subtle improvements)
                    // Task counter and urgency indicator
                    Row(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0x1A000000).copy(alpha = 0.2f))
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        if (isUrgent) {
                            Icon(
                                imageVector = Icons.Rounded.Timer,
                                contentDescription = null,
                                tint = if (luminance < 0.5f) Color.White else Color.Black,
                                modifier = Modifier
                                    .size(14.dp)
                                    .alpha(0.9f)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                        }

                        Text(
                            text = taskCountText,
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Medium,
                                letterSpacing = 0.2.sp
                            ),
                            color = secondaryTextColor
                        )
                    }

                    // Main content in the center with improved typography
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = task.title,
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = (-0.5).sp,
                                lineHeight = 40.sp
                            ),
                            color = textColor,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Due time with adaptive styling based on urgency
                        Surface(
                            color = if (isUrgent)
                                Color(0x22FF0000).copy(alpha = if (luminance < 0.5f) 0.3f else 0.1f)
                            else
                                Color.Transparent,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = task.getDueText(),
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = if (isUrgent) FontWeight.Bold else FontWeight.Medium
                                ),
                                color = if (luminance < 0.5f) Color.White.copy(alpha = 0.9f) else Color.Black.copy(
                                    alpha = 0.8f
                                ),
                                modifier = Modifier.padding(
                                    horizontal = if (isUrgent) 12.dp else 6.dp,
                                    vertical = if (isUrgent) 4.dp else 2.dp
                                )
                            )
                        }
                    }

                    // Action buttons at the bottom with improved spacing and visual design
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter)
                            .padding(top = 16.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ActionButton(
                            icon = Icons.Rounded.Check,
                            description = "Complete",
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onCompleteClick()
                            },
                            tint = MaterialTheme.colorScheme.primary,
                            textColor = textColor,
                            isPrimary = true,
                            isUrgent = isUrgent
                        )

                        ActionButton(
                            icon = Icons.Rounded.Edit,
                            description = "Edit",
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onEditClick()
                            },
                            tint = MaterialTheme.colorScheme.secondary,
                            textColor = textColor,
                            isPrimary = false
                        )

                        ActionButton(
                            icon = Icons.Rounded.MoreTime,
                            description = "Snooze",
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onSnoozeClick()
                            },
                            tint = MaterialTheme.colorScheme.tertiary,
                            textColor = textColor,
                            isPrimary = false,
                            isUrgent = isUrgent && timeRemaining < 300000 // Extra emphasis if < 5 min
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ActionButton(
    icon: ImageVector,
    description: String,
    onClick: () -> Unit,
    tint: Color,
    textColor: Color,
    isPrimary: Boolean = false,
    isUrgent: Boolean = false,
    isLandscape: Boolean = false
) {
    // Scale animation for primary/urgent buttons
    val buttonScale by animateFloatAsState(
        targetValue = if (isPrimary && isUrgent) 1.08f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "buttonScale"
    )

    if (isLandscape) {
        // Landscape layout: horizontal buttons that don't overlap
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End,
            modifier = Modifier
                .scale(buttonScale)
                .semantics {
                    contentDescription = "$description task"
                }
                .padding(vertical = 4.dp)
        ) {
            Text(
                text = description,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = if (isPrimary || isUrgent) FontWeight.SemiBold else FontWeight.Medium
                ),
                color = textColor.copy(alpha = if (isPrimary || isUrgent) 1f else 0.9f)
            )

            Spacer(modifier = Modifier.width(8.dp))

            FilledIconButton(
                onClick = onClick,
                modifier = Modifier
                    .size(if (isPrimary) 46.dp else 40.dp)
                    .shadow(
                        elevation = if (isPrimary) 6.dp else 4.dp,
                        shape = CircleShape,
                        spotColor = if (isUrgent && (isPrimary || description == "Snooze"))
                            tint.copy(alpha = 0.4f)
                        else
                            tint.copy(alpha = 0.2f)
                    ),
                shape = CircleShape,
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = when {
                        isPrimary -> tint
                        isUrgent && description == "Snooze" ->
                            MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.9f)
                        else -> MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                    },
                    contentColor = when {
                        isPrimary -> Color.White
                        isUrgent && description == "Snooze" -> MaterialTheme.colorScheme.error
                        else -> tint
                    }
                )
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(if (isPrimary) 22.dp else 18.dp)
                )
            }
        }
    } else {
        // Original portrait layout
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .scale(buttonScale)
                .semantics {
                    contentDescription = "$description task"
                }
        ) {
            FilledIconButton(
                onClick = onClick,
                modifier = Modifier
                    .size(if (isPrimary) 52.dp else 44.dp)
                    .shadow(
                        elevation = if (isPrimary) 6.dp else 4.dp,
                        shape = CircleShape,
                        spotColor = if (isUrgent && (isPrimary || description == "Snooze"))
                            tint.copy(alpha = 0.4f)
                        else
                            tint.copy(alpha = 0.2f)
                    ),
                shape = CircleShape,
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = when {
                        isPrimary -> tint
                        isUrgent && description == "Snooze" ->
                            MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.9f)
                        else -> MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                    },
                    contentColor = when {
                        isPrimary -> Color.White
                        isUrgent && description == "Snooze" -> MaterialTheme.colorScheme.error
                        else -> tint
                    }
                )
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(if (isPrimary) 24.dp else 20.dp)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = description,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = if (isPrimary || isUrgent) FontWeight.SemiBold else FontWeight.Medium
                ),
                color = textColor.copy(alpha = if (isPrimary || isUrgent) 1f else 0.9f)
            )
        }
    }
}