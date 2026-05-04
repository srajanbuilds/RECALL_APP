package com.recall.app.feature.ai.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.recall.app.core.ui.theme.Accent
import com.recall.app.core.ui.theme.Border
import com.recall.app.core.ui.theme.Surface
import com.recall.app.core.ui.theme.TextMuted
import com.recall.app.feature.ai.AiMessage
import com.recall.app.feature.ai.AiViewModel
import kotlinx.coroutines.delay
import java.util.UUID

// ── Main Screen ─────────────────────────────────────────────────────────

/**
 * The primary interface for interacting with the on-device AI.
 *
 * This screen provides a conversational chat UI where users can query their notes.
 * The chat history is ephemeral and tied to the ViewModel lifecycle.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AskRecallScreen(
    viewModel: AiViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    // ── State ─────────────────────────────────────────────────────────

    var inputText by remember { mutableStateOf("") }
    val isThinking by viewModel.isThinking.collectAsStateWithLifecycle()
    val vmMessages by viewModel.messages.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()

    // Convert ViewModel messages to chat bubble format
    val chatMessages = remember(vmMessages) {
        buildList {
            if (vmMessages.isEmpty()) add(
                ChatBubbleData(
                    text = "Hi! I'm Recall AI. Ask me anything about your notes. Everything stays on your device. 🔒",
                    isUser = false
                )
            )
            vmMessages.forEach { msg ->
                add(ChatBubbleData(text = msg.content, isUser = msg.role == "user"))
            }
        }
    }

    // ── Effects ───────────────────────────────────────────────────────

    LaunchedEffect(chatMessages.size) {
        if (chatMessages.isNotEmpty()) listState.animateScrollToItem(chatMessages.size - 1)
    }

    DisposableEffect(Unit) { onDispose { viewModel.clearSession() } }

    // ── UI Composition ────────────────────────────────────────────────

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier.size(32.dp).clip(CircleShape).background(Accent),
                            contentAlignment = Alignment.Center
                        ) { Text("✨", fontSize = 16.sp) }
                        Column {
                            Text("Ask Recall", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                            Text("On-device • Private • Ephemeral", fontSize = 11.sp, color = TextMuted)
                        }
                    }
                },
                navigationIcon = {
                    TextButton(onClick = onBack) {
                        Text("←", color = MaterialTheme.colorScheme.onSurface, fontSize = 20.sp)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        bottomBar = {
            Surface(
                color = MaterialTheme.colorScheme.background,
                border = androidx.compose.foundation.BorderStroke(1.dp, Border)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    TextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        placeholder = { Text("Ask about your notes...", color = TextMuted, fontSize = 15.sp) },
                        textStyle = TextStyle(fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurface),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Surface,
                            unfocusedContainerColor = Surface,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                        keyboardActions = KeyboardActions(onSend = {
                            if (inputText.isNotBlank()) { viewModel.sendMessage(inputText); inputText = "" }
                        }),
                        maxLines = 4
                    )
                    IconButton(
                        onClick = { if (inputText.isNotBlank()) { viewModel.sendMessage(inputText); inputText = "" } },
                        modifier = Modifier.size(48.dp).clip(CircleShape)
                            .background(if (inputText.isNotBlank() && !isThinking) Accent else Surface),
                        enabled = inputText.isNotBlank() && !isThinking
                    ) {
                        Icon(
                            Icons.Filled.Send, "Send",
                            tint = if (inputText.isNotBlank() && !isThinking) Color.White else TextMuted
                        )
                    }
                }
            }
        }
    ) { padding ->
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            items(chatMessages, key = { it.id }) { msg -> ChatBubble(msg) }
            if (isThinking) item(key = "thinking") { ThinkingBubble() }
        }
    }
}

// ── UI Components ───────────────────────────────────────────────────────

data class ChatBubbleData(val id: String = UUID.randomUUID().toString(), val text: String, val isUser: Boolean)

@Composable
fun ChatBubble(message: ChatBubbleData) {
    val bgColor = if (message.isUser) Accent else Surface
    val shape = if (message.isUser) RoundedCornerShape(20.dp, 20.dp, 4.dp, 20.dp)
                else RoundedCornerShape(20.dp, 20.dp, 20.dp, 4.dp)
    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = if (message.isUser) Alignment.End else Alignment.Start) {
        Box(modifier = Modifier.widthIn(max = 300.dp).clip(shape).background(bgColor).padding(16.dp, 12.dp)) {
            Text(message.text, color = if (message.isUser) Color.White else MaterialTheme.colorScheme.onSurface, fontSize = 15.sp, lineHeight = 22.sp)
        }
    }
}

@Composable
fun ThinkingBubble() {
    var dotCount by remember { mutableStateOf(1) }
    LaunchedEffect(Unit) { while (true) { delay(400); dotCount = (dotCount % 3) + 1 } }
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
        Box(modifier = Modifier.clip(RoundedCornerShape(20.dp, 20.dp, 20.dp, 4.dp)).background(Surface).padding(20.dp, 14.dp)) {
            Text("●".repeat(dotCount), color = TextMuted, fontSize = 14.sp, letterSpacing = 4.sp)
        }
    }
}
