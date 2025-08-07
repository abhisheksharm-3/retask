package `in`.xroden.retask.service

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import `in`.xroden.retask.data.database.TaskDatabase
import `in`.xroden.retask.data.model.Task
import `in`.xroden.retask.data.repository.TaskRepository
import `in`.xroden.retask.receiver.TaskAlarmReceiver
import `in`.xroden.retask.utils.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

/**
 * A foreground service that manages task notifications by observing upcoming tasks
 * and scheduling exact alarms using [AlarmManager].
 *
 * This service's responsibilities are:
 * 1. To run persistently and show an ongoing notification summarizing upcoming tasks.
 * 2. To monitor tasks in the database that are due within the scheduling window.
 * 3. To tell the Android [AlarmManager] to fire a notification at a precise time
 * for each upcoming task.
 */
class TaskNotificationService : Service() {

    // A job that supervises all coroutines started by this service
    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.IO + serviceJob)

    private lateinit var taskRepository: TaskRepository
    private lateinit var alarmManager: AlarmManager

    // A set to keep track of task IDs for which an alarm has already been scheduled.
    private val scheduledAlarms = mutableSetOf<Long>()

    override fun onCreate() {
        super.onCreate()
        taskRepository = TaskRepository(TaskDatabase.getDatabase(this).taskDao())
        alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager

        NotificationHelper.createNotificationChannels(this)
        Log.d(TAG, "Service created and initialized.")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "Service started.")

        // Start the service in the foreground.
        val initialNotification = NotificationHelper.buildPersistentNotification(this, emptyList())
        startForeground(NotificationHelper.NOTIFICATION_ID_PERSISTENT, initialNotification.build())

        // Start the main observation loop.
        observeAndScheduleTasks()

        return START_STICKY
    }

    /**
     * The main loop of the service. Observes upcoming tasks, updates the persistent
     * notification, and schedules alarms for tasks that need one.
     */
    private fun observeAndScheduleTasks() {
        serviceScope.launch {
            // Observe tasks that are not completed and are due in the future.
            taskRepository.allTasks
                .catch { e -> Log.e(TAG, "Error collecting task flow", e) }
                .collectLatest { tasks ->
                    val upcomingTasks = tasks.filter { !it.isCompleted }
                    Log.d(TAG, "Observing ${upcomingTasks.size} active tasks.")

                    updatePersistentNotification(upcomingTasks)
                    scheduleAlarmsForUpcomingTasks(upcomingTasks)
                }
        }
    }

    /**
     * Updates the persistent foreground notification with a summary of upcoming tasks.
     * @param tasks A list of all active tasks.
     */
    private fun updatePersistentNotification(tasks: List<Task>) {
        val summaryTasks = tasks.sortedBy { it.dueDate }.take(MAX_TASKS_IN_SUMMARY)
        val notification = NotificationHelper.buildPersistentNotification(this, summaryTasks)
        NotificationHelper.showNotification(
            context = this,
            builder = notification,
            notificationId = NotificationHelper.NOTIFICATION_ID_PERSISTENT
        )
    }

    /**
     * Iterates through upcoming tasks and schedules an alarm for each one if it
     * meets the criteria and doesn't already have an alarm set.
     * @param tasks The list of tasks to evaluate for scheduling.
     */
    private fun scheduleAlarmsForUpcomingTasks(tasks: List<Task>) {
        val currentTime = System.currentTimeMillis()
        tasks.forEach { task ->
            val timeUntilDue = task.dueDate - currentTime
            val shouldSchedule = timeUntilDue > 0 && timeUntilDue <= NOTIFICATION_WINDOW_MS

            if (shouldSchedule && task.id !in scheduledAlarms) {
                scheduleAlarmForTask(task)
            }
        }
    }

    /**
     * Schedules a single, exact alarm for a specific task using [AlarmManager].
     * On Android 12+, this first checks for the SCHEDULE_EXACT_ALARM permission.
     *
     * @param task The task for which to schedule an alarm.
     */
    private fun scheduleAlarmForTask(task: Task) {
        // Calculate when the notification should appear (e.g., 10 minutes before it's due).
        val notificationTime = task.dueDate - NOTIFICATION_EARLY_WARNING_MS

        // Only schedule alarms in the future.
        if (notificationTime < System.currentTimeMillis()) {
            return
        }

        val intent = Intent(this, TaskAlarmReceiver::class.java).apply {
            action = TaskAlarmReceiver.ACTION_SHOW_TASK_NOTIFICATION
            putExtra(TaskAlarmReceiver.EXTRA_TASK_ID, task.id)
            putExtra(TaskAlarmReceiver.EXTRA_TASK_TITLE, task.title)
            putExtra(TaskAlarmReceiver.EXTRA_DUE_DATE, task.dueDate)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            this,
            task.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // CRITICAL: Check if the app has permission to schedule exact alarms.
        if (alarmManager.canScheduleExactAlarms()) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                notificationTime,
                pendingIntent
            )

            scheduledAlarms.add(task.id)
            Log.i(TAG, "Successfully scheduled alarm for task ${task.id} at $notificationTime.")
        } else {
            // In a real app, you should handle this gracefully by informing the user
            // and providing a button that takes them to settings to grant the permission.
            // Example Intent: Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
            Log.w(TAG, "Cannot schedule exact alarm. Missing SCHEDULE_EXACT_ALARM permission.")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceJob.cancel() // Cancel all coroutines.
        Log.d(TAG, "Service destroyed.")
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        private const val TAG = "TaskNotificationService"

        private val NOTIFICATION_WINDOW_MS = TimeUnit.HOURS.toMillis(24)
        private val NOTIFICATION_EARLY_WARNING_MS = TimeUnit.MINUTES.toMillis(10)
        private const val MAX_TASKS_IN_SUMMARY = 5
    }
}