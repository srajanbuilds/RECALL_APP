package com.recall.app.feature.onboarding

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.recall.app.core.ui.theme.Accent
import com.recall.app.core.ui.theme.Background
import com.recall.app.core.ui.theme.Surface
import com.recall.app.core.ui.theme.TextMuted

private data class OnboardingPage(
    val headline: String,
    val subheadline: String,
    val buttonLabel: String,
    val visual: String
)

private val pages = listOf(
    OnboardingPage(
        headline = "Notes that never forget.",
        subheadline = "Recall",
        buttonLabel = "Next",
        visual = "📝"
    ),
    OnboardingPage(
        headline = "Your AI remembers everything.",
        subheadline = "Ask anything you've ever written — months or years later — and get a direct, cited answer instantly.",
        buttonLabel = "Next",
        visual = "✨"
    ),
    OnboardingPage(
        headline = "No account needed.\nEverything stays on your device.",
        subheadline = "100% offline · 100% private · Always free",
        buttonLabel = "Get Started",
        visual = "🔒"
    )
)

@Composable
fun OnboardingScreen(onComplete: () -> Unit) {
    var currentPage by remember { mutableIntStateOf(0) }
    val page = pages[currentPage]

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            // Visual
            AnimatedContent(
                targetState = currentPage,
                transitionSpec = { fadeIn(initialAlpha = 0f) togetherWith fadeOut() },
                label = "visual"
            ) { idx ->
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .background(Surface, RoundedCornerShape(32.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(pages[idx].visual, fontSize = 56.sp)
                }
            }

            Spacer(Modifier.height(48.dp))

            // Wordmark on screen 1
            if (currentPage == 0) {
                Text(
                    "Recall",
                    fontSize = 42.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    letterSpacing = (-1).sp
                )
                Spacer(Modifier.height(16.dp))
            }

            // Headline
            AnimatedContent(
                targetState = currentPage,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "headline"
            ) { idx ->
                Text(
                    pages[idx].headline,
                    fontSize = if (idx == 0) 22.sp else 26.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    lineHeight = 34.sp
                )
            }

            Spacer(Modifier.height(16.dp))

            // Subheadline
            AnimatedContent(
                targetState = currentPage,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "sub"
            ) { idx ->
                Text(
                    pages[idx].subheadline,
                    fontSize = 15.sp,
                    color = TextMuted,
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp
                )
            }

            Spacer(Modifier.height(64.dp))

            // CTA button
            Button(
                onClick = {
                    if (currentPage < pages.size - 1) currentPage++
                    else onComplete()
                },
                colors = ButtonDefaults.buttonColors(containerColor = Accent),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) {
                Text(page.buttonLabel, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
