package com.recall.app.feature.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.*
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.provideContent
import androidx.glance.layout.*
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.recall.app.core.data.local.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RecallWidget : GlanceAppWidget() {

    override val sizeMode = SizeMode.Exact

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val recentTitles = withContext(Dispatchers.IO) {
            try {
                AppDatabase.getInstance(context)
                    .noteDao()
                    .getAllNotesOnce()
                    .filter { !it.isPrivate }
                    .take(2)
                    .map { it.title.ifBlank { "Untitled" } }
            } catch (e: Exception) { emptyList() }
        }
        provideContent { WidgetContent(recentTitles) }
    }
}

@Composable
private fun WidgetContent(recentTitles: List<String>) {
    val bgColor    = ColorProvider(androidx.compose.ui.graphics.Color(0xFF1A1A1A))
    val textColor  = ColorProvider(androidx.compose.ui.graphics.Color(0xFFF0F0F0))
    val mutedColor = ColorProvider(androidx.compose.ui.graphics.Color(0xFF888888))
    val accentColor = ColorProvider(androidx.compose.ui.graphics.Color(0xFF7C6FF7))

    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(bgColor)
            .padding(16.dp)
    ) {
        Column(modifier = GlanceModifier.fillMaxSize()) {
            // Header
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "✦ Recall",
                    style = TextStyle(color = accentColor, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                )
            }
            Spacer(GlanceModifier.height(10.dp))
            Text(
                "Capture a thought…",
                style = TextStyle(color = mutedColor, fontSize = 13.sp)
            )
            Spacer(GlanceModifier.height(8.dp))
            recentTitles.forEach { title ->
                Text(
                    "📄 $title",
                    style = TextStyle(color = mutedColor, fontSize = 12.sp),
                    maxLines = 1
                )
                Spacer(GlanceModifier.height(2.dp))
            }
        }
    }
}

class RecallWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = RecallWidget()
}
