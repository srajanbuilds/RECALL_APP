package com.recall.app.feature.notes.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.recall.app.core.common.DateDetector
import com.recall.app.core.ui.theme.*
import com.recall.app.feature.notes.NotesViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteEditorScreen(
    viewModel: NotesViewModel,
    noteId: String? = null,
    onNavigateBack: () -> Unit,
    onScheduleReminder: ((remId: String, triggerAtMs: Long, label: String) -> Unit)? = null
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val existingNote = remember(noteId) { noteId?.let { viewModel.getNoteById(it) } }

    var title by remember { mutableStateOf(existingNote?.title ?: "") }
    var content by remember { mutableStateOf(existingNote?.body ?: "") }
    var isPrivate by remember { mutableStateOf(existingNote?.isPrivate ?: false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var detectedDate by remember { mutableStateOf<DateDetector.DetectedDate?>(null) }

    val isEditing = existingNote != null

    // Auto-save: debounce 500ms after content change (spec requirement)
    LaunchedEffect(title, content) {
        if (title.isBlank() && content.isBlank()) return@LaunchedEffect
        delay(500)
        viewModel.saveNote(
            title = title.ifBlank { content.take(60) },
            content = content,
            noteId = noteId,
            isPrivate = isPrivate
        )
    }

    // Date detection: scan content for date mentions, debounced 800ms
    LaunchedEffect(content) {
        delay(800)
        if (content.length > 5) {
            val found = DateDetector.detect(content)
            if (found != null && found != detectedDate) {
                detectedDate = found
                val result = snackbarHostState.showSnackbar(
                    message = "Set a reminder for ${found.displayText}?",
                    actionLabel = "Set",
                    duration = SnackbarDuration.Long
                )
                if (result == SnackbarResult.ActionPerformed) {
                    val remId = UUID.randomUUID().toString()
                    val label = title.ifBlank { content.take(60) }
                    onScheduleReminder?.invoke(remId, found.triggerAtMs, label)
                    snackbarHostState.showSnackbar("Reminder set for ${found.displayText} ✓")
                }
            }
        }
    }

    // Delete confirmation dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete note?") },
            text = { Text("This note will be permanently deleted.", color = TextMuted) },
            confirmButton = {
                TextButton(onClick = {
                    noteId?.let { viewModel.deleteNote(it) }
                    showDeleteDialog = false
                    onNavigateBack()
                }) { Text("Delete", color = Destructive) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") }
            },
            containerColor = Surface
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    TextButton(onClick = onNavigateBack) {
                        Text("← Back", color = MaterialTheme.colorScheme.onSurface, fontSize = 16.sp)
                    }
                },
                actions = {
                    if (isEditing) {
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(Icons.Filled.Delete, contentDescription = "Delete note", tint = TextMuted)
                        }
                    }
                    // Private toggle
                    TextButton(onClick = {
                        isPrivate = !isPrivate
                        scope.launch {
                            viewModel.saveNote(
                                title = title.ifBlank { content.take(60) },
                                content = content,
                                noteId = noteId,
                                isPrivate = isPrivate
                            )
                        }
                    }) {
                        Text(
                            if (isPrivate) "🔒" else "🔓",
                            fontSize = 18.sp
                        )
                    }
                    Text(
                        if (isPrivate) "Private" else if (isEditing) "Saved" else "Draft",
                        color = TextMuted,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(end = 12.dp)
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        bottomBar = {
            // Formatting toolbar
            Surface(
                color = Surface,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Border)
            ) {
                Row(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                        Text("B", color = TextMuted, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Text("I", color = TextMuted, style = TextStyle(fontStyle = FontStyle.Italic), fontSize = 15.sp)
                        Text("U", color = TextMuted, fontSize = 15.sp)
                        Text("</>", color = TextMuted, fontSize = 12.sp)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                        Text("🏷️", fontSize = 16.sp)
                        Text("🎙️", fontSize = 16.sp)
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp)
        ) {
            // Title
            TextField(
                value = title,
                onValueChange = { if (it.length <= 200) title = it },
                placeholder = { Text("Title", fontSize = 24.sp, fontWeight = FontWeight.SemiBold, color = TextMuted) },
                textStyle = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Body
            TextField(
                value = content,
                onValueChange = { if (it.length <= 50_000) content = it },
                placeholder = { Text("Start typing...", fontSize = 16.sp, color = TextMuted) },
                textStyle = TextStyle(fontSize = 16.sp, lineHeight = 26.sp, color = MaterialTheme.colorScheme.onSurface),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
