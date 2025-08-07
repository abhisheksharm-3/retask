package `in`.xroden.retask.service

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.util.Log
import `in`.xroden.retask.data.model.Task
import `in`.xroden.retask.receiver.TaskAlarmReceiver
import java.util.concurrent.TimeUnit

/**
 * A short-lived service to schedule or cancel a single task reminder using AlarmManager.
 *
 * This service is started on-demand, performs its action, and then immediately stops itself.
 * It provides a clean and reliable way to interact with the Android alarm system.
 */
class TaskReminderService : Service() {

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "Service received command with action: ${intent?.action}")
        intent?.let { processIntent(it) }
        // We want the service to stop itself, so it shouldn't be sticky.
        return START_NOT_STICKY
    }

    private fun processIntent(intent: Intent) {
        when (intent.action) {
            ACTION_SCHEDULE_TASK -> {
                val taskId = intent.getLongExtra(EXTRA_TASK_ID, INVALID_ID)
                val taskTitle = intent.getStringExtra(EXTRA_TASK_TITLE) ?: ""
                val dueDate = intent.getLongExtra(EXTRA_DUE_DATE, INVALID_ID)

                if (taskId != INVALID_ID && dueDate != INVALID_ID) {
                    scheduleTaskReminder(taskId, taskTitle, dueDate)
                } else {
                    Log.w(TAG, "Received invalid data for scheduling. ID: $taskId")
                }
            }
            ACTION_CANCEL_TASK -> {
                val taskId = intent.getLongExtra(EXTRA_TASK_ID, INVALID_ID)
                if (taskId != INVALID_ID) {
                    cancelTaskReminder(taskId)
                } else {
                    Log.w(TAG, "Received invalid ID for cancellation.")
                }
            }
            else -> Log.w(TAG, "Unknown action received: ${intent.action}")
        }
        // The service has completed its work and should now stop.
        stopSelf()
    }

    /**
     * Schedules a reminder for a task using AlarmManager.
     * The reminder is set to trigger 10 minutes before the task's due time.
     */
    private fun scheduleTaskReminder(taskId: Long, taskTitle: String, dueDate: Long) {
        val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
        val notificationTime = dueDate - TimeUnit.MINUTES.toMillis(NOTIFICATION_ADVANCE_MINUTES)

        // Don't schedule reminders for tasks that are already due or are due very soon.
        if (notificationTime <= System.currentTimeMillis()) {
            Log.d(TAG, "Not scheduling reminder for task $taskId - due time is in the past.")
            return
        }

        val pendingIntent = createAlarmPendingIntent(taskId, taskTitle, dueDate)

        // Use the appropriate method based on Android version and permissions.
        if (alarmManager.canScheduleExactAlarms()) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, notificationTime, pendingIntent)
            Log.i(TAG, "Exact alarm scheduled for task $taskId at ${java.util.Date(notificationTime)}")
        } else {
            // Fallback to an inexact alarm if permission is not granted.
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, notificationTime, pendingIntent)
            Log.w(TAG, "Inexact alarm scheduled for task $taskId. Missing SCHEDULE_EXACT_ALARM permission.")
        }
    }

    /**
     * Cancels a previously scheduled reminder for a task.
     */
    private fun cancelTaskReminder(taskId: Long) {
        val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
        // To cancel, we must create a PendingIntent that is identical to the one used for scheduling.
        val pendingIntent = createAlarmPendingIntent(taskId, "", 0L)
        alarmManager.cancel(pendingIntent)
        Log.i(TAG, "Canceled alarm for task: $taskId")
    }

    /**
     * Creates a PendingIntent for the TaskAlarmReceiver.
     * The intent must be consistent for both scheduling and canceling.
     */
    private fun createAlarmPendingIntent(taskId: Long, taskTitle: String, dueDate: Long): PendingIntent {
        val intent = Intent(this, TaskAlarmReceiver::class.java).apply {
            action = TaskAlarmReceiver.ACTION_SHOW_TASK_NOTIFICATION
            putExtra(TaskAlarmReceiver.EXTRA_TASK_ID, taskId)
            putExtra(TaskAlarmReceiver.EXTRA_TASK_TITLE, taskTitle)
            putExtra(TaskAlarmReceiver.EXTRA_DUE_DATE, dueDate)
        }

        return PendingIntent.getBroadcast(
            this,
            taskId.toInt(), // Use the task ID as a unique request code.
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    companion object {
        private const val TAG = "TaskReminderService"
        private const val INVALID_ID = -1L
        private const val NOTIFICATION_ADVANCE_MINUTES = 10L

        const val ACTION_SCHEDULE_TASK = "in.xroden.retask.action.SCHEDULE_TASK"
        const val ACTION_CANCEL_TASK = "in.xroden.retask.action.CANCEL_TASK"
        const val EXTRA_TASK_ID = "in.xroden.retask.extra.TASK_ID"
        const val EXTRA_TASK_TITLE = "in.xroden.retask.extra.TASK_TITLE"
        const val EXTRA_DUE_DATE = "in.xroden.retask.extra.DUE_DATE"

        /**
         * Starts this service to schedule a reminder for the given task.
         */
        fun scheduleTask(context: Context, task: Task) {
            val intent = Intent(context, TaskReminderService::class.java).apply {
                action = ACTION_SCHEDULE_TASK
                putExtra(EXTRA_TASK_ID, task.id)
                putExtra(EXTRA_TASK_TITLE, task.title)
                putExtra(EXTRA_DUE_DATE, task.dueDate)
            }
            context.startService(intent)
        }

        /**
         * Starts this service to cancel the reminder for the given task ID.
         */
        fun cancelTask(context: Context, taskId: Long) {
            val intent = Intent(context, TaskReminderService::class.java).apply {
                action = ACTION_CANCEL_TASK
                putExtra(EXTRA_TASK_ID, taskId)
            }
            context.startService(intent)
        }
    }
}