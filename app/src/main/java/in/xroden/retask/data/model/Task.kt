package `in`.xroden.retask.data.model
import androidx.compose.ui.graphics.Color
import androidx.core.graphics.toColorInt

data class Task(
    val id: String,
    val title: String,
    val dueDate: Long, // Timestamp
    val colorHex: String = "#FFFFD6" // Default light yellow
) {
    fun getDueText(): String {
        val now = System.currentTimeMillis()
        val diffMinutes = (now - dueDate) / (1000 * 60)

        return when {
            diffMinutes < 0 -> "Due in ${-diffMinutes} minutes"
            diffMinutes == 0L -> "Due now"
            else -> "Due $diffMinutes minutes ago"
        }
    }

    fun getBackgroundColor(): Color {
        return Color(colorHex.toColorInt())
    }
}