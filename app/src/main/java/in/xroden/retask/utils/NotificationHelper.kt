package `in`.xroden.retask.utils

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import `in`.xroden.retask.MainActivity
import `in`.xroden.retask.R
import `in`.xroden.retask.data.model.Task

/**
 * Utility object for creating, displaying, and managing all notifications for the app.
 */
object NotificationHelper {

    // Notification channel IDs
    const val CHANNEL_ID_UPCOMING = "upcoming_tasks_channel"
    const val CHANNEL_ID_PERSISTENT = "persistent_tasks_channel"

    // Notification IDs
    const val NOTIFICATION_ID_PERSISTENT = 1001
    private const val TAG = "NotificationHelper"

    /**
     * Creates the necessary notification channels for the application.
     * This should be called once when the application starts.
     */
    fun createNotificationChannels(context: Context) {
        val notificationManager = context.getSystemService(NotificationManager::class.java)

        val upcomingChannel = NotificationChannel(
            CHANNEL_ID_UPCOMING,
            "Upcoming Task Reminders",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Notifications for tasks that are due soon."
            enableVibration(true)
        }

        val persistentChannel = NotificationChannel(
            CHANNEL_ID_PERSISTENT,
            "Active Task Status",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "A persistent notification showing the upcoming task summary."
            setShowBadge(false)
        }

        notificationManager.createNotificationChannel(upcomingChannel)
        notificationManager.createNotificationChannel(persistentChannel)
    }

    /**
     * Builds a notification for an individual task reminder.
     */
    fun buildTaskNotification(context: Context, task: Task): NotificationCompat.Builder {
        val pendingIntent = createTaskPendingIntent(context, task)
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
     * Builds the persistent notification that displays a summary of upcoming tasks.
     */
    fun buildPersistentNotification(context: Context, upcomingTasks: List<Task>): NotificationCompat.Builder {
        val pendingIntent = createMainActivityPendingIntent(context)
        val builder = NotificationCompat.Builder(context, CHANNEL_ID_PERSISTENT)
            .setSmallIcon(R.drawable.ic_notification_ongoing)
            .setContentTitle("Upcoming Tasks")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

        if (upcomingTasks.isEmpty()) {
            builder.setContentText("No upcoming tasks for today.")
        } else {
            builder.setStyle(createInboxStyle(upcomingTasks))
            builder.setContentText("${upcomingTasks.size} tasks coming up")
        }
        return builder
    }

    /**
     * Displays a notification after checking for the required permission on Android 13+.
     */
    fun showNotification(context: Context, builder: NotificationCompat.Builder, notificationId: Int) {
        // On Android 13+, we must check for the POST_NOTIFICATIONS permission at runtime.
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            // If it's not granted, we cannot show the notification.
            Log.w(TAG, "Cannot show notification $notificationId: POST_NOTIFICATIONS permission not granted.")
            return
        }
        NotificationManagerCompat.from(context).notify(notificationId, builder.build())
    }

    private fun createTaskPendingIntent(context: Context, task: Task): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            // Use the safe constant instead of a "magic string"
            putExtra(MainActivity.EXTRA_TASK_ID, task.id)
        }
        return PendingIntent.getActivity(
            context,
            task.id.toInt(), // Use .toInt() for a more direct and reliable request code
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun createMainActivityPendingIntent(context: Context): PendingIntent {
        val intent = Intent(context, MainActivity::class.java)
        return PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)
    }

    private fun createInboxStyle(tasks: List<Task>): NotificationCompat.InboxStyle {
        val inboxStyle = NotificationCompat.InboxStyle()
        tasks.take(5).forEach { task -> // Show up to 5 tasks for more detail
            inboxStyle.addLine("${task.title} - ${task.getDueText()}")
        }
        if (tasks.size > 5) {
            inboxStyle.setSummaryText("+ ${tasks.size - 5} more")
        }
        return inboxStyle
    }
}