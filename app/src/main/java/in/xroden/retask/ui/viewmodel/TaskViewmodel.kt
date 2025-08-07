package `in`.xroden.retask.ui.viewmodel

import android.app.Application
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import `in`.xroden.retask.data.database.TaskDatabase
import `in`.xroden.retask.data.model.Task
import `in`.xroden.retask.data.repository.TaskRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.comparisons.compareBy

@RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
class TaskViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: TaskRepository

    val allTasks: StateFlow<List<Task>>

    /**
     * A StateFlow that holds tasks grouped by date categories for the UI.
     */
    val groupedTasks: StateFlow<Map<String, List<Task>>>

    init {
        val taskDao = TaskDatabase.getDatabase(application).taskDao()
        repository = TaskRepository(taskDao)

        allTasks = repository.allTasks.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        groupedTasks = repository.allTasks.map { tasks ->
            val formatter = DateTimeFormatter.ofPattern("EEEE, MMMM d", Locale.getDefault())
            val today = LocalDate.now()

            val grouped = tasks.groupBy { task ->
                val taskDate = LocalDate.ofInstant(Instant.ofEpochMilli(task.dueDate), ZoneId.systemDefault())
                when {
                    taskDate.isBefore(today) -> "Overdue"
                    taskDate.isEqual(today) -> "Today"
                    taskDate.isEqual(today.plusDays(1)) -> "Tomorrow"
                    else -> taskDate.format(formatter)
                }
            }

            val sortOrder = listOf("Overdue", "Today", "Tomorrow")
            grouped.toSortedMap(compareBy { key ->
                val index = sortOrder.indexOf(key)
                if (index != -1) index else Int.MAX_VALUE
            })
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyMap()
        )
    }

    fun addTask(title: String, dueMinutes: Int, colorHex: String) = viewModelScope.launch {
        val now = System.currentTimeMillis()
        val dueDate = now + (dueMinutes * 60 * 1000L)
        repository.createTask(getApplication(), title, dueDate, colorHex)
    }

    fun editTask(task: Task, title: String, dueMinutes: Int, colorHex: String) = viewModelScope.launch {
        val now = System.currentTimeMillis()
        val newDueDate = now + (dueMinutes * 60 * 1000L)
        val updatedTask = task.copy(title = title, dueDate = newDueDate, colorHex = colorHex)
        repository.updateTask(getApplication(), updatedTask)
    }

    fun completeTask(task: Task) = viewModelScope.launch {
        repository.deleteTask(getApplication(), task)
    }

    fun uncompleteTask(task: Task) = viewModelScope.launch {
        repository.uncompleteTask(getApplication(), task)
    }

    fun deleteTask(task: Task) = viewModelScope.launch {
        repository.deleteTask(getApplication(), task)
    }

    fun snoozeTask(task: Task) = viewModelScope.launch {
        val newDueDate = task.dueDate + (15 * 60 * 1000L)
        val updatedTask = task.copy(dueDate = newDueDate)
        repository.updateTask(getApplication(), updatedTask)
    }

    fun addSampleTasks() = viewModelScope.launch {
        val context = getApplication<Application>()
        val colors = listOf("#F6D8CE", "#D5F5E3", "#FADBD8", "#D6EAF8")
        val now = System.currentTimeMillis()

        repository.createTask(context, "Chai with Priya", now - (30 * 60 * 1000L), colors[0])
        repository.createTask(context, "Water tulsi plant", now - (20 * 60 * 1000L), colors[1])
        repository.createTask(context, "Book Ola to Bangalore airport!", now, colors[2])
        repository.createTask(context, "Attend yoga class", now + (10 * 60 * 1000L), colors[3])
    }
}