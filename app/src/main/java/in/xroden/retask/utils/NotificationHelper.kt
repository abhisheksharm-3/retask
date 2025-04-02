package `in`.xroden.retask.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import `in`.xroden.retask.MainActivity
import `in`.xroden.retask.R
import `in`.xroden.retask.data.model.Task

object NotificationHelper {
    const val CHANNEL_ID_UPCOMING = "upcoming_tasks_channel"
    const val CHANNEL_ID_PERSISTENT = "persistent_tasks_channel"
    const val NOTIFICATION_ID_PERSISTENT = 1001

    // Create notification channels
    fun createNotificationChannels(context: Context) {
        // Channel for upcoming task reminders
        val upcomingChannel = NotificationChannel(
            CHANNEL_ID_UPCOMING,
            "Upcoming Tasks",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Notifications for tasks that are due soon"
            enableVibration(true)
        }

        // Channel for persistent ongoing notification
        val persistentChannel = NotificationChannel(
            CHANNEL_ID_PERSISTENT,
            "Active Task Tracking",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Persistent notification showing upcoming tasks"
            setShowBadge(false)
        }

        val notificationManager = context.getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(upcomingChannel)
        notificationManager.createNotificationChannel(persistentChannel)
    }

    // Build a notification for an upcoming task
    fun buildTaskNotification(
        context: Context,
        task: Task,
        notificationId: Int
    ): NotificationCompat.Builder {
        // Create pending intent for when notification is tapped
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("TASK_ID", task.id)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            task.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Build the notification
        return NotificationCompat.Builder(context, CHANNEL_ID_UPCOMING)
            .setSmallIcon(R.drawable.ic_notification_task)
            .setContentTitle(task.title)
            .setContentText(task.getDueText())
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
    }

    // Build persistent notification showing upcoming tasks
    fun buildPersistentNotification(
        context: Context,
        upcomingTasks: List<Task>
    ): NotificationCompat.Builder {
        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID_PERSISTENT)
            .setSmallIcon(R.drawable.ic_notification_ongoing)
            .setContentTitle("Upcoming Tasks")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setCategory(NotificationCompat.CATEGORY_STATUS)

        // Add info about upcoming tasks
        if (upcomingTasks.isEmpty()) {
            builder.setContentText("No upcoming tasks")
        } else {
            val notificationStyle = NotificationCompat.InboxStyle()

            upcomingTasks.take(3).forEach { task ->
                notificationStyle.addLine("${task.title} - ${task.getDueText()}")
            }

            if (upcomingTasks.size > 3) {
                notificationStyle.setSummaryText("+ ${upcomingTasks.size - 3} more tasks")
            }

            builder.setStyle(notificationStyle)
            builder.setContentText("${upcomingTasks.size} tasks coming up")
        }

        return builder
    }

    // Show a notification
    fun showNotification(context: Context, builder: NotificationCompat.Builder, notificationId: Int) {
        val notificationManager = NotificationManagerCompat.from(context)

        try {
            notificationManager.notify(notificationId, builder.build())
        } catch (e: SecurityException) {
            // Handle permission not granted
            // You should request notification permission at app start
        }
    }
}