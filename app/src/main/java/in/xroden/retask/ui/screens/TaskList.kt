package `in`.xroden.retask.ui.screens


import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import `in`.xroden.retask.data.model.Task
import `in`.xroden.retask.ui.components.TaskCard
import `in`.xroden.retask.ui.components.TaskDrawer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    tasks: List<Task>,
    onCompleteTask: (Task) -> Unit,
    onSnoozeTask: (Task) -> Unit,
    modifier: Modifier = Modifier
) {
    // Sort tasks by due date to show the most urgent one first
    val sortedTasks = remember(tasks) {
        tasks.sortedBy { it.dueDate }
    }

    // Current task is the first one (most urgent)
    val currentTask = sortedTasks.firstOrNull() ?: return

    // Other tasks for the drawer
    val otherTasks = sortedTasks.drop(1)

    // State for bottom sheet
    val sheetState = rememberBottomSheetScaffoldState()
    var selectedTask by remember { mutableStateOf(currentTask) }

    BottomSheetScaffold(
        scaffoldState = sheetState,
        sheetContent = {
            TaskDrawer(
                tasks = otherTasks,
                onTaskClick = { task ->
                    selectedTask = task
                    // In a real app, you might want to collapse the drawer here
                },
                modifier = Modifier.fillMaxWidth()
            )
        },
        sheetPeekHeight = 72.dp
    ) { paddingValues ->
        TaskCard(
            task = selectedTask,
            onCompleteClick = { onCompleteTask(selectedTask) },
            onSnoozeClick = { onSnoozeTask(selectedTask) },
            modifier = Modifier.padding(paddingValues)
        )
    }
}