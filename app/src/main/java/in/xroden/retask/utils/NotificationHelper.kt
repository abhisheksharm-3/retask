package `in`.xroden.retask.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import `in`.xroden.retask.MainActivity
import `in`.xroden.retask.R
import `in`.xroden.retask.data.model.Task

/**
 * Utility object for managing and displaying notifications for tasks.
 */
object NotificationHelper {

    // Notification channel IDs
    const val CHANNEL_ID_UPCOMING = "upcoming_tasks_channel"
    const val CHANNEL_ID_PERSISTENT = "persistent_tasks_channel"

    // Notification IDs
    const val NOTIFICATION_ID_PERSISTENT = 1001

    /**
     * Creates notification channels for the application.
     *
     * @param context The application context.
     */
    fun createNotificationChannels(context: Context) {
        // Only create channels on devices running Android O (API 26) or higher
        val notificationManager = context.getSystemService(NotificationManager::class.java)

        // Channel for upcoming task reminders
        val upcomingChannel = NotificationChannel(
            CHANNEL_ID_UPCOMING,
            "Upcoming Tasks",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Notifications for tasks that are due soon"
            enableVibration(true)
        }

        // Channel for persistent notifications
        val persistentChannel = NotificationChannel(
            CHANNEL_ID_PERSISTENT,
            "Active Task Tracking",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Persistent notification showing upcoming tasks"
            setShowBadge(false)
        }

        // Create the channels
        notificationManager.createNotificationChannel(upcomingChannel)
        notificationManager.createNotificationChannel(persistentChannel)
    }

    /**
     * Builds a notification for an individual task that is due soon.
     *
     * @param context The application context.
     * @param task The task to display in the notification.
     * @param notificationId The unique identifier for the notification.
     * @return The built NotificationCompat.Builder object.
     */
    fun buildTaskNotification(
        context: Context,
        task: Task,
        notificationId: Int
    ): NotificationCompat.Builder {
        // Create pending intent for when notification is tapped
        val pendingIntent = createTaskPendingIntent(context, task)

        // Build and return the notification
        return NotificationCompat.Builder(context, CHANNEL_ID_UPCOMING)
            .setSmallIcon(R.drawable.ic_notification_task)
            .setContentTitle(task.title)
            .setContentText(task.getDueText())
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
    }

    /**
     * Builds a persistent notification that displays a list of upcoming tasks.
     *
     * @param context The application context.
     * @param upcomingTasks The list of upcoming tasks to display.
     * @return The built NotificationCompat.Builder object.
     */
    fun buildPersistentNotification(
        context: Context,
        upcomingTasks: List<Task>
    ): NotificationCompat.Builder {
        val pendingIntent = createMainActivityPendingIntent(context)

        val builder = NotificationCompat.Builder(context, CHANNEL_ID_PERSISTENT)
            .setSmallIcon(R.drawable.ic_notification_ongoing)
            .setContentTitle("Upcoming Tasks")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setCategory(NotificationCompat.CATEGORY_STATUS)

        // Add task details to the notification
        if (upcomingTasks.isEmpty()) {
            builder.setContentText("No upcoming tasks")
        } else {
            builder.setStyle(createInboxStyle(upcomingTasks))
            builder.setContentText("${upcomingTasks.size} tasks coming up")
        }

        return builder
    }

    /**
     * Displays a notification using the NotificationManagerCompat.
     *
     * @param context The application context.
     * @param builder The notification builder.
     * @param notificationId The unique identifier for the notification.
     */
    fun showNotification(context: Context, builder: NotificationCompat.Builder, notificationId: Int) {
        val notificationManager = NotificationManagerCompat.from(context)

        try {
            notificationManager.notify(notificationId, builder.build())
        } catch (e: SecurityException) {
            // Handle the case where notification permissions are not granted
            // Ensure that the app requests notification permissions at startup
        }
    }

    /**
     * Creates a pending intent for launching the MainActivity with a specific task.
     *
     * @param context The application context.
     * @param task The task to pass as extra data.
     * @return The PendingIntent object.
     */
    private fun createTaskPendingIntent(context: Context, task: Task): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("TASK_ID", task.id)
        }

        return PendingIntent.getActivity(
            context,
            task.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    /**
     * Creates a pending intent for launching the MainActivity.
     *
     * @param context The application context.
     * @return The PendingIntent object.
     */
    private fun createMainActivityPendingIntent(context: Context): PendingIntent {
        val intent = Intent(context, MainActivity::class.java)

        return PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )
    }

    /**
     * Creates an InboxStyle notification for displaying a list of upcoming tasks.
     *
     * @param tasks The list of tasks to display.
     * @return The NotificationCompat.InboxStyle object.
     */
    private fun createInboxStyle(tasks: List<Task>): NotificationCompat.InboxStyle {
        val inboxStyle = NotificationCompat.InboxStyle()

        // Add up to 3 tasks to the style
        tasks.take(3).forEach { task ->
            inboxStyle.addLine("${task.title} - ${task.getDueText()}")
        }

        // Add a summary if there are more than 3 tasks
        if (tasks.size > 3) {
            inboxStyle.setSummaryText("+ ${tasks.size - 3} more tasks")
        }

        return inboxStyle
    }
}