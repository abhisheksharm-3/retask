package `in`.xroden.retask.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.rounded.AccessTime
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.ColorLens
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Error
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.graphics.toColorInt
import `in`.xroden.retask.data.model.Task
import kotlin.math.max

@OptIn(ExperimentalLayoutApi::class, ExperimentalComposeUiApi::class, ExperimentalMaterial3Api::class)
@Composable
fun EditTaskDialog(
    task: Task,
    onDismiss: () -> Unit,
    onUpdateTask: (Task, String, Int, String) -> Unit
) {
    var title by remember { mutableStateOf(task.title) }
    var titleError by remember { mutableStateOf<String?>(null) }

    // Calculate initial due minutes (time left from now)
    val initialDueMinutes = remember {
        val now = System.currentTimeMillis()
        val diffMinutes = (task.dueDate - now) / (1000 * 60)
        if (diffMinutes <= 0) 15 else diffMinutes.toInt() // Default to 15 min if already due
    }

    var dueMinutes by remember { mutableIntStateOf(initialDueMinutes) }
    var customTimeInput by remember { mutableStateOf(initialDueMinutes.toString()) }
    var isCustomTimeMode by remember { mutableStateOf(false) }
    var selectedColor by remember { mutableStateOf(task.colorHex) }
    var isColorPanelExpanded by remember { mutableStateOf(false) }
    var sliderDragging by remember { mutableStateOf(false) }
    var lastDueMinutesValue by remember { mutableIntStateOf(initialDueMinutes) }

    val scrollState = rememberScrollState()
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val haptic = LocalHapticFeedback.current
    val titleFocusRequester = remember { FocusRequester() }

    // Track whether time value has changed significantly to trigger haptic feedback
    val significantTimeChange = remember(dueMinutes, lastDueMinutesValue) {
        val change = kotlin.math.abs(dueMinutes - lastDueMinutesValue)
        val threshold = when {
            lastDueMinutesValue < 60 -> 5  // 5 min increments for < 1h
            lastDueMinutesValue < 120 -> 15 // 15 min increments for 1-2h
            else -> 30 // 30 min increments for > 2h
        }
        change >= threshold
    }

    // If time changes significantly while dragging, provide haptic feedback and update lastValue
    LaunchedEffect(significantTimeChange, sliderDragging) {
        if (significantTimeChange && sliderDragging) {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            lastDueMinutesValue = dueMinutes
        }
    }

    // Enhanced color palette with semantic names for accessibility
    val colorOptions = listOf(
        Pair("#FFD6D6", "Soft Pink"),
        Pair("#FFE2B8", "Peach"),
        Pair("#FFF9B8", "Soft Yellow"),
        Pair("#D6FFB8", "Lime Green"),
        Pair("#B8FFD6", "Mint"),
        Pair("#B8FFEC", "Aqua"),
        Pair("#B8F1FF", "Sky Blue"),
        Pair("#B8D6FF", "Baby Blue"),
        Pair("#D6B8FF", "Lavender"),
        Pair("#F1B8FF", "Light Purple"),
        Pair("#FFB8EC", "Rose"),
        Pair("#FFB8D6", "Bubblegum")
    )

    // Common presets for time selection
    val timePresets = listOf(
        Pair(5, "5m"),
        Pair(15, "15m"),
        Pair(30, "30m"),
        Pair(60, "1h"),
        Pair(120, "2h"),
        Pair(480, "8h")
    )

    // Function to format time display
    fun formatTimeDisplay(minutes: Int): String {
        return when {
            minutes < 60 -> "$minutes minutes"
            minutes % 60 == 0 -> "${minutes / 60} hour${if (minutes > 60) "s" else ""}"
            else -> "${minutes / 60} hour${if (minutes > 60) "s" else ""} ${minutes % 60} minute${if (minutes % 60 > 1) "s" else ""}"
        }
    }

    // Focus on title field when dialog opens
    LaunchedEffect(Unit) {
        titleFocusRequester.requestFocus()
    }

    // Function to validate input before submission
    fun validateAndSubmit() {
        if (title.isBlank()) {
            titleError = "Please enter a task title"
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            return
        }

        val finalMinutes = if (isCustomTimeMode) {
            max(1, customTimeInput.toIntOrNull() ?: dueMinutes)
        } else {
            dueMinutes
        }

        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        onUpdateTask(task, title.trim(), finalMinutes, selectedColor)
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .pointerInput(Unit) {
                    detectTapGestures(onTap = {
                        // Clear focus when tapping outside input fields
                        focusManager.clearFocus()
                    })
                },
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .verticalScroll(scrollState),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header with subtle decoration
                Text(
                    text = "Edit Task",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    textAlign = TextAlign.Center
                )

                HorizontalDivider(
                    modifier = Modifier
                        .width(64.dp)
                        .padding(bottom = 24.dp),
                    thickness = 4.dp,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                )

                // Task title input with better validation
                OutlinedTextField(
                    value = title,
                    onValueChange = {
                        title = it
                        if (it.isNotBlank()) titleError = null
                    },
                    label = { Text("Task Title") },
                    placeholder = { Text("What needs to be done?") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Rounded.Edit,
                            contentDescription = "Task Title Icon",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    trailingIcon = {
                        if (titleError != null) {
                            Icon(
                                imageVector = Icons.Rounded.Error,
                                contentDescription = "Error Icon",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    },
                    isError = titleError != null,
                    supportingText = {
                        if (titleError != null) {
                            Text(
                                text = titleError!!,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    },
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(titleFocusRequester)
                        .semantics { contentDescription = "Enter task title" },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp),
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp),
                        errorContainerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.1f)
                    ),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = {
                            focusManager.moveFocus(FocusDirection.Down)
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        }
                    )
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Time selection section with improved UI
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        // Time header with accessibility enhancement
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .semantics { contentDescription = "Time selection section" }
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.AccessTime,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = "Due Time",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )

                                Text(
                                    text = "Task will be due in ${
                                        if (isCustomTimeMode) {
                                            val mins = customTimeInput.toIntOrNull() ?: dueMinutes
                                            formatTimeDisplay(max(1, mins))
                                        } else {
                                            formatTimeDisplay(dueMinutes)
                                        }
                                    }",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            TextButton(
                                onClick = {
                                    isCustomTimeMode = !isCustomTimeMode
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    if (!isCustomTimeMode) {
                                        keyboardController?.hide()
                                    } else if (customTimeInput.isEmpty()) {
                                        customTimeInput = dueMinutes.toString()
                                    }
                                },
                                modifier = Modifier.semantics {
                                    contentDescription = if (isCustomTimeMode) "Switch to slider mode" else "Switch to custom time entry"
                                }
                            ) {
                                Text(
                                    if (isCustomTimeMode) "Use Slider" else "Custom",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Slider or custom input with animations
                        AnimatedVisibility(
                            visible = !isCustomTimeMode,
                            enter = fadeIn() + expandVertically(),
                            exit = fadeOut() + shrinkVertically()
                        ) {
                            Column {
                                // Add min/max slider labels
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "5m",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "8h",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                // Visual time indicator
                                val animatedDueMinutes by animateFloatAsState(
                                    targetValue = dueMinutes.toFloat(),
                                    label = "DueMinutesAnimation"
                                )

                                Box(
                                    modifier = Modifier.fillMaxWidth(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Slider(
                                        value = animatedDueMinutes,
                                        onValueChange = { dueMinutes = it.toInt() },
                                        valueRange = 5f..480f,  // Extended to 8 hours
                                        steps = 0,  // Continuous slider for better UX
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .semantics {
                                                contentDescription = "Adjust due time, currently ${formatTimeDisplay(dueMinutes)}"
                                            },
                                        colors = SliderDefaults.colors(
                                            thumbColor = MaterialTheme.colorScheme.primary,
                                            activeTrackColor = MaterialTheme.colorScheme.primary,
                                            inactiveTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                                        ),
                                        onValueChangeFinished = {
                                            sliderDragging = false
                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        },
                                        // Track when slider is being dragged to provide haptic feedback
                                        interactionSource = remember {
                                            androidx.compose.foundation.interaction.MutableInteractionSource()
                                        }.also { interactionSource ->
                                            LaunchedEffect(interactionSource) {
                                                interactionSource.interactions.collect { interaction ->
                                                    when (interaction) {
                                                        is androidx.compose.foundation.interaction.DragInteraction.Start -> {
                                                            sliderDragging = true
                                                            lastDueMinutesValue = dueMinutes
                                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    )
                                }

                                // Current value indication
                                Text(
                                    text = formatTimeDisplay(dueMinutes),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.align(Alignment.CenterHorizontally),
                                    fontWeight = FontWeight.SemiBold
                                )

                                // Quick time presets in flow layout
                                FlowRow(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 12.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    timePresets.forEach { (time, label) ->
                                        OutlinedButton(
                                            onClick = {
                                                if (dueMinutes != time) {
                                                    dueMinutes = time
                                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                }
                                            },
                                            shape = RoundedCornerShape(12.dp),
                                            colors = ButtonDefaults.outlinedButtonColors(
                                                containerColor = if (dueMinutes == time)
                                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                                else
                                                    Color.Transparent
                                            ),
                                            border = BorderStroke(
                                                width = 1.dp,
                                                color = if (dueMinutes == time)
                                                    MaterialTheme.colorScheme.primary
                                                else
                                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                                            ),
                                            modifier = Modifier.semantics {
                                                contentDescription = "Set due time to $label"
                                            }
                                        ) {
                                            Text(
                                                text = label,
                                                style = MaterialTheme.typography.bodySmall
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        AnimatedVisibility(
                            visible = isCustomTimeMode,
                            enter = fadeIn() + expandVertically(),
                            exit = fadeOut() + shrinkVertically()
                        ) {
                            Column {
                                OutlinedTextField(
                                    value = customTimeInput,
                                    onValueChange = {
                                        if (it.isEmpty() || it.matches(Regex("^\\d{0,4}$"))) {
                                            // Provide subtle haptic feedback when typing
                                            if (it.length != customTimeInput.length) {
                                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                            }
                                            customTimeInput = it
                                            it.toIntOrNull()?.let { mins ->
                                                if (mins > 0) dueMinutes = mins
                                            }
                                        }
                                    },
                                    label = { Text("Minutes") },
                                    placeholder = { Text("Enter custom time in minutes") },
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .semantics {
                                            contentDescription = "Enter custom due time in minutes"
                                        },
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Number,
                                        imeAction = ImeAction.Done
                                    ),
                                    keyboardActions = KeyboardActions(
                                        onDone = {
                                            keyboardController?.hide()
                                            focusManager.clearFocus()
                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        }
                                    ),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                                        unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                                    ),
                                    singleLine = true,
                                    supportingText = {
                                        Text(
                                            "Enter time in minutes (1-1440)",
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                )

                                // Add quick preset buttons for custom mode too
                                FlowRow(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 12.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    timePresets.forEach { (time, label) ->
                                        OutlinedButton(
                                            onClick = {
                                                customTimeInput = time.toString()
                                                dueMinutes = time
                                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                            },
                                            shape = RoundedCornerShape(12.dp),
                                            colors = ButtonDefaults.outlinedButtonColors(
                                                containerColor = if (customTimeInput == time.toString())
                                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                                else
                                                    Color.Transparent
                                            ),
                                            border = BorderStroke(
                                                width = 1.dp,
                                                color = if (customTimeInput == time.toString())
                                                    MaterialTheme.colorScheme.primary
                                                else
                                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                                            )
                                        ) {
                                            Text(
                                                text = label,
                                                style = MaterialTheme.typography.bodySmall
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Collapsible color selection with preview
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        // Color header with preview
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    isColorPanelExpanded = !isColorPanelExpanded
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                }
                                .semantics {
                                    contentDescription = if (isColorPanelExpanded)
                                        "Collapse color selection"
                                    else
                                        "Expand color selection"
                                },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.ColorLens,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Task Color",
                                    style = MaterialTheme.typography.titleMedium
                                )

                                // Show the semantic color name for better feedback
                                val colorName = colorOptions.find { it.first == selectedColor }?.second ?: "Custom"
                                Text(
                                    text = colorName,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            // Selected color preview
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(Color(selectedColor.toColorInt()))
                                    .border(
                                        width = 1.dp,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                                        shape = CircleShape
                                    )
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            // Expandable indicator
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = if (isColorPanelExpanded) "Collapse" else "Expand",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Color selection grid
                        AnimatedVisibility(
                            visible = isColorPanelExpanded,
                            enter = fadeIn() + expandVertically(),
                            exit = fadeOut() + shrinkVertically()
                        ) {
                            FlowRow(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                maxItemsInEachRow = 6
                            ) {
                                colorOptions.forEach { (color, name) ->
                                    val colorValue = Color(color.toColorInt())
                                    val isSelected = color == selectedColor

                                    Box(
                                        modifier = Modifier
                                            .size(48.dp)
                                            .shadow(
                                                elevation = if (isSelected) 4.dp else 1.dp,
                                                shape = CircleShape
                                            )
                                            .clip(CircleShape)
                                            .background(colorValue)
                                            .border(
                                                width = if (isSelected) 2.dp else 0.dp,
                                                color = MaterialTheme.colorScheme.primary,
                                                shape = CircleShape
                                            )
                                            .clickable {
                                                if (color != selectedColor) {
                                                    selectedColor = color
                                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                }
                                            }
                                            .semantics { contentDescription = "Select $name color" },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (isSelected) {
                                            Icon(
                                                imageVector = Icons.Rounded.Check,
                                                contentDescription = null,
                                                tint = if (colorValue.red + colorValue.green + colorValue.blue < 2f)
                                                    Color.White
                                                else
                                                    Color.Black,
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Action buttons with improved styling
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onDismiss()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp)
                            .semantics { contentDescription = "Cancel editing task" },
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                        ),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Text(
                            "Cancel",
                            style = MaterialTheme.typography.labelLarge
                        )
                    }

                    Button(
                        onClick = { validateAndSubmit() },
                        modifier = Modifier
                            .weight(1.5f)
                            .height(56.dp)
                            .semantics { contentDescription = "Update task" },
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                        ),
                        enabled = title.isNotBlank()
                    ) {
                        Text(
                            "Update Task",
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }
        }
    }
}