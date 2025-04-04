package `in`.xroden.retask.utils

import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.max

/**
 * Utility functions for time-related operations
 */
object TimeUtils {
    /**
     * Formats time based on duration in minutes
     */
    fun formatTimeFromMinutes(minutes: Int): String {
        return when {
            minutes < 60 -> "$minutes minutes"
            minutes % 60 == 0 -> "${minutes / 60} hour${if (minutes > 60) "s" else ""}"
            else -> "${minutes / 60} hour${if (minutes > 60) "s" else ""} ${minutes % 60} minute${if (minutes % 60 > 1) "s" else ""}"
        }
    }

    /**
     * Calculate minutes between now and a future timestamp
     */
    fun calculateMinutesFromTimestamp(timestamp: Long): Int {
        val currentTime = System.currentTimeMillis()
        val diffInMillis = timestamp - currentTime
        return max(1, TimeUnit.MILLISECONDS.toMinutes(diffInMillis).toInt())
    }

    /**
     * Get future timestamp from current time plus minutes
     */
    fun getFutureTimestamp(minutesFromNow: Int): Long {
        return System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(minutesFromNow.toLong())
    }

    /**
     * Format timestamp as readable date and time
     */
    fun formatDateTime(timestamp: Long): String {
        val dateFormat = SimpleDateFormat("EEE, MMM d, yyyy", Locale.getDefault())
        val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
        val date = Date(timestamp)
        return "${dateFormat.format(date)} at ${timeFormat.format(date)}"
    }
}