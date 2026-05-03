package com.recall.app.feature.reminders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.recall.app.core.data.local.NoteDao
import com.recall.app.core.data.model.Reminder
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

class RemindersViewModel(private val dao: NoteDao) : ViewModel() {

    val reminders: StateFlow<List<Reminder>> = dao.getAllReminders()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun addReminder(label: String, triggerAtMs: Long, noteId: String = "standalone") {
        viewModelScope.launch {
            val reminder = Reminder(
                id = UUID.randomUUID().toString(),
                noteId = noteId,
                label = label,
                triggerAt = triggerAtMs
            )
            dao.insertReminder(reminder)
        }
    }

    fun deleteReminder(reminder: Reminder) {
        viewModelScope.launch {
            dao.deleteReminder(reminder)
        }
    }
}

class RemindersViewModelFactory(private val dao: NoteDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RemindersViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return RemindersViewModel(dao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
