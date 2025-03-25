package `in`.xroden.retask.data.database


import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import `in`.xroden.retask.data.dao.TaskDao
import `in`.xroden.retask.data.model.Task

/**
 * Room database for the application.
 * Contains all data tables and provides access to DAOs.
 */
@Database(entities = [Task::class], version = 1, exportSchema = false)
abstract class TaskDatabase : RoomDatabase() {
    /**
     * Provides access to the Task Data Access Object.
     */
    abstract fun taskDao(): TaskDao

    companion object {
        @Volatile
        private var INSTANCE: TaskDatabase? = null

        /**
         * Gets the singleton instance of TaskDatabase.
         *
         * @param context The application context
         * @return The singleton TaskDatabase instance
         */
        fun getDatabase(context: Context): TaskDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TaskDatabase::class.java,
                    "task_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}