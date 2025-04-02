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

class TaskViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: TaskRepository
    val allTasks: StateFlow<List<Task>>

    init {
        val taskDao = TaskDatabase.getDatabase(application).taskDao()
        repository = TaskRepository(taskDao)
        allTasks = repository.allTasks.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )
    }

    fun completeTask(task: Task) = viewModelScope.launch {
        repository.completeTask(task)
        TaskReminderService.cancelTask(getApplication(), task.id)
    }

    fun snoozeTask(task: Task) = viewModelScope.launch {
        // Add 15 minutes to due date
        val newDueDate = task.dueDate + (15 * 60 * 1000)
        val updatedTask = task.copy(dueDate = newDueDate)
        repository.updateTask(updatedTask)

        // Update the scheduled notification
        TaskReminderService.scheduleTask(getApplication(), updatedTask)

        // Ensure notification service is running
        ensureNotificationServiceRunning()
    }

    fun addTask(
        title: String,
        dueMinutes: Int,
        colorHex: String
    ) = viewModelScope.launch {
        val task = repository.createTask(title, dueMinutes, colorHex)

        // Schedule notification for this task
        TaskReminderService.scheduleTask(getApplication(), task)

        // Ensure notification service is running
        ensureNotificationServiceRunning()
    }

    // Method to edit an existing task
    fun editTask(
        task: Task,
        title: String,
        dueMinutes: Int,
        colorHex: String
    ) = viewModelScope.launch {
        // Calculate new due date based on current time plus specified minutes
        val now = System.currentTimeMillis()
        val newDueDate = now + (dueMinutes * 60 * 1000)

        // Create updated task with existing ID but new values
        val updatedTask = task.copy(
            title = title,
            dueDate = newDueDate,
            colorHex = colorHex
        )

        repository.updateTask(updatedTask)

        // Update the scheduled notification
        TaskReminderService.scheduleTask(getApplication(), updatedTask)

        // Ensure notification service is running
        ensureNotificationServiceRunning()
    }

    fun addSampleTasks() = viewModelScope.launch {
        val colors = listOf("#F6D8CE", "#D5F5E3", "#FADBD8", "#D6EAF8")

        val task1 = repository.createTask(
            "Chai with Priya",
            -30, // 30 minutes ago
            colors[0]
        )

        val task2 = repository.createTask(
            "Water tulsi plant",
            -20, // 20 minutes ago
            colors[1]
        )

        val task3 = repository.createTask(
            "Book Ola to Bangalore airport!",
            0, // Now
            colors[2]
        )

        val task4 = repository.createTask(
            "Attend yoga class",
            10, // 10 minutes from now
            colors[3]
        )

        // Schedule notifications only for future tasks
        TaskReminderService.scheduleTask(getApplication(), task3)
        TaskReminderService.scheduleTask(getApplication(), task4)

        // Ensure notification service is running
        ensureNotificationServiceRunning()
    }
    private fun ensureNotificationServiceRunning() {
        val context = getApplication<Application>()
        val serviceIntent = Intent(context, TaskNotificationService::class.java)

        context.startForegroundService(serviceIntent)
    }
}