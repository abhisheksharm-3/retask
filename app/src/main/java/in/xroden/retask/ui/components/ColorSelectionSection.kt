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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.ColorLens
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
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
 * for task categorization. The component features an expandable grid of color options
 * with smooth animations and visual feedback for selections.
 *
 * @param selectedColor The currently selected color in hex format (e.g., "#FFD6D6")
 * @param onColorSelected Callback function invoked when a new color is selected
 * @param isExpanded Boolean indicating if the color palette is expanded
 * @param onToggleExpanded Callback function to toggle the expanded state
 * @param modifier Optional modifier for customizing the component layout
 * @param title Optional custom title for the color selector (defaults to "Task Color")
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
    // Predefined color palette with semantic names for accessibility
    val colorOptions = remember {
        listOf(
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
    }

    // Find the selected color's name for accessibility
    val selectedColorName by remember(selectedColor, colorOptions) {
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
            Color.Gray // Fallback color in case of invalid hex
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 2.dp,
                shape = RoundedCornerShape(20.dp),
                spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
            ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp)
        ),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header with color preview and dropdown
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable(
                        onClick = onToggleExpanded,
                        role = Role.Button
                    )
                    .semantics {
                        contentDescription = "Color selector. Currently selected: $selectedColorName. ${if (isExpanded) "Tap to collapse" else "Tap to expand"}"
                        role = Role.Button
                    }
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Icon
                Icon(
                    imageVector = Icons.Rounded.ColorLens,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.width(12.dp))

                // Title
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp
                    )
                )

                Spacer(modifier = Modifier.weight(1f))

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
                        .semantics {
                            contentDescription = "Selected color: $selectedColorName"
                        }
                )

                Spacer(modifier = Modifier.width(12.dp))

                // Dropdown icon
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = if (isExpanded) "Collapse color palette" else "Expand color palette",
                    modifier = Modifier.rotate(rotationState)
                )
            }

            // Color grid with animation
            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn(animationSpec = tween(300)) + expandVertically(animationSpec = tween(300)),
                exit = fadeOut(animationSpec = tween(200)) + shrinkVertically(animationSpec = tween(200))
            ) {
                ColorGrid(
                    colorOptions = colorOptions,
                    selectedColor = selectedColor,
                    onColorSelected = onColorSelected
                )
            }
        }
    }
}

/**
 * Data class to hold color information with semantic name for accessibility
 */
private data class ColorOption(val hex: String, val name: String)

/**
 * A grid layout for displaying selectable color options.
 *
 * @param colorOptions List of color options to display
 * @param selectedColor Currently selected color hex value
 * @param onColorSelected Callback when a color is selected
 */
@Composable
private fun ColorGrid(
    colorOptions: List<ColorOption>,
    selectedColor: String,
    onColorSelected: (String) -> Unit
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
            .padding(16.dp)
    ) {
        // First row of colors (first half of the list)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            colorOptions.take(6).forEach { colorOption ->
                key(colorOption.hex) {
                    ColorDot(
                        colorOption = colorOption,
                        isSelected = colorOption.hex == selectedColor,
                        onClick = { onColorSelected(colorOption.hex) }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Second row of colors (second half of the list)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            colorOptions.drop(6).forEach { colorOption ->
                key(colorOption.hex) {
                    ColorDot(
                        colorOption = colorOption,
                        isSelected = colorOption.hex == selectedColor,
                        onClick = { onColorSelected(colorOption.hex) }
                    )
                }
            }
        }
    }
}

/**
 * A colored circular dot representing a selectable color option.
 *
 * @param colorOption The color option to display
 * @param isSelected Whether this color is currently selected
 * @param onClick Callback when this color is clicked
 */
@Composable
private fun ColorDot(
    colorOption: ColorOption,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    // Convert hex to Color
    val colorValue = remember(colorOption.hex) {
        try {
            Color(colorOption.hex.toColorInt())
        } catch (_: Exception) {
            Color.Gray // Fallback color
        }
    }

    // Check color luminance for contrast (more accurate than RGB sum)
    val needsDarkText = remember(colorValue) {
        colorValue.luminance() > 0.5f
    }

    // Selection indicator colors
    val selectionBgColor = remember(needsDarkText) {
        if (needsDarkText) Color.Black.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.3f)
    }

    val checkColor = remember(needsDarkText) {
        if (needsDarkText) Color.Black else Color.White
    }

    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .shadow(
                elevation = if (isSelected) 4.dp else 1.dp,
                shape = CircleShape,
                spotColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.Black.copy(alpha = 0.1f),
                ambientColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.Black.copy(alpha = 0.1f)
            )
            .background(colorValue)
            .border(
                width = if (isSelected) 2.5.dp else 0.dp,
                color = MaterialTheme.colorScheme.primary,
                shape = CircleShape
            )
            .clickable(
                onClick = onClick,
                role = Role.Button
            )
            .semantics {
                contentDescription = "${colorOption.name}${if (isSelected) ", selected" else ""}"
                role = Role.Button
            },
        contentAlignment = Alignment.Center
    ) {
        if (isSelected) {
            Box(
                modifier = Modifier
                    .size(26.dp)
                    .clip(CircleShape)
                    .background(selectionBgColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Check,
                    contentDescription = null,
                    tint = checkColor,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}