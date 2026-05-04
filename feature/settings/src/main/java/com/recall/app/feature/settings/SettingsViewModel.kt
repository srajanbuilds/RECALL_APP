package com.recall.app.feature.settings

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.recall.app.core.data.local.NoteDao
import com.recall.app.core.prefs.AppPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val dao: NoteDao,
    private val prefs: AppPreferences
) : ViewModel() {

    var biometricEnabled by mutableStateOf(false)
        private set

    init {
        viewModelScope.launch {
            prefs.isBiometricEnabled.collect { biometricEnabled = it }
        }
    }

    fun toggleBiometric(enabled: Boolean) {
        viewModelScope.launch {
            prefs.setBiometricEnabled(enabled)
            biometricEnabled = enabled
        }
    }

    /** Export all non-private notes + reminders to an encrypted file. */
    suspend fun exportNotes(context: Context, uri: Uri, passphrase: String): String {
        return try {
            val notes = dao.getAllNotesOnce()
                .filter { !it.isPrivate }
                .map {
                    ExportManager.ExportNote(
                        id = it.id, title = it.title, body = it.body,
                        createdAt = it.createdAt, updatedAt = it.updatedAt,
                        isPrivate = false, isPinned = it.isPinned,
                        isArchived = it.isArchived, tags = it.tags, colorHex = it.colorHex
                    )
                }
            val reminders = dao.getAllRemindersOnce().map {
                ExportManager.ExportReminder(
                    id = it.id, noteId = it.noteId, label = it.label,
                    triggerAt = it.triggerAt, isCompleted = it.isCompleted
                )
            }
            val payload = ExportManager.ExportPayload(
                exportedAt = System.currentTimeMillis(),
                notes = notes,
                reminders = reminders
            )
            when (ExportManager.export(context, uri, passphrase, payload)) {
                is ExportManager.ExportResult.Success ->
                    "Export successful — ${notes.size} note${if (notes.size != 1) "s" else ""} saved"
                is ExportManager.ExportResult.Error ->
                    "Export failed. Please try again."
            }
        } catch (e: Exception) {
            "Export failed: ${e.message}"
        }
    }

    /** Decrypt and import notes + reminders from a backup file. */
    suspend fun importNotes(context: Context, uri: Uri, passphrase: String): String {
        return when (val result = ExportManager.import(context, uri, passphrase)) {
            is ExportManager.ImportResult.Success -> {
                try {
                    result.payload.notes.forEach { n ->
                        dao.insertNote(
                            com.recall.app.core.data.model.Note(
                                id = n.id, title = n.title, body = n.body,
                                createdAt = n.createdAt, updatedAt = n.updatedAt,
                                isPrivate = n.isPrivate, isPinned = n.isPinned,
                                isArchived = n.isArchived, tags = n.tags, colorHex = n.colorHex
                            )
                        )
                    }
                    "Imported ${result.payload.notes.size} note${if (result.payload.notes.size != 1) "s" else ""} successfully"
                } catch (e: Exception) {
                    "Import failed during write: ${e.message}"
                }
            }
            is ExportManager.ImportResult.WrongPassphrase ->
                "Wrong passphrase. No data was changed."
            is ExportManager.ImportResult.CorruptFile ->
                "This file appears damaged. Export a fresh backup."
            is ExportManager.ImportResult.Error ->
                "Import failed: ${result.message}"
        }
    }
}
