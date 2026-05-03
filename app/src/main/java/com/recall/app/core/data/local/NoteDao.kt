package com.recall.app.core.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.recall.app.core.data.model.Note
import com.recall.app.core.data.model.Reminder
import com.recall.app.core.data.model.VectorEntry
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {
    // ── Notes ─────────────────────────────────────────────────────────────
    @Query("SELECT * FROM notes ORDER BY updated_at DESC")
    fun getAllNotes(): Flow<List<Note>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: Note)

    @Query("DELETE FROM notes WHERE id = :noteId")
    suspend fun deleteNote(noteId: String)

    @Query("SELECT notes.* FROM notes JOIN notes_fts ON notes.id = notes_fts.note_id WHERE notes_fts MATCH :query")
    suspend fun searchNotes(query: String): List<Note>

    // ── Vector Entries (RAG) ──────────────────────────────────────────────
    @Query("SELECT * FROM vector_entries")
    suspend fun getAllVectorEntries(): List<VectorEntry>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVectorEntries(entries: List<VectorEntry>)

    @Query("DELETE FROM vector_entries WHERE note_id = :noteId")
    suspend fun deleteVectorEntriesForNote(noteId: String)

    @Transaction
    suspend fun updateNoteAndEmbeddings(note: Note, entries: List<VectorEntry>) {
        insertNote(note)
        deleteVectorEntriesForNote(note.id)
        if (entries.isNotEmpty()) insertVectorEntries(entries)
    }

    // ── Reminders ─────────────────────────────────────────────────────────
    @Query("SELECT * FROM reminders ORDER BY trigger_at ASC")
    fun getAllReminders(): Flow<List<Reminder>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminder(reminder: Reminder)

    @Delete
    suspend fun deleteReminder(reminder: Reminder)

    @Query("SELECT * FROM reminders WHERE note_id = :noteId ORDER BY trigger_at ASC")
    fun getRemindersForNote(noteId: String): Flow<List<Reminder>>
}
