package com.recall.app.core.data.local

import androidx.room.*
import com.recall.app.core.data.model.Note
import com.recall.app.core.data.model.Reminder
import com.recall.app.core.data.model.VectorEntry
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {

    // ── Notes ────────────────────────────────────────────────────────
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

    @Query("UPDATE notes SET is_ai_indexed = :indexed WHERE id = :noteId")
    suspend fun markNoteIndexed(noteId: String, indexed: Boolean)

    // ── FTS4 Search (never returns private notes in AI context) ───────
    @Query("""
        SELECT notes.* FROM notes
        JOIN notes_fts ON notes.id = notes_fts.note_id
        WHERE notes_fts MATCH :query
        ORDER BY notes.updated_at DESC
    """)
    suspend fun searchNotes(query: String): List<Note>

    // ── Vector Entries (RAG) ──────────────────────────────────────────
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

    // ── Reminders ─────────────────────────────────────────────────────
    @Query("SELECT * FROM reminders ORDER BY trigger_at ASC")
    fun getAllReminders(): Flow<List<Reminder>>

    @Query("SELECT * FROM reminders WHERE is_completed = 0 ORDER BY trigger_at ASC")
    suspend fun getAllRemindersOnce(): List<Reminder>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminder(reminder: Reminder)

    @Delete
    suspend fun deleteReminder(reminder: Reminder)
}
