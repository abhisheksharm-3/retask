package `in`.xroden.retask.ui.components

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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.rounded.AccessTime
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.SoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import `in`.xroden.retask.utils.TimeUtils
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.max

/**
 * Time selection section with multiple input modes
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimeSection(
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
    keyboardController: SoftwareKeyboardController?,
    focusManager: FocusManager
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 2.dp,
                shape = RoundedCornerShape(20.dp),
                spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
            ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
        ),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            SectionHeader(
                icon = Icons.Rounded.AccessTime,
                title = "Due Time"
            )

            // Tab selector
            TimeModeTabs(
                selectedMode = selectedTimeMode,
                onModeSelected = onTimeModeChange
            )

            // Time input based on selected mode
            when (selectedTimeMode) {
                "calendar" -> {
                    CalendarTimeInput(
                        onShowDatePicker = onShowDatePicker,
                        onShowTimePicker = onShowTimePicker,
                        datePickerState = datePickerState,
                        timePickerState = timePickerState,
                        selectedDateTimeFormatted = selectedDateTimeFormatted
                    )
                }
                "custom" -> {
                    CustomTimeInput(
                        customTimeInput = customTimeInput,
                        onCustomTimeInputChange = onCustomTimeInputChange,
                        keyboardController = keyboardController,
                        focusManager = focusManager,
                        dueMinutes = dueMinutes
                    )
                }
                else -> {
                    SliderTimeInput(
                        dueMinutes = dueMinutes,
                        onDueMinutesChange = onDueMinutesChange
                    )
                }
            }
        }
    }
}

/**
 * Section header with icon and title
 */
@Composable
fun SectionHeader(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(bottom = 12.dp)
    ) {
        // Icon container
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
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(18.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.SemiBold
            )
        )
    }
}

/**
 * Tabs for selecting time input mode
 */
@Composable
fun TimeModeTabs(
    selectedMode: String,
    onModeSelected: (String) -> Unit
) {
    // Mode options (cached to prevent recreations)
    val modes = remember {
        listOf(
            Triple("slider", Icons.Filled.Schedule, "Slider"),
            Triple("custom", Icons.Rounded.AccessTime, "Custom"),
            Triple("calendar", Icons.Filled.CalendarMonth, "Calendar")
        )
    }

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
        modes.forEach { (mode, icon, label) ->
            TabButton(
                selected = selectedMode == mode,
                onClick = { onModeSelected(mode) },
                icon = { Icon(icon, null, Modifier.size(16.dp)) },
                text = label
            )
        }
    }
}

/**
 * Tab button for time mode selection
 */
@Composable
fun TabButton(
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

/**
 * Calendar-based time input
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarTimeInput(
    onShowDatePicker: () -> Unit,
    onShowTimePicker: () -> Unit,
    datePickerState: androidx.compose.material3.DatePickerState,
    timePickerState: androidx.compose.material3.TimePickerState,
    selectedDateTimeFormatted: String
) {
    // Date and time buttons
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

            // Format date for display
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

            // Format time for display (memoized to prevent unnecessary recreations)
            val timeStr = remember(timePickerState.hour, timePickerState.minute) {
                SimpleDateFormat("h:mm a", Locale.getDefault()).format(
                    Calendar.getInstance().apply {
                        set(Calendar.HOUR_OF_DAY, timePickerState.hour)
                        set(Calendar.MINUTE, timePickerState.minute)
                    }.time
                )
            }

            Text(
                text = timeStr,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }

    // Due time summary
    DueTimeSummary(text = "Due: $selectedDateTimeFormatted")
}

/**
 * Custom minutes input
 */
@Composable
fun CustomTimeInput(
    customTimeInput: String,
    onCustomTimeInputChange: (String) -> Unit,
    keyboardController: SoftwareKeyboardController?,
    focusManager: FocusManager,
    dueMinutes: Int
) {
    // Text field for custom minutes
    OutlinedTextField(
        value = customTimeInput,
        onValueChange = { value ->
            if (value.isEmpty() || value.matches(Regex("^\\d{0,4}$"))) {
                onCustomTimeInputChange(value)
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
    )

    // Display formatted time
    val formattedTime = remember(customTimeInput, dueMinutes) {
        "Task due in ${TimeUtils.formatTimeFromMinutes(
            max(1, customTimeInput.toIntOrNull() ?: dueMinutes)
        )}"
    }

    DueTimeSummary(text = formattedTime)
}

/**
 * Slider-based time input
 */
@Composable
fun SliderTimeInput(
    dueMinutes: Int,
    onDueMinutesChange: (Int) -> Unit
) {
    // Presets for quick selection
    val presets = remember { listOf(Pair(15, "15m"), Pair(30, "30m"), Pair(60, "1h"), Pair(120, "2h")) }

    // Display formatted time
    val formattedTime = remember(dueMinutes) {
        "Due in ${TimeUtils.formatTimeFromMinutes(dueMinutes)}"
    }

    DueTimeSummary(text = formattedTime)

    // Time slider
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

    // Quick presets
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        presets.forEach { (time, label) ->
            key(time) {  // Add key for better performance with collections
                PresetChip(
                    text = label,
                    selected = dueMinutes == time,
                    onClick = { onDueMinutesChange(time) }
                )
            }
        }
    }
}

/**
 * Highlighted text box for displaying due time summary
 */
@Composable
fun DueTimeSummary(text: String) {
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
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

/**
 * Preset time chip for quick selection
 */
@Composable
fun PresetChip(
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