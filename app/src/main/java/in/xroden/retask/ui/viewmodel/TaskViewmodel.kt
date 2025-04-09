package `in`.xroden.retask.ui.viewmodel

import android.app.Application
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import `in`.xroden.retask.data.repository.TaskRepository
import `in`.xroden.retask.data.model.Task
import `in`.xroden.retask.data.database.TaskDatabase
import `in`.xroden.retask.service.TaskNotificationService
import `in`.xroden.retask.service.TaskReminderService

/**
 * ViewModel class for managing tasks and their associated operations.
 * Provides an interface for the UI to interact with the task repository and services.
 *
 * @param application The application context, used for accessing resources and services.
 */
class TaskViewModel(application: Application) : AndroidViewModel(application) {

    // Repository for managing task data
    private val repository: TaskRepository

    // StateFlow holding all tasks, automatically updated from the database
    val allTasks: StateFlow<List<Task>>

    init {
        val taskDao = TaskDatabase.getDatabase(application).taskDao()
        repository = TaskRepository(taskDao)

        // Collect all tasks into a StateFlow
        allTasks = repository.allTasks.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000), // Share the flow while the UI is subscribed
            emptyList() // Default value when no tasks are available
        )
    }

    /**
     * Marks a task as completed and cancels its associated reminder.
     *
     * @param task The task to be marked as completed.
     */
    fun completeTask(task: Task) = viewModelScope.launch {
        repository.completeTask(task)
        TaskReminderService.cancelTask(getApplication(), task.id)
    }

    /**
     * Snoozes a task by adding 15 minutes to its due date and updating its reminder.
     *
     * @param task The task to be snoozed.
     */
    fun snoozeTask(task: Task) = viewModelScope.launch {
        val newDueDate = task.dueDate + (15 * 60 * 1000) // Add 15 minutes
        val updatedTask = task.copy(dueDate = newDueDate)

        repository.updateTask(updatedTask)
        TaskReminderService.scheduleTask(getApplication(), updatedTask)
        ensureNotificationServiceRunning()
    }

    /**
     * Adds a new task, schedules its reminder, and ensures the notification service is running.
     *
     * @param title The title of the task.
     * @param dueMinutes The due time in minutes from the current time.
     * @param colorHex The color associated with the task.
     */
    fun addTask(title: String, dueMinutes: Int, colorHex: String) = viewModelScope.launch {
        val task = repository.createTask(title, dueMinutes, colorHex)
        TaskReminderService.scheduleTask(getApplication(), task)
        ensureNotificationServiceRunning()
    }

    /**
     * Edits an existing task, updates its values, and reschedules its reminder.
     *
     * @param task The existing task to be updated.
     * @param title The new title for the task.
     * @param dueMinutes The new due time in minutes from the current time.
     * @param colorHex The new color associated with the task.
     */
    fun editTask(task: Task, title: String, dueMinutes: Int, colorHex: String) = viewModelScope.launch {
        val now = System.currentTimeMillis()
        val newDueDate = now + (dueMinutes * 60 * 1000)

        val updatedTask = task.copy(
            title = title,
            dueDate = newDueDate,
            colorHex = colorHex
        )

        repository.updateTask(updatedTask)
        TaskReminderService.scheduleTask(getApplication(), updatedTask)
        ensureNotificationServiceRunning()
    }

    /**
     * Adds sample tasks to the repository for demonstration purposes.
     * Only schedules reminders for tasks that are due in the future.
     */
    fun addSampleTasks() = viewModelScope.launch {
        val colors = listOf("#F6D8CE", "#D5F5E3", "#FADBD8", "#D6EAF8")

        val tasks = listOf(
            repository.createTask("Chai with Priya", -30, colors[0]), // 30 minutes ago
            repository.createTask("Water tulsi plant", -20, colors[1]), // 20 minutes ago
            repository.createTask("Book Ola to Bangalore airport!", 0, colors[2]), // Now
            repository.createTask("Attend yoga class", 10, colors[3]) // 10 minutes from now
        )

        // Schedule reminders only for future tasks
        tasks.filter { it?.dueDate!! > System.currentTimeMillis() }.forEach { task ->
            TaskReminderService.scheduleTask(getApplication(), task)
        }

        ensureNotificationServiceRunning()
    }

    /**
     * Ensures that the notification service is running in the foreground.
     */
    private fun ensureNotificationServiceRunning() {
        val context = getApplication<Application>()
        val serviceIntent = Intent(context, TaskNotificationService::class.java)
        context.startForegroundService(serviceIntent)
    }
}