package `in`.xroden.retask.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import `in`.xroden.retask.data.model.Task
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TaskDrawer(
    tasks: List<Task>,
    selectedTask: Task?,
    onTaskClick: (Task) -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current

    // Get the current date for grouping tasks
    val today = remember { System.currentTimeMillis() }
    val dateFormat = remember { SimpleDateFormat("EEEE, MMMM d", Locale.getDefault()) }

    // Group tasks by due date (today, tomorrow, future)
    val groupedTasks by remember(tasks) {
        derivedStateOf {
            val grouped = mutableMapOf<String, MutableList<Task>>()
            val oneDayMs = 24 * 60 * 60 * 1000

            tasks.forEach { task ->
                val dueDate = task.dueDate
                val daysDifference = (dueDate - today) / oneDayMs

                val groupKey = when {
                    daysDifference < 0 -> "Overdue"
                    daysDifference < 1 -> "Today"
                    daysDifference < 2 -> "Tomorrow"
                    else -> dateFormat.format(Date(dueDate))
                }

                if (!grouped.containsKey(groupKey)) {
                    grouped[groupKey] = mutableListOf()
                }
                grouped[groupKey]?.add(task)
            }

            // Sort the groups in chronological order
            val sortOrder = listOf("Overdue", "Today", "Tomorrow")
            grouped.toSortedMap(compareBy {
                val index = sortOrder.indexOf(it)
                if (index != -1) index else Int.MAX_VALUE
            })
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .semantics { contentDescription = "Task list drawer with ${tasks.size} tasks" }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp)
        ) {
            // Enhanced drawer header with task count
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "My Tasks",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.25.sp
                    ),
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.weight(1f))

                // Enhanced task counter pill
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    tonalElevation = 2.dp,
                    shadowElevation = 1.dp,
                    modifier = Modifier.semantics {
                        contentDescription = "${tasks.size} tasks"
                    }
                ) {
                    Text(
                        text = "${tasks.size}",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Task list grouped by date
            if (tasks.isEmpty()) {
                // Enhanced empty state
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 48.dp, bottom = 48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.size(60.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Rounded.Timer,
                                    contentDescription = null,
                                    modifier = Modifier.size(28.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Your task list is empty",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.Medium
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // For each group, add a header and its tasks
                    groupedTasks.forEach { (dateGroup, tasksInGroup) ->
                        item {
                            DateGroupHeader(
                                dateGroup = dateGroup,
                                count = tasksInGroup.size
                            )
                        }

                        items(
                            items = tasksInGroup,
                            key = { it.id }
                        ) { task ->
                            TaskListItem(
                                task = task,
                                isSelected = selectedTask?.id == task.id,
                                onClick = {
                                    haptic.performHapticFeedback(
                                        if (selectedTask?.id == task.id)
                                            HapticFeedbackType.TextHandleMove
                                        else
                                            HapticFeedbackType.LongPress
                                    )
                                    onTaskClick(task)
                                }
                            )
                        }

                        // Add spacing between groups
                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }

                    // Add bottom padding
                    item {
                        Spacer(modifier = Modifier.height(32.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun DateGroupHeader(
    dateGroup: String,
    count: Int,
    modifier: Modifier = Modifier
) {
    // Determine if this date group represents urgent tasks (today or overdue)
    val isUrgent = dateGroup == "Today" || dateGroup == "Overdue"
    val headerColor = when (dateGroup) {
        "Overdue" -> MaterialTheme.colorScheme.error
        "Today" -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Date group label with optional urgency indicator
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isUrgent) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(headerColor)
                        .shadow(
                            elevation = 1.dp,
                            shape = CircleShape,
                            spotColor = headerColor.copy(alpha = 0.5f)
                        )
                )
                Spacer(modifier = Modifier.width(10.dp))
            }

            Text(
                text = dateGroup,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.4.sp
                ),
                color = headerColor
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // Count label with enhanced styling
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = headerColor.copy(alpha = 0.15f),
            modifier = Modifier.semantics {
                contentDescription = "$count tasks for $dateGroup"
            }
        ) {
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Medium
                ),
                color = headerColor,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
    }
}

@Composable
fun TaskListItem(
    modifier: Modifier = Modifier,
    task: Task,
    isSelected: Boolean = false,
    onClick: () -> Unit
) {
    // Enhanced animation properties
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.02f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "itemScale"
    )

    val elevation by animateFloatAsState(
        targetValue = if (isSelected) 6f else 2f,
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
        label = "itemElevation"
    )

    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected)
            MaterialTheme.colorScheme.surfaceColorAtElevation(8.dp)
        else
            MaterialTheme.colorScheme.surface,
        animationSpec = tween(durationMillis = 300),
        label = "backgroundColor"
    )

    // Calculate if task is urgent (due within 15 minutes)
    val now = System.currentTimeMillis()
    val timeRemaining = task.dueDate - now
    val isUrgent = timeRemaining in 1..900000 // Due within 15 minutes
    val isOverdue = timeRemaining <= 0

    // Enhanced indicator color with better contrast
    val indicatorColor = when {
        isOverdue -> MaterialTheme.colorScheme.error
        isUrgent -> task.getBackgroundColor().copy(
            red = minOf(1f, task.getBackgroundColor().red + 0.15f),
            alpha = 0.9f
        )
        else -> task.getBackgroundColor()
    }

    // Task semantic description
    val urgencyText = when {
        isOverdue -> ", overdue"
        isUrgent -> ", urgent"
        else -> ""
    }
    val taskDescription = "Task: ${task.title}, due ${task.getDueText()}$urgencyText"

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .scale(scale)
            .clip(RoundedCornerShape(18.dp))
            .shadow(
                elevation = elevation.dp,
                shape = RoundedCornerShape(18.dp),
                spotColor = indicatorColor.copy(alpha = 0.3f)
            )
            .border(
                width = if (isSelected) 2.dp else 0.dp,
                color = if (isSelected) indicatorColor else Color.Transparent,
                shape = RoundedCornerShape(18.dp)
            )
            .clickable(onClick = onClick)
            .semantics { contentDescription = taskDescription },
        color = backgroundColor,
        shape = RoundedCornerShape(18.dp),
        tonalElevation = elevation.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Enhanced color indicator with animation
            val indicatorWidth by animateFloatAsState(
                targetValue = when {
                    isSelected -> 6f
                    isUrgent -> 5f
                    else -> 4f
                },
                animationSpec = tween(durationMillis = 200),
                label = "indicatorWidth"
            )

            Box(
                modifier = Modifier
                    .size(
                        width = indicatorWidth.dp,
                        height = 48.dp
                    )
                    .clip(RoundedCornerShape(8.dp))
                    .background(indicatorColor)
                    .shadow(
                        elevation = 1.dp,
                        shape = RoundedCornerShape(8.dp),
                        spotColor = indicatorColor
                    )
            )

            Spacer(modifier = Modifier.width(16.dp))

            // Task content with better spacing
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = task.title,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = if (isSelected || isUrgent) FontWeight.Bold else FontWeight.Medium,
                            letterSpacing = (-0.2).sp
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f, fill = false)
                    )

                    // Enhanced urgency indicator
                    if (isUrgent || isOverdue) {
                        Spacer(modifier = Modifier.width(8.dp))

                        val iconTint = if (isOverdue)
                            MaterialTheme.colorScheme.error
                        else
                            MaterialTheme.colorScheme.primary

                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(iconTint.copy(alpha = 0.15f))
                                .padding(4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Timer,
                                contentDescription = if (isOverdue) "Overdue" else "Urgent",
                                tint = iconTint,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Enhanced due time with better styling based on urgency
                Text(
                    text = task.getDueText(),
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = if (isOverdue || isUrgent) FontWeight.Medium else FontWeight.Normal,
                        letterSpacing = 0.1.sp
                    ),
                    color = when {
                        isOverdue -> MaterialTheme.colorScheme.error
                        isUrgent -> MaterialTheme.colorScheme.primary
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }

            // Enhanced visual indicator for selected state
            AnimatedVisibility(
                visible = isSelected,
                enter = fadeIn(tween(200)) + expandVertically(tween(200)),
                exit = fadeOut(tween(150)) + shrinkVertically(tween(150))
            ) {
                Box(
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(indicatorColor)
                        .shadow(
                            elevation = 2.dp,
                            shape = CircleShape,
                            spotColor = indicatorColor
                        )
                )
            }
        }
    }
}