package com.recall.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.recall.app.data.AppDatabase
import com.recall.app.data.NotesRepository
import com.recall.app.ui.NoteEditorScreen
import com.recall.app.ui.NotesListScreen
import com.recall.app.ui.NotesViewModel
import com.recall.app.ui.NotesViewModelFactory
import com.recall.app.ui.RecallTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val db = AppDatabase.getInstance(this)
        val repository = NotesRepository(db.noteDao())
        val factory = NotesViewModelFactory(repository)

        setContent {
            RecallTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    val navController = rememberNavController()
                    val notesViewModel: NotesViewModel = viewModel(factory = factory)

                    NavHost(navController = navController, startDestination = "notes_list") {
                        composable("notes_list") {
                            NotesListScreen(
                                viewModel = notesViewModel,
                                onNavigateToEditor = { navController.navigate("editor/new") },
                                onNavigateToEditNote = { noteId -> navController.navigate("editor/\$noteId") }
                            )
                        }
                        composable("editor/{noteId}") { back ->
                            val rawId = back.arguments?.getString("noteId") ?: "new"
                            NoteEditorScreen(
                                viewModel = notesViewModel,
                                noteId = if (rawId == "new") null else rawId,
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }
}

