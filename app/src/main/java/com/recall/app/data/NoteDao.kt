package com.recall.app.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {
    @Query("SELECT * FROM notes ORDER BY is_pinned DESC, updated_at DESC")
    fun getAllNotes(): Flow<List<Note>>

    @Query("SELECT * FROM notes ORDER BY is_pinned DESC, updated_at DESC")
    suspend fun getAllNotesOnce(): List<Note>

    @Query("SELECT * FROM notes WHERE id = :noteId")
    suspend fun getNoteById(noteId: String): Note?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: Note)

    @Query("DELETE FROM notes WHERE id = :noteId")
    suspend fun deleteNote(noteId: String)
}
