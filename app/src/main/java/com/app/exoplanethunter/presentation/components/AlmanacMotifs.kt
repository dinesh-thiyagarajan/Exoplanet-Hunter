package com.app.exoplanethunter.presentation.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.app.exoplanethunter.presentation.theme.Brass
import com.app.exoplanethunter.presentation.theme.InkText
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

/**
 * The faint measuring-graticule that sits behind hero areas — the signature
 * almanac motif (a 28dp square grid in a near-invisible warm ink).
 */
fun Modifier.graticule(spacing: Dp = 28.dp): Modifier = drawBehind {
    val color = InkText.copy(alpha = 0.045f)
    val step = spacing.toPx()
    var x = step
    while (x < size.width) {
        drawLine(color, Offset(x, 0f), Offset(x, size.height), strokeWidth = 1f)
        x += step
    }
    var y = step
    while (y < size.height) {
        drawLine(color, Offset(0f, y), Offset(size.width, y), strokeWidth = 1f)
        y += step
    }
}

/**
 * A slowly-rotating brass reticle: a thin ring, a four-way crosshair (the top
 * arm brass, the rest faint), and a single registration tick orbiting the ring.
 * Framed around a hero subject (e.g. the rendered planet).
 */
@Composable
fun ReticleOverlay(modifier: Modifier = Modifier, periodMillis: Int = 90_000) {
    val transition = rememberInfiniteTransition(label = "reticle")
    val angle by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = periodMillis, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "reticle_angle"
    )

    Canvas(modifier = modifier) {
        val cx = size.width / 2f
        val cy = size.height / 2f
        val r = min(cx, cy) * 0.92f
        val faint = InkText.copy(alpha = 0.18f)

        // Ring.
        drawCircle(color = faint, radius = r, center = Offset(cx, cy), style = Stroke(width = 1f))

        // Crosshair arms — top arm brass, others faint.
        val arm = r * 0.12f
        drawLine(Brass, Offset(cx, cy - r - arm), Offset(cx, cy - r + arm), strokeWidth = 1.5f, cap = StrokeCap.Round)
        drawLine(faint, Offset(cx, cy + r - arm), Offset(cx, cy + r + arm), strokeWidth = 1.5f)
        drawLine(faint, Offset(cx - r - arm, cy), Offset(cx - r + arm, cy), strokeWidth = 1.5f)
        drawLine(faint, Offset(cx + r - arm, cy), Offset(cx + r + arm, cy), strokeWidth = 1.5f)

        // Orbiting registration tick.
        val a = angle * (PI / 180.0)
        val tx = cx + (r * cos(a)).toFloat()
        val ty = cy + (r * sin(a)).toFloat()
        val ox = cx + ((r + arm) * cos(a)).toFloat()
        val oy = cy + ((r + arm) * sin(a)).toFloat()
        drawLine(Brass, Offset(tx, ty), Offset(ox, oy), strokeWidth = 1.5f, cap = StrokeCap.Round)
    }
}
