package com.recall.app

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
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
import com.recall.app.feature.reminders.RemindersScreen
import com.recall.app.feature.reminders.RemindersViewModel
import com.recall.app.feature.reminders.RemindersViewModelFactory
import com.recall.app.feature.reminders.cancelReminder
import com.recall.app.feature.reminders.scheduleReminder

class MainActivity : ComponentActivity() {

    private val db by lazy {
        Room.databaseBuilder(applicationContext, AppDatabase::class.java, "recall-db")
            .fallbackToDestructiveMigration().build()
    }
    private val embeddingEngine by lazy { EmbeddingEngine(applicationContext) }
    private val modelPrefs by lazy { ModelPrefs(applicationContext) }

    private val notesViewModel: NotesViewModel by viewModels {
        NotesViewModelFactory(db.noteDao(), embeddingEngine)
    }
    private val remindersViewModel: RemindersViewModel by viewModels {
        RemindersViewModelFactory(db.noteDao())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Request notification permission on Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 1001)
        }

        setContent {
            RecallTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    RecallApp(notesViewModel, remindersViewModel, modelPrefs)
                }
            }
        }
    }
}

@Composable
fun RecallApp(
    notesViewModel: NotesViewModel,
    remindersViewModel: RemindersViewModel,
    modelPrefs: ModelPrefs
) {
    val navController = rememberNavController()
    val context = LocalContext.current
    val reminders by remindersViewModel.reminders.collectAsState()
    val startDest = if (!modelPrefs.isOnboardingComplete) "onboarding" else "notes_list"

    NavHost(navController = navController, startDestination = startDest) {

        composable("onboarding") {
            OnboardingScreen(onComplete = {
                modelPrefs.isOnboardingComplete = true
                navController.navigate("notes_list") { popUpTo("onboarding") { inclusive = true } }
            })
        }

        composable("notes_list") {
            NotesListScreen(
                viewModel = notesViewModel,
                onNavigateToEditor = { navController.navigate("editor") },
                onNavigateToAskRecall = {
                    if (modelPrefs.isModelDownloaded) navController.navigate("ask_recall")
                    else navController.navigate("model_download")
                },
                onNavigateToReminders = { navController.navigate("reminders") }
            )
        }

        composable("editor") {
            NoteEditorScreen(
                viewModel = notesViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable("reminders") {
            RemindersScreen(
                reminders = reminders,
                onAddReminder = { label, triggerAtMs ->
                    remindersViewModel.addReminder(label, triggerAtMs)
                    scheduleReminder(context, java.util.UUID.randomUUID().toString(), triggerAtMs, label)
                },
                onDeleteReminder = { reminder ->
                    cancelReminder(context, reminder.id)
                    remindersViewModel.deleteReminder(reminder)
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable("model_download") {
            ModelDownloadScreen(
                onDownloadComplete = {
                    modelPrefs.isModelDownloaded = true
                    navController.navigate("ask_recall") { popUpTo("model_download") { inclusive = true } }
                },
                onCancel = { navController.popBackStack() }
            )
        }

        composable("ask_recall") {
            AskRecallScreen(onBack = { navController.popBackStack() })
        }
    }
}
