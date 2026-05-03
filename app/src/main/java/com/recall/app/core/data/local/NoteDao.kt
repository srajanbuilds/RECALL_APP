package com.recall.app.core.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.recall.app.core.data.model.Note
import com.recall.app.core.data.model.VectorEntry
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {
    @Query("SELECT * FROM notes ORDER BY updated_at DESC")
    fun getAllNotes(): Flow<List<Note>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: Note)

    @Query("SELECT notes.* FROM notes JOIN notes_fts ON notes.id = notes_fts.note_id WHERE notes_fts MATCH :query")
    suspend fun searchNotes(query: String): List<Note>

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
        if (entries.isNotEmpty()) {
            insertVectorEntries(entries)
        }
    }
}
