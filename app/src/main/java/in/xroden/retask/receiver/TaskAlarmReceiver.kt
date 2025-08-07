package `in`.xroden.retask.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import `in`.xroden.retask.data.model.Task
import `in`.xroden.retask.utils.NotificationHelper

/**
 * A BroadcastReceiver that handles scheduled task alarms by displaying notifications.
 *
 * This receiver is triggered by the [android.app.AlarmManager] when a task is due.
 * It extracts task details from the incoming [Intent] and uses the [NotificationHelper]
 * to present a system notification to the user.
 */
class TaskAlarmReceiver : BroadcastReceiver() {

    /**
     * This method is called when the BroadcastReceiver receives an Intent broadcast.
     * It validates the intent, extracts task data, and triggers a notification.
     *
     * @param context The Context in which the receiver is running.
     * @param intent The Intent being received, which should contain the task data.
     */
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ACTION_SHOW_TASK_NOTIFICATION) {
            Log.w(TAG, "Received an intent with an unknown action: ${intent.action}")
            return
        }

        val taskId = intent.getLongExtra(EXTRA_TASK_ID, INVALID_ID)
        val taskTitle = intent.getStringExtra(EXTRA_TASK_TITLE) ?: "Task Reminder"
        val dueDate = intent.getLongExtra(EXTRA_DUE_DATE, INVALID_ID)

        // Validate that we have the essential data to show a notification.
        if (taskId == INVALID_ID || dueDate == INVALID_ID) {
            Log.e(TAG, "Received invalid task data. Cannot show notification.")
            return
        }

        try {
            // Note: We are creating a partial Task object here to pass to the
            // NotificationHelper. Ideally, the helper might be refactored to
            // accept raw primitives, but this works safely.
            val task = Task(id = taskId, title = taskTitle, dueDate = dueDate)
            val notificationId = taskId.toInt() // Use the task's unique ID for the notification.

            val notification = NotificationHelper.buildTaskNotification(
                context = context,
                task = task,
            )

            NotificationHelper.showNotification(
                context = context,
                builder = notification,
                notificationId = notificationId
            )

            Log.d(TAG, "Successfully showed notification for task ID: $taskId")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to show notification for task ID: $taskId", e)
        }
    }

    companion object {
        private const val TAG = "TaskAlarmReceiver"

        // Using a package-name-based action string is a best practice to ensure uniqueness.
        const val ACTION_SHOW_TASK_NOTIFICATION = "in.xroden.retask.action.SHOW_TASK_NOTIFICATION"

        const val EXTRA_TASK_ID = "in.xroden.retask.extra.TASK_ID"
        const val EXTRA_TASK_TITLE = "in.xroden.retask.extra.TASK_TITLE"
        const val EXTRA_DUE_DATE = "in.xroden.retask.extra.DUE_DATE"

        private const val INVALID_ID = -1L
    }
}