package `in`.xroden.retask.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.ColorLens
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.toColorInt

/**
 * A color selector component that displays a customizable palette of colors
 * for task categorization, featuring a dynamically expanding grid.
 */
@Composable
fun ColorSelector(
    selectedColor: String,
    onColorSelected: (String) -> Unit,
    isExpanded: Boolean,
    onToggleExpanded: () -> Unit,
    modifier: Modifier = Modifier,
    title: String = "Task Color"
) {
    // Find the selected color's name for accessibility
    val selectedColorName by remember(selectedColor) {
        derivedStateOf {
            colorOptions.find { it.hex == selectedColor }?.name ?: "Custom color"
        }
    }

    // Rotate animation for dropdown icon
    val rotationState by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        animationSpec = tween(durationMillis = 300),
        label = "rotation"
    )

    // Convert hex to Color object
    val colorObj = remember(selectedColor) {
        try {
            Color(selectedColor.toColorInt())
        } catch (_: Exception) {
            Color.Gray // Fallback color for invalid hex
        }
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp)
        ),
        // Use the Card's built-in elevation for a more idiomatic approach
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header with color preview and dropdown
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable(onClick = onToggleExpanded, role = Role.Button)
                    .semantics {
                        contentDescription = "Color selector. Currently selected: $selectedColorName. ${if (isExpanded) "Tap to collapse" else "Tap to expand"}"
                        role = Role.Button
                    }
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Rounded.ColorLens,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp
                    )
                )
                Spacer(modifier = Modifier.weight(1f))
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
                        .semantics { contentDescription = "Selected color: $selectedColorName" }
                )
                Spacer(modifier = Modifier.width(12.dp))
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = if (isExpanded) "Collapse color palette" else "Expand color palette",
                    modifier = Modifier.rotate(rotationState)
                )
            }

            // Animated, dynamic color grid
            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn(tween(300)) + expandVertically(tween(300)),
                exit = fadeOut(tween(200)) + shrinkVertically(tween(200))
            ) {
                ColorGrid(
                    colorOptions = colorOptions,
                    selectedColor = selectedColor,
                    onColorSelected = onColorSelected,
                    columns = 6 // You can easily change this to 5, 4, etc.
                )
            }
        }
    }
}

private data class ColorOption(val hex: String, val name: String)

private val colorOptions = listOf(
    ColorOption("#FFD6D6", "Light Red"),
    ColorOption("#FFE2B8", "Light Orange"),
    ColorOption("#FFF9B8", "Light Yellow"),
    ColorOption("#D6FFB8", "Light Lime"),
    ColorOption("#B8FFD6", "Light Green"),
    ColorOption("#B8FFEC", "Light Teal"),
    ColorOption("#B8F1FF", "Light Cyan"),
    ColorOption("#B8D6FF", "Light Blue"),
    ColorOption("#D6B8FF", "Light Purple"),
    ColorOption("#F1B8FF", "Light Magenta"),
    ColorOption("#FFB8EC", "Light Pink"),
    ColorOption("#FFB8D6", "Light Rose")
)

/**
 * A dynamically generated grid layout for displaying selectable color options.
 */
@Composable
private fun ColorGrid(
    colorOptions: List<ColorOption>,
    selectedColor: String,
    onColorSelected: (String) -> Unit,
    columns: Int
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(
                MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                RoundedCornerShape(16.dp)
            )
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Use .chunked() to dynamically create rows based on the number of columns.
        colorOptions.chunked(columns).forEach { rowOptions ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                rowOptions.forEach { colorOption ->
                    key(colorOption.hex) {
                        ColorDot(
                            colorOption = colorOption,
                            isSelected = colorOption.hex == selectedColor,
                            onClick = { onColorSelected(colorOption.hex) }
                        )
                    }
                }
                // Add spacers if the last row is not full, to maintain layout
                if (rowOptions.size < columns) {
                    val spacers = columns - rowOptions.size
                    repeat(spacers) {
                        Spacer(modifier = Modifier.size(44.dp))
                    }
                }
            }
        }
    }
}

/**
 * A circular dot representing a selectable color, with a selection indicator.
 */
@Composable
private fun ColorDot(
    colorOption: ColorOption,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val colorValue = remember(colorOption.hex) {
        try { Color(colorOption.hex.toColorInt()) } catch (_: Exception) { Color.Gray }
    }

    // Determine if the checkmark should be dark or light for contrast
    val needsDarkCheck = colorValue.luminance() > 0.5f
    val checkColor = if (needsDarkCheck) Color.Black else Color.White
    val selectionBgColor = if (needsDarkCheck) Color.Black.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.3f)

    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(colorValue)
            .border(
                width = if (isSelected) 2.5.dp else 0.dp,
                color = MaterialTheme.colorScheme.primary,
                shape = CircleShape
            )
            .clickable(onClick = onClick, role = Role.Button)
            .semantics {
                contentDescription = "${colorOption.name}${if (isSelected) ", selected" else ""}"
                role = Role.Button
            },
        contentAlignment = Alignment.Center
    ) {
        AnimatedVisibility(visible = isSelected) {
            Box(
                modifier = Modifier
                    .size(26.dp)
                    .clip(CircleShape)
                    .background(selectionBgColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Check,
                    contentDescription = null, // Content description is on the parent
                    tint = checkColor,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}