package com.recall.app.feature.reminders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.recall.app.core.data.local.NoteDao
import com.recall.app.core.data.model.Reminder
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class RemindersViewModel @Inject constructor(private val dao: NoteDao) : ViewModel() {

    val reminders: StateFlow<List<Reminder>> = dao.getAllReminders()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun addReminder(label: String, triggerAtMs: Long, noteId: String = "standalone") {
        viewModelScope.launch {
            dao.insertReminder(
                Reminder(
                    id = UUID.randomUUID().toString(),
                    noteId = noteId,
                    label = label,
                    triggerAt = triggerAtMs
                )
            )
        }
    }

    fun deleteReminder(reminder: Reminder) {
        viewModelScope.launch { dao.deleteReminder(reminder) }
    }
}
