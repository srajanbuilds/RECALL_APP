package com.recall.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.recall.app.data.Note
import com.recall.app.data.NotesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class NotesViewModel(private val repository: NotesRepository) : ViewModel() {

    val allNotes: StateFlow<List<Note>> = repository.getAllNotes()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val _searchResults = MutableStateFlow<List<Note>>(emptyList())
    val searchResults: StateFlow<List<Note>> = _searchResults

    fun search(query: String) {
        viewModelScope.launch {
            _searchResults.value = repository.searchNotes(query)
        }
    }

    fun saveNote(title: String, content: String, noteId: String? = null) {
        viewModelScope.launch {
            repository.saveNote(title, content, noteId)
        }
    }

    fun deleteNote(noteId: String) {
        viewModelScope.launch {
            repository.deleteNote(noteId)
        }
    }

    fun getNoteById(noteId: String): Note? = allNotes.value.find { it.id == noteId }
}
