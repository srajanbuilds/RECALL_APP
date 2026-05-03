package com.recall.app.feature.notes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.recall.app.core.ai.EmbeddingEngine
import com.recall.app.core.data.local.NoteDao
import com.recall.app.core.data.model.Note
import com.recall.app.core.data.model.VectorEntry
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class NotesViewModel(
    private val dao: NoteDao,
    private val embeddingEngine: EmbeddingEngine
) : ViewModel() {

    val allNotes: StateFlow<List<Note>> = dao.getAllNotes()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val _searchResults = MutableStateFlow<List<Note>>(emptyList())
    val searchResults: StateFlow<List<Note>> = _searchResults

    fun search(query: String) {
        viewModelScope.launch {
            _searchResults.value = if (query.isBlank()) {
                emptyList()
            } else {
                try {
                    dao.searchNotes("$query*")
                } catch (e: Exception) {
                    allNotes.value.filter {
                        it.title.contains(query, ignoreCase = true) ||
                        it.body.contains(query, ignoreCase = true)
                    }
                }
            }
        }
    }

    /**
     * Search notes and return the results directly (for AI chat RAG).
     */
    suspend fun searchForContext(query: String): List<Note> {
        return try {
            dao.searchNotes("$query*").take(5)
        } catch (e: Exception) {
            allNotes.value
                .filter {
                    it.title.contains(query, ignoreCase = true) ||
                    it.body.contains(query, ignoreCase = true)
                }
                .take(5)
        }
    }

    fun saveNote(
        title: String,
        content: String,
        noteId: String? = null,
        isPrivate: Boolean = false
    ) {
        viewModelScope.launch {
            val existingNote = if (noteId != null) {
                allNotes.value.find { it.id == noteId }
            } else null

            val note = Note(
                id = existingNote?.id ?: java.util.UUID.randomUUID().toString(),
                title = title,
                body = content,
                isPrivate = isPrivate,
                createdAt = existingNote?.createdAt ?: System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )

            val chunks = embeddingEngine.chunkText(content)
            val vectorEntries = chunks.mapIndexed { index, chunk ->
                val floatEmbedding = embeddingEngine.generateEmbedding(chunk)
                val byteBuffer = java.nio.ByteBuffer
                    .allocate(floatEmbedding.size * 4)
                    .order(java.nio.ByteOrder.LITTLE_ENDIAN)
                floatEmbedding.forEach { byteBuffer.putFloat(it) }
                VectorEntry(
                    noteId = note.id,
                    chunkIndex = index,
                    chunkText = chunk,
                    embedding = byteBuffer.array()
                )
            }
            dao.updateNoteAndEmbeddings(note, vectorEntries)
        }
    }

    fun deleteNote(noteId: String) {
        viewModelScope.launch {
            dao.deleteNote(noteId)
        }
    }

    fun getNoteById(noteId: String): Note? {
        return allNotes.value.find { it.id == noteId }
    }
}

class NotesViewModelFactory(
    private val dao: NoteDao,
    private val embeddingEngine: EmbeddingEngine
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NotesViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return NotesViewModel(dao, embeddingEngine) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
