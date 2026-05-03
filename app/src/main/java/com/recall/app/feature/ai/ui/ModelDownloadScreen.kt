package com.recall.app.feature.ai.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.recall.app.core.ui.theme.Accent
import com.recall.app.core.ui.theme.Border
import com.recall.app.core.ui.theme.Surface
import com.recall.app.core.ui.theme.TextMuted

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModelDownloadScreen(
    onDownloadComplete: () -> Unit,
    onCancel: () -> Unit
) {
    // In a real implementation, this would be tied to a DownloadManager Flow
    var progress by remember { mutableStateOf(0f) }
    var isDownloading by remember { mutableStateOf(false) }

    // Mock download progress for UI demonstration
    LaunchedEffect(isDownloading) {
        if (isDownloading) {
            while (progress < 1f) {
                kotlinx.coroutines.delay(100)
                progress += 0.02f
            }
            onDownloadComplete()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    TextButton(onClick = onCancel) {
                        Text("← Back", color = MaterialTheme.colorScheme.onSurface, fontSize = 16.sp)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "✨",
                fontSize = 48.sp,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            Text(
                text = "Recall AI Brain",
                fontSize = 24.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            Text(
                text = "To keep your notes 100% private, Recall runs a powerful AI model directly on your device. It requires a one-time download.",
                fontSize = 15.sp,
                color = TextMuted,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            Surface(
                color = Surface,
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Border),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Model size", color = TextMuted, fontSize = 14.sp)
                        Text("~1.5 GB", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Medium, fontSize = 14.sp)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Network", color = TextMuted, fontSize = 14.sp)
                        Text("Wi-Fi Recommended", color = Accent, fontWeight = FontWeight.Medium, fontSize = 14.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            if (!isDownloading) {
                Button(
                    onClick = { isDownloading = true },
                    colors = ButtonDefaults.buttonColors(containerColor = Accent),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().height(56.dp)
                ) {
                    Text("Download & Enable AI", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    LinearProgressIndicator(
                        progress = progress,
                        color = Accent,
                        trackColor = Surface,
                        modifier = Modifier.fillMaxWidth().height(8.dp).background(Surface, RoundedCornerShape(4.dp))
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Downloading... ${(progress * 100).toInt()}%",
                        color = TextMuted,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}
