package `in`.xroden.retask.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import `in`.xroden.retask.data.model.Task
import java.util.concurrent.TimeUnit

/**
 * A drawer component that displays a list of tasks, grouped by date.
 *
 * @param groupedTasks A map of tasks, pre-grouped by date category (String).
 * @param totalTasksCount The total number of tasks.
 * @param selectedTask The currently selected task, for highlighting.
 * @param onTaskClick Callback invoked when a task is clicked.
 */
@Composable
fun TaskDrawer(
    groupedTasks: Map<String, List<Task>>,
    totalTasksCount: Int,
    selectedTask: Task?,
    onTaskClick: (Task) -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .semantics { contentDescription = "Task list drawer with $totalTasksCount tasks" }
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(top = 24.dp)) {
            // Drawer header
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("My Tasks", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
                Spacer(Modifier.weight(1f))
                Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer) {
                    Text(
                        text = "$totalTasksCount",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
            HorizontalDivider(Modifier.padding(horizontal = 20.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
            Spacer(Modifier.height(16.dp))

            // Task list
            if (totalTasksCount == 0) {
                EmptyState()
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    groupedTasks.forEach { (dateGroup, tasksInGroup) ->
                        item {
                            DateGroupHeader(dateGroup, tasksInGroup.size)
                        }
                        items(items = tasksInGroup, key = { it.id }) { task ->
                            EnhancedTaskListItem(
                                task = task,
                                isSelected = selectedTask?.id == task.id,
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    onTaskClick(task)
                                }
                            )
                        }
                        item { Spacer(Modifier.height(16.dp)) }
                    }
                    item { Spacer(Modifier.height(32.dp)) }
                }
            }
        }
    }
}

@Composable
private fun EmptyState() {
    Box(Modifier.fillMaxWidth().padding(vertical = 48.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Icon(Icons.Rounded.Timer, null, modifier = Modifier.size(28.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(
                text = "Your task list is empty",
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun DateGroupHeader(dateGroup: String, count: Int, modifier: Modifier = Modifier) {
    val headerColor = when (dateGroup) {
        "Overdue" -> MaterialTheme.colorScheme.error
        "Today" -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Row(
        modifier = modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (dateGroup == "Today" || dateGroup == "Overdue") {
            Box(Modifier.size(8.dp).clip(CircleShape).background(headerColor))
            Spacer(Modifier.width(10.dp))
        }
        Text(dateGroup, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold), color = headerColor)
        Spacer(Modifier.weight(1f))
        Surface(shape = CircleShape, color = headerColor.copy(alpha = 0.1f)) {
            Text(
                text = "$count",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium),
                color = headerColor,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
    }
}

@Composable
private fun EnhancedTaskListItem(modifier: Modifier = Modifier, task: Task, isSelected: Boolean = false, onClick: () -> Unit) {
    val scale by animateFloatAsState(if (isSelected) 1.02f else 1f, spring(Spring.DampingRatioMediumBouncy), label = "itemScale")
    val elevation by animateFloatAsState(if (isSelected) 6f else 2f, tween(300, easing = FastOutSlowInEasing), label = "itemElevation")
    val backgroundColor by animateColorAsState(if (isSelected) MaterialTheme.colorScheme.surfaceColorAtElevation(8.dp) else MaterialTheme.colorScheme.surface, tween(300), label = "backgroundColor")

    val timeRemaining = task.dueDate - System.currentTimeMillis()
    val isUrgent = timeRemaining in 1..TimeUnit.MINUTES.toMillis(15)
    val isOverdue = timeRemaining <= 0
    val indicatorColor = when {
        isOverdue -> MaterialTheme.colorScheme.error
        isUrgent -> task.getBackgroundColor().copy(red = (task.getBackgroundColor().red + 0.15f).coerceAtMost(1f))
        else -> task.getBackgroundColor()
    }

    Surface(
        modifier = modifier.fillMaxWidth().scale(scale).clip(RoundedCornerShape(18.dp)).border(if (isSelected) 2.dp else 0.dp, if (isSelected) indicatorColor else Color.Transparent, RoundedCornerShape(18.dp)).clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { onClick() },
        color = backgroundColor, shape = RoundedCornerShape(18.dp), tonalElevation = elevation.dp
    ) {
        Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            val indicatorWidth by animateFloatAsState(if (isSelected) 6f else 4f, tween(200), label = "indicatorWidth")
            Box(Modifier.width(indicatorWidth.dp).height(48.dp).clip(RoundedCornerShape(8.dp)).background(indicatorColor))
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(task.title, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = if (isSelected || isUrgent) FontWeight.Bold else FontWeight.Medium), maxLines = 1, overflow = TextOverflow.Ellipsis, color = MaterialTheme.colorScheme.onSurface)
                Spacer(Modifier.height(8.dp))
                val dueTextColor = when {
                    isOverdue -> MaterialTheme.colorScheme.error
                    isUrgent -> MaterialTheme.colorScheme.primary
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                }
                Text(task.getDueText(), style = MaterialTheme.typography.bodySmall, color = dueTextColor)
            }
        }
    }
}