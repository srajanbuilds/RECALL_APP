package com.recall.app.feature.settings

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.recall.app.core.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var showExportDialog by remember { mutableStateOf(false) }
    var showImportDialog by remember { mutableStateOf(false) }
    var exportPassphrase by remember { mutableStateOf("") }
    var importPassphrase by remember { mutableStateOf("") }
    var pendingImportUri by remember { mutableStateOf<Uri?>(null) }

    // SAF launchers
    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/octet-stream")
    ) { uri ->
        uri?.let { dest ->
            scope.launch {
                val result = viewModel.exportNotes(context, dest, exportPassphrase)
                snackbarHostState.showSnackbar(result)
                exportPassphrase = ""
            }
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            pendingImportUri = it
            showImportDialog = true
        }
    }

    // Export passphrase dialog
    if (showExportDialog) {
        AlertDialog(
            onDismissRequest = { showExportDialog = false; exportPassphrase = "" },
            containerColor = Surface,
            title = { Text("Set export passphrase", fontWeight = FontWeight.SemiBold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Choose a strong passphrase. You'll need it to import this file.", color = TextMuted, fontSize = 13.sp)
                    OutlinedTextField(
                        value = exportPassphrase,
                        onValueChange = { exportPassphrase = it },
                        label = { Text("Passphrase") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (exportPassphrase.length >= 8) {
                        showExportDialog = false
                        exportLauncher.launch("recall_backup_${System.currentTimeMillis()}.rcl")
                    }
                }, enabled = exportPassphrase.length >= 8) { Text("Export") }
            },
            dismissButton = {
                TextButton(onClick = { showExportDialog = false; exportPassphrase = "" }) { Text("Cancel") }
            }
        )
    }

    // Import passphrase dialog
    if (showImportDialog) {
        AlertDialog(
            onDismissRequest = { showImportDialog = false; importPassphrase = ""; pendingImportUri = null },
            containerColor = Surface,
            title = { Text("Enter passphrase", fontWeight = FontWeight.SemiBold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Enter the passphrase used when this backup was created.", color = TextMuted, fontSize = 13.sp)
                    OutlinedTextField(
                        value = importPassphrase,
                        onValueChange = { importPassphrase = it },
                        label = { Text("Passphrase") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val uri = pendingImportUri ?: return@TextButton
                    showImportDialog = false
                    scope.launch {
                        val result = viewModel.importNotes(context, uri, importPassphrase)
                        snackbarHostState.showSnackbar(result)
                        importPassphrase = ""
                        pendingImportUri = null
                    }
                }, enabled = importPassphrase.isNotBlank()) { Text("Import") }
            },
            dismissButton = {
                TextButton(onClick = { showImportDialog = false; importPassphrase = ""; pendingImportUri = null }) { Text("Cancel") }
            }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Settings", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Background)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            // ── Security ──────────────────────────────────────────────
            SettingsSectionHeader("Security")

            SettingsCard {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Biometric Lock", fontWeight = FontWeight.Medium, fontSize = 15.sp)
                        Text("Locks after 30s in background", color = TextMuted, fontSize = 12.sp)
                    }
                    Switch(
                        checked = viewModel.biometricEnabled,
                        onCheckedChange = { viewModel.toggleBiometric(it) },
                        colors = SwitchDefaults.colors(checkedThumbColor = Accent, checkedTrackColor = Accent.copy(alpha = 0.4f))
                    )
                }
            }

            // ── Backup ────────────────────────────────────────────────
            SettingsSectionHeader("Backup & Restore")

            SettingsCard {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Encrypted Local Backup", fontWeight = FontWeight.Medium, fontSize = 15.sp)
                    Text("AES-256-GCM · Protected with your passphrase · Notes, reminders · Private notes excluded", color = TextMuted, fontSize = 12.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedButton(
                            onClick = { showExportDialog = true },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Border)
                        ) { Text("Export", color = MaterialTheme.colorScheme.onSurface) }

                        Button(
                            onClick = { importLauncher.launch(arrayOf("application/octet-stream", "*/*")) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Accent)
                        ) { Text("Import") }
                    }
                }
            }

            // ── AI Model ──────────────────────────────────────────────
            SettingsSectionHeader("AI Engine")

            SettingsCard {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Embedding Model", fontWeight = FontWeight.Medium, fontSize = 15.sp)
                    Text("all-MiniLM-L6-v2 · ~22 MB · Bundled", color = TextMuted, fontSize = 12.sp)
                    Spacer(Modifier.height(8.dp))
                    Text("LLM", fontWeight = FontWeight.Medium, fontSize = 15.sp)
                    Text("Gemma 2B / TinyLlama 1.1B / Qwen2 0.5B · Selected at runtime by RAM", color = TextMuted, fontSize = 12.sp)
                }
            }

            // ── About ─────────────────────────────────────────────────
            SettingsSectionHeader("About")

            SettingsCard {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Recall", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                    Text("Version 1.0", color = TextMuted, fontSize = 13.sp)
                    Text("100% on-device · 100% private · 100% free", color = TextMuted, fontSize = 12.sp)
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun SettingsSectionHeader(text: String) {
    Text(
        text.uppercase(),
        fontSize = 11.sp,
        fontWeight = FontWeight.SemiBold,
        color = TextMuted,
        letterSpacing = 1.2.sp,
        modifier = Modifier.padding(start = 4.dp, top = 16.dp, bottom = 4.dp)
    )
}

@Composable
private fun SettingsCard(content: @Composable () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Surface,
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Border)
    ) { content() }
}
