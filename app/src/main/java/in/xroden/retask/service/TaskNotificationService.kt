package `in`.xroden.retask.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import `in`.xroden.retask.data.database.TaskDatabase
import `in`.xroden.retask.data.model.Task
import `in`.xroden.retask.data.repository.TaskRepository
import `in`.xroden.retask.utils.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

/**
 * A foreground service that manages task notifications by observing upcoming tasks
 * and scheduling notifications for tasks that are due soon.
 */
class TaskNotificationService : Service() {

    // Coroutine job and scope for managing service tasks
    private val serviceJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.IO + serviceJob)

    // Repository for accessing task data
    private lateinit var taskRepository: TaskRepository

    // Job for observing upcoming tasks
    private var upcomingTasksJob: Job? = null

    /**
     * Called when the service is created. Initializes the notification system,
     * task repository, and starts observing tasks.
     */
    override fun onCreate() {
        super.onCreate()

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
    }

    /**
     * Initializes the TaskRepository using the application's database.
     */
    private fun initializeTaskRepository(): TaskRepository {
        val taskDao = TaskDatabase.getDatabase(application).taskDao()
        return TaskRepository(taskDao)
    }

    /**
     * Starts the service in the foreground with an initial persistent notification.
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
    }

    /**
     * Observes upcoming tasks and updates persistent notifications and task-specific notifications.
     */
    private fun observeUpcomingTasks() {
        val currentTime = System.currentTimeMillis()
        val in24Hours = currentTime + TimeUnit.HOURS.toMillis(24)

        // Cancel any existing job to avoid duplicate observers
        upcomingTasksJob?.cancel()

        // Launch a new coroutine to observe task flow
        upcomingTasksJob = serviceScope.launch {
            val upcomingTasksFlow: Flow<List<Task>> = taskRepository.getTasksDueBetween(currentTime, in24Hours)

            // Collect and process the task flow
            upcomingTasksFlow.collectLatest { tasks ->
                updatePersistentNotification(tasks)
                scheduleTaskNotifications(tasks.filterDueSoon(currentTime))
            }
        }
    }

    /**
     * Updates the persistent notification with the list of upcoming tasks.
     *
     * @param tasks List of tasks to display in the notification.
     */
    private fun updatePersistentNotification(tasks: List<Task>) {
        val sortedTasks = tasks.sortedBy { it.dueDate }

        val notification = NotificationHelper.buildPersistentNotification(
            context = this,
            upcomingTasks = sortedTasks
        )

        NotificationHelper.showNotification(
            context = this,
            builder = notification,
            notificationId = NotificationHelper.NOTIFICATION_ID_PERSISTENT
        )
    }

    /**
     * Schedules notifications for tasks that are due soon (within 30 minutes).
     *
     * @param tasks List of tasks to schedule notifications for.
     */
    private fun scheduleTaskNotifications(tasks: List<Task>) {
        tasks.forEach { task ->
            val timeUntilDue = task.dueDate - System.currentTimeMillis()

            // Launch a coroutine to delay and schedule the notification
            if (timeUntilDue in 1..TimeUnit.MINUTES.toMillis(30)) {
                serviceScope.launch {
                    delay(maxOf(0, timeUntilDue - TimeUnit.MINUTES.toMillis(10)))

                    val notification = NotificationHelper.buildTaskNotification(
                        context = this@TaskNotificationService,
                        task = task,
                        notificationId = task.id.hashCode()
                    )

                    NotificationHelper.showNotification(
                        context = this@TaskNotificationService,
                        builder = notification,
                        notificationId = task.id.hashCode()
                    )
                }
            }
        }
    }

    /**
     * Starts a periodic task to refresh data and re-observe tasks every 15 minutes.
     */
    private fun startPeriodicCheck() {
        serviceScope.launch {
            while (true) {
                delay(TimeUnit.MINUTES.toMillis(15))
                observeUpcomingTasks()
            }
        }
    }

    /**
     * Handles the service's start command. Ensures service restarts if killed.
     */
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    /**
     * Handles binding requests (not used as this is a background service).
     */
    override fun onBind(intent: Intent?): IBinder? = null

    /**
     * Handles service destruction. Cleans up jobs and resources.
     */
    override fun onDestroy() {
        upcomingTasksJob?.cancel()
        serviceJob.cancel()
        super.onDestroy()
    }

    /**
     * Filters tasks that are due within the next 30 minutes.
     *
     * @param currentTime The current system time in milliseconds.
     * @return List of tasks due soon.
     */
    private fun List<Task>.filterDueSoon(currentTime: Long): List<Task> {
        return this.filter {
            it.dueDate > currentTime &&
                    it.dueDate <= currentTime + TimeUnit.MINUTES.toMillis(30)
        }
    }
}