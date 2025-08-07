package `in`.xroden.retask.data.repository

import android.content.Context
import `in`.xroden.retask.data.dao.TaskDao
import `in`.xroden.retask.data.model.Task
import `in`.xroden.retask.service.TaskReminderService
import kotlinx.coroutines.flow.Flow

class TaskRepository(private val taskDao: TaskDao) {

    val allTasks: Flow<List<Task>> = taskDao.getAllTasks()

    suspend fun createTask(context: Context, title: String, dueDate: Long, colorHex: String): Task {
        val newTask = Task(title = title, dueDate = dueDate, colorHex = colorHex)
        val newId = taskDao.insertTask(newTask)
        val createdTask = newTask.copy(id = newId)
        TaskReminderService.scheduleTask(context, createdTask)
        return createdTask
    }

    suspend fun updateTask(context: Context, task: Task) {
        taskDao.updateTask(task)
        TaskReminderService.scheduleTask(context, task)
    }

    suspend fun deleteTask(context: Context, task: Task) {
        TaskReminderService.cancelTask(context, task.id)
        taskDao.deleteTask(task)
    }

    suspend fun deleteAllTasks() {
        // For a real app, you would fetch all tasks, loop through them to cancel
        // each alarm, and then delete them from the database.
        taskDao.deleteAllTasks()
    }

    suspend fun completeTask(context: Context, task: Task) {
        val completedTask = task.copy(isCompleted = true)
        taskDao.updateTask(completedTask)
        TaskReminderService.cancelTask(context, task.id)
    }

    suspend fun uncompleteTask(context: Context, task: Task) {
        val uncompletedTask = task.copy(isCompleted = false)
        taskDao.updateTask(uncompletedTask)
        TaskReminderService.scheduleTask(context, uncompletedTask)
    }
}