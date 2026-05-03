package com.recall.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.room.Room
import com.recall.app.core.data.local.AppDatabase
import com.recall.app.core.ui.theme.RecallTheme
import com.recall.app.feature.notes.NotesViewModel
import com.recall.app.feature.notes.NotesViewModelFactory
import com.recall.app.feature.notes.ui.NoteEditorScreen
import com.recall.app.feature.notes.ui.NotesListScreen

class MainActivity : ComponentActivity() {

    private val db by lazy {
        Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java, "recall-db"
        ).fallbackToDestructiveMigration().build()
    }

    private val viewModel: NotesViewModel by viewModels {
        NotesViewModelFactory(db.noteDao())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RecallTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    RecallAppNavigation(viewModel)
                }
            }
        }
    }
}

@Composable
fun RecallAppNavigation(viewModel: NotesViewModel) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "notes_list") {
        composable("notes_list") {
            NotesListScreen(
                viewModel = viewModel,
                onNavigateToEditor = { navController.navigate("editor") },
                onNavigateToAskRecall = { navController.navigate("model_download") }
            )
        }
        composable("editor") {
            NoteEditorScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable("model_download") {
            com.recall.app.feature.ai.ui.ModelDownloadScreen(
                onDownloadComplete = { /* Phase 3: Transition to AI Chat */ },
                onCancel = { navController.popBackStack() }
            )
        }
    }
}
