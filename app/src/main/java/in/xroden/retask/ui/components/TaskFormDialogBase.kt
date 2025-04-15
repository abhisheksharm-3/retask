package `in`.xroden.retask.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreTime
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material.icons.outlined.Colorize
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.material3.TimePickerLayoutType
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
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
import `in`.xroden.retask.utils.TimeUtils
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Calendar
import kotlin.math.max
import androidx.core.graphics.toColorInt

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
    // --- State management ---
    var title by remember { mutableStateOf(initialTitle) }
    var titleError by remember { mutableStateOf<String?>(null) }
    var dueMinutes by remember { mutableIntStateOf(initialDueMinutes) }
    var sliderPosition by remember { mutableFloatStateOf(initialDueMinutes.toFloat()) }
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
    val interactionSource = remember { MutableInteractionSource() }

    // --- Date/Time picker states ---
    val initialDateMillis = remember { TimeUtils.getFutureTimestamp(initialDueMinutes) }
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = initialDateMillis)

    val calendar = remember { Calendar.getInstance() }
    val timePickerState = rememberTimePickerState(
        initialHour = calendar.get(Calendar.HOUR_OF_DAY),
        initialMinute = calendar.get(Calendar.MINUTE),
        is24Hour = true
    )

    // Derived state for slider
    val sliderLabel by remember(sliderPosition) {
        derivedStateOf {
            formatTimeFromMinutes(sliderPosition.toInt())
        }
    }

    // Derived state for selected date/time
    val selectedDateTimeFormatted by remember(datePickerState.selectedDateMillis, timePickerState.hour, timePickerState.minute) {
        derivedStateOf {
            datePickerState.selectedDateMillis?.let { millis ->
                formatDateTime(millis, timePickerState.hour, timePickerState.minute)
            } ?: "Not set"
        }
    }

    // Derived state for estimated completion time
    val estimatedCompletionTime by remember(dueMinutes) {
        derivedStateOf {
            val now = LocalDateTime.now()
            val completion = now.plusMinutes(dueMinutes.toLong())
            val formatter = DateTimeFormatter.ofPattern("h:mm a, E")
            "Est. completion: ${completion.format(formatter)}"
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
                sliderPosition = minOf(dueMinutes.toFloat(), 240f) // Cap at slider max
            }
        }
    }

    // Update dueMinutes when slider changes
    LaunchedEffect(sliderPosition) {
        if (selectedTimeMode == "slider") {
            dueMinutes = sliderPosition.toInt()
            customTimeInput = dueMinutes.toString()
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
            "slider" -> {
                sliderPosition = minOf(dueMinutes.toFloat(), 240f) // Cap at slider max
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
                // Dialog title with styling
                Text(
                    text = dialogTitle,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 20.dp)
                )

                // Task title input with error handling
                OutlinedTextField(
                    value = title,
                    onValueChange = {
                        title = it
                        if (it.isNotBlank()) titleError = null
                    },
                    label = { Text("Task Title") },
                    placeholder = { Text("Enter task title") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester),
                    isError = titleError != null,
                    supportingText = titleError?.let {
                        { Text(it, color = MaterialTheme.colorScheme.error) }
                    },
                    leadingIcon = {
                        Icon(
                            Icons.Outlined.Edit,
                            contentDescription = "Task title",
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                        )
                    },
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            focusManager.clearFocus()
                            keyboardController?.hide()
                        }
                    ),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Time Section with tabbed interface (keeping original style)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    // Time section header
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 8.dp)
                    ) {
                        Icon(
                            Icons.Filled.Schedule,
                            contentDescription = "Set time",
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Time Allocation",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }

                    // Original Tab Row style
                    TabRow(
                        selectedTabIndex = when(selectedTimeMode) {
                            "slider" -> 0
                            "custom" -> 1
                            else -> 2
                        }
                    ) {
                        Tab(
                            selected = selectedTimeMode == "slider",
                            onClick = { switchTimeMode("slider") },
                            text = { Text("Slider") },
                            icon = { Icon(Icons.Filled.MoreTime, contentDescription = null, modifier = Modifier.size(18.dp)) }
                        )
                        Tab(
                            selected = selectedTimeMode == "custom",
                            onClick = { switchTimeMode("custom") },
                            text = { Text("Custom") },
                            icon = { Icon(Icons.Filled.Edit, contentDescription = null, modifier = Modifier.size(18.dp)) }
                        )
                        Tab(
                            selected = selectedTimeMode == "calendar",
                            onClick = { switchTimeMode("calendar") },
                            text = { Text("Calendar") },
                            icon = { Icon(Icons.Filled.CalendarMonth, contentDescription = null, modifier = Modifier.size(18.dp)) }
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Different time input options based on selected mode
                    when (selectedTimeMode) {
                        "slider" -> {
                            // Slider time selection
                            Column {
                                Text(
                                    text = sliderLabel,
                                    style = MaterialTheme.typography.titleLarge,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Slider(
                                    value = sliderPosition,
                                    onValueChange = { sliderPosition = it },
                                    valueRange = 5f..240f,
                                    steps = 47, // 240/5 - 1 = 47 steps
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 16.dp)
                                )

                                // Time markers
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("5m", style = MaterialTheme.typography.bodySmall)
                                    Text("1h", style = MaterialTheme.typography.bodySmall)
                                    Text("2h", style = MaterialTheme.typography.bodySmall)
                                    Text("4h", style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                        "custom" -> {
                            // Custom time input
                            OutlinedTextField(
                                value = customTimeInput,
                                onValueChange = { input ->
                                    // Only allow numbers
                                    if (input.all { it.isDigit() } || input.isEmpty()) {
                                        customTimeInput = input
                                        // Update dueMinutes if valid
                                        input.toIntOrNull()?.let { mins ->
                                            if (mins > 0) dueMinutes = mins
                                        }
                                    }
                                },
                                label = { Text("Minutes") },
                                placeholder = { Text("Enter minutes") },
                                modifier = Modifier.fillMaxWidth(),
                                keyboardOptions = KeyboardOptions(
                                    imeAction = ImeAction.Done
                                ),
                                keyboardActions = KeyboardActions(
                                    onDone = {
                                        focusManager.clearFocus()
                                        keyboardController?.hide()
                                    }
                                ),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                leadingIcon = {
                                    Icon(
                                        Icons.Outlined.AccessTime,
                                        contentDescription = "Minutes",
                                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                                    )
                                }
                            )
                        }
                        "calendar" -> {
                            // Calendar & time selection - keeping original UI style
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                // Display selected date and time
                                Text(
                                    text = selectedDateTimeFormatted,
                                    style = MaterialTheme.typography.titleMedium,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(bottom = 16.dp)
                                )

                                // Date and time selection buttons
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    Button(
                                        onClick = { showDatePicker = true },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                Icons.Outlined.CalendarToday,
                                                contentDescription = "Select date",
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Text(
                                                "Select Date",
                                                modifier = Modifier.padding(start = 8.dp)
                                            )
                                        }
                                    }

                                    Button(
                                        onClick = { showTimePicker = true },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                Icons.Outlined.AccessTime,
                                                contentDescription = "Select time",
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Text(
                                                "Select Time",
                                                modifier = Modifier.padding(start = 8.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Estimated completion time
                    Text(
                        text = estimatedCompletionTime,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .padding(top = 16.dp)
                            .fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Color Selector with animation
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        // Color section header with dropdown toggle
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(
                                    interactionSource = interactionSource,
                                    indication = null
                                ) { isColorPanelExpanded = !isColorPanelExpanded }
                                .padding(bottom = 8.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Outlined.Colorize,
                                    contentDescription = "Color",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "Task Color",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.padding(start = 8.dp)
                                )
                            }

                            // Color preview and dropdown icon
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(Color(selectedColor.toColorInt()))
                                        .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                                )

                                val rotationState by animateFloatAsState(
                                    targetValue = if (isColorPanelExpanded) 180f else 0f,
                                    label = "dropdownRotation"
                                )

                                Icon(
                                    Icons.Filled.ArrowDropDown,
                                    contentDescription = "Expand",
                                    modifier = Modifier
                                        .padding(start = 4.dp)
                                        .rotate(rotationState)
                                )
                            }
                        }

                        // Animated color grid
                        AnimatedVisibility(
                            visible = isColorPanelExpanded,
                            enter = fadeIn() + expandVertically(),
                            exit = fadeOut() + shrinkVertically()
                        ) {
                            ColorGrid(
                                selectedColor = selectedColor,
                                onColorSelected = { newColor ->
                                    selectedColor = newColor
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                // Action buttons with enhanced styling
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            "Cancel",
                            style = MaterialTheme.typography.labelLarge
                        )
                    }

                    Button(
                        onClick = { validateAndSubmit() },
                        modifier = Modifier.weight(1f),
                        enabled = title.isNotBlank(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            submitButtonText,
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }
        }
    }

    // Date Picker Dialog with enhanced styling
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
        Dialog(
            onDismissRequest = { showTimePicker = false },
            properties = DialogProperties(
                usePlatformDefaultWidth = false
            )
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .shadow(8.dp, RoundedCornerShape(24.dp)),
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        "Select Time",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    TimePicker(
                        state = timePickerState,
                        layoutType = TimePickerLayoutType.Vertical,
                        colors = TimePickerDefaults.colors(
                            timeSelectorSelectedContainerColor = MaterialTheme.colorScheme.primary,
                            timeSelectorSelectedContentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(
                            onClick = { showTimePicker = false }
                        ) {
                            Text("Cancel")
                        }

                        TextButton(
                            onClick = { showTimePicker = false },
                            modifier = Modifier.padding(start = 8.dp)
                        ) {
                            Text("OK")
                        }
                    }
                }
            }
        }
    }
}

/**
 * A grid of color options for task categorization
 */
@Composable
fun ColorGrid(
    selectedColor: String,
    onColorSelected: (String) -> Unit
) {
    val colorOptions = remember {
        listOf(
            "#FFD6D6", // Light Red
            "#FFECB3", // Light Yellow
            "#E8F5E9", // Light Green
            "#E3F2FD", // Light Blue
            "#EDE7F6", // Light Purple
            "#F3E5F5", // Light Pink
            "#FFCCBC", // Light Orange
            "#F5F5F5", // Light Gray
            "#FF8A80", // Vibrant Red
            "#FFD180", // Vibrant Orange
            "#CCFF90", // Vibrant Green
            "#80D8FF", // Vibrant Blue
        )
    }

    Column(modifier = Modifier.padding(top = 8.dp)) {
        // Grid layout for colors
        for (i in 0 until (colorOptions.size / 4 + (if (colorOptions.size % 4 > 0) 1 else 0))) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                for (j in 0 until 4) {
                    val index = i * 4 + j
                    if (index < colorOptions.size) {
                        ColorOption(
                            color = colorOptions[index],
                            isSelected = selectedColor == colorOptions[index],
                            onSelect = { onColorSelected(colorOptions[index]) }
                        )
                    } else {
                        // Empty space holder
                        Spacer(modifier = Modifier.size(48.dp))
                    }
                }
            }
        }
    }
}

/**
 * A selectable color option with selection indicator
 */
@Composable
fun ColorOption(
    color: String,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Box(contentAlignment = Alignment.Center) {
        // Color circle
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(Color(color.toColorInt()))
                .border(
                    width = if (isSelected) 2.dp else 1.dp,
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                    shape = CircleShape
                )
                .clickable { onSelect() }
        )

        // Selection indicator
        if (isSelected) {
            Icon(
                Icons.Outlined.Circle,
                contentDescription = "Selected",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

/**
 * Formats a time duration from minutes into a user-friendly string
 */
private fun formatTimeFromMinutes(minutes: Int): String {
    return when {
        minutes < 60 -> "$minutes minutes"
        minutes % 60 == 0 -> "${minutes / 60} hours"
        else -> "${minutes / 60} hours ${minutes % 60} minutes"
    }
}

/**
 * Formats a date and time for display in the calendar mode
 */
private fun formatDateTime(dateMillis: Long, hour: Int, minute: Int): String {
    val dateTime = LocalDateTime.ofInstant(
        java.time.Instant.ofEpochMilli(dateMillis),
        java.time.ZoneId.systemDefault()
    ).withHour(hour).withMinute(minute)

    val dateFormatter = DateTimeFormatter.ofPattern("E, MMM d")
    val timeFormatter = DateTimeFormatter.ofPattern("h:mm a")

    return "${dateTime.format(dateFormatter)} at ${dateTime.format(timeFormatter)}"
}