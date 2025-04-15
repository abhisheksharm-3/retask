package `in`.xroden.retask.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import `in`.xroden.retask.data.model.Task
import `in`.xroden.retask.utils.NotificationHelper

/**
 * A BroadcastReceiver that handles task reminder alarms and displays notifications.
 *
 * This receiver is triggered when scheduled task alarms fire and is responsible for
 * creating and showing notifications to remind users about their tasks.
 *
 * The receiver extracts task data from the incoming intent and uses [NotificationHelper]
 * to create and display the appropriate notification.
 */
class TaskAlarmReceiver : BroadcastReceiver() {

    private val TAG = "TaskAlarmReceiver"

    /**
     * Triggered when the BroadcastReceiver receives an intent.
     *
     * This method processes the received intent, extracts task data,
     * and shows a notification if valid task data is present.
     *
     * @param context The application context.
     * @param intent The intent containing the broadcast action and task data.
     */
    override fun onReceive(context: Context, intent: Intent) {
        try {
            // Handle the task notification action
            if (intent.action == ACTION_SHOW_TASK_NOTIFICATION) {
                processTaskNotificationIntent(context, intent)
            } else {
                Log.d(TAG, "Received unknown action: ${intent.action}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error processing notification intent", e)
        }
    }

    /**
     * Processes an intent with the task notification action.
     *
     * @param context The application context.
     * @param intent The intent containing the task data.
     */
    private fun processTaskNotificationIntent(context: Context, intent: Intent) {
        val taskId = intent.getLongExtra(EXTRA_TASK_ID, INVALID_ID)
        val taskTitle = intent.getStringExtra(EXTRA_TASK_TITLE) ?: DEFAULT_TITLE
        val dueDate = intent.getLongExtra(EXTRA_DUE_DATE, INVALID_ID)

        if (isValidTaskData(taskId, dueDate)) {
            // Create a Task object from the intent data
            val task = createTask(taskId, taskTitle, dueDate)

            // Build and show the notification
            showTaskNotification(context, task)
            Log.d(TAG, "Successfully showed notification for task ID: $taskId")
        } else {
            Log.w(TAG, "Received invalid task data. ID: $taskId, dueDate: $dueDate")
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
     * @return A Task object with the specified parameters.
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
     * This method uses the [NotificationHelper] to create and show a notification
     * for the specified task.
     *
     * @param context The application context.
     * @param task The task data to display in the notification.
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
        /**
         * Action string for task notifications.
         * Used to identify intents that should trigger task reminders.
         */
        const val ACTION_SHOW_TASK_NOTIFICATION = "in.xroden.retask.action.SHOW_TASK_NOTIFICATION"

        /**
         * Key for the task ID extra in the intent.
         */
        const val EXTRA_TASK_ID = "in.xroden.retask.extra.TASK_ID"

        /**
         * Key for the task title extra in the intent.
         */
        const val EXTRA_TASK_TITLE = "in.xroden.retask.extra.TASK_TITLE"

        /**
         * Key for the due date extra in the intent.
         */
        const val EXTRA_DUE_DATE = "in.xroden.retask.extra.DUE_DATE"

        /**
         * Value indicating an invalid ID.
         */
        private const val INVALID_ID = -1L

        /**
         * Default title value when none is provided.
         */
        private const val DEFAULT_TITLE = ""
    }
}