package `in`.xroden.retask.ui.screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import `in`.xroden.retask.data.model.Task
import `in`.xroden.retask.ui.components.TaskCard
import `in`.xroden.retask.ui.components.TaskDrawer
import `in`.xroden.retask.ui.components.TaskFormDialog
import `in`.xroden.retask.ui.viewmodel.TaskViewModel
import `in`.xroden.retask.ui.components.NoTasks

/**
 * The main screen of the application, responsible for displaying tasks and handling user interactions.
 *
 * This screen displays an empty state if no tasks are present. Otherwise, it uses a
 * [BottomSheetScaffold] to show a primary [TaskCard] for the selected task and a
 * [TaskDrawer] containing the list of all tasks.
 *
 * @param viewModel The [TaskViewModel] that provides state and handles business logic.
 */
@RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: TaskViewModel,
    modifier: Modifier = Modifier
) {
    // Collect state from the ViewModel in a lifecycle-aware manner.
    val tasks by viewModel.allTasks.collectAsStateWithLifecycle()
    val groupedTasks by viewModel.groupedTasks.collectAsStateWithLifecycle()

    // Local UI state for managing which task is selected and which dialogs are visible.
    var selectedTask by remember { mutableStateOf<Task?>(null) }
    var taskToEdit by remember { mutableStateOf<Task?>(null) }
    var showAddTaskDialog by remember { mutableStateOf(false) }

    // This effect ensures the selected task is stable across data changes.
    // It only resets the selection if the current task is deleted or on initial load.
    LaunchedEffect(tasks) {
        val sortedTasks = tasks.sortedBy { it.dueDate }
        if (selectedTask == null || tasks.none { it.id == selectedTask?.id }) {
            selectedTask = sortedTasks.firstOrNull()
        }
    }

    // Main UI router: show empty state or the task scaffold.
    if (tasks.isEmpty()) {
        NoTasks(
            onAddSampleTasks = { viewModel.addSampleTasks() },
            onAddNewTask = { showAddTaskDialog = true },
            modifier = modifier.fillMaxSize()
        )
    } else {
        val scaffoldState = rememberBottomSheetScaffoldState()

        Box(modifier = modifier.fillMaxSize()) {
            BottomSheetScaffold(
                scaffoldState = scaffoldState,
                sheetContent = {
                    TaskDrawer(
                        groupedTasks = groupedTasks,
                        totalTasksCount = tasks.size,
                        selectedTask = selectedTask,
                        onTaskClick = { clickedTask ->
                            selectedTask = clickedTask
                        }
                    )
                },
                sheetPeekHeight = 80.dp,
                sheetShape = MaterialTheme.shapes.extraLarge
            ) { paddingValues ->
                // Only show the TaskCard if a task is actually selected.
                selectedTask?.let { task ->
                    TaskCard(
                        task = task,
                        totalTasks = tasks.size,
                        onCompleteClick = { viewModel.completeTask(task) },
                        onSnoozeClick = { viewModel.snoozeTask(task) },
                        onEditClick = { taskToEdit = task },
                        modifier = Modifier.padding(paddingValues)
                    )
                }
            }

            // FAB for adding new tasks.
            FloatingActionButton(
                onClick = { showAddTaskDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
            ) {
                Icon(Icons.Rounded.Add, contentDescription = "Add Task")
            }
        }
    }

    // --- Dialogs ---

    if (showAddTaskDialog) {
        TaskFormDialog(
            dialogTitle = "Create New Task",
            submitButtonText = "Create Task",
            onDismiss = { showAddTaskDialog = false },
            onSubmit = { title, dueMinutes, colorHex ->
                viewModel.addTask(title, dueMinutes, colorHex)
                showAddTaskDialog = false
            }
        )
    }

    taskToEdit?.let { task ->
        TaskFormDialog(
            dialogTitle = "Edit Task",
            submitButtonText = "Save Changes",
            initialTitle = task.title,
            initialDueMinutes = ((task.dueDate - System.currentTimeMillis()) / 60000).toInt().coerceAtLeast(1),
            initialColor = task.colorHex,
            onDismiss = { taskToEdit = null },
            onSubmit = { title, dueMinutes, colorHex ->
                viewModel.editTask(task, title, dueMinutes, colorHex)
                taskToEdit = null
            }
        )
    }
}