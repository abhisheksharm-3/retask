package `in`.xroden.retask.ui.components

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandIn
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
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
import androidx.compose.material3.FilledTonalIconButton
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import `in`.xroden.retask.data.model.Task
import kotlinx.coroutines.delay

/**
 * A modern, adaptive task card component that displays task information
 * and provides actions for completing, editing, and snoozing tasks.
 *
 * @param task The task to display
 * @param totalTasks Total number of active tasks
 * @param onCompleteClick Callback when the complete button is clicked
 * @param onSnoozeClick Callback when the snooze button is clicked
 * @param onEditClick Callback when the edit button is clicked
 * @param modifier Modifier for the card
 */
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
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    // Task visual attributes and states
    val cardState = remember(task, totalTasks) {
        TaskCardState(task, totalTasks)
    }

    // Animation state
    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(key1 = Unit) {
        delay(150)
        isVisible = true
    }

    // Animation values
    val animations = rememberTaskCardAnimations(isVisible, cardState.isUrgent)

    // Card container
    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(animationSpec = tween(durationMillis = 400)) +
                expandIn(animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing)),
        exit = fadeOut(animationSpec = tween(durationMillis = 200)) +
                shrinkOut(animationSpec = tween(durationMillis = 250))
    ) {
        ElevatedCard(
            modifier = modifier
                .fillMaxSize()
                .padding(12.dp)
                .scale(animations.cardScale)
                .shadow(
                    elevation = animations.cardElevation,
                    shape = RoundedCornerShape(28.dp),
                    spotColor = cardState.backgroundColor.copy(alpha = 0.25f)
                )
                .semantics {
                    contentDescription = cardState.accessibilityDescription
                },
            shape = RoundedCornerShape(28.dp),
            elevation = CardDefaults.elevatedCardElevation(
                defaultElevation = animations.cardElevation,
                pressedElevation = 2.dp
            ),
            colors = CardDefaults.elevatedCardColors(
                containerColor = cardState.backgroundColor
            )
        ) {
            CardContent(
                cardState = cardState,
                isLandscape = isLandscape,
                onCompleteClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onCompleteClick()
                },
                onEditClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onEditClick()
                },
                onSnoozeClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onSnoozeClick()
                }
            )
        }
    }
}

/**
 * Main card content that adapts based on orientation
 */
@Composable
private fun CardContent(
    cardState: TaskCardState,
    isLandscape: Boolean,
    onCompleteClick: () -> Unit,
    onEditClick: () -> Unit,
    onSnoozeClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(cardState.backgroundGradient)
            .padding(24.dp)
    ) {
        if (isLandscape) {
            LandscapeContent(cardState, onCompleteClick, onEditClick, onSnoozeClick)
        } else {
            PortraitContent(cardState, onCompleteClick, onEditClick, onSnoozeClick)
        }
    }
}

/**
 * Landscape layout content
 */
@Composable
private fun LandscapeContent(
    cardState: TaskCardState,
    onCompleteClick: () -> Unit,
    onEditClick: () -> Unit,
    onSnoozeClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxSize(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left side: Task counter and content
        Column(
            modifier = Modifier
                .weight(0.65f)
                .padding(end = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Task counter with optional urgent indicator
            TaskCounter(
                totalTasks = cardState.totalTasks,
                isUrgent = cardState.isUrgent,
                textColor = cardState.secondaryTextColor
            )

            // Task title with enhanced visual style
            Text(
                text = cardState.task.title,
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.5).sp,
                    lineHeight = 32.sp
                ),
                color = cardState.textColor,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            // Due time indicator with contextual styling
            DueTimeIndicator(
                dueText = cardState.task.getDueText(),
                isUrgent = cardState.isUrgent,
                textColor = cardState.textColor
            )
        }

        // Right side: Action buttons
        Column(
            modifier = Modifier
                .weight(0.35f)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.SpaceEvenly,
            horizontalAlignment = Alignment.End
        ) {
            TaskActionButton(
                icon = Icons.Rounded.Check,
                label = "Complete",
                onClick = onCompleteClick,
                isPrimary = true,
                isUrgent = cardState.isUrgent,
                textColor = cardState.textColor,
                tint = MaterialTheme.colorScheme.primary,
                isLandscape = true
            )

            TaskActionButton(
                icon = Icons.Rounded.Edit,
                label = "Edit",
                onClick = onEditClick,
                textColor = cardState.textColor,
                tint = MaterialTheme.colorScheme.secondary,
                isLandscape = true
            )

            TaskActionButton(
                icon = Icons.Rounded.MoreTime,
                label = "Snooze",
                onClick = onSnoozeClick,
                isUrgent = cardState.isUrgentSnooze,
                textColor = cardState.textColor,
                tint = MaterialTheme.colorScheme.tertiary,
                isLandscape = true
            )
        }
    }
}

/**
 * Portrait layout content
 */
@Composable
private fun PortraitContent(
    cardState: TaskCardState,
    onCompleteClick: () -> Unit,
    onEditClick: () -> Unit,
    onSnoozeClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Top: Task counter
        TaskCounter(
            totalTasks = cardState.totalTasks,
            isUrgent = cardState.isUrgent,
            textColor = cardState.secondaryTextColor,
            horizontalArrangement = Arrangement.Center
        )

        // Center: Task content
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.weight(1f, fill = false),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = cardState.task.title,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.5).sp,
                    lineHeight = 40.sp
                ),
                color = cardState.textColor,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            DueTimeIndicator(
                dueText = cardState.task.getDueText(),
                isUrgent = cardState.isUrgent,
                textColor = cardState.textColor
            )
        }

        // Bottom: Action buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TaskActionButton(
                icon = Icons.Rounded.Check,
                label = "Complete",
                onClick = onCompleteClick,
                isPrimary = true,
                isUrgent = cardState.isUrgent,
                textColor = cardState.textColor,
                tint = MaterialTheme.colorScheme.primary
            )

            TaskActionButton(
                icon = Icons.Rounded.Edit,
                label = "Edit",
                onClick = onEditClick,
                textColor = cardState.textColor,
                tint = MaterialTheme.colorScheme.secondary
            )

            TaskActionButton(
                icon = Icons.Rounded.MoreTime,
                label = "Snooze",
                onClick = onSnoozeClick,
                isUrgent = cardState.isUrgentSnooze,
                textColor = cardState.textColor,
                tint = MaterialTheme.colorScheme.tertiary
            )
        }
    }
}

/**
 * Task counter component that shows total tasks and urgency
 */
@Composable
private fun TaskCounter(
    totalTasks: Int,
    isUrgent: Boolean,
    textColor: Color,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start
) {
    val taskCountText = if (totalTasks == 1) "1 Task Remaining" else "$totalTasks Tasks Remaining"

    Row(
        modifier = Modifier
            .wrapContentHeight()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0x1A000000).copy(alpha = 0.15f))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = horizontalArrangement
    ) {
        if (isUrgent) {
            Icon(
                imageVector = Icons.Rounded.Timer,
                contentDescription = null,
                tint = textColor.copy(alpha = 0.9f),
                modifier = Modifier
                    .size(14.dp)
                    .alpha(0.9f)
            )
            Spacer(modifier = Modifier.width(8.dp))
        }

        Text(
            text = taskCountText,
            style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = FontWeight.Medium,
                letterSpacing = 0.2.sp
            ),
            color = textColor
        )
    }
}

/**
 * Due time indicator with contextual styling for urgency
 */
@Composable
private fun DueTimeIndicator(
    dueText: String,
    isUrgent: Boolean,
    textColor: Color
) {
    Surface(
        color = if (isUrgent)
            MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.25f)
        else
            Color.Transparent,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.wrapContentHeight()
    ) {
        Text(
            text = dueText,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = if (isUrgent) FontWeight.Bold else FontWeight.Medium
            ),
            color = if (isUrgent)
                MaterialTheme.colorScheme.error.copy(alpha = 0.9f)
            else
                textColor.copy(alpha = 0.85f),
            modifier = Modifier.padding(
                horizontal = if (isUrgent) 12.dp else 8.dp,
                vertical = if (isUrgent) 4.dp else 2.dp
            )
        )
    }
}

/**
 * Redesigned task action button with improved visual feedback
 */
@Composable
private fun TaskActionButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    tint: Color,
    textColor: Color,
    isPrimary: Boolean = false,
    isUrgent: Boolean = false,
    isLandscape: Boolean = false,
    iconSize: Dp = if (isPrimary) 24.dp else 20.dp
) {
    val buttonScale by animateFloatAsState(
        targetValue = if (isPrimary && isUrgent) 1.08f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "buttonScale"
    )

    val containerColor = when {
        isPrimary -> tint
        isUrgent && label == "Snooze" -> MaterialTheme.colorScheme.errorContainer
        else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f)
    }

    val contentColor = when {
        isPrimary -> Color.White
        isUrgent && label == "Snooze" -> MaterialTheme.colorScheme.error
        else -> tint
    }

    if (isLandscape) {
        // Landscape orientation - horizontal layout
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End,
            modifier = Modifier
                .scale(buttonScale)
                .height(IntrinsicSize.Min)
                .semantics {
                    contentDescription = "$label task"
                }
                .padding(vertical = 4.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = if (isPrimary || isUrgent) FontWeight.SemiBold else FontWeight.Medium
                ),
                color = textColor.copy(alpha = if (isPrimary || isUrgent) 1f else 0.9f)
            )

            Spacer(modifier = Modifier.width(8.dp))

            if (isPrimary) {
                FilledIconButton(
                    onClick = onClick,
                    modifier = Modifier
                        .size(46.dp)
                        .shadow(6.dp, CircleShape),
                    shape = CircleShape,
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = containerColor,
                        contentColor = contentColor
                    )
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(iconSize)
                    )
                }
            } else {
                FilledTonalIconButton(
                    onClick = onClick,
                    modifier = Modifier
                        .size(40.dp)
                        .shadow(4.dp, CircleShape),
                    shape = CircleShape,
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = containerColor,
                        contentColor = contentColor
                    )
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(iconSize)
                    )
                }
            }
        }
    } else {
        // Portrait orientation - vertical layout
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .scale(buttonScale)
                .semantics {
                    contentDescription = "$label task"
                }
        ) {
            if (isPrimary) {
                FilledIconButton(
                    onClick = onClick,
                    modifier = Modifier
                        .size(54.dp)
                        .shadow(
                            elevation = 6.dp,
                            shape = CircleShape,
                            spotColor = if (isUrgent) tint.copy(alpha = 0.4f) else tint.copy(alpha = 0.2f)
                        ),
                    shape = CircleShape,
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = containerColor,
                        contentColor = contentColor
                    )
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(iconSize + 2.dp)
                    )
                }
            } else {
                FilledTonalIconButton(
                    onClick = onClick,
                    modifier = Modifier
                        .size(46.dp)
                        .shadow(
                            elevation = 4.dp,
                            shape = CircleShape,
                            spotColor = if (isUrgent) tint.copy(alpha = 0.4f) else tint.copy(alpha = 0.2f)
                        ),
                    shape = CircleShape,
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = containerColor,
                        contentColor = contentColor
                    )
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(iconSize)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = if (isPrimary || isUrgent) FontWeight.SemiBold else FontWeight.Medium,
                    letterSpacing = 0.3.sp
                ),
                color = textColor.copy(alpha = if (isPrimary || isUrgent) 1f else 0.9f)
            )
        }
    }
}

/**
 * Task card state and derived visual properties
 */
private data class TaskCardState(
    val task: Task,
    val totalTasks: Int
) {
    // Time-based states
    val now = System.currentTimeMillis()
    val timeRemaining = task.dueDate - now
    val isUrgent = timeRemaining in 1..900000 // 15 minutes
    val isUrgentSnooze = isUrgent && timeRemaining < 300000 // 5 minutes

    // Visual properties
    val backgroundColor = task.getBackgroundColor()
    val luminance = backgroundColor.luminance()
    val textColor = if (luminance < 0.5f) Color.White else Color.Black
    val secondaryTextColor = if (luminance < 0.5f)
        Color.White.copy(alpha = 0.75f) else Color.Black.copy(alpha = 0.65f)

    // Generate gradient based on urgency
    val backgroundGradient = if (isUrgent) {

        Brush.radialGradient(
            colors = listOf(
                backgroundColor.copy(alpha = 0.95f).copy(red = minOf(1f, backgroundColor.red + 0.05f)),
                backgroundColor.copy(alpha = 1f),
                backgroundColor.copy(alpha = 0.98f).copy(red = minOf(1f, backgroundColor.red + 0.1f))
            ),
            radius = 1200f
        )
    } else {
        // Regular subtle gradient
        Brush.linearGradient(
            colors = listOf(
                backgroundColor.copy(alpha = 0.95f),
                backgroundColor,
                backgroundColor.copy(alpha = 0.97f)
            )
        )
    }

    // Accessibility description for screen readers
    val accessibilityDescription = buildString {
        append("Task: ${task.title}, due ${task.getDueText()}")
        if (isUrgent) append(", urgent")
        append(", $totalTasks ")
        append(if (totalTasks == 1) "task remaining" else "tasks remaining")
    }
}

/**
 * Animation values for the task card
 */
@Composable
private fun rememberTaskCardAnimations(
    isVisible: Boolean,
    isUrgent: Boolean
): TaskCardAnimations {
    val cardScale by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0.92f,
        animationSpec = tween(
            durationMillis = 350,
            easing = FastOutSlowInEasing
        ),
        label = "cardScale"
    )

    val baseElevation = if (isUrgent) 6f else 4f

    val cardElevation by animateFloatAsState(
        targetValue = if (isVisible) baseElevation else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "cardElevation"
    )

    return TaskCardAnimations(cardScale, cardElevation.dp)
}

private data class TaskCardAnimations(
    val cardScale: Float,
    val cardElevation: Dp
)