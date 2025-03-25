package `in`.xroden.retask.data.model
import androidx.compose.ui.graphics.Color
import androidx.core.graphics.toColorInt
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Represents a task in the application.
 * Stored in the database and used throughout the app for task management.
 */
@Entity(tableName = "tasks")
data class Task(
    /** Unique identifier for the task */
    @PrimaryKey val id: String,

    /** Title or name of the task */
    val title: String,

    /** Due date timestamp in milliseconds since epoch */
    val dueDate: Long,

    /** Color for visual representation in hex format (e.g., "#RRGGBB") */
    val colorHex: String = "#FFFFD6" // Default light yellow
) {
    /**
     * Generates a human-readable text describing when the task is due.
     * @return A string representation of the time until or since the due date
     */
    fun getDueText(): String {
        val now = System.currentTimeMillis()
        val diffMillis = dueDate - now  // Note this is dueDate - now, not now - dueDate

        return when {
            diffMillis > 0 -> {
                when {
                    diffMillis < 60 * 1000 -> "Due in less than a minute"
                    diffMillis < 60 * 60 * 1000 -> "Due in ${diffMillis / (60 * 1000)} minutes"
                    diffMillis < 24 * 60 * 60 * 1000 -> "Due in ${diffMillis / (60 * 60 * 1000)} hours"
                    else -> "Due in ${diffMillis / (24 * 60 * 60 * 1000)} days"
                }
            }
            diffMillis > -60 * 1000 -> "Due now"
            diffMillis > -60 * 60 * 1000 -> "Was Due ${-diffMillis / (60 * 1000)} minutes ago"
            diffMillis > -24 * 60 * 60 * 1000 -> "Was Due ${-diffMillis / (60 * 60 * 1000)} hours ago"
            else -> "Was Due ${-diffMillis / (24 * 60 * 60 * 1000)} days ago"
        }
    }

    /**
     * Converts the hex color string to a Compose Color object for UI rendering.
     * @return The Color object representing the task's color
     */
    fun getBackgroundColor(): Color {
        return Color(colorHex.toColorInt())
    }
}