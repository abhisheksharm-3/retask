package `in`.xroden.retask.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import `in`.xroden.retask.utils.TimeUtils
import java.util.Calendar
import kotlin.math.max

/**
 * An elegant dialog component for creating or editing tasks with customizable fields.
 *
 * @param dialogTitle The title displayed at the top of the dialog.
 * @param submitButtonText Text for the submit button.
 * @param initialTitle Initial value for the task title input field.
 * @param initialDueMinutes Initial value for task due time in minutes.
 * @param initialColor Initial color for the task (hex format).
 * @param onDismiss Callback invoked when the dialog is dismissed.
 * @param onSubmit Callback invoked when a task is submitted with (title, dueMinutes, colorHex).
 */
@OptIn(ExperimentalMaterial3Api::class)
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
    // --- State management ---
    var title by remember { mutableStateOf(initialTitle) }
    var titleError by remember { mutableStateOf<String?>(null) }
    var dueMinutes by remember { mutableIntStateOf(initialDueMinutes) }
    var customTimeInput by remember { mutableStateOf(initialDueMinutes.toString()) }
    var selectedTimeMode by remember { mutableStateOf("slider") }
    var selectedColor by remember { mutableStateOf(initialColor) }
    var isColorPanelExpanded by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    // --- Utility components ---
    val haptic = LocalHapticFeedback.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }
    val scrollState = rememberScrollState()

    // --- Date/Time picker states ---
    val initialDateMillis = remember { TimeUtils.getFutureTimestamp(initialDueMinutes) }
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = initialDateMillis)

    val calendar = remember { Calendar.getInstance() }
    val timePickerState = rememberTimePickerState(
        initialHour = calendar.get(Calendar.HOUR_OF_DAY),
        initialMinute = calendar.get(Calendar.MINUTE),
        is24Hour = true
    )

    // Derived state for selected date/time
    val selectedDateTimeFormatted by remember(datePickerState.selectedDateMillis, timePickerState.hour, timePickerState.minute) {
        derivedStateOf {
            datePickerState.selectedDateMillis?.let { millis ->
                formatDateTimeCompact(millis, timePickerState.hour, timePickerState.minute)
            } ?: "Not set"
        }
    }

    // --- Side effects ---

    // Update due minutes when date/time selection changes
    LaunchedEffect(datePickerState.selectedDateMillis, timePickerState.hour, timePickerState.minute) {
        if (selectedTimeMode == "calendar" && datePickerState.selectedDateMillis != null) {
            val dateCalendar = Calendar.getInstance().apply {
                timeInMillis = datePickerState.selectedDateMillis!!
                set(Calendar.HOUR_OF_DAY, timePickerState.hour)
                set(Calendar.MINUTE, timePickerState.minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            val calculatedMinutes = TimeUtils.calculateMinutesFromTimestamp(dateCalendar.timeInMillis)
            if (calculatedMinutes > 0) {
                dueMinutes = calculatedMinutes
                customTimeInput = dueMinutes.toString()
            }
        }
    }

    // Auto-focus title field when dialog opens
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    /**
     * Validates form input and submits if valid
     */
    fun validateAndSubmit() {
        if (title.isBlank()) {
            titleError = "Please enter a task title"
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            return
        }

        val finalMinutes = when (selectedTimeMode) {
            "calendar" -> dueMinutes
            "custom" -> max(1, customTimeInput.toIntOrNull() ?: dueMinutes)
            else -> dueMinutes
        }

        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        onSubmit(title.trim(), finalMinutes, selectedColor)
    }

    /**
     * Switches between different time input modes
     */
    fun switchTimeMode(mode: String) {
        if (selectedTimeMode == mode) return  // Skip if already in this mode

        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        selectedTimeMode = mode
        keyboardController?.hide()

        when (mode) {
            "custom" -> {
                if (customTimeInput.isEmpty()) {
                    customTimeInput = dueMinutes.toString()
                }
            }
            "calendar" -> {
                // Update the calendar to reflect current dueMinutes
                val newTimeMillis = TimeUtils.getFutureTimestamp(dueMinutes)
                datePickerState.selectedDateMillis = newTimeMillis

                // Also update time picker
                val cal = Calendar.getInstance().apply { timeInMillis = newTimeMillis }
                timePickerState.hour = cal.get(Calendar.HOUR_OF_DAY)
                timePickerState.minute = cal.get(Calendar.MINUTE)
            }
        }
    }

    // --- Main Dialog UI ---
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .shadow(8.dp, RoundedCornerShape(24.dp)),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(scrollState),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Dialog title
                Text(
                    text = dialogTitle,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier.padding(bottom = 20.dp)
                )

                // Task title input
                TaskTitleInput(
                    title = title,
                    titleError = titleError,
                    onTitleChange = {
                        title = it
                        if (it.isNotBlank()) titleError = null
                    },
                    focusRequester = focusRequester,
                    onDone = {
                        focusManager.clearFocus()
                        keyboardController?.hide()
                    }
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Time Section
                TimeSection(
                    selectedTimeMode = selectedTimeMode,
                    dueMinutes = dueMinutes,
                    customTimeInput = customTimeInput,
                    onDueMinutesChange = { dueMinutes = it },
                    onCustomTimeInputChange = {
                        customTimeInput = it
                        it.toIntOrNull()?.let { mins ->
                            if (mins > 0) dueMinutes = mins
                        }
                    },
                    onTimeModeChange = { switchTimeMode(it) },
                    onShowDatePicker = { showDatePicker = true },
                    onShowTimePicker = { showTimePicker = true },
                    datePickerState = datePickerState,
                    timePickerState = timePickerState,
                    selectedDateTimeFormatted = selectedDateTimeFormatted,
                    keyboardController = keyboardController,
                    focusManager = focusManager
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Color Selection
                ColorSelector(
                    selectedColor = selectedColor,
                    onColorSelected = { selectedColor = it },
                    isExpanded = isColorPanelExpanded,
                    onToggleExpanded = { isColorPanelExpanded = !isColorPanelExpanded }
                )

                Spacer(modifier = Modifier.height(28.dp))

                // Action buttons
                ActionButtons(
                    onCancel = onDismiss,
                    onSubmit = { validateAndSubmit() },
                    submitText = submitButtonText,
                    isSubmitEnabled = title.isNotBlank()
                )
            }
        }
    }

    // Date Picker Dialog
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = { showDatePicker = false },
                    modifier = Modifier.padding(end = 8.dp, bottom = 8.dp)
                ) { Text("OK") }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDatePicker = false },
                    modifier = Modifier.padding(end = 8.dp, bottom = 8.dp)
                ) { Text("Cancel") }
            },
            shape = RoundedCornerShape(24.dp)
        ) {
            DatePicker(
                state = datePickerState,
                modifier = Modifier.padding(16.dp)
            )
        }
    }

    // Time Picker Dialog
    if (showTimePicker) {
        EnhancedTimePickerDialog(
            onDismiss = { showTimePicker = false },
            onConfirm = { showTimePicker = false },
            timePickerState = timePickerState
        )
    }
}