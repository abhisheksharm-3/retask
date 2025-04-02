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

class TaskNotificationService : Service() {
    private val serviceJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.IO + serviceJob)

    private lateinit var taskRepository: TaskRepository
    private var upcomingTasksJob: Job? = null

    override fun onCreate() {
        super.onCreate()

        // Create notification channels
        NotificationHelper.createNotificationChannels(this)

        // Initialize repository with dao from database
        val taskDao = TaskDatabase.getDatabase(application).taskDao()
        taskRepository = TaskRepository(taskDao)

        // Create and show initial empty notification to start foreground service
        val initialNotification = NotificationHelper.buildPersistentNotification(
            context = this,
            upcomingTasks = emptyList()
        )

        startForeground(
            NotificationHelper.NOTIFICATION_ID_PERSISTENT,
            initialNotification.build()
        )

        // Initialize task observer
        observeUpcomingTasks()

        // Start periodic check for upcoming tasks to update notifications
        startPeriodicCheck()
    }

    private fun observeUpcomingTasks() {
        // Get tasks due in the next 24 hours
        val currentTime = System.currentTimeMillis()
        val in24Hours = currentTime + TimeUnit.HOURS.toMillis(24)

        // Cancel any existing collection job
        upcomingTasksJob?.cancel()

        // Create a new job to collect flow
        upcomingTasksJob = serviceScope.launch {
            val upcomingTasksFlow: Flow<List<Task>> = taskRepository.getTasksDueBetween(currentTime, in24Hours)

            // Collect flow and process tasks
            upcomingTasksFlow.collectLatest { tasks ->
                // Update the persistent notification with these tasks
                updatePersistentNotification(tasks)

                // Schedule individual notifications for tasks due soon (next 30 minutes)
                val soonDueTasks = tasks.filter {
                    it.dueDate > currentTime &&
                            it.dueDate <= currentTime + TimeUnit.MINUTES.toMillis(30)
                }

                scheduleTaskNotifications(soonDueTasks)
            }
        }
    }

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

    private fun scheduleTaskNotifications(tasks: List<Task>) {
        // For each task, schedule a notification if it's due soon
        tasks.forEach { task ->
            val timeUntilDue = task.dueDate - System.currentTimeMillis()

            // If due in less than 30 minutes, show notification
            if (timeUntilDue in 1..TimeUnit.MINUTES.toMillis(30)) {
                serviceScope.launch {
                    // Schedule notification for this task
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

    private fun startPeriodicCheck() {
        serviceScope.launch {
            while (true) {
                // Refresh data every 15 minutes
                delay(TimeUnit.MINUTES.toMillis(15))

                // Force refresh by re-observing
                observeUpcomingTasks()
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Restart service if it gets killed
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        upcomingTasksJob?.cancel()
        serviceJob.cancel()
        super.onDestroy()
    }
}