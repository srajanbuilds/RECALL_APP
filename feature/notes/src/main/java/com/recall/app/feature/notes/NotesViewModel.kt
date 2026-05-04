package com.recall.app.feature.notes

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.recall.app.core.ai.EmbeddingEngine
import com.recall.app.core.ai.IndexNoteWorker
import com.recall.app.core.data.local.NoteDao
import com.recall.app.core.data.model.Note
import com.recall.app.core.data.model.VectorEntry
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel managing the state and business logic for the notes feature.
 *
 * This class handles fetching notes from the local database, performing text
 * searches, and orchestrating the creation/deletion of notes. It also triggers
 * the background AI indexing process via [IndexNoteWorker].
 *
 * @property allNotes A reactive flow emitting the complete list of notes, ordered by pinned status and recency.
 * @property searchResults A reactive flow emitting notes that match the current search query.
 */
@HiltViewModel
class NotesViewModel @Inject constructor(
    application: Application,
    private val dao: NoteDao,
    private val embeddingEngine: EmbeddingEngine
) : AndroidViewModel(application) {

    // ── State ─────────────────────────────────────────────────────────

    val allNotes: StateFlow<List<Note>> = dao.getAllNotes()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val _searchResults = MutableStateFlow<List<Note>>(emptyList())
    val searchResults: StateFlow<List<Note>> = _searchResults

    // ── Intent ────────────────────────────────────────────────────────

    /**
     * Executes a full-text search across all notes.
     * Updates the [searchResults] state flow.
     */
    fun search(query: String) {
        viewModelScope.launch {
            _searchResults.value = if (query.isBlank()) emptyList()
            else try {
                dao.searchNotes("$query*")
            } catch (e: Exception) {
                allNotes.value.filter {
                    it.title.contains(query, ignoreCase = true) ||
                    it.body.contains(query, ignoreCase = true)
                }
            }
        }
    }

    /**
     * Used by the AI chat engine to retrieve relevant notes as context (RAG).
     * Private notes are strictly filtered out to prevent data leakage into AI prompts.
     */
    suspend fun searchForContext(query: String): List<Note> =
        try { dao.searchNotes("$query*").filter { !it.isPrivate }.take(5) }
        catch (e: Exception) {
            allNotes.value.filter {
                !it.isPrivate && (
                    it.title.contains(query, ignoreCase = true) ||
                    it.body.contains(query, ignoreCase = true)
                )
            }.take(5)
        }

    /**
     * Upserts a note to the local database. If the note is not private,
     * it also enqueues an AI background indexing job.
     */
    fun saveNote(title: String, content: String, noteId: String? = null, isPrivate: Boolean = false) {
        viewModelScope.launch {
            val existing = noteId?.let { id -> allNotes.value.find { it.id == id } }
            val note = Note(
                id = existing?.id ?: java.util.UUID.randomUUID().toString(),
                title = title,
                body = content,
                isPrivate = isPrivate,
                createdAt = existing?.createdAt ?: System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )

            // Private notes: insert into Room but NEVER generate embeddings (enforced here)
            dao.insertNote(note)

            if (!isPrivate) {
                // Enqueue background indexing via WorkManager
                IndexNoteWorker.enqueue(getApplication(), note.id)
            }
        }
    }

    fun deleteNote(noteId: String) {
        viewModelScope.launch { dao.deleteNote(noteId) }
    }

    fun getNoteById(noteId: String): Note? = allNotes.value.find { it.id == noteId }
}
