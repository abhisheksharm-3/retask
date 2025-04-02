package `in`.xroden.retask.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import `in`.xroden.retask.data.model.Task
import `in`.xroden.retask.utils.NotificationHelper

class TaskAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == ACTION_SHOW_TASK_NOTIFICATION) {
            val taskId = intent.getLongExtra(EXTRA_TASK_ID, -1)
            val taskTitle = intent.getStringExtra(EXTRA_TASK_TITLE) ?: ""
            val dueDate = intent.getLongExtra(EXTRA_DUE_DATE, -1)

            if (taskId != -1L && dueDate != -1L) {
                // Create a task object from intent data
                val task = Task(
                    id = taskId.toString(),
                    title = taskTitle,
                    dueDate = dueDate,
                )

                // Build and show notification
                val notification = NotificationHelper.buildTaskNotification(
                    context = context,
                    task = task,
                    notificationId = taskId.hashCode()
                )

                NotificationHelper.showNotification(
                    context = context,
                    builder = notification,
                    notificationId = taskId.hashCode()
                )
            }
        }
    }

    companion object {
        const val ACTION_SHOW_TASK_NOTIFICATION = "in.xroden.retask.action.SHOW_TASK_NOTIFICATION"
        const val EXTRA_TASK_ID = "in.xroden.retask.extra.TASK_ID"
        const val EXTRA_TASK_TITLE = "in.xroden.retask.extra.TASK_TITLE"
        const val EXTRA_DUE_DATE = "in.xroden.retask.extra.DUE_DATE"
    }
}