package com.recall.app.feature.notes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.recall.app.core.data.local.NoteDao
import com.recall.app.core.data.model.Note
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

import com.recall.app.core.ai.EmbeddingEngine
import com.recall.app.core.data.model.VectorEntry

class NotesViewModel(
    private val dao: NoteDao,
    private val embeddingEngine: EmbeddingEngine
) : ViewModel() {
    
    val allNotes: StateFlow<List<Note>> = dao.getAllNotes()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun saveNote(title: String, content: String, isPrivate: Boolean = false) {
        viewModelScope.launch {
            val note = Note(
                title = title,
                body = content,
                isPrivate = isPrivate,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            
            // Generate embeddings for RAG
            val chunks = embeddingEngine.chunkText(content)
            val vectorEntries = chunks.mapIndexed { index, chunk ->
                val floatEmbedding = embeddingEngine.generateEmbedding(chunk)
                val byteBuffer = java.nio.ByteBuffer.allocate(floatEmbedding.size * 4).order(java.nio.ByteOrder.LITTLE_ENDIAN)
                floatEmbedding.forEach { byteBuffer.putFloat(it) }
                
                VectorEntry(
                    noteId = note.id,
                    chunkIndex = index,
                    chunkText = chunk,
                    embedding = byteBuffer.array()
                )
            }
            
            // Transactional update
            dao.updateNoteAndEmbeddings(note, vectorEntries)
        }
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
