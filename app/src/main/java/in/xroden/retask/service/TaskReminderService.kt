package `in`.xroden.retask.service

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import `in`.xroden.retask.data.model.Task
import `in`.xroden.retask.receiver.TaskAlarmReceiver
import java.util.concurrent.TimeUnit

class TaskReminderService : Service() {
    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let { processIntent(it) }
        return START_NOT_STICKY
    }

    private fun processIntent(intent: Intent) {
        when (intent.action) {
            ACTION_SCHEDULE_TASK -> {
                val taskId = intent.getLongExtra(EXTRA_TASK_ID, -1)
                val taskTitle = intent.getStringExtra(EXTRA_TASK_TITLE) ?: ""
                val dueDate = intent.getLongExtra(EXTRA_DUE_DATE, -1)

                if (taskId != -1L && dueDate != -1L) {
                    scheduleTaskReminder(
                        taskId = taskId,
                        taskTitle = taskTitle,
                        dueDate = dueDate
                    )
                }
            }

            ACTION_CANCEL_TASK -> {
                val taskId = intent.getLongExtra(EXTRA_TASK_ID, -1)
                if (taskId != -1L) {
                    cancelTaskReminder(taskId)
                }
            }
        }

        stopSelf()
    }

    private fun scheduleTaskReminder(taskId: Long, taskTitle: String, dueDate: Long) {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // Schedule notification 10 minutes before due time
        val notificationTime = dueDate - TimeUnit.MINUTES.toMillis(10)
        val currentTime = System.currentTimeMillis()

        // Don't schedule if already past or less than 1 minute away
        if (notificationTime <= currentTime + TimeUnit.MINUTES.toMillis(1)) {
            return
        }

        val intent = Intent(this, TaskAlarmReceiver::class.java).apply {
            action = TaskAlarmReceiver.ACTION_SHOW_TASK_NOTIFICATION
            putExtra(TaskAlarmReceiver.EXTRA_TASK_ID, taskId)
            putExtra(TaskAlarmReceiver.EXTRA_TASK_TITLE, taskTitle)
            putExtra(TaskAlarmReceiver.EXTRA_DUE_DATE, dueDate)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            this,
            taskId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

// Check if we can schedule exact alarms
if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
    if (alarmManager.canScheduleExactAlarms()) {
        try {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                notificationTime,
                pendingIntent
            )
        } catch (se: SecurityException) {
            // Fallback to inexact alarm
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                notificationTime,
                pendingIntent
            )
        }
    } else {
        // Use inexact alarm if permission not granted
        alarmManager.setAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            notificationTime,
            pendingIntent
        )
    }
} else {
    // Below Android 12, we can set exact alarms without special permission
    alarmManager.setExactAndAllowWhileIdle(
        AlarmManager.RTC_WAKEUP,
        notificationTime,
        pendingIntent
    )
}
    }

    private fun cancelTaskReminder(taskId: Long) {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(this, TaskAlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            this,
            taskId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.cancel(pendingIntent)
    }

    companion object {
        const val ACTION_SCHEDULE_TASK = "in.xroden.retask.action.SCHEDULE_TASK"
        const val ACTION_CANCEL_TASK = "in.xroden.retask.action.CANCEL_TASK"

        const val EXTRA_TASK_ID = "in.xroden.retask.extra.TASK_ID"
        const val EXTRA_TASK_TITLE = "in.xroden.retask.extra.TASK_TITLE"
        const val EXTRA_DUE_DATE = "in.xroden.retask.extra.DUE_DATE"

        fun scheduleTask(context: Context, task: Task?) {
            val intent = Intent(context, TaskReminderService::class.java).apply {
                action = ACTION_SCHEDULE_TASK
                putExtra(EXTRA_TASK_ID, task?.id)
                putExtra(EXTRA_TASK_TITLE, task?.title)
                putExtra(EXTRA_DUE_DATE, task?.dueDate )
            }

            context.startService(intent)
        }

        fun cancelTask(context: Context, taskId: String) {
            val intent = Intent(context, TaskReminderService::class.java).apply {
                action = ACTION_CANCEL_TASK
                putExtra(EXTRA_TASK_ID, taskId)
            }

            context.startService(intent)
        }
    }
}