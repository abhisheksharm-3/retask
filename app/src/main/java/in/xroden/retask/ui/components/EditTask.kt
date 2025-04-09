package `in`.xroden.retask.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import `in`.xroden.retask.data.model.Task

/**
 * Dialog for editing an existing task.
 *
 * @param task The task to be edited.
 * @param onDismiss Callback invoked when the dialog is dismissed.
 * @param onUpdateTask Callback invoked when a task is updated with (task, title, dueMinutes, colorHex).
 */
@Composable
fun EditTaskDialog(
    task: Task,
    onDismiss: () -> Unit,
    onUpdateTask: (Task, String, Int, String) -> Unit
) {
    // Calculate initial due minutes (time left from now)
    val initialDueMinutes = remember {
        val now = System.currentTimeMillis()
        val diffMinutes = (task.dueDate - now) / (1000 * 60)
        if (diffMinutes <= 0) 15 else diffMinutes.toInt() // Default to 15 min if already due
    }

    TaskFormDialog(
        dialogTitle = "Edit Task",
        submitButtonText = "Update Task",
        initialTitle = task.title,
        initialDueMinutes = initialDueMinutes,
        initialColor = task.colorHex,
        onDismiss = onDismiss,
        onSubmit = { title, dueMinutes, color ->
            onUpdateTask(task, title, dueMinutes, color)
        }
    )
}