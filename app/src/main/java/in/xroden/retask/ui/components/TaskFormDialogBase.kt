package `in`.xroden.retask.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.rounded.AccessTime
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.ColorLens
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.graphics.toColorInt
import `in`.xroden.retask.utils.TimeUtils
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.max

/**
 * Elegant task form dialog for task creation and editing
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
    // State variables
    var title by remember { mutableStateOf(initialTitle) }
    var titleError by remember { mutableStateOf<String?>(null) }
    var dueMinutes by remember { mutableIntStateOf(initialDueMinutes) }
    var customTimeInput by remember { mutableStateOf(initialDueMinutes.toString()) }
    var selectedTimeMode by remember { mutableStateOf("slider") }
    var selectedColor by remember { mutableStateOf(initialColor) }
    var isColorPanelExpanded by remember { mutableStateOf(false) }

    // Calendar/date picker states
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    // Utility components
    val haptic = LocalHapticFeedback.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }
    val scrollState = rememberScrollState()

    // Calendar/Time picker states
    val initialDateMillis = remember { TimeUtils.getFutureTimestamp(initialDueMinutes) }
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = initialDateMillis)

    val calendar = Calendar.getInstance()
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

    // Update due minutes from date/time selection
    LaunchedEffect(datePickerState.selectedDateMillis, timePickerState.hour, timePickerState.minute) {
        datePickerState.selectedDateMillis?.let { selectedDateMillis ->
            val dateCalendar = Calendar.getInstance().apply {
                timeInMillis = selectedDateMillis
                set(Calendar.HOUR_OF_DAY, timePickerState.hour)
                set(Calendar.MINUTE, timePickerState.minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            val calculatedMinutes = TimeUtils.calculateMinutesFromTimestamp(dateCalendar.timeInMillis)
            if (selectedTimeMode == "calendar" && calculatedMinutes > 0) {
                dueMinutes = calculatedMinutes
                customTimeInput = dueMinutes.toString()
            }
        }
    }

    // Auto-focus title field when dialog opens
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    // Function to validate input before submission
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

    // Switch to a specific time input mode
    fun switchTimeMode(mode: String) {
        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        selectedTimeMode = mode
        keyboardController?.hide()

        if (mode == "custom" && customTimeInput.isEmpty()) {
            customTimeInput = dueMinutes.toString()
        } else if (mode == "calendar") {
            // Update the calendar to reflect current dueMinutes
            val newTimeMillis = TimeUtils.getFutureTimestamp(dueMinutes)
            datePickerState.selectedDateMillis = newTimeMillis

            // Also update time picker
            val cal = Calendar.getInstance().apply { timeInMillis = newTimeMillis }
            timePickerState.hour = cal.get(Calendar.HOUR_OF_DAY)
            timePickerState.minute = cal.get(Calendar.MINUTE)
        }
    }

    // Main dialog
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
                .defaultMinSize(minHeight = 100.dp)
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
                OutlinedTextField(
                    value = title,
                    onValueChange = {
                        title = it
                        if (it.isNotBlank()) titleError = null
                    },
                    label = { Text("Task Title") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester),
                    isError = titleError != null,
                    supportingText = titleError?.let { { Text(it) } },
                    leadingIcon = {
                        Icon(
                            Icons.Rounded.Edit,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                        )
                    },
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(onDone = {
                        focusManager.clearFocus()
                        keyboardController?.hide()
                    }),
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp)
                    // Removed TextFieldDefaults.outlinedTextFieldColors
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Time Section with rounded card
                TimeSection(
                    selectedTimeMode = selectedTimeMode,
                    dueMinutes = dueMinutes,
                    customTimeInput = customTimeInput,
                    onDueMinutesChange = { dueMinutes = it },
                    onCustomTimeInputChange = { customTimeInput = it },
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(
                            width = 1.5.dp,
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                        )
                    ) {
                        Text(
                            "Cancel",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }

                    Button(
                        onClick = { validateAndSubmit() },
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        enabled = title.isNotBlank()
                    ) {
                        Text(
                            submitButtonText,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        }
    }

    // Date Picker Dialog - improved styling
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = { showDatePicker = false },
                    modifier = Modifier.padding(end = 8.dp, bottom = 8.dp)
                ) {
                    Text(
                        "OK",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDatePicker = false },
                    modifier = Modifier.padding(end = 8.dp, bottom = 8.dp)
                ) {
                    Text(
                        "Cancel",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            },
            shape = RoundedCornerShape(24.dp)
        ) {
            DatePicker(
                state = datePickerState,
                modifier = Modifier.padding(16.dp)
            )
        }
    }

    // Time Picker Dialog - improved styling
    if (showTimePicker) {
        EnhancedTimePickerDialog(
            onDismiss = { showTimePicker = false },
            onConfirm = { showTimePicker = false },
            timePickerState = timePickerState
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimeSection(
    selectedTimeMode: String,
    dueMinutes: Int,
    customTimeInput: String,
    onDueMinutesChange: (Int) -> Unit,
    onCustomTimeInputChange: (String) -> Unit,
    onTimeModeChange: (String) -> Unit,
    onShowDatePicker: () -> Unit,
    onShowTimePicker: () -> Unit,
    datePickerState: androidx.compose.material3.DatePickerState,
    timePickerState: androidx.compose.material3.TimePickerState,
    selectedDateTimeFormatted: String,
    keyboardController: androidx.compose.ui.platform.SoftwareKeyboardController?,
    focusManager: androidx.compose.ui.focus.FocusManager
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 2.dp, shape = RoundedCornerShape(20.dp), spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
        ),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header with improved styling
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary,
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.AccessTime,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = "Due Time",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    )
                )
            }

            // Time mode selector - Enhanced pill tabs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        MaterialTheme.colorScheme.surface,
                        RoundedCornerShape(16.dp)
                    )
                    .padding(4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                TabButton(
                    selected = selectedTimeMode == "slider",
                    onClick = { onTimeModeChange("slider") },
                    icon = { Icon(Icons.Filled.Schedule, null, Modifier.size(16.dp)) },
                    text = "Slider"
                )

                TabButton(
                    selected = selectedTimeMode == "custom",
                    onClick = { onTimeModeChange("custom") },
                    icon = { Icon(Icons.Rounded.AccessTime, null, Modifier.size(16.dp)) },
                    text = "Custom"
                )

                TabButton(
                    selected = selectedTimeMode == "calendar",
                    onClick = { onTimeModeChange("calendar") },
                    icon = { Icon(Icons.Filled.CalendarMonth, null, Modifier.size(16.dp)) },
                    text = "Calendar"
                )
            }

            // Time content based on selected mode
            when (selectedTimeMode) {
                "calendar" -> {
                    // Enhanced calendar mode
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Date button
                        OutlinedButton(
                            onClick = onShowDatePicker,
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            contentPadding = ButtonDefaults.ButtonWithIconContentPadding,
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(
                                width = 1.5.dp,
                                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                            )
                        ) {
                            Icon(
                                Icons.Filled.CalendarMonth,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            // Simplified date display to avoid overflow
                            val dateStr = datePickerState.selectedDateMillis?.let {
                                SimpleDateFormat("MMM d", Locale.getDefault()).format(it)
                            } ?: "Date"

                            Text(
                                text = dateStr,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        // Time button
                        OutlinedButton(
                            onClick = onShowTimePicker,
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            contentPadding = ButtonDefaults.ButtonWithIconContentPadding,
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(
                                width = 1.5.dp,
                                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                            )
                        ) {
                            Icon(
                                Icons.Rounded.AccessTime,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            val timeStr = SimpleDateFormat("h:mm a", Locale.getDefault()).format(
                                Calendar.getInstance().apply {
                                    set(Calendar.HOUR_OF_DAY, timePickerState.hour)
                                    set(Calendar.MINUTE, timePickerState.minute)
                                }.time
                            )

                            Text(
                                text = timeStr,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }

                    // Due time summary - improved styling
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                RoundedCornerShape(12.dp)
                            )
                            .padding(vertical = 10.dp, horizontal = 12.dp)
                    ) {
                        Text(
                            text = "Due: $selectedDateTimeFormatted",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                "custom" -> {
                    // Custom time input - enhanced
                    OutlinedTextField(
                        value = customTimeInput,
                        onValueChange = { value ->
                            if (value.isEmpty() || value.matches(Regex("^\\d{0,4}$"))) {
                                onCustomTimeInputChange(value)
                                value.toIntOrNull()?.let { mins ->
                                    if (mins > 0) onDueMinutesChange(mins)
                                }
                            }
                        },
                        label = { Text("Minutes") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(onDone = {
                            keyboardController?.hide()
                            focusManager.clearFocus()
                        }),
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp)
                        // Removed TextFieldDefaults.outlinedTextFieldColors
                    )

                    // Display formatted time - enhanced styling
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                RoundedCornerShape(12.dp)
                            )
                            .padding(vertical = 10.dp, horizontal = 12.dp)
                    ) {
                        Text(
                            text = "Task due in ${TimeUtils.formatTimeFromMinutes(
                                max(1, customTimeInput.toIntOrNull() ?: dueMinutes)
                            )}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                else -> {
                    // Slider time selection - enhanced
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                RoundedCornerShape(12.dp)
                            )
                            .padding(vertical = 10.dp, horizontal = 12.dp)
                    ) {
                        Text(
                            text = "Due in ${TimeUtils.formatTimeFromMinutes(dueMinutes)}",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Medium
                            ),
                            color = MaterialTheme.colorScheme.primary,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Slider(
                        value = dueMinutes.toFloat(),
                        onValueChange = { onDueMinutesChange(it.toInt()) },
                        valueRange = 5f..480f,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        colors = SliderDefaults.colors(
                            thumbColor = MaterialTheme.colorScheme.primary,
                            activeTrackColor = MaterialTheme.colorScheme.primary,
                            inactiveTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                        )
                    )

                    // Quick presets - enhanced horizontal row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        listOf(
                            Pair(15, "15m"),
                            Pair(30, "30m"),
                            Pair(60, "1h"),
                            Pair(120, "2h")
                        ).forEach { (time, label) ->
                            PresetChip(
                                text = label,
                                selected = dueMinutes == time,
                                onClick = { onDueMinutesChange(time) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TabButton(
    selected: Boolean,
    onClick: () -> Unit,
    icon: @Composable () -> Unit,
    text: String
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                else Color.Transparent
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onClick() }
            .padding(horizontal = 12.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            icon()

            Spacer(modifier = Modifier.width(6.dp))

            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
                ),
                color = if (selected) MaterialTheme.colorScheme.primary
                else LocalContentColor.current.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
private fun PresetChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(30.dp))
            .background(
                if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                else Color.Transparent
            )
            .border(
                width = 1.5.dp,
                color = if (selected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                shape = RoundedCornerShape(30.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = if (selected) FontWeight.Medium else FontWeight.Normal
            ),
            color = if (selected) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
        )
    }
}

@Composable
private fun ColorSelector(
    selectedColor: String,
    onColorSelected: (String) -> Unit,
    isExpanded: Boolean,
    onToggleExpanded: () -> Unit
) {
    val colorOptions = remember {
        listOf(
            "#FFD6D6", "#FFE2B8", "#FFF9B8", "#D6FFB8",
            "#B8FFD6", "#B8FFEC", "#B8F1FF", "#B8D6FF",
            "#D6B8FF", "#F1B8FF", "#FFB8EC", "#FFB8D6"
        )
    }

    val rotationState by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        animationSpec = tween(300),
        label = "rotation"
    )

    val colorObj = remember(selectedColor) { Color(selectedColor.toColorInt()) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 2.dp, shape = RoundedCornerShape(20.dp), spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
        ),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header with color preview - enhanced styling
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onToggleExpanded() }
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary,
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.ColorLens,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = "Task Color",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    modifier = Modifier.weight(1f)
                )

                // Selected color preview
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(colorObj)
                        .border(
                            width = 1.5.dp,
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                            shape = CircleShape
                        )
                )

                Spacer(modifier = Modifier.width(8.dp))

                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                    modifier = Modifier.rotate(rotationState)
                )
            }

            // Color grid - enhanced with better animation
            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn(animationSpec = tween(300)) + expandVertically(animationSpec = tween(300)),
                exit = fadeOut(animationSpec = tween(200)) + shrinkVertically(animationSpec = tween(200))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                            RoundedCornerShape(16.dp)
                        )
                        .padding(16.dp)
                ) {
                    // First row of colors
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        colorOptions.take(6).forEach { color ->
                            ColorDot(
                                color = color,
                                isSelected = color == selectedColor,
                                onClick = { onColorSelected(color) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Second row of colors
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        colorOptions.drop(6).forEach { color ->
                            ColorDot(
                                color = color,
                                isSelected = color == selectedColor,
                                onClick = { onColorSelected(color) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ColorDot(
    color: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val colorValue = Color(color.toColorInt())

    Box(
        modifier = Modifier
            .size(42.dp)
            .clip(CircleShape)
            .shadow(
                elevation = if (isSelected) 4.dp else 1.dp,
                shape = CircleShape,
                spotColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.Black.copy(alpha = 0.1f)
            )
            .background(colorValue)
            .border(
                width = if (isSelected) 2.5.dp else 0.dp,
                color = MaterialTheme.colorScheme.primary,
                shape = CircleShape
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (isSelected) {
            Box(
                modifier = Modifier
                    .size(26.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Check,
                    contentDescription = null,
                    tint = if (colorValue.red + colorValue.green + colorValue.blue < 2f)
                        Color.White
                    else
                        Color.Black,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EnhancedTimePickerDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    timePickerState: androidx.compose.material3.TimePickerState
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .shadow(8.dp, RoundedCornerShape(24.dp))
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Select Time",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                TimePicker(
                    state = timePickerState,
                    modifier = Modifier.padding(vertical = 12.dp)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text(
                            "Cancel",
                            style = MaterialTheme.typography.labelLarge
                        )
                    }

                    TextButton(
                        onClick = onConfirm
                    ) {
                        Text(
                            "OK",
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }
        }
    }
}

// Helper function for formatting date and time in a compact way
private fun formatDateTimeCompact(dateMillis: Long, hour: Int, minute: Int): String {
    val calendar = Calendar.getInstance().apply {
        timeInMillis = dateMillis
        set(Calendar.HOUR_OF_DAY, hour)
        set(Calendar.MINUTE, minute)
    }

    val dateFormat = SimpleDateFormat("MMM d", Locale.getDefault())
    val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())

    return "${dateFormat.format(calendar.time)} at ${timeFormat.format(calendar.time)}"
}