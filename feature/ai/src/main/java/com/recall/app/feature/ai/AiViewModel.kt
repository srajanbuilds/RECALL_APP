package com.recall.app.feature.ai

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.recall.app.core.data.local.NoteDao
import com.recall.app.core.data.model.Note
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AiMessage(
    val role: String,   // "user" | "assistant"
    val content: String,
    val citations: List<String> = emptyList()
)

@HiltViewModel
class AiViewModel @Inject constructor(
    application: Application,
    private val dao: NoteDao
) : AndroidViewModel(application) {

    private val _messages = MutableStateFlow<List<AiMessage>>(emptyList())
    val messages: StateFlow<List<AiMessage>> = _messages

    private val _isThinking = MutableStateFlow(false)
    val isThinking: StateFlow<Boolean> = _isThinking

    /** Retrieve up to 5 non-private notes matching the query for RAG context. */
    suspend fun searchForContext(query: String): List<Note> =
        try { dao.searchNotes("$query*").filter { !it.isPrivate }.take(5) }
        catch (e: Exception) { emptyList() }

    fun sendMessage(userText: String) {
        if (userText.isBlank()) return
        viewModelScope.launch {
            // Add user message
            _messages.value = _messages.value + AiMessage("user", userText)
            _isThinking.value = true

            // Retrieve context
            val context = searchForContext(userText)
            val response = buildResponse(userText, context)
            _messages.value = _messages.value + response
            _isThinking.value = false
        }
    }

    private fun buildResponse(query: String, contextNotes: List<Note>): AiMessage {
        if (contextNotes.isEmpty()) {
            return AiMessage(
                role = "assistant",
                content = "I couldn't find anything about that in your notes."
            )
        }
        // Build a structured response citing each note
        val sb = StringBuilder()
        val citations = mutableListOf<String>()
        contextNotes.forEach { note ->
            val excerpt = note.body.take(200).trimEnd()
            sb.appendLine("From **${note.title}**: $excerpt…")
            citations.add(note.title)
        }
        return AiMessage(
            role = "assistant",
            content = sb.toString().trim(),
            citations = citations
        )
    }

    fun clearSession() { _messages.value = emptyList() }
}
