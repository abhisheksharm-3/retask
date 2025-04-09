package `in`.xroden.retask.ui.components

import androidx.compose.runtime.Composable

/**
 * Dialog for creating a new task.
 *
 * @param onDismiss Callback invoked when the dialog is dismissed.
 * @param onAddTask Callback invoked when a task is added with (title, dueMinutes, colorHex).
 */
@Composable
fun AddTaskDialog(
    onDismiss: () -> Unit,
    onAddTask: (String, Int, String) -> Unit
) {
    TaskFormDialog(
        dialogTitle = "Create New Task",
        submitButtonText = "Create Task",
        onDismiss = onDismiss,
        onSubmit = onAddTask
    )
}