package com.workspace.exoplanethunter.presentation.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import kotlin.math.sin
import kotlin.random.Random

data class Star(
    val x: Float,
    val y: Float,
    val size: Float,
    val brightness: Float,
    val twinkleSpeed: Float
)

@Composable
fun StarField(modifier: Modifier = Modifier, starCount: Int = 150) {
    val stars = remember {
        val random = Random(42)
        List(starCount) {
            Star(
                x = random.nextFloat(),
                y = random.nextFloat(),
                size = random.nextFloat() * 2f + 0.5f,
                brightness = random.nextFloat() * 0.5f + 0.5f,
                twinkleSpeed = random.nextFloat() * 2f + 1f
            )
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "starfield")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(100000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "star_time"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        for (star in stars) {
            val twinkle = (sin(time * star.twinkleSpeed * 0.1f) * 0.3f + 0.7f)
            val alpha = star.brightness * twinkle

            drawCircle(
                color = Color.White.copy(alpha = alpha),
                radius = star.size,
                center = Offset(star.x * w, star.y * h)
            )

            // Subtle glow for brighter stars
            if (star.brightness > 0.8f) {
                drawCircle(
                    color = Color.White.copy(alpha = alpha * 0.15f),
                    radius = star.size * 4f,
                    center = Offset(star.x * w, star.y * h)
                )
            }
        }
    }
}
