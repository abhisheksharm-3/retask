package `in`.xroden.retask.data.repository

import `in`.xroden.retask.data.dao.TaskDao
import `in`.xroden.retask.data.model.Task
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class TaskRepository(private val taskDao: TaskDao) {
    val allTasks: Flow<List<Task>> = taskDao.getAllTasks()

    suspend fun insertTask(task: Task) {
        taskDao.insertTask(task)
    }

    suspend fun updateTask(task: Task) {
        taskDao.updateTask(task)
    }

    suspend fun deleteTask(task: Task) {
        taskDao.deleteTask(task)
    }


    suspend fun createTask(
        title: String,
        dueMinutes: Int,
        colorHex: String
    ): Task {
        val now = System.currentTimeMillis()
        val dueDate = now + (dueMinutes * 60 * 1000)

        val task = Task(
            id = UUID.randomUUID().toString(),
            title = title,
            dueDate = dueDate,
            colorHex = colorHex
        )

        taskDao.insertTask(task)
        return task
    }

    suspend fun completeTask(task: Task) {
        taskDao.deleteTask(task)
    }
}