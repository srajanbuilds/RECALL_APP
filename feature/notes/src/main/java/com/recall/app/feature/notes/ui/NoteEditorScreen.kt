package com.recall.app.feature.notes.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.recall.app.feature.notes.NotesViewModel
import com.recall.app.core.ui.theme.Border
import com.recall.app.core.ui.theme.Surface
import com.recall.app.core.ui.theme.TextMuted

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteEditorScreen(
    viewModel: NotesViewModel,
    noteId: String? = null,
    onNavigateBack: () -> Unit
) {
    // Pre-fill fields if editing an existing note
    val existingNote = remember(noteId) { noteId?.let { viewModel.getNoteById(it) } }

    var title by remember { mutableStateOf(existingNote?.title ?: "") }
    var content by remember { mutableStateOf(existingNote?.body ?: "") }
    var showDeleteDialog by remember { mutableStateOf(false) }

    val isEditing = existingNote != null

    // Delete confirmation dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete note?") },
            text = { Text("This note will be permanently deleted and cannot be recovered.", color = TextMuted) },
            confirmButton = {
                TextButton(
                    onClick = {
                        noteId?.let { viewModel.deleteNote(it) }
                        showDeleteDialog = false
                        onNavigateBack()
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            },
            containerColor = Surface
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    TextButton(onClick = {
                        if (title.isNotBlank() || content.isNotBlank()) {
                            viewModel.saveNote(
                                title = title,
                                content = content,
                                noteId = noteId
                            )
                        }
                        onNavigateBack()
                    }) {
                        Text("← Back", color = MaterialTheme.colorScheme.onSurface, fontSize = 16.sp)
                    }
                },
                actions = {
                    if (isEditing) {
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(
                                Icons.Filled.Delete,
                                contentDescription = "Delete note",
                                tint = TextMuted
                            )
                        }
                    }
                    Text(
                        if (isEditing) "Editing" else "Draft",
                        color = TextMuted,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(end = 16.dp)
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        bottomBar = {
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
                        Text("B", color = TextMuted, fontWeight = FontWeight.Bold)
                        Text("I", color = TextMuted, style = TextStyle(fontStyle = androidx.compose.ui.text.font.FontStyle.Italic))
                        Text("U", color = TextMuted)
                        Text("</>", color = TextMuted)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                        Text("🏷️")
                        Text("🎙️")
                        Text(if (existingNote?.isPrivate == true) "🔒" else "🔓")
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
            TextField(
                value = title,
                onValueChange = { title = it },
                placeholder = {
                    Text("Title", fontSize = 24.sp, fontWeight = FontWeight.SemiBold, color = TextMuted)
                },
                textStyle = TextStyle(
                    fontSize = 24.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                ),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                modifier = Modifier.fillMaxWidth()
            )

            TextField(
                value = content,
                onValueChange = { content = it },
                placeholder = { Text("Start typing...", fontSize = 16.sp, color = TextMuted) },
                textStyle = TextStyle(
                    fontSize = 16.sp,
                    lineHeight = 24.sp,
                    color = MaterialTheme.colorScheme.onSurface
                ),
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
