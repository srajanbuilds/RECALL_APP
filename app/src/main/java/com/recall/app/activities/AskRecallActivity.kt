package com.recall.app.activities

import android.os.Bundle
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.appbar.MaterialToolbar
import com.recall.app.R
import com.recall.app.core.ai.AiRepository
import com.recall.app.room.NotallyDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AskRecallActivity : AppCompatActivity() {

    private lateinit var chatHistoryTextView: TextView
    private lateinit var promptEditText: EditText
    private lateinit var sendButton: ImageButton
    
    private var chatHistory = StringBuilder()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ask_recall)

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Ask Recall"

        com.recall.app.core.ai.ModelDownloader.downloadModelsIfMissing(this)

        chatHistoryTextView = findViewById(R.id.chatHistoryTextView)
        promptEditText = findViewById(R.id.promptEditText)
        sendButton = findViewById(R.id.sendButton)

        sendButton.setOnClickListener {
            val query = promptEditText.text.toString().trim()
            if (query.isNotEmpty()) {
                handleQuery(query)
                promptEditText.text.clear()
            }
        }
    }

    private fun handleQuery(query: String) {
        appendMessage("You", query)
        
        lifecycleScope.launch {
            val responseBuilder = java.lang.StringBuilder()
            chatHistory.append("\nRecall: ")
            
            withContext(Dispatchers.IO) {
                // 1. Fetch notes from DB
                val db = NotallyDatabase.getDatabase(application)
                val allNotes = db.getBaseNoteDao().getAllNotes()
                
                // 2. Perform Vector Search
                val relevantNotes = AiRepository.searchNotes(this@AskRecallActivity, query, allNotes)
                
                // 3. Build Prompt with Context
                val contextString = relevantNotes.joinToString("\n---\n") { 
                    "Title: ${it.title}\nBody: ${it.body}" 
                }
                
                val llmPrompt = """
                    You are Recall, a helpful AI assistant that answers questions based strictly on the user's notes below.
                    
                    User's Notes:
                    $contextString
                    
                    User's Question: $query
                    
                    Answer:
                """.trimIndent()

                // 4. Generate Response using MediaPipe
                try {
                    val engine = AiRepository.getLlamaEngine(this@AskRecallActivity)
                    engine.generateResponseAsync(llmPrompt).collect { partial ->
                        withContext(Dispatchers.Main) {
                            chatHistory.append(partial)
                            chatHistoryTextView.text = chatHistory.toString()
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        chatHistory.append("Error connecting to LLM: ${e.message}")
                        chatHistoryTextView.text = chatHistory.toString()
                    }
                }
            }
        }
    }

    private fun appendMessage(sender: String, message: String) {
        chatHistory.append("\n$sender: $message\n")
        chatHistoryTextView.text = chatHistory.toString()
    }
    
    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
