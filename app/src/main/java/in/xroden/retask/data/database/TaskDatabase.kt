package `in`.xroden.retask.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import `in`.xroden.retask.data.dao.TaskDao
import `in`.xroden.retask.data.model.Task

/**
 * The Room database for the application.
 *
 * This class defines the database configuration and serves as the main access point
 * to the persisted data. It follows the Singleton pattern to prevent having multiple
 * instances of the database opened at the same time.
 *
 */
@Database(entities = [Task::class], version = 2, exportSchema = true)
abstract class TaskDatabase : RoomDatabase() {

    /**
     * Provides an abstract method to get the [TaskDao]. Room will generate the implementation.
     *
     * @return The Data Access Object for the 'tasks' table.
     */
    abstract fun taskDao(): TaskDao

    companion object {
        /**
         * The singleton instance of the [TaskDatabase].
         * The @Volatile annotation ensures that changes to this field are immediately visible
         * to all other threads, preventing issues with cached values.
         */
        @Volatile
        private var INSTANCE: TaskDatabase? = null

        /**
         * Gets the singleton instance of [TaskDatabase], creating it if it doesn't exist.
         * This synchronized method is thread-safe and ensures that the database is initialized only once.
         *
         * @param context The application context, used to get the path to the database.
         * @return The singleton [TaskDatabase] instance.
         */
        fun getDatabase(context: Context): TaskDatabase {
            // Return the existing instance if it's not null, otherwise enter the synchronized block.
            return INSTANCE ?: synchronized(this) {
                // Inside the synchronized block, check again to ensure another thread didn't initialize it.
                val instance = INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    TaskDatabase::class.java,
                    "task_database"
                )
                    // IMPORTANT: This is NOT a production-ready migration strategy.
                    // It will delete all user data on a version upgrade.
                    // Replace this with .addMigrations() and provide proper Migration objects.
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}