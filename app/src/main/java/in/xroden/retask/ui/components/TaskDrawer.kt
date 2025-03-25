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
import androidx.compose.ui.draw.alpha
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

            // One day in milliseconds
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
                .padding(top = 16.dp)
        ) {
            // Drawer header with task count
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "My Tasks",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.2.sp
                    ),
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.weight(1f))

                // Task counter pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f))
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "${tasks.size}",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Task list grouped by date
            if (tasks.isEmpty()) {
                // Empty state
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Your task list is empty",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
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
                            key = { it.id } // Use stable key for better animations
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
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                    }

                    // Add bottom padding
                    item {
                        Spacer(modifier = Modifier.height(24.dp))
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

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Date group label with optional urgency indicator
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isUrgent) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(
                            if (dateGroup == "Overdue")
                                MaterialTheme.colorScheme.error
                            else
                                MaterialTheme.colorScheme.primary
                        )
                )
                Spacer(modifier = Modifier.width(8.dp))
            }

            Text(
                text = dateGroup,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.4.sp
                ),
                color = when (dateGroup) {
                    "Overdue" -> MaterialTheme.colorScheme.error
                    "Today" -> MaterialTheme.colorScheme.primary
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // Count label
        Text(
            text = "$count",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )
    }
}

@Composable
fun TaskListItem(
    modifier: Modifier = Modifier,
    task: Task,
    isSelected: Boolean = false,
    onClick: () -> Unit
) {
    // Animation properties
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.02f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "itemScale"
    )

    val elevation by animateFloatAsState(
        targetValue = if (isSelected) 4f else 1f,
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
            .scale(scale)
            .clip(RoundedCornerShape(16.dp))
            .shadow(
                elevation = elevation.dp,
                shape = RoundedCornerShape(16.dp),
                spotColor = task.getBackgroundColor().copy(alpha = 0.2f)
            )
            .border(
                width = if (isSelected) 2.dp else 0.dp,
                color = if (isSelected) task.getBackgroundColor() else Color.Transparent,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(onClick = onClick)
            .semantics { contentDescription = taskDescription },
        color = backgroundColor,
        shape = RoundedCornerShape(16.dp),
        tonalElevation = elevation.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Colored indicator with variable thickness based on selection/urgency
            Box(
                modifier = Modifier
                    .size(
                        width = when {
                            isSelected -> 6.dp
                            isUrgent -> 5.dp
                            else -> 4.dp
                        },
                        height = 44.dp
                    )
                    .clip(RoundedCornerShape(4.dp))
                    .background(
                        when {
                            isOverdue -> MaterialTheme.colorScheme.error
                            isUrgent -> task.getBackgroundColor()
                                .copy(red = minOf(1f, task.getBackgroundColor().red + 0.1f))

                            else -> task.getBackgroundColor()
                        }
                    )
            )

            Spacer(modifier = Modifier.width(16.dp))

            // Task content with better organization
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
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

                    // Show urgency indicator if needed
                    if (isUrgent) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Rounded.Timer,
                            contentDescription = "Urgent",
                            tint = if (isOverdue)
                                MaterialTheme.colorScheme.error
                            else
                                MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .size(16.dp)
                                .alpha(0.8f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Due time with styling based on urgency
                Text(
                    text = task.getDueText(),
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = if (isOverdue || isUrgent) FontWeight.Medium else FontWeight.Normal
                    ),
                    color = when {
                        isOverdue -> MaterialTheme.colorScheme.error
                        isUrgent -> MaterialTheme.colorScheme.primary
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }

            // Visual indicator for selected state
            AnimatedVisibility(
                visible = isSelected,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(task.getBackgroundColor())
                )
            }
        }
    }
}