package `in`.xroden.retask.data.repository

import `in`.xroden.retask.data.dao.TaskDao
import `in`.xroden.retask.data.model.Task
import kotlinx.coroutines.flow.Flow
import java.util.UUID

/**
 * Repository for accessing and managing Task data.
 * Acts as the single source of truth for task-related operations.
 */
class TaskRepository(private val taskDao: TaskDao) {
    /**
     * Provides a flow of all tasks from the database, ordered by due date.
     * This flow will automatically emit new values when the database changes.
     */
    val allTasks: Flow<List<Task>> = taskDao.getAllTasks()

    /**
     * Inserts a task into the database.
     * @param task The task to be inserted
     * @return True if the operation was successful, false otherwise
     */
    suspend fun insertTask(task: Task): Boolean {
        return try {
            taskDao.insertTask(task)
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Updates an existing task in the database.
     * @param task The task with updated values
     * @return True if the operation was successful, false otherwise
     */
    suspend fun updateTask(task: Task): Boolean {
        return try {
            taskDao.updateTask(task)
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Deletes a task from the database.
     * @param task The task to be deleted
     * @return True if the operation was successful, false otherwise
     */
    suspend fun deleteTask(task: Task): Boolean {
        return try {
            taskDao.deleteTask(task)
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Creates a new task with the given details and inserts it into the database.
     * Generates a UUID for the task and calculates the due date based on minutes from now.
     *
     * @param title The title of the task
     * @param dueMinutes Minutes from now when the task is due
     * @param colorHex The hex color code for the task's visual representation
     * @return The newly created task if successful, null otherwise
     */
    suspend fun createTask(
        title: String,
        dueMinutes: Int,
        colorHex: String
    ): Task? {
        return try {
            val now = System.currentTimeMillis()
            val dueDate = now + (dueMinutes * 60 * 1000L)

            val task = Task(
                id = UUID.randomUUID().toString(),
                title = title,
                dueDate = dueDate,
                colorHex = colorHex
            )

            taskDao.insertTask(task)
            task
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Marks a task as complete by removing it from the database.
     * Note: In this implementation, completion means deletion.
     *
     * @param task The task to mark as complete
     * @return True if the operation was successful, false otherwise
     */
    suspend fun completeTask(task: Task): Boolean {
        return try {
            taskDao.deleteTask(task)
            true
        } catch (e: Exception) {
            false
        }
    }
}