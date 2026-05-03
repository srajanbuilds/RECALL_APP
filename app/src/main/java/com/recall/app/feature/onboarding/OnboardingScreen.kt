package com.recall.app.feature.onboarding

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.recall.app.core.ui.theme.Accent
import com.recall.app.core.ui.theme.TextMuted

private data class OnboardingPage(
    val emoji: String,
    val title: String,
    val subtitle: String,
    val gradient: List<Color>
)

private val pages = listOf(
    OnboardingPage(
        emoji = "📝",
        title = "Remember Everything",
        subtitle = "Capture ideas, thoughts, and knowledge. Recall keeps every detail safe and searchable.",
        gradient = listOf(Color(0xFF1a0533), Color(0xFF0d0d10))
    ),
    OnboardingPage(
        emoji = "🔒",
        title = "Private by Design",
        subtitle = "Your notes never leave your phone. No cloud, no accounts, no surveillance. Ever.",
        gradient = listOf(Color(0xFF003322), Color(0xFF0d0d10))
    ),
    OnboardingPage(
        emoji = "✨",
        title = "Ask Anything",
        subtitle = "A powerful AI lives entirely on your device. Ask questions about your notes, get instant answers.",
        gradient = listOf(Color(0xFF1a1533), Color(0xFF0d0d10))
    )
)

@Composable
fun OnboardingScreen(onComplete: () -> Unit) {
    var currentPage by remember { mutableStateOf(0) }
    val page = pages[currentPage]

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(page.gradient)
            )
    ) {
        // Skip button
        TextButton(
            onClick = onComplete,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
        ) {
            Text("Skip", color = TextMuted, fontSize = 14.sp)
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Big emoji
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(RoundedCornerShape(32.dp))
                    .background(Color.White.copy(alpha = 0.08f)),
                contentAlignment = Alignment.Center
            ) {
                Text(page.emoji, fontSize = 56.sp)
            }

            Spacer(modifier = Modifier.height(40.dp))

            AnimatedContent(
                targetState = currentPage,
                transitionSpec = {
                    fadeIn() togetherWith fadeOut()
                }
            ) { _ ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        page.title,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        lineHeight = 34.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        page.subtitle,
                        fontSize = 16.sp,
                        color = TextMuted,
                        textAlign = TextAlign.Center,
                        lineHeight = 24.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            // Dots indicator
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                pages.indices.forEach { index ->
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(
                                if (index == currentPage) Accent else Color.White.copy(alpha = 0.2f)
                            )
                            .size(if (index == currentPage) 24.dp else 8.dp, 8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Next / Get Started button
            Button(
                onClick = {
                    if (currentPage < pages.size - 1) {
                        currentPage++
                    } else {
                        onComplete()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Accent),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text(
                    if (currentPage < pages.size - 1) "Continue →" else "Get Started",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
