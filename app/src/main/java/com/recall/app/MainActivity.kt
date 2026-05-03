package com.recall.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.room.Room
import com.recall.app.core.ai.EmbeddingEngine
import com.recall.app.core.data.local.AppDatabase
import com.recall.app.core.prefs.ModelPrefs
import com.recall.app.core.ui.theme.RecallTheme
import com.recall.app.feature.ai.ui.AskRecallScreen
import com.recall.app.feature.ai.ui.ModelDownloadScreen
import com.recall.app.feature.notes.NotesViewModel
import com.recall.app.feature.notes.NotesViewModelFactory
import com.recall.app.feature.notes.ui.NoteEditorScreen
import com.recall.app.feature.notes.ui.NotesListScreen
import com.recall.app.feature.onboarding.OnboardingScreen

class MainActivity : ComponentActivity() {

    private val db by lazy {
        Room.databaseBuilder(applicationContext, AppDatabase::class.java, "recall-db")
            .fallbackToDestructiveMigration().build()
    }

    private val embeddingEngine by lazy { EmbeddingEngine(applicationContext) }
    private val modelPrefs by lazy { ModelPrefs(applicationContext) }

    private val viewModel: NotesViewModel by viewModels {
        NotesViewModelFactory(db.noteDao(), embeddingEngine)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RecallTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    RecallApp(viewModel, modelPrefs)
                }
            }
        }
    }
}

@Composable
fun RecallApp(viewModel: NotesViewModel, modelPrefs: ModelPrefs) {
    val navController = rememberNavController()

    // Determine start destination
    val startDest = if (!modelPrefs.isOnboardingComplete) "onboarding" else "notes_list"

    NavHost(navController = navController, startDestination = startDest) {

        composable("onboarding") {
            OnboardingScreen(
                onComplete = {
                    modelPrefs.isOnboardingComplete = true
                    navController.navigate("notes_list") {
                        popUpTo("onboarding") { inclusive = true }
                    }
                }
            )
        }

        composable("notes_list") {
            NotesListScreen(
                viewModel = viewModel,
                onNavigateToEditor = { navController.navigate("editor") },
                onNavigateToAskRecall = {
                    // Gate: show download screen if model not ready
                    if (modelPrefs.isModelDownloaded) {
                        navController.navigate("ask_recall")
                    } else {
                        navController.navigate("model_download")
                    }
                }
            )
        }

        composable("editor") {
            NoteEditorScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable("model_download") {
            ModelDownloadScreen(
                onDownloadComplete = {
                    modelPrefs.isModelDownloaded = true
                    navController.navigate("ask_recall") {
                        popUpTo("model_download") { inclusive = true }
                    }
                },
                onCancel = { navController.popBackStack() }
            )
        }

        composable("ask_recall") {
            AskRecallScreen(onBack = { navController.popBackStack() })
        }
    }
}
