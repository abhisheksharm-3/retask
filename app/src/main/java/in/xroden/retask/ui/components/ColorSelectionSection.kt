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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt

/**
 * Color selector with expandable color palette
 */
@Composable
fun ColorSelector(
    selectedColor: String,
    onColorSelected: (String) -> Unit,
    isExpanded: Boolean,
    onToggleExpanded: () -> Unit
) {
    // Color options (memoized to prevent recreations)
    val colorOptions = remember {
        listOf(
            "#FFD6D6", "#FFE2B8", "#FFF9B8", "#D6FFB8",
            "#B8FFD6", "#B8FFEC", "#B8F1FF", "#B8D6FF",
            "#D6B8FF", "#F1B8FF", "#FFB8EC", "#FFB8D6"
        )
    }

    // Rotate animation for dropdown icon
    val rotationState by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        animationSpec = tween(300),
        label = "rotation"
    )

    // Convert hex to Color object (memoized to prevent recreations)
    val colorObj = remember(selectedColor) { Color(selectedColor.toColorInt()) }

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
            // Header with color preview
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onToggleExpanded() }
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Icon and title
                SectionHeader(
                    icon = Icons.Rounded.ColorLens,
                    title = "Task Color"
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
                )

                Spacer(modifier = Modifier.width(8.dp))

                // Dropdown icon
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
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
 * Color grid for selecting task color
 */
@Composable
fun ColorGrid(
    colorOptions: List<String>,
    selectedColor: String,
    onColorSelected: (String) -> Unit
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
                key(color) {  // Add key for better performance with collections
                    ColorDot(
                        color = color,
                        isSelected = color == selectedColor,
                        onClick = { onColorSelected(color) }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Second row of colors
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            colorOptions.drop(6).forEach { color ->
                key(color) {  // Add key for better performance with collections
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

/**
 * Colored dot representing a selectable color
 */
@Composable
fun ColorDot(
    color: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    // Convert hex to Color (memoized to prevent recreations)
    val colorValue = remember(color) { Color(color.toColorInt()) }

    // Check color brightness for contrast (memoized to prevent recreations)
    val isDarkColor = remember(colorValue) {
        colorValue.red + colorValue.green + colorValue.blue < 2f
    }

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
                    tint = if (isDarkColor) Color.White else Color.Black,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}