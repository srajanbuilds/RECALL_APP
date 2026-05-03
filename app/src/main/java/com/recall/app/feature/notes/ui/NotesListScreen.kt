package com.recall.app.feature.notes.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.recall.app.feature.notes.NotesViewModel
import com.recall.app.core.data.model.Note
import com.recall.app.core.ui.theme.Accent
import com.recall.app.core.ui.theme.Border
import com.recall.app.core.ui.theme.Surface
import com.recall.app.core.ui.theme.TextMuted

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesListScreen(
    viewModel: NotesViewModel,
    onNavigateToEditor: () -> Unit,
    onNavigateToAskRecall: () -> Unit
) {
    val notes by viewModel.allNotes.collectAsState()

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToEditor,
                containerColor = Accent,
                contentColor = MaterialTheme.colorScheme.onPrimary,
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
                    onClick = { },
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
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            Text("Recall", fontSize = 24.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 2.sp)
            Spacer(modifier = Modifier.height(16.dp))
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Surface, RoundedCornerShape(12.dp))
                    .padding(16.dp)
            ) {
                Text("🔍 Search notes...", color = TextMuted, fontSize = 15.sp)
            }
            
            Spacer(modifier = Modifier.height(24.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(notes) { note ->
                    NoteCard(note = note, onClick = { /* Navigate to edit existing */ })
                }
            }
        }
    }
}

@Composable
fun NoteCard(note: Note, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        color = Surface,
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Border)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = note.title.ifEmpty { "Untitled" },
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Just now", 
                    fontSize = 12.sp,
                    color = TextMuted
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = note.body.ifEmpty { "No content" },
                fontSize = 15.sp,
                color = TextMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
