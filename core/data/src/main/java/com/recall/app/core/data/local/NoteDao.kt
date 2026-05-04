package com.recall.app.core.data.local

import androidx.room.*
import com.recall.app.core.data.model.Note
import com.recall.app.core.data.model.Reminder
import com.recall.app.core.data.model.VectorEntry
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) for managing notes, vector embeddings, and reminders.
 *
 * This DAO provides methods for standard CRUD operations as well as specialized
 * functions like Full-Text Search (FTS4) and atomic transactions for updating
 * a note alongside its semantic vector embeddings.
 */
@Dao
interface NoteDao {

    // ── Notes ────────────────────────────────────────────────────────

    /**
     * Retrieves all notes from the database as a reactive stream.
     * Notes are sorted primarily by their pinned status (pinned first),
     * and secondarily by their last updated timestamp (newest first).
     */
    @Query("SELECT * FROM notes ORDER BY is_pinned DESC, updated_at DESC")
    fun getAllNotes(): Flow<List<Note>>

    /**
     * Retrieves all notes from the database once without subscribing to future updates.
     */
    @Query("SELECT * FROM notes ORDER BY is_pinned DESC, updated_at DESC")
    suspend fun getAllNotesOnce(): List<Note>

    /**
     * Fetches a specific note by its unique identifier.
     */
    @Query("SELECT * FROM notes WHERE id = :noteId")
    suspend fun getNoteById(noteId: String): Note?

    /**
     * Inserts a new note or replaces an existing one (upsert).
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: Note)

    /**
     * Permanently deletes a note by its ID.
     */
    @Query("DELETE FROM notes WHERE id = :noteId")
    suspend fun deleteNote(noteId: String)

    /**
     * Marks a note as indexed or unindexed by the AI background worker.
     */
    @Query("UPDATE notes SET is_ai_indexed = :indexed WHERE id = :noteId")
    suspend fun markNoteIndexed(noteId: String, indexed: Boolean)

    // ── FTS4 Search (never returns private notes in AI context) ───────

    /**
     * Performs a fast full-text search (FTS4) across all notes.
     *
     * @param query The search term.
     * @return A list of notes matching the query, ordered by most recently updated.
     */
    @Query("""
        SELECT notes.* FROM notes
        JOIN notes_fts ON notes.id = notes_fts.note_id
        WHERE notes_fts MATCH :query
        ORDER BY notes.updated_at DESC
    """)
    suspend fun searchNotes(query: String): List<Note>

    // ── Vector Entries (RAG) ──────────────────────────────────────────

    /**
     * Retrieves all semantic vector embeddings currently stored.
     */
    @Query("SELECT * FROM vector_entries")
    suspend fun getAllVectorEntries(): List<VectorEntry>

    /**
     * Inserts a batch of vector embeddings.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVectorEntries(entries: List<VectorEntry>)

    /**
     * Deletes all vector embeddings associated with a specific note.
     */
    @Query("DELETE FROM vector_entries WHERE note_id = :noteId")
    suspend fun deleteVectorEntriesForNote(noteId: String)

    /**
     * Atomically updates a note and replaces its vector embeddings.
     *
     * This transaction ensures that a note and its AI representation remain
     * perfectly synchronized, preventing dangling or stale embeddings.
     */
    @Transaction
    suspend fun updateNoteAndEmbeddings(note: Note, entries: List<VectorEntry>) {
        insertNote(note)
        deleteVectorEntriesForNote(note.id)
        if (entries.isNotEmpty()) insertVectorEntries(entries)
    }

    // ── Reminders ─────────────────────────────────────────────────────

    /**
     * Retrieves all reminders as a reactive stream, ordered by their trigger time.
     */
    @Query("SELECT * FROM reminders ORDER BY trigger_at ASC")
    fun getAllReminders(): Flow<List<Reminder>>

    /**
     * Retrieves all uncompleted reminders once. Useful for restoring alarms after boot.
     */
    @Query("SELECT * FROM reminders WHERE is_completed = 0 ORDER BY trigger_at ASC")
    suspend fun getAllRemindersOnce(): List<Reminder>

    /**
     * Inserts a new reminder or updates an existing one.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminder(reminder: Reminder)

    /**
     * Permanently deletes a reminder.
     */
    @Delete
    suspend fun deleteReminder(reminder: Reminder)
}
