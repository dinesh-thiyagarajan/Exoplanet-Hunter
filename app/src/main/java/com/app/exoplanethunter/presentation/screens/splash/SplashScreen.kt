package com.app.exoplanethunter.presentation.screens.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.koin.androidx.compose.koinViewModel
import com.app.exoplanethunter.presentation.components.StarField
import com.app.exoplanethunter.presentation.theme.CosmicCyan
import com.app.exoplanethunter.presentation.theme.NebulaPink
import com.app.exoplanethunter.presentation.theme.SpaceBlack
import com.app.exoplanethunter.presentation.theme.StarGold
import com.app.exoplanethunter.presentation.theme.TextSecondary
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

@Composable
fun SplashScreen(
    onDataLoaded: () -> Unit,
    viewModel: SplashViewModel = koinViewModel()
) {
    val fadeIn = remember { Animatable(0f) }
    val titleSlide = remember { Animatable(50f) }

    LaunchedEffect(Unit) {
        fadeIn.animateTo(1f, tween(1200))
        titleSlide.animateTo(0f, tween(800))
    }

    LaunchedEffect(viewModel.isLoaded) {
        if (viewModel.isLoaded) {
            kotlinx.coroutines.delay(500)
            onDataLoaded()
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "splash")
    val pulseAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse"
    )

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // Background
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawRect(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF0F1629),
                        SpaceBlack
                    ),
                    center = center,
                    radius = size.maxDimension
                )
            )
        }

        StarField(starCount = 200)

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .alpha(fadeIn.value)
                .padding(bottom = titleSlide.value.dp)
        ) {
            // Animated planet logo
            Canvas(modifier = Modifier.size(140.dp)) {
                val cx = size.width / 2
                val cy = size.height / 2
                val radius = min(cx, cy) * 0.6f

                // Outer glow
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            CosmicCyan.copy(alpha = 0.3f),
                            Color.Transparent
                        ),
                        center = Offset(cx, cy),
                        radius = radius * 2f
                    ),
                    radius = radius * 2f,
                    center = Offset(cx, cy)
                )

                // Planet
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            CosmicCyan,
                            Color(0xFF1565C0),
                            Color(0xFF0D47A1)
                        ),
                        center = Offset(cx - radius * 0.3f, cy - radius * 0.3f),
                        radius = radius * 1.5f
                    ),
                    radius = radius,
                    center = Offset(cx, cy)
                )

                // Orbit ring
                val angleRad = Math.toRadians(pulseAngle.toDouble()).toFloat()
                drawCircle(
                    color = StarGold,
                    radius = 6f,
                    center = Offset(
                        cx + radius * 1.4f * cos(angleRad),
                        cy + radius * 0.4f * sin(angleRad)
                    )
                )

                drawOval(
                    color = CosmicCyan.copy(alpha = 0.3f),
                    topLeft = Offset(cx - radius * 1.5f, cy - radius * 0.4f),
                    size = androidx.compose.ui.geometry.Size(radius * 3f, radius * 0.8f),
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2f)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "EXOPLANET",
                style = MaterialTheme.typography.displayLarge.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 8.sp,
                    brush = Brush.linearGradient(
                        colors = listOf(CosmicCyan, NebulaPink)
                    )
                )
            )

            Text(
                text = "HUNTER",
                style = MaterialTheme.typography.displayMedium.copy(
                    fontWeight = FontWeight.Light,
                    letterSpacing = 12.sp,
                    color = TextSecondary
                )
            )

            Spacer(modifier = Modifier.height(48.dp))

            Text(
                text = if (viewModel.isLoaded) "Ready" else "Loading exoplanet data...",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
        }
    }
}
