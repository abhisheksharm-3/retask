package `in`.xroden.retask.utils

import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.max

/**
 * A utility object for performing common time-related operations, such as
 * formatting durations, calculating timestamps, and converting between time units.
 */
object TimeUtils {

    private const val ONE_MINUTE_IN_MILLIS = 60000L

    /**
     * Formats a duration in minutes into a human-readable string.
     *
     * @param minutes The duration in minutes.
     * @return A formatted string representing the duration in hours and minutes.
     */
    fun formatTimeFromMinutes(minutes: Int): String {
        return when {
            minutes < 60 -> "$minutes minute${if (minutes != 1) "s" else ""}"
            minutes % 60 == 0 -> "${minutes / 60} hour${if (minutes / 60 > 1) "s" else ""}"
            else -> "${minutes / 60} hour${if (minutes / 60 > 1) "s" else ""} ${minutes % 60} minute${if (minutes % 60 != 1) "s" else ""}"
        }
    }

    /**
     * Calculates the number of minutes remaining between the current time and a future timestamp.
     *
     * @param timestamp A future timestamp in milliseconds.
     * @return The number of minutes remaining, with a minimum of 1 minute.
     */
    fun calculateMinutesFromTimestamp(timestamp: Long): Int {
        val currentTime = System.currentTimeMillis()
        val diffInMillis = timestamp - currentTime
        return max(1, TimeUnit.MILLISECONDS.toMinutes(diffInMillis).toInt())
    }

    /**
     * Calculates a future timestamp by adding a specified number of minutes to the current time.
     *
     * @param minutesFromNow The number of minutes to add to the current time.
     * @return A future timestamp in milliseconds.
     */
    fun getFutureTimestamp(minutesFromNow: Int): Long {
        return System.currentTimeMillis() + (minutesFromNow * ONE_MINUTE_IN_MILLIS)
    }

    /**
     * Formats a timestamp into a human-readable date and time string.
     *
     * @param timestamp The timestamp in milliseconds to format.
     * @return A string formatted as "Day, Month Date, Year at Hour:Minute AM/PM".
     */
    fun formatDateTime(timestamp: Long): String {
        val dateFormat = SimpleDateFormat("EEE, MMM d, yyyy", Locale.getDefault())
        val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
        val date = Date(timestamp)
        return "${dateFormat.format(date)} at ${timeFormat.format(date)}"
    }
}