package com.recall.app.core.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.recall.app.core.data.model.Folder
import com.recall.app.core.data.model.Note
import com.recall.app.core.data.model.NoteFts
import com.recall.app.core.data.model.Reminder
import com.recall.app.core.data.model.VectorEntry

/**
 * The primary Room database for the Recall app.
 *
 * This database stores the user's notes, semantic vector embeddings for AI search,
 * and scheduled reminders. It operates entirely offline, ensuring maximum privacy.
 *
 * @see Note
 * @see NoteFts
 * @see VectorEntry
 * @see Reminder
 */
@Database(
    entities = [
        Note::class,
        NoteFts::class,
        Folder::class,
        Reminder::class,
        VectorEntry::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    /**
     * Provides access to the Data Access Object (DAO) for note-related database operations.
     */
    abstract fun noteDao(): NoteDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        /**
         * Retrieves the singleton instance of [AppDatabase].
         *
         * Uses double-checked locking to ensure thread safety during initialization.
         *
         * @param context The application context.
         * @return The singleton [AppDatabase] instance.
         */
        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "recall-db"
                ).fallbackToDestructiveMigration().build().also { INSTANCE = it }
            }
    }
}
