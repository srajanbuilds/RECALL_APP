package com.recall.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.recall.app.ui.*
import com.recall.app.ui.NotesViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.UUID

// ── Main Screen ─────────────────────────────────────────────────────────

/**
 * Screen for creating and editing notes.
 * Features auto-saving, natural language date detection for reminders,
 * and privacy toggling.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteEditorScreen(
    viewModel: NotesViewModel,
    noteId: String? = null,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val existingNote = remember(noteId) { noteId?.let { viewModel.getNoteById(it) } }

    // ── State ─────────────────────────────────────────────────────────

    var title by remember { mutableStateOf(existingNote?.title ?: "") }
    var content by remember { mutableStateOf(existingNote?.body ?: "") }
    var showDeleteDialog by remember { mutableStateOf(false) }

    val isEditing = existingNote != null

    // ── Effects ───────────────────────────────────────────────────────

    // Auto-save: debounce 500ms after content change (spec requirement)
    LaunchedEffect(title, content) {
        if (title.isBlank() && content.isBlank()) return@LaunchedEffect
        delay(500)
        viewModel.saveNote(
            title = title.ifBlank { content.take(60) },
            content = content,
            noteId = noteId
        )
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

    // ── UI Composition ────────────────────────────────────────────────

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
                    Text(
                        if (isEditing) "Saved" else "Draft",
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
