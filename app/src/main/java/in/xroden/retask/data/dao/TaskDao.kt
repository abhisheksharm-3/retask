package `in`.xroden.retask.data.dao

import androidx.room.*
import `in`.xroden.retask.data.model.Task
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) for Task entities.
 *
 * This interface provides methods to interact with the 'tasks' table in the database,
 * allowing CRUD operations (Create, Read, Update, Delete) for Task objects.
 */
@Dao
interface TaskDao {
    /**
     * Retrieves all tasks from the database ordered by due date in ascending order.
     *
     * @return A Flow that emits a new List of Task objects whenever the database changes.
     * Using Flow ensures that the UI will automatically update when the underlying data changes.
     */
    @Query("SELECT * FROM tasks ORDER BY dueDate ASC")
    fun getAllTasks(): Flow<List<Task>>

    /**
     * Inserts a new task into the database or replaces an existing one if there's a conflict.
     *
     * @param task The Task object to be inserted or updated.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: Task): Long

    /**
     * Updates an existing task in the database.
     *
     * @param task The Task object with updated fields. The task must have an ID that exists
     * in the database.
     */
    @Transaction
    @Update
    suspend fun updateTask(task: Task)

    /**
     * Deletes an existing task from the database.
     *
     * @param task The Task object to be deleted. The task must have an ID that exists
     * in the database.
     */
    @Transaction
    @Delete
    suspend fun deleteTask(task: Task)

    /**
     * Deletes all tasks from the database.
     * This operation cannot be undone.
     */
    @Transaction
    @Query("DELETE FROM tasks")
    suspend fun deleteAllTasks()

    /**
     * Retrieves tasks due between the specified start and end times.
     *
     * @param startTime The start time in milliseconds since epoch
     * @param endTime The end time in milliseconds since epoch
     * @return Flow of tasks due between the specified time range
     */
    @Query("SELECT * FROM tasks WHERE dueDate BETWEEN :startTime AND :endTime ORDER BY dueDate ASC")
    fun getTasksDueBetween(startTime: Long, endTime: Long): Flow<List<Task>>
}