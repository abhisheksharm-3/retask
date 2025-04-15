package `in`.xroden.retask.service

import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.os.PowerManager
import android.util.Log
import androidx.core.content.getSystemService
import `in`.xroden.retask.data.database.TaskDatabase
import `in`.xroden.retask.data.model.Task
import `in`.xroden.retask.data.repository.TaskRepository
import `in`.xroden.retask.utils.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

/**
 * A foreground service that manages task notifications by observing upcoming tasks
 * and scheduling notifications for tasks that are due soon.
 *
 * This service performs several key functions:
 * 1. Maintains a persistent notification showing upcoming tasks
 * 2. Monitors tasks that are due within the next 24 hours
 * 3. Schedules timely notifications for tasks approaching their due date
 * 4. Periodically refreshes task data to ensure notifications remain accurate
 *
 * The service runs continuously in the background and restarts automatically if terminated.
 */
class TaskNotificationService : Service() {

    private val TAG = "TaskNotificationService"

    // Coroutine job and scope for managing service tasks
    private val serviceJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.IO + serviceJob)

    // Repository for accessing task data
    private lateinit var taskRepository: TaskRepository

    // Job for observing upcoming tasks
    private var upcomingTasksJob: Job? = null

    // Wake lock to prevent the service from being killed during critical operations
    private var wakeLock: PowerManager.WakeLock? = null

    // Notification-related constants
    companion object {
        // Notification timing constants (in milliseconds)
        private val NOTIFICATION_WINDOW = TimeUnit.HOURS.toMillis(24) // Look ahead 24 hours
        private val NOTIFICATION_REFRESH_INTERVAL = TimeUnit.MINUTES.toMillis(15) // Refresh every 15 minutes
        private val NOTIFICATION_ADVANCE_WARNING = TimeUnit.MINUTES.toMillis(30) // Notify 30 mins before due
        private val NOTIFICATION_EARLY_WARNING = TimeUnit.MINUTES.toMillis(10) // Early warning 10 mins before

        // Important notification states
        private const val MAX_TASKS_IN_SUMMARY = 5 // Maximum number of tasks to show in persistent notification
    }

    /**
     * Called when the service is created. Initializes the notification system,
     * task repository, and starts observing tasks.
     */
    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Service created")

        try {
            // Setup notification channels
            NotificationHelper.createNotificationChannels(this)

            // Initialize the task repository using the database
            taskRepository = initializeTaskRepository()

            // Start the service in the foreground with an initial persistent notification
            startForegroundWithInitialNotification()

            // Observe upcoming tasks in real-time
            observeUpcomingTasks()

            // Start a periodic task to refresh data and notifications
            startPeriodicCheck()

            // Acquire wake lock for critical operations
            acquireWakeLock()
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing service", e)
            stopSelf()
        }
    }

    /**
     * Initializes the TaskRepository using the application's database.
     *
     * @return Initialized TaskRepository instance
     */
    private fun initializeTaskRepository(): TaskRepository {
        val taskDao = TaskDatabase.getDatabase(applicationContext).taskDao()
        return TaskRepository(taskDao)
    }

    /**
     * Starts the service in the foreground with an initial persistent notification.
     * This ensures the service keeps running and isn't killed by the system.
     */
    private fun startForegroundWithInitialNotification() {
        val initialNotification = NotificationHelper.buildPersistentNotification(
            context = this,
            upcomingTasks = emptyList()
        )

        startForeground(
            NotificationHelper.NOTIFICATION_ID_PERSISTENT,
            initialNotification.build()
        )
        Log.d(TAG, "Started foreground service with initial notification")
    }

    /**
     * Acquires a wake lock to prevent the service from being killed during critical operations.
     * The wake lock is released when the service is destroyed.
     */
    private fun acquireWakeLock() {
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "ReTask:TaskNotificationWakeLock"
        )
        wakeLock?.acquire(10 * 60 * 1000L) // 10 minutes timeout
    }

    /**
     * Observes upcoming tasks and updates persistent notifications and task-specific notifications.
     * This method sets up a Flow to continuously monitor tasks due in the next 24 hours.
     */
    private fun observeUpcomingTasks() {
        val currentTime = System.currentTimeMillis()
        val lookAheadTime = currentTime + NOTIFICATION_WINDOW

        // Cancel any existing job to avoid duplicate observers
        upcomingTasksJob?.cancel()

        // Launch a new coroutine to observe task flow
        upcomingTasksJob = serviceScope.launch {
            try {
                val upcomingTasksFlow: Flow<List<Task>> = taskRepository.getTasksDueBetween(currentTime, lookAheadTime)

                // Collect and process the task flow with error handling
                upcomingTasksFlow
                    .catch { e ->
                        Log.e(TAG, "Error collecting task flow", e)
                    }
                    .collectLatest { tasks ->
                        Log.d(TAG, "Collected ${tasks.size} upcoming tasks")

                        // Update UI on main thread
                        withContext(Dispatchers.Main) {
                            updatePersistentNotification(tasks)
                            scheduleTaskNotifications(tasks.filterDueSoon(currentTime))
                        }
                    }
            } catch (e: Exception) {
                Log.e(TAG, "Error in observeUpcomingTasks", e)
            }
        }
    }

    /**
     * Updates the persistent notification with the list of upcoming tasks.
     * The notification shows a summary of tasks due in the next 24 hours,
     * prioritizing those with the nearest due dates.
     *
     * @param tasks List of tasks to display in the notification.
     */
    private fun updatePersistentNotification(tasks: List<Task>) {
        val sortedTasks = tasks.sortedBy { it.dueDate }
            .take(MAX_TASKS_IN_SUMMARY) // Limit to avoid overly long notifications

        val notification = NotificationHelper.buildPersistentNotification(
            context = this,
            upcomingTasks = sortedTasks
        )

        NotificationHelper.showNotification(
            context = this,
            builder = notification,
            notificationId = NotificationHelper.NOTIFICATION_ID_PERSISTENT
        )

        Log.d(TAG, "Updated persistent notification with ${sortedTasks.size} tasks")
    }

    /**
     * Schedules notifications for tasks that are due soon.
     * This method creates delayed coroutines that will trigger notifications
     * at appropriate times before each task's due date.
     *
     * @param tasks List of tasks to schedule notifications for.
     */
    private fun scheduleTaskNotifications(tasks: List<Task>) {
        val currentTime = System.currentTimeMillis()

        tasks.forEach { task ->
            val timeUntilDue = task.dueDate - currentTime

            // Only schedule for tasks that are due within the notification window
            // and have not yet passed their due date
            if (timeUntilDue > 0 && timeUntilDue <= NOTIFICATION_ADVANCE_WARNING) {
                scheduleTaskNotification(task, timeUntilDue)
            }
        }

        Log.d(TAG, "Scheduled notifications for ${tasks.size} soon-due tasks")
    }

    /**
     * Schedules a notification for a specific task based on its due time.
     *
     * @param task The task to schedule a notification for
     * @param timeUntilDue Time in milliseconds until the task is due
     */
    private fun scheduleTaskNotification(task: Task, timeUntilDue: Long) {
        val notificationId = task.id.hashCode()

        // Schedule the notification to appear at an appropriate time before the due date
        serviceScope.launch {
            try {
                // Calculate when to show notification (10 minutes before due or immediately if less than 10 mins)
                val delayTime = maxOf(0, timeUntilDue - NOTIFICATION_EARLY_WARNING)

                // Wait until it's time to show the notification
                if (delayTime > 0) {
                    Log.d(TAG, "Scheduling notification for task ${task.id} in ${delayTime/1000} seconds")
                    delay(delayTime)
                }

                // Build and show the notification
                withContext(Dispatchers.Main) {
                    val notification = NotificationHelper.buildTaskNotification(
                        context = this@TaskNotificationService,
                        task = task,
                        notificationId = notificationId
                    )

                    NotificationHelper.showNotification(
                        context = this@TaskNotificationService,
                        builder = notification,
                        notificationId = notificationId
                    )

                    Log.d(TAG, "Showed notification for task: ${task.id}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error scheduling notification for task ${task.id}", e)
            }
        }
    }

    /**
     * Starts a periodic task to refresh data and re-observe tasks every 15 minutes.
     * This ensures that the service stays up-to-date with any changes to tasks
     * that might have been made externally.
     */
    private fun startPeriodicCheck() {
        serviceScope.launch {
            try {
                while (true) {
                    delay(NOTIFICATION_REFRESH_INTERVAL)
                    Log.d(TAG, "Performing periodic task refresh")
                    observeUpcomingTasks()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error in periodic check", e)
                // Restart the periodic check if it fails
                delay(TimeUnit.MINUTES.toMillis(1))
                startPeriodicCheck()
            }
        }
    }

    /**
     * Handles the service's start command. Ensures service restarts if killed.
     *
     * @return START_STICKY to indicate that the service should be restarted if killed
     */
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "Service received start command")

        // If service is restarted, ensure we're properly initialized
        if (intent == null && !::taskRepository.isInitialized) {
            // We're being restarted after being killed
            try {
                taskRepository = initializeTaskRepository()
                observeUpcomingTasks()
            } catch (e: Exception) {
                Log.e(TAG, "Error reinitializing service after restart", e)
            }
        }

        // Request a refresh of task data
        if (::taskRepository.isInitialized) {
            serviceScope.launch {
                observeUpcomingTasks()
            }
        }

        return START_STICKY
    }

    /**
     * Handles binding requests (not used as this is a background service).
     *
     * @return null as this service doesn't support binding
     */
    override fun onBind(intent: Intent?): IBinder? = null

    /**
     * Handles service destruction. Cleans up jobs, resources, and releases the wake lock.
     */
    override fun onDestroy() {
        Log.d(TAG, "Service being destroyed")

        // Release wake lock if held
        wakeLock?.let {
            if (it.isHeld) {
                it.release()
            }
        }

        // Cancel all coroutines
        upcomingTasksJob?.cancel()
        serviceJob.cancel()

        super.onDestroy()
    }

    /**
     * Filters tasks that are due within the notification advance warning period.
     *
     * @param currentTime The current system time in milliseconds.
     * @return List of tasks due soon.
     */
    private fun List<Task>.filterDueSoon(currentTime: Long): List<Task> {
        return this.filter {
            val timeUntilDue = it.dueDate - currentTime
            timeUntilDue > 0 && timeUntilDue <= NOTIFICATION_ADVANCE_WARNING
        }
    }
}