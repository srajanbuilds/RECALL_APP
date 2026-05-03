package com.recall.app

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.recall.app.core.prefs.AppPreferences
import com.recall.app.core.ui.theme.RecallTheme
import com.recall.app.feature.ai.ui.AskRecallScreen
import com.recall.app.feature.notes.NotesViewModel
import com.recall.app.feature.notes.ui.NoteEditorScreen
import com.recall.app.feature.notes.ui.NotesListScreen
import com.recall.app.feature.onboarding.OnboardingScreen
import com.recall.app.feature.reminders.RemindersScreen
import com.recall.app.feature.reminders.RemindersViewModel
import com.recall.app.feature.reminders.cancelReminder
import com.recall.app.feature.reminders.scheduleReminder
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var appPreferences: AppPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 1001)
        }

        setContent {
            RecallTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    RecallApp(appPreferences)
                }
            }
        }
    }
}

@Composable
fun RecallApp(appPreferences: AppPreferences) {
    val navController = rememberNavController()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Observe onboarding state from DataStore
    val onboardingDone by appPreferences.isOnboardingComplete.collectAsStateWithLifecycle(initialValue = null)

    // Wait until DataStore emits before deciding start dest
    if (onboardingDone == null) return   // Blank frame while DataStore loads

    val startDest = if (onboardingDone == true) "notes_list" else "onboarding"

    val notesViewModel: NotesViewModel = hiltViewModel()
    val remindersViewModel: RemindersViewModel = hiltViewModel()
    val reminders by remindersViewModel.reminders.collectAsStateWithLifecycle()

    NavHost(navController = navController, startDestination = startDest) {

        composable("onboarding") {
            OnboardingScreen(onComplete = {
                scope.launch { appPreferences.setOnboardingComplete(true) }
                navController.navigate("notes_list") {
                    popUpTo("onboarding") { inclusive = true }
                }
            })
        }

        composable("notes_list") {
            NotesListScreen(
                viewModel = notesViewModel,
                onNavigateToEditor = { navController.navigate("editor/new") },
                onNavigateToEditNote = { noteId -> navController.navigate("editor/$noteId") },
                onNavigateToAskRecall = { navController.navigate("ask_recall") },
                onNavigateToReminders = { navController.navigate("reminders") }
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

        composable("reminders") {
            RemindersScreen(
                reminders = reminders,
                onAddReminder = { label, triggerAtMs ->
                    val id = java.util.UUID.randomUUID().toString()
                    remindersViewModel.addReminder(label, triggerAtMs)
                    scheduleReminder(context, id, triggerAtMs, label)
                },
                onDeleteReminder = { reminder ->
                    cancelReminder(context, reminder.id)
                    remindersViewModel.deleteReminder(reminder)
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable("ask_recall") {
            AskRecallScreen(
                viewModel = notesViewModel,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
