package com.recall.app.core.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.recall.app.core.data.model.Folder
import com.recall.app.core.data.model.Note
import com.recall.app.core.data.model.NoteFts
import com.recall.app.core.data.model.Reminder
import com.recall.app.core.data.model.VectorEntry

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
    abstract fun noteDao(): NoteDao
}
