package com.recall.app.data

import kotlinx.coroutines.flow.Flow
import java.util.UUID

class NotesRepository(private val dao: NoteDao) {
    fun getAllNotes(): Flow<List<Note>> = dao.getAllNotes()

    suspend fun getNoteById(id: String): Note? = dao.getNoteById(id)

    suspend fun saveNote(title: String, body: String, noteId: String? = null) {
        val existing = noteId?.let { dao.getNoteById(it) }
        val note = Note(
            id = existing?.id ?: UUID.randomUUID().toString(),
            title = title,
            body = body,
            createdAt = existing?.createdAt ?: System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        dao.insertNote(note)
    }

    suspend fun deleteNote(noteId: String) {
        dao.deleteNote(noteId)
    }

    suspend fun searchNotes(query: String): List<Note> {
        val notes = dao.getAllNotesOnce()
        if (query.isBlank()) return notes
        return notes.filter {
            it.title.contains(query, ignoreCase = true) || it.body.contains(query, ignoreCase = true)
        }
    }
}
