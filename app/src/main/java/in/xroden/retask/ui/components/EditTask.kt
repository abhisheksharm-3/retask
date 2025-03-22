package `in`.xroden.retask.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.AccessTime
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.ColorLens
import androidx.compose.material.icons.rounded.Error
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
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

    var dueMinutes by remember { mutableStateOf(initialDueMinutes) }
    var customTimeInput by remember { mutableStateOf(initialDueMinutes.toString()) }
    var isCustomTimeMode by remember { mutableStateOf(false) }
    var selectedColor by remember { mutableStateOf(task.colorHex) }
    var isColorPanelExpanded by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val haptic = LocalHapticFeedback.current
    val titleFocusRequester = remember { FocusRequester() }

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

                Divider(
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
                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
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
                                    if (!isCustomTimeMode) {
                                        keyboardController?.hide()
                                    }
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
                                // Visual time indicator
                                val animatedDueMinutes by animateFloatAsState(
                                    targetValue = dueMinutes.toFloat(),
                                    label = "DueMinutesAnimation"
                                )

                                Slider(
                                    value = animatedDueMinutes,
                                    onValueChange = { dueMinutes = it.toInt() },
                                    valueRange = 5f..480f,  // Extended to 8 hours
                                    steps = 0,  // Continuous slider for better UX
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = SliderDefaults.colors(
                                        thumbColor = MaterialTheme.colorScheme.primary,
                                        activeTrackColor = MaterialTheme.colorScheme.primary,
                                        inactiveTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                                    )
                                )

                                // Quick time presets in flow layout
                                FlowRow(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 8.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    timePresets.forEach { (time, label) ->
                                        OutlinedButton(
                                            onClick = {
                                                dueMinutes = time
                                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
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
                            OutlinedTextField(
                                value = customTimeInput,
                                onValueChange = {
                                    if (it.isEmpty() || it.matches(Regex("^\\d{0,4}$"))) {
                                        customTimeInput = it
                                        it.toIntOrNull()?.let { mins ->
                                            if (mins > 0) dueMinutes = mins
                                        }
                                    }
                                },
                                label = { Text("Minutes") },
                                placeholder = { Text("Enter custom time in minutes") },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth(),
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Number,
                                    imeAction = ImeAction.Done
                                ),
                                keyboardActions = KeyboardActions(
                                    onDone = {
                                        keyboardController?.hide()
                                        focusManager.clearFocus()
                                    }
                                ),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                                ),
                                singleLine = true
                            )
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
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                }
                                .semantics { contentDescription = "Color selection section" },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.ColorLens,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )

                            Spacer(modifier = Modifier.width(12.dp))

                            Text(
                                text = "Task Color",
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.weight(1f)
                            )

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
                                    Box(
                                        modifier = Modifier
                                            .size(48.dp)
                                            .shadow(
                                                elevation = if (color == selectedColor) 4.dp else 1.dp,
                                                shape = CircleShape
                                            )
                                            .clip(CircleShape)
                                            .background(colorValue)
                                            .border(
                                                width = if (color == selectedColor) 2.dp else 0.dp,
                                                color = MaterialTheme.colorScheme.primary,
                                                shape = CircleShape
                                            )
                                            .clickable {
                                                selectedColor = color
                                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                            }
                                            .semantics { contentDescription = "Select $name color" },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (color == selectedColor) {
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
                        onClick = onDismiss,
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