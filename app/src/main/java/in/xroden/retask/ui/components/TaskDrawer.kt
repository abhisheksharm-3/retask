package `in`.xroden.retask.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import `in`.xroden.retask.data.model.Task

@Composable
fun TaskDrawer(
    tasks: List<Task>,
    selectedTask: Task?, // Add this parameter
    onTaskClick: (Task) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(tasks) { task ->
                TaskListItem(
                    task = task,
                    isSelected = selectedTask?.id == task.id, // Pass selection state
                    onClick = { onTaskClick(task) }
                )
            }
        }
    }
}

@Composable
fun TaskListItem(
    task: Task,
    isSelected: Boolean = false, // Add this parameter
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val surfaceColor = if (isSelected) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surface
    }

    val borderModifier = if (isSelected) {
        Modifier.border(
            width = 2.dp,
            color = task.getBackgroundColor(),
            shape = RoundedCornerShape(12.dp)
        )
    } else {
        Modifier
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .shadow(
                elevation = if (isSelected) 4.dp else 2.dp,
                shape = RoundedCornerShape(12.dp),
                spotColor = task.getBackgroundColor().copy(alpha = 0.1f)
            )
            .then(borderModifier) // Apply conditional border
            .background(surfaceColor)
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Colored indicator - make it thicker when selected
            Box(
                modifier = Modifier
                    .size(width = if (isSelected) 6.dp else 4.dp, height = 40.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(task.getBackgroundColor())
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Task content
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                    else MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = task.getDueText(),
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isSelected)
                        MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    else
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }

            // Priority indicator or status
            if (isSelected) {
                // Show a check icon or a larger indicator for selected item
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(task.getBackgroundColor())
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(task.getBackgroundColor())
                )
            }
        }
    }
}