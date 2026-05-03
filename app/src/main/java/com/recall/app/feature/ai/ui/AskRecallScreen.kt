package com.recall.app.feature.ai.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
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
import com.recall.app.core.ui.theme.Accent
import com.recall.app.core.ui.theme.Border
import com.recall.app.core.ui.theme.Surface
import com.recall.app.core.ui.theme.TextMuted
import com.recall.app.feature.notes.NotesViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val text: String,
    val isUser: Boolean
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AskRecallScreen(
    viewModel: NotesViewModel,
    onBack: () -> Unit
) {
    var inputText by remember { mutableStateOf("") }
    var isThinking by remember { mutableStateOf(false) }
    val messages = remember {
        mutableStateListOf(
            ChatMessage(
                text = "Hi! I'm Recall AI. Ask me anything about your notes — I'll search them for you instantly. Everything stays private on your device. 🔒",
                isUser = false
            )
        )
    }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    fun sendMessage() {
        val query = inputText.trim()
        if (query.isBlank() || isThinking) return
        inputText = ""
        messages.add(ChatMessage(text = query, isUser = true))
        isThinking = true

        scope.launch {
            listState.animateScrollToItem(messages.size - 1)

            // Real FTS4 search through user notes
            val relevantNotes = viewModel.searchForContext(query)

            delay(600) // brief pause to feel natural

            val response = if (relevantNotes.isEmpty()) {
                "I couldn't find anything in your notes about \"$query\". Try writing a note about it first!"
            } else {
                val sb = StringBuilder()
                sb.append("I found ${relevantNotes.size} relevant note${if (relevantNotes.size > 1) "s" else ""}:\n\n")
                relevantNotes.take(3).forEachIndexed { i, note ->
                    sb.append("📄 **${note.title.ifEmpty { "Untitled" }}**\n")
                    val preview = note.body.take(200).trimEnd()
                    sb.append("${if (note.body.length > 200) "$preview…" else preview}\n\n")
                }
                if (relevantNotes.size > 3) {
                    sb.append("…and ${relevantNotes.size - 3} more note${if (relevantNotes.size - 3 > 1) "s" else ""}.")
                }
                sb.toString().trimEnd()
            }

            isThinking = false
            messages.add(ChatMessage(text = response, isUser = false))
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Accent),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("✨", fontSize = 16.sp)
                        }
                        Column {
                            Text(
                                "Ask Recall",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text("On-device • Private", fontSize = 11.sp, color = TextMuted)
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
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
                        keyboardActions = KeyboardActions(onSend = { sendMessage() }),
                        maxLines = 4
                    )
                    IconButton(
                        onClick = { sendMessage() },
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(if (inputText.isNotBlank() && !isThinking) Accent else Surface),
                        enabled = inputText.isNotBlank() && !isThinking
                    ) {
                        Icon(
                            Icons.Filled.Send,
                            contentDescription = "Send",
                            tint = if (inputText.isNotBlank() && !isThinking) Color.White else TextMuted
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            items(messages, key = { it.id }) { message ->
                ChatBubble(message = message)
            }
            if (isThinking) {
                item(key = "thinking") {
                    ThinkingBubble()
                }
            }
        }
    }
}

@Composable
fun ChatBubble(message: ChatMessage) {
    val bgColor = if (message.isUser) Accent else Surface
    val textColor = if (message.isUser) Color.White else MaterialTheme.colorScheme.onSurface
    val shape = if (message.isUser)
        RoundedCornerShape(20.dp, 20.dp, 4.dp, 20.dp)
    else
        RoundedCornerShape(20.dp, 20.dp, 20.dp, 4.dp)
    val alignment = if (message.isUser) Alignment.End else Alignment.Start

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = alignment
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 300.dp)
                .clip(shape)
                .background(bgColor)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Text(message.text, color = textColor, fontSize = 15.sp, lineHeight = 22.sp)
        }
    }
}

@Composable
fun ThinkingBubble() {
    var dotCount by remember { mutableStateOf(1) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(400)
            dotCount = (dotCount % 3) + 1
        }
    }
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp, 20.dp, 20.dp, 4.dp))
                .background(Surface)
                .padding(horizontal = 20.dp, vertical = 14.dp)
        ) {
            Text("●".repeat(dotCount), color = TextMuted, fontSize = 14.sp, letterSpacing = 4.sp)
        }
    }
}
