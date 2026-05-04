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
import androidx.compose.ui.Modifier
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
import com.recall.app.feature.settings.SettingsScreen
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
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
    val scope = rememberCoroutineScope()

    val onboardingDone by appPreferences.isOnboardingComplete.collectAsStateWithLifecycle(initialValue = null)
    if (onboardingDone == null) return  // wait for DataStore

    val startDest = if (onboardingDone == true) "notes_list" else "onboarding"

    val notesViewModel: NotesViewModel = hiltViewModel()
    val remindersViewModel: RemindersViewModel = hiltViewModel()
    val reminders by remindersViewModel.reminders.collectAsStateWithLifecycle()

    NavHost(navController = navController, startDestination = startDest) {

        composable("onboarding") {
            OnboardingScreen(onComplete = {
                scope.launch { appPreferences.setOnboardingComplete(true) }
                navController.navigate("notes_list") { popUpTo("onboarding") { inclusive = true } }
            })
        }

        composable("notes_list") {
            NotesListScreen(
                viewModel = notesViewModel,
                onNavigateToEditor = { navController.navigate("editor/new") },
                onNavigateToEditNote = { noteId -> navController.navigate("editor/$noteId") },
                onNavigateToAskRecall = { navController.navigate("ask_recall") },
                onNavigateToReminders = { navController.navigate("reminders") },
                onNavigateToSettings = { navController.navigate("settings") }
            )
        }

        composable("editor/{noteId}") { back ->
            val rawId = back.arguments?.getString("noteId") ?: "new"
            val ctx = navController.context
            NoteEditorScreen(
                viewModel = notesViewModel,
                noteId = if (rawId == "new") null else rawId,
                onNavigateBack = { navController.popBackStack() },
                onScheduleReminder = { remId, triggerAtMs, label ->
                    com.recall.app.feature.reminders.scheduleReminder(ctx, remId, triggerAtMs, label)
                }
            )
        }

        composable("reminders") {
            RemindersScreen(
                reminders = reminders,
                onAddReminder = { label, triggerAtMs ->
                    val id = java.util.UUID.randomUUID().toString()
                    remindersViewModel.addReminder(label, triggerAtMs)
                    scheduleReminder(navController.context, id, triggerAtMs, label)
                },
                onDeleteReminder = { reminder ->
                    cancelReminder(navController.context, reminder.id)
                    remindersViewModel.deleteReminder(reminder)
                },
                onBack = { navController.popBackStack() }
            )
        }

        // AI chat — AiViewModel injected inside AskRecallScreen via hiltViewModel()
        composable("ask_recall") {
            AskRecallScreen(onBack = { navController.popBackStack() })
        }

        composable("settings") {
            SettingsScreen(onBack = { navController.popBackStack() })
        }
    }
}
