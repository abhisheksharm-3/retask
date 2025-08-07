package `in`.xroden.retask.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import kotlin.math.max

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun TaskFormDialog(
    dialogTitle: String,
    submitButtonText: String,
    initialTitle: String = "",
    initialDueMinutes: Int = 15,
    initialColor: String = "#FFD6D6",
    onDismiss: () -> Unit,
    onSubmit: (String, Int, String) -> Unit
) {
    val formState = rememberTaskFormState(initialTitle, initialDueMinutes, initialColor)
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(0.95f),
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(dialogTitle, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.height(24.dp))
                TitleInput(formState, focusRequester)
                Spacer(Modifier.height(20.dp))
                TimeAllocationSection(formState)
                Spacer(Modifier.height(20.dp))
                ColorSelector(
                    selectedColor = formState.selectedColor,
                    onColorSelected = { formState.selectedColor = it },
                    isExpanded = formState.isColorPanelExpanded,
                    onToggleExpanded = { formState.isColorPanelExpanded = !formState.isColorPanelExpanded }
                )
                Spacer(Modifier.height(28.dp))
                ActionButtons(formState, submitButtonText, onDismiss, onSubmit)
            }
        }
    }

    if (formState.showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { formState.showDatePicker = false },
            confirmButton = { TextButton(onClick = { formState.showDatePicker = false }) { Text("OK") } },
            dismissButton = { TextButton(onClick = { formState.showDatePicker = false }) { Text("Cancel") } }
        ) {
            DatePicker(state = formState.datePickerState)
        }
    }

    if (formState.showTimePicker) {
        TimePickerDialog(formState)
    }
}

// --- State Holder ---

@OptIn(ExperimentalMaterial3Api::class)
@Stable
class TaskFormState(
    initialTitle: String,
    initialDueMinutes: Int,
    initialColor: String,
    val datePickerState: DatePickerState,
    val timePickerState: TimePickerState
) {
    var title by mutableStateOf(initialTitle)
    var titleError by mutableStateOf<String?>(null)
    var dueMinutes by mutableIntStateOf(initialDueMinutes)
    var sliderPosition by mutableFloatStateOf(initialDueMinutes.toFloat().coerceIn(5f, 240f))
    var customTimeInput by mutableStateOf(initialDueMinutes.toString())
    var selectedTimeMode by mutableStateOf("slider") // "slider", "custom", "calendar"
    var selectedColor by mutableStateOf(initialColor)
    var isColorPanelExpanded by mutableStateOf(false)
    var showDatePicker by mutableStateOf(false)
    var showTimePicker by mutableStateOf(false)

    fun syncStateFromPickers() {
        if (selectedTimeMode == "calendar" && datePickerState.selectedDateMillis != null) {
            val localDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(datePickerState.selectedDateMillis!!), ZoneId.systemDefault())
                .withHour(timePickerState.hour)
                .withMinute(timePickerState.minute)
            val selectedMillis = localDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
            val calculatedMinutes = ((selectedMillis - System.currentTimeMillis()) / 60000).toInt()
            if (calculatedMinutes > 0) {
                dueMinutes = calculatedMinutes
                customTimeInput = calculatedMinutes.toString()
                sliderPosition = calculatedMinutes.toFloat().coerceIn(5f, 240f)
            }
        }
    }

    fun syncStateFromSlider() {
        if (selectedTimeMode == "slider") {
            dueMinutes = sliderPosition.toInt()
            customTimeInput = dueMinutes.toString()
        }
    }

    fun validateAndSubmit(onSubmit: (String, Int, String) -> Unit) {
        if (title.isBlank()) {
            titleError = "Please enter a task title"
            return
        }
        val finalMinutes = when (selectedTimeMode) {
            "calendar" -> dueMinutes
            "custom" -> max(1, customTimeInput.toIntOrNull() ?: dueMinutes)
            else -> dueMinutes
        }
        onSubmit(title.trim(), finalMinutes, selectedColor)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun rememberTaskFormState(
    initialTitle: String,
    initialDueMinutes: Int,
    initialColor: String
): TaskFormState {
    val initialMillis = System.currentTimeMillis() + (initialDueMinutes * 60 * 1000L)
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = initialMillis)
    val timePickerState = rememberTimePickerState(
        initialHour = LocalDateTime.ofInstant(Instant.ofEpochMilli(initialMillis), ZoneId.systemDefault()).hour,
        initialMinute = LocalDateTime.ofInstant(Instant.ofEpochMilli(initialMillis), ZoneId.systemDefault()).minute,
        is24Hour = true
    )

    return remember(initialTitle, initialDueMinutes, initialColor) {
        TaskFormState(
            initialTitle = initialTitle,
            initialDueMinutes = initialDueMinutes,
            initialColor = initialColor,
            datePickerState = datePickerState,
            timePickerState = timePickerState
        )
    }
}

// --- Decomposed UI Components ---

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun TitleInput(state: TaskFormState, focusRequester: FocusRequester) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    OutlinedTextField(
        value = state.title,
        onValueChange = {
            state.title = it
            if (it.isNotBlank()) state.titleError = null
        },
        label = { Text("Task Title") },
        modifier = Modifier.fillMaxWidth().focusRequester(focusRequester),
        isError = state.titleError != null,
        supportingText = { state.titleError?.let { Text(it, color = MaterialTheme.colorScheme.error) } },
        leadingIcon = { Icon(Icons.Outlined.Edit, "Task title") },
        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(onDone = {
            focusManager.clearFocus()
            keyboardController?.hide()
        }),
        singleLine = true,
        shape = RoundedCornerShape(16.dp)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimeAllocationSection(state: TaskFormState) {
    val sliderLabel by remember(state.sliderPosition) { derivedStateOf { formatTimeFromMinutes(state.sliderPosition.toInt()) } }

    LaunchedEffect(state.sliderPosition) { state.syncStateFromSlider() }
    LaunchedEffect(state.datePickerState.selectedDateMillis, state.timePickerState.hour, state.timePickerState.minute) {
        state.syncStateFromPickers()
    }

    Column(Modifier.fillMaxWidth()) {
        TabRow(selectedTabIndex = when (state.selectedTimeMode) {
            "slider" -> 0
            "custom" -> 1
            else -> 2
        }) {
            Tab(selected = state.selectedTimeMode == "slider", onClick = { state.selectedTimeMode = "slider" }, text = { Text("Quick") })
            Tab(selected = state.selectedTimeMode == "custom", onClick = { state.selectedTimeMode = "custom" }, text = { Text("Minutes") })
            Tab(selected = state.selectedTimeMode == "calendar", onClick = { state.selectedTimeMode = "calendar" }, text = { Text("Date/Time") })
        }
        Spacer(Modifier.height(24.dp))
        when (state.selectedTimeMode) {
            "slider" -> {
                Text(sliderLabel, style = MaterialTheme.typography.titleLarge, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                Slider(value = state.sliderPosition, onValueChange = { state.sliderPosition = it }, valueRange = 5f..240f, steps = 47)
            }
            "custom" -> {
                OutlinedTextField(
                    value = state.customTimeInput,
                    onValueChange = { input -> if (input.all { it.isDigit() }) state.customTimeInput = input },
                    label = { Text("Minutes from now") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            "calendar" -> {
                Row(Modifier.fillMaxWidth(), Arrangement.SpaceEvenly) {
                    Button(onClick = { state.showDatePicker = true }) { Text("Select Date") }
                    Button(onClick = { state.showTimePicker = true }) { Text("Select Time") }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimePickerDialog(state: TaskFormState) {
    Dialog(onDismissRequest = { state.showTimePicker = false }) {
        Surface(shape = RoundedCornerShape(28.dp), tonalElevation = 6.dp) {
            Column(Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Select Time", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(bottom = 20.dp))
                TimePicker(state = state.timePickerState)
                Row(Modifier.fillMaxWidth().padding(top = 16.dp), Arrangement.End) {
                    TextButton(onClick = { state.showTimePicker = false }) { Text("Cancel") }
                    TextButton(onClick = { state.showTimePicker = false }, modifier = Modifier.padding(start = 8.dp)) { Text("OK") }
                }
            }
        }
    }
}

@Composable
private fun ActionButtons(
    state: TaskFormState,
    submitButtonText: String,
    onDismiss: () -> Unit,
    onSubmit: (String, Int, String) -> Unit
) {
    val haptic = LocalHapticFeedback.current
    Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(16.dp)) {
        TextButton(onClick = onDismiss, modifier = Modifier.weight(1f)) { Text("Cancel") }
        Button(
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                state.validateAndSubmit(onSubmit)
            },
            modifier = Modifier.weight(1f),
            enabled = state.title.isNotBlank()
        ) {
            Text(submitButtonText)
        }
    }
}

private fun formatTimeFromMinutes(minutes: Int): String {
    return when {
        minutes < 60 -> "$minutes minutes"
        minutes % 60 == 0 -> "${minutes / 60} hours"
        else -> "${minutes / 60}h ${minutes % 60}m"
    }
}