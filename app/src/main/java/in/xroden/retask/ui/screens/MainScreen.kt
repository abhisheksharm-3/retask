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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: TaskViewModel,
    modifier: Modifier = Modifier
) {
    val tasks by viewModel.allTasks.collectAsStateWithLifecycle()

    // If tasks are empty, show a button to add sample tasks
    if (tasks.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize()) {
            Button(
                onClick = { viewModel.addSampleTasks() },
                modifier = Modifier.align(Alignment.Center)
            ) {
                Text("Add Sample Tasks")
            }
        }
        return
    }

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

    // Add dialog state
    var showAddTaskDialog by remember { mutableStateOf(false) }

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
                onCompleteClick = { viewModel.completeTask(selectedTask) },
                onSnoozeClick = { viewModel.snoozeTask(selectedTask) },
                modifier = Modifier.padding(paddingValues)
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

    // Add task dialog
    if (showAddTaskDialog) {
        AddTaskDialog(
            onDismiss = { showAddTaskDialog = false },
            onAddTask = { title, dueMinutes, colorHex ->
                viewModel.addTask(title, dueMinutes, colorHex)
                showAddTaskDialog = false
            }
        )
    }
}

@Composable
fun AddTaskDialog(
    onDismiss: () -> Unit,
    onAddTask: (String, Int, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var dueMinutes by remember { mutableStateOf("15") }
    var selectedColor by remember { mutableStateOf("#F6D8CE") }
    val colors = listOf("#F6D8CE", "#D5F5E3", "#FADBD8", "#D6EAF8")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Task") },
        text = {
            Column {
                TextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Task Title") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                TextField(
                    value = dueMinutes,
                    onValueChange = { dueMinutes = it },
                    label = { Text("Due in minutes") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text("Select Color:")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    colors.forEach { color ->
                        Surface(
                            color = Color(color.toColorInt()),
                            shape = MaterialTheme.shapes.small,
                            border = BorderStroke(
                                width = 2.dp,
                                color = if (color == selectedColor)
                                    MaterialTheme.colorScheme.primary
                                else
                                    Color.Transparent
                            ),
                            modifier = Modifier
                                .size(40.dp)
                                .clickable { selectedColor = color }
                        ) {}
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onAddTask(
                        title,
                        dueMinutes.toIntOrNull() ?: 15,
                        selectedColor
                    )
                },
                enabled = title.isNotBlank()
            ) {
                Text("Add Task")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}