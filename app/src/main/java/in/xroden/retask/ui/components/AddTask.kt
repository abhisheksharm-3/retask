package `in`.xroden.retask.ui.components

import androidx.compose.runtime.Composable

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