package `in`.xroden.retask.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import `in`.xroden.retask.data.model.Task
import `in`.xroden.retask.utils.NotificationHelper

/**
 * A BroadcastReceiver that handles task reminder alarms and displays notifications.
 */
class TaskAlarmReceiver : BroadcastReceiver() {

    /**
     * Triggered when the BroadcastReceiver receives an intent.
     *
     * @param context The application context.
     * @param intent The intent containing the broadcast action and data.
     */
    override fun onReceive(context: Context, intent: Intent) {
        // Handle the task notification action
        if (intent.action == ACTION_SHOW_TASK_NOTIFICATION) {
            val taskId = intent.getLongExtra(EXTRA_TASK_ID, INVALID_ID)
            val taskTitle = intent.getStringExtra(EXTRA_TASK_TITLE) ?: DEFAULT_TITLE
            val dueDate = intent.getLongExtra(EXTRA_DUE_DATE, INVALID_ID)

            if (isValidTaskData(taskId, dueDate)) {
                // Create a Task object from the intent data
                val task = createTask(taskId, taskTitle, dueDate)

                // Build and show the notification
                showTaskNotification(context, task)
            }
        }
    }

    /**
     * Validates task data by checking if the IDs and dates are valid.
     *
     * @param taskId The ID of the task.
     * @param dueDate The due date of the task.
     * @return True if the data is valid, false otherwise.
     */
    private fun isValidTaskData(taskId: Long, dueDate: Long): Boolean {
        return taskId != INVALID_ID && dueDate != INVALID_ID
    }

    /**
     * Creates a Task object from the provided data.
     *
     * @param taskId The ID of the task.
     * @param taskTitle The title of the task.
     * @param dueDate The due date of the task.
     * @return A Task object.
     */
    private fun createTask(taskId: Long, taskTitle: String, dueDate: Long): Task {
        return Task(
            id = taskId.toString(),
            title = taskTitle,
            dueDate = dueDate
        )
    }

    /**
     * Builds and displays the task notification.
     *
     * @param context The application context.
     * @param task The task data.
     */
    private fun showTaskNotification(context: Context, task: Task) {
        val notificationId = task.id.hashCode()
        val notification = NotificationHelper.buildTaskNotification(
            context = context,
            task = task,
            notificationId = notificationId
        )
        NotificationHelper.showNotification(
            context = context,
            builder = notification,
            notificationId = notificationId
        )
    }

    companion object {
        // Action string for task notifications
        const val ACTION_SHOW_TASK_NOTIFICATION = "in.xroden.retask.action.SHOW_TASK_NOTIFICATION"

        // Keys for intent extras
        const val EXTRA_TASK_ID = "in.xroden.retask.extra.TASK_ID"
        const val EXTRA_TASK_TITLE = "in.xroden.retask.extra.TASK_TITLE"
        const val EXTRA_DUE_DATE = "in.xroden.retask.extra.DUE_DATE"

        // Default or invalid values
        private const val INVALID_ID = -1L
        private const val DEFAULT_TITLE = ""
    }
}