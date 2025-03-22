package `in`.xroden.retask.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import `in`.xroden.retask.data.repository.TaskRepository
import `in`.xroden.retask.data.model.Task
import `in`.xroden.retask.data.database.TaskDatabase


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
    }

    fun snoozeTask(task: Task) = viewModelScope.launch {
        // Add 15 minutes to due date
        val newDueDate = task.dueDate + (15 * 60 * 1000)
        val updatedTask = task.copy(dueDate = newDueDate)
        repository.updateTask(updatedTask)
    }

    fun addTask(
        title: String,
        dueMinutes: Int,
        colorHex: String
    ) = viewModelScope.launch {
        repository.createTask(title, dueMinutes, colorHex)
    }

    // New method to edit an existing task
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
    }

    // Add sample data for testing
    fun addSampleTasks() = viewModelScope.launch {
        val colors = listOf("#F6D8CE", "#D5F5E3", "#FADBD8", "#D6EAF8")

        repository.createTask(
            "Coffee with Mel",
            -30, // 30 minutes ago
            colors[0]
        )

        repository.createTask(
            "Water plants",
            -20, // 20 minutes ago
            colors[1]
        )

        repository.createTask(
            "Get to the airport!",
            0, // Now
            colors[2]
        )

        repository.createTask(
            "Go to the gym",
            10, // 10 minutes from now
            colors[3]
        )
    }
}