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

@HiltViewModel
class NotesViewModel @Inject constructor(
    application: Application,
    private val dao: NoteDao,
    private val embeddingEngine: EmbeddingEngine
) : AndroidViewModel(application) {

    val allNotes: StateFlow<List<Note>> = dao.getAllNotes()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val _searchResults = MutableStateFlow<List<Note>>(emptyList())
    val searchResults: StateFlow<List<Note>> = _searchResults

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

    /** Used by AI chat for real note context (FTS4 RAG retrieval). */
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
