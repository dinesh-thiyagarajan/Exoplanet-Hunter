package com.app.exoplanethunter.presentation.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.app.exoplanethunter.presentation.theme.CautionYellow
import com.app.exoplanethunter.presentation.theme.HabitableGreen
import com.app.exoplanethunter.presentation.theme.HostileRed
import com.app.exoplanethunter.presentation.theme.SurfaceCard
import com.app.exoplanethunter.presentation.theme.TextSecondary

@Composable
fun HabitabilityScoreBar(
    label: String,
    score: Double,
    modifier: Modifier = Modifier
) {
    var targetValue by remember { mutableFloatStateOf(0f) }
    val animatedValue by animateFloatAsState(
        targetValue = targetValue,
        animationSpec = tween(1200),
        label = "score_anim"
    )

    LaunchedEffect(score) {
        targetValue = score.toFloat()
    }

    val barColor = when {
        score > 0.7 -> HabitableGreen
        score > 0.4 -> CautionYellow
        else -> HostileRed
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
            Text(
                text = "${(score * 100).toInt()}%",
                style = MaterialTheme.typography.bodySmall,
                color = barColor
            )
        }

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .padding(top = 4.dp)
        ) {
            // Background
            drawRoundRect(
                color = SurfaceCard,
                size = Size(size.width, size.height),
                cornerRadius = CornerRadius(4.dp.toPx())
            )

            // Filled portion
            val filledWidth = size.width * animatedValue
            if (filledWidth > 0) {
                drawRoundRect(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            barColor.copy(alpha = 0.7f),
                            barColor
                        )
                    ),
                    size = Size(filledWidth, size.height),
                    cornerRadius = CornerRadius(4.dp.toPx())
                )

                // Glow effect at end
                drawCircle(
                    color = barColor.copy(alpha = 0.4f),
                    radius = size.height,
                    center = Offset(filledWidth, size.height / 2)
                )
            }
        }
    }
}
