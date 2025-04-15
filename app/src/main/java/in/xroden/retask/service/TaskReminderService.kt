package `in`.xroden.retask.service

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import `in`.xroden.retask.data.model.Task
import `in`.xroden.retask.receiver.TaskAlarmReceiver
import java.util.concurrent.TimeUnit

/**
 * Service responsible for scheduling and canceling task reminders using the AlarmManager.
 *
 * This service handles two primary operations:
 * 1. Scheduling alarms for task reminders based on their due dates
 * 2. Canceling previously scheduled alarms when tasks are updated or deleted
 *
 * The service works with [TaskAlarmReceiver] to deliver notifications at the appropriate times.
 * For each task, it schedules an alarm to trigger 10 minutes before the task's due time.
 */
class TaskReminderService : Service() {

    private val TAG = "TaskReminderService"

    /**
     * Not used as this is not a bound service.
     *
     * @return Always returns null as binding is not supported.
     */
    override fun onBind(intent: Intent?): IBinder? = null

    /**
     * Handles commands sent to the service.
     *
     * The service processes the intent based on its action (scheduling or canceling)
     * and stops itself once the operation is complete.
     *
     * @param intent The intent that contains the action and task data.
     * @param flags Additional data about this start request.
     * @param startId A unique integer representing this specific request to start.
     * @return [START_NOT_STICKY] since we don't want the service to restart if killed.
     */
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "Service started with action: ${intent?.action}")
        intent?.let { processIntent(it) }
        return START_NOT_STICKY
    }

    /**
     * Processes the intent received by the service.
     *
     * Extracts the relevant task data and performs the requested operation
     * (scheduling or canceling a reminder).
     *
     * @param intent The intent containing the action and task data.
     */
    private fun processIntent(intent: Intent) {
        when (intent.action) {
            ACTION_SCHEDULE_TASK -> {
                val taskId = intent.getLongExtra(EXTRA_TASK_ID, INVALID_ID)
                val taskTitle = intent.getStringExtra(EXTRA_TASK_TITLE) ?: ""
                val dueDate = intent.getLongExtra(EXTRA_DUE_DATE, INVALID_ID)

                if (isValidTaskData(taskId, dueDate)) {
                    Log.d(TAG, "Scheduling reminder for task: $taskId, title: $taskTitle, due: $dueDate")
                    scheduleTaskReminder(
                        taskId = taskId,
                        taskTitle = taskTitle,
                        dueDate = dueDate
                    )
                } else {
                    Log.w(TAG, "Received invalid task data for scheduling. ID: $taskId, dueDate: $dueDate")
                }
            }

            ACTION_CANCEL_TASK -> {
                val taskId = intent.getLongExtra(EXTRA_TASK_ID, INVALID_ID)
                if (taskId != INVALID_ID) {
                    Log.d(TAG, "Canceling reminder for task: $taskId")
                    cancelTaskReminder(taskId)
                } else {
                    Log.w(TAG, "Received invalid task ID for cancellation: $taskId")
                }
            }

            else -> {
                Log.w(TAG, "Unknown action received: ${intent.action}")
            }
        }

        stopSelf()
    }

    /**
     * Checks if the task data is valid for scheduling.
     *
     * @param taskId The ID of the task.
     * @param dueDate The due date of the task.
     * @return True if the data is valid, false otherwise.
     */
    private fun isValidTaskData(taskId: Long, dueDate: Long): Boolean {
        return taskId != INVALID_ID && dueDate != INVALID_ID
    }

    /**
     * Schedules a reminder for a task using AlarmManager.
     *
     * The reminder is set to trigger 10 minutes before the task's due time.
     * On Android 12 and above, it checks if the app has permission to schedule exact alarms
     * and falls back to inexact alarms if necessary.
     *
     * @param taskId The ID of the task.
     * @param taskTitle The title of the task.
     * @param dueDate The due date of the task.
     */
    private fun scheduleTaskReminder(taskId: Long, taskTitle: String, dueDate: Long) {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // Schedule notification 10 minutes before due time
        val notificationTime = dueDate - TimeUnit.MINUTES.toMillis(NOTIFICATION_ADVANCE_MINUTES)
        val currentTime = System.currentTimeMillis()

        // Don't schedule if already past or less than 1 minute away
        if (notificationTime <= currentTime + TimeUnit.MINUTES.toMillis(MINIMUM_SCHEDULE_MINUTES)) {
            Log.d(TAG, "Not scheduling reminder - due time too soon or in the past")
            return
        }

        // Create the intent for the alarm broadcast
        val intent = createTaskAlarmIntent(taskId, taskTitle, dueDate)

        // Create the pending intent
        val pendingIntent = PendingIntent.getBroadcast(
            this,
            taskId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Schedule the alarm based on Android version and permissions
        scheduleAlarmWithAppropriateMethod(alarmManager, notificationTime, pendingIntent)

        Log.d(TAG, "Reminder scheduled for ${formatTime(notificationTime)}")
    }

    /**
     * Creates an intent for the TaskAlarmReceiver with task details.
     *
     * @param taskId The ID of the task.
     * @param taskTitle The title of the task.
     * @param dueDate The due date of the task.
     * @return The configured intent.
     */
    private fun createTaskAlarmIntent(taskId: Long, taskTitle: String, dueDate: Long): Intent {
        return Intent(this, TaskAlarmReceiver::class.java).apply {
            action = TaskAlarmReceiver.ACTION_SHOW_TASK_NOTIFICATION
            putExtra(TaskAlarmReceiver.EXTRA_TASK_ID, taskId)
            putExtra(TaskAlarmReceiver.EXTRA_TASK_TITLE, taskTitle)
            putExtra(TaskAlarmReceiver.EXTRA_DUE_DATE, dueDate)
        }
    }

    /**
     * Schedules the alarm using the appropriate method based on the Android version and permissions.
     *
     * @param alarmManager The AlarmManager service.
     * @param triggerTime The time when the alarm should trigger.
     * @param pendingIntent The PendingIntent to trigger when the alarm fires.
     */
    private fun scheduleAlarmWithAppropriateMethod(
        alarmManager: AlarmManager,
        triggerTime: Long,
        pendingIntent: PendingIntent
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // Android 12 (API 31) and above require permission for exact alarms
            if (alarmManager.canScheduleExactAlarms()) {
                try {
                    Log.d(TAG, "Setting exact alarm (Android 12+)")
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerTime,
                        pendingIntent
                    )
                } catch (se: SecurityException) {
                    // Fallback to inexact alarm if there's a security exception
                    Log.w(TAG, "Security exception when setting exact alarm, falling back to inexact alarm", se)
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerTime,
                        pendingIntent
                    )
                }
            } else {
                // Use inexact alarm if permission not granted
                Log.d(TAG, "Permission for exact alarms not granted, using inexact alarm")
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
            }
        } else {
            // Below Android 12, we can set exact alarms without special permission
            Log.d(TAG, "Setting exact alarm (Android 11 or below)")
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent
            )
        }
    }

    /**
     * Cancels a previously scheduled reminder for a task.
     *
     * @param taskId The ID of the task whose reminder should be canceled.
     */
    private fun cancelTaskReminder(taskId: Long) {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // Create a matching intent to identify the alarm to cancel
        val intent = Intent(this, TaskAlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            this,
            taskId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Cancel the alarm
        alarmManager.cancel(pendingIntent)
        Log.d(TAG, "Reminder canceled for task: $taskId")
    }

    /**
     * Formats a timestamp for logging purposes.
     *
     * @param timeMillis The time in milliseconds.
     * @return A formatted string representing the time.
     */
    private fun formatTime(timeMillis: Long): String {
        val minutes = TimeUnit.MILLISECONDS.toMinutes(timeMillis - System.currentTimeMillis())
        return "in $minutes minutes (${java.util.Date(timeMillis)})"
    }

    companion object {
        // Invalid ID constant
        private const val INVALID_ID = -1L

        // Timing constants
        private const val NOTIFICATION_ADVANCE_MINUTES = 10L
        private const val MINIMUM_SCHEDULE_MINUTES = 1L

        /**
         * Action string for scheduling a task reminder.
         */
        const val ACTION_SCHEDULE_TASK = "in.xroden.retask.action.SCHEDULE_TASK"

        /**
         * Action string for canceling a task reminder.
         */
        const val ACTION_CANCEL_TASK = "in.xroden.retask.action.CANCEL_TASK"

        /**
         * Extra key for the task ID.
         */
        const val EXTRA_TASK_ID = "in.xroden.retask.extra.TASK_ID"

        /**
         * Extra key for the task title.
         */
        const val EXTRA_TASK_TITLE = "in.xroden.retask.extra.TASK_TITLE"

        /**
         * Extra key for the due date.
         */
        const val EXTRA_DUE_DATE = "in.xroden.retask.extra.DUE_DATE"

        /**
         * Convenience method to schedule a reminder for a task.
         *
         * @param context The context used to start the service.
         * @param task The task to schedule a reminder for.
         */
        fun scheduleTask(context: Context, task: Task?) {
            if (task == null) {
                Log.w("TaskReminderService", "Attempted to schedule null task")
                return
            }

            try {
                // Parse the string ID to long
                val taskIdLong = task.id.toLongOrNull() ?: INVALID_ID

                val intent = Intent(context, TaskReminderService::class.java).apply {
                    action = ACTION_SCHEDULE_TASK
                    putExtra(EXTRA_TASK_ID, taskIdLong)
                    putExtra(EXTRA_TASK_TITLE, task.title)
                    putExtra(EXTRA_DUE_DATE, task.dueDate)
                }

                context.startService(intent)
                Log.d("TaskReminderService", "Scheduled task: ${task.id} - ${task.title}")
            } catch (e: Exception) {
                Log.e("TaskReminderService", "Error scheduling task: ${task.id}", e)
            }
        }

        /**
         * Convenience method to cancel a reminder for a task.
         *
         * @param context The context used to start the service.
         * @param taskId The ID of the task whose reminder should be canceled.
         */
        fun cancelTask(context: Context, taskId: String) {
            try {
                // Parse the string ID to long
                val taskIdLong = taskId.toLongOrNull() ?: INVALID_ID

                val intent = Intent(context, TaskReminderService::class.java).apply {
                    action = ACTION_CANCEL_TASK
                    putExtra(EXTRA_TASK_ID, taskIdLong)
                }

                context.startService(intent)
                Log.d("TaskReminderService", "Canceled task: $taskId")
            } catch (e: Exception) {
                Log.e("TaskReminderService", "Error canceling task: $taskId", e)
            }
        }
    }
}