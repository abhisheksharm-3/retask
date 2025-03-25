package `in`.xroden.retask.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import `in`.xroden.retask.ui.components.TaskCard
import `in`.xroden.retask.ui.components.TaskDrawer
import `in`.xroden.retask.ui.viewmodel.TaskViewModel
import androidx.core.graphics.toColorInt
import `in`.xroden.retask.ui.components.AddTaskDialog
import `in`.xroden.retask.ui.components.EditTaskDialog
import `in`.xroden.retask.ui.components.NoTasks
import `in`.xroden.retask.data.model.Task

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: TaskViewModel,
    modifier: Modifier = Modifier
) {
    val tasks by viewModel.allTasks.collectAsStateWithLifecycle()

    // Add dialog state
    var showAddTaskDialog by remember { mutableStateOf(false) }
    var showEditTaskDialog by remember { mutableStateOf(false) }
    var taskToEdit by remember { mutableStateOf<Task?>(null) }

    // If tasks are empty, show the improved empty state UI
    if (tasks.isEmpty()) {
        NoTasks(
            onAddSampleTasks = { viewModel.addSampleTasks() },
            onAddNewTask = { showAddTaskDialog = true },
            modifier = modifier.fillMaxSize()
        )
    } else {
        // Sort tasks by due date to show the most urgent one first
        val sortedTasks = remember(tasks) {
            tasks.sortedBy { it.dueDate }
        }

        // Current task is the first one (most urgent)
        val currentTask = sortedTasks.firstOrNull() ?: return

        // Other tasks for the drawer
        val otherTasks = sortedTasks

        // State for bottom sheet
        val sheetState = rememberBottomSheetScaffoldState()
        var selectedTask by remember { mutableStateOf(currentTask) }

        // If the selected task is no longer in the list, reset to the first task
        LaunchedEffect(tasks) {
            if (!tasks.contains(selectedTask)) {
                selectedTask = tasks.firstOrNull() ?: return@LaunchedEffect
            }
        }

        // Correctly using Box and BottomSheetScaffold
        Box(modifier = Modifier.fillMaxSize()) {
            BottomSheetScaffold(
                scaffoldState = sheetState,
                sheetContent = {
                    TaskDrawer(
                        tasks = otherTasks,
                        selectedTask = selectedTask,
                        onTaskClick = { task ->
                            selectedTask = task
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                sheetPeekHeight = 72.dp
            ) { paddingValues ->
                TaskCard(
                    task = selectedTask,
                    totalTasks = tasks.size,
                    onCompleteClick = { viewModel.completeTask(selectedTask) },
                    onSnoozeClick = { viewModel.snoozeTask(selectedTask) },
                    modifier = Modifier.padding(paddingValues),
                    onEditClick = {
                        taskToEdit = selectedTask
                        showEditTaskDialog = true
                    }
                )
            }

            // FloatingActionButton positioned in the Box
            FloatingActionButton(
                onClick = { showAddTaskDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Task"
                )
            }
        }
    }

    if (showAddTaskDialog) {
        AddTaskDialog(
            onDismiss = { showAddTaskDialog = false },
            onAddTask = { title, dueMinutes, colorHex ->
                viewModel.addTask(title, dueMinutes, colorHex)
                showAddTaskDialog = false
            }
        )
    }

    // Edit task dialog
    if (showEditTaskDialog && taskToEdit != null) {
        EditTaskDialog(
            task = taskToEdit!!,
            onDismiss = {
                showEditTaskDialog = false
                taskToEdit = null
            },
            onUpdateTask = { task, title, dueMinutes, colorHex ->
                viewModel.editTask(task, title, dueMinutes, colorHex)
                showEditTaskDialog = false
                taskToEdit = null
            }
        )
    }
}