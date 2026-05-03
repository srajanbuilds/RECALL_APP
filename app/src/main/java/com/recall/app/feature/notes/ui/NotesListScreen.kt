package com.recall.app.feature.notes.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.recall.app.feature.notes.NotesViewModel
import com.recall.app.core.data.model.Note
import com.recall.app.core.ui.theme.Accent
import com.recall.app.core.ui.theme.Border
import com.recall.app.core.ui.theme.Surface
import com.recall.app.core.ui.theme.TextMuted
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesListScreen(
    viewModel: NotesViewModel,
    onNavigateToEditor: () -> Unit,
    onNavigateToAskRecall: () -> Unit,
    onNavigateToReminders: () -> Unit
) {
    val notes by viewModel.allNotes.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var isSearching by remember { mutableStateOf(false) }

    val displayNotes = if (searchQuery.isNotBlank()) searchResults else notes

    LaunchedEffect(searchQuery) {
        if (searchQuery.isNotBlank()) {
            viewModel.search(searchQuery)
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToEditor,
                containerColor = Accent,
                contentColor = Color.White,
                shape = RoundedCornerShape(28.dp)
            ) {
                Icon(Icons.Filled.Add, contentDescription = "New Note")
            }
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.background,
                contentColor = TextMuted,
                tonalElevation = 0.dp
            ) {
                NavigationBarItem(
                    selected = true,
                    onClick = { },
                    icon = { Text("📄") },
                    label = { Text("Notes", fontSize = 12.sp) }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = onNavigateToReminders,
                    icon = { Text("⏰") },
                    label = { Text("Reminders", fontSize = 12.sp) }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = onNavigateToAskRecall,
                    icon = { Text("✨") },
                    label = { Text("Ask Recall", fontSize = 12.sp) }
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Header row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (!isSearching) {
                    Text(
                        "Recall",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 2.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                if (!isSearching) {
                    IconButton(onClick = { isSearching = true }) {
                        Icon(Icons.Filled.Search, contentDescription = "Search", tint = TextMuted)
                    }
                }
            }

            // Live search bar
            AnimatedVisibility(visible = isSearching) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search notes...", color = TextMuted, fontSize = 15.sp) },
                        textStyle = TextStyle(fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurface),
                        leadingIcon = {
                            Icon(Icons.Filled.Search, contentDescription = null, tint = TextMuted)
                        },
                        trailingIcon = {
                            if (searchQuery.isNotBlank()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Filled.Close, contentDescription = "Clear", tint = TextMuted)
                                }
                            }
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = { viewModel.search(searchQuery) }),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Surface,
                            unfocusedContainerColor = Surface,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(onClick = {
                        isSearching = false
                        searchQuery = ""
                    }) {
                        Text("Cancel", color = Accent, fontSize = 15.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (searchQuery.isNotBlank()) {
                Text(
                    "${displayNotes.size} result${if (displayNotes.size != 1) "s" else ""}",
                    color = TextMuted, fontSize = 13.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            // Notes list or empty states
            if (displayNotes.isEmpty() && searchQuery.isBlank()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("📝", fontSize = 48.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("No notes yet", color = MaterialTheme.colorScheme.onSurface, fontSize = 18.sp, fontWeight = FontWeight.Medium)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("Tap + to create your first note", color = TextMuted, fontSize = 14.sp)
                    }
                }
            } else if (displayNotes.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🔍", fontSize = 40.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("No results for \"$searchQuery\"", color = TextMuted, fontSize = 15.sp)
                    }
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(displayNotes, key = { it.id }) { note ->
                        NoteCard(note = note, onClick = { onNavigateToEditor() })
                    }
                }
            }
        }
    }
}

fun formatTimestamp(timestamp: Long): String {
    val diff = System.currentTimeMillis() - timestamp
    return when {
        diff < 60_000 -> "Just now"
        diff < 3_600_000 -> "${diff / 60_000}m ago"
        diff < 86_400_000 -> "${diff / 3_600_000}h ago"
        diff < 604_800_000 -> SimpleDateFormat("EEE", Locale.getDefault()).format(Date(timestamp))
        else -> SimpleDateFormat("MMM d", Locale.getDefault()).format(Date(timestamp))
    }
}

@Composable
fun NoteCard(note: Note, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        color = Surface,
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Border)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = note.title.ifEmpty { "Untitled" },
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(formatTimestamp(note.updatedAt), fontSize = 12.sp, color = TextMuted)
            }
            if (note.body.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = note.body,
                    fontSize = 14.sp,
                    color = TextMuted,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 20.sp
                )
            }
        }
    }
}
