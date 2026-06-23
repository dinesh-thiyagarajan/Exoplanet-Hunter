package com.app.exoplanethunter.presentation.screens.statistics

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.app.exoplanethunter.R
import com.app.exoplanethunter.presentation.preview.PreviewSurface
import com.app.exoplanethunter.presentation.theme.AuroraGreen
import com.app.exoplanethunter.presentation.theme.CosmicCyan
import com.app.exoplanethunter.presentation.theme.SolarOrange
import com.app.exoplanethunter.presentation.theme.SpaceBlack
import com.app.exoplanethunter.presentation.theme.StarGold
import com.app.exoplanethunter.presentation.theme.SurfaceCardLight
import com.app.exoplanethunter.presentation.theme.TextMuted
import com.app.exoplanethunter.presentation.theme.TextSecondary
import kotlin.math.abs

/**
 * Educational content for the "Transit Method" section on the Statistics screen.
 *
 * Pairs an animated light curve with a short caption and legend so the dominant
 * discovery method in the catalog explains itself: a planet glides across its
 * star while the brightness graph dips in sync. Sizes are illustrative and the
 * dip is exaggerated for clarity — a real transit dims a star by ~1%.
 */
@Composable
internal fun TransitMethodContent() {
    Column(modifier = Modifier.fillMaxWidth()) {
        TransitLightCurve(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            LegendItem(color = StarGold, label = stringResource(R.string.stats_transit_legend_star))
            LegendItem(color = SpaceBlack, label = stringResource(R.string.stats_transit_legend_planet), ringColor = CosmicCyan)
            LegendItem(color = AuroraGreen, label = stringResource(R.string.stats_transit_legend_brightness))
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = stringResource(R.string.stats_transit_caption),
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary
        )
    }
}

@Composable
private fun LegendItem(color: Color, label: String, ringColor: Color? = null) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = TextMuted
        )
    }
}

@Composable
private fun TransitLightCurve(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "transit")
    // One full crossing every 6s, restarting — the master clock for both the
    // planet's position and the moving marker on the light curve.
    val progress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 6000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "transit_progress"
    )

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // --- Layout regions: star scene on top, light curve below ---
        val sceneH = h * 0.46f
        val graphTop = h * 0.52f

        val starCx = w / 2f
        val starCy = sceneH / 2f
        val starR = sceneH * 0.30f
        val planetR = starR * 0.22f
        val depth = (planetR / starR) * (planetR / starR)

        // Planet travels fully off-screen on both sides for a clean entry/exit.
        val margin = planetR + 8.dp.toPx()
        val startX = -margin
        val span = (w + margin) - startX
        val planetX = startX + progress * span

        // Fraction of the star's light blocked when the planet is at x.
        fun blockedAt(x: Float): Float {
            val d = abs(x - starCx)
            if (d >= starR + planetR) return 0f
            if (d <= starR - planetR) return depth
            val overlap = (starR + planetR - d) / (2f * planetR)
            return depth * overlap
        }

        // --- Star with soft glow ---
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(SolarOrange.copy(alpha = 0.45f), Color.Transparent),
                center = Offset(starCx, starCy),
                radius = starR * 1.8f
            ),
            radius = starR * 1.8f,
            center = Offset(starCx, starCy)
        )
        drawCircle(color = StarGold, radius = starR, center = Offset(starCx, starCy))

        // --- Planet crossing the star's face ---
        drawCircle(color = SpaceBlack, radius = planetR, center = Offset(planetX, starCy))
        drawCircle(
            color = CosmicCyan.copy(alpha = 0.5f),
            radius = planetR,
            center = Offset(planetX, starCy),
            style = Stroke(width = 1.5.dp.toPx())
        )

        // --- Light curve graph ---
        val gLeft = 4.dp.toPx()
        val gRight = w - 4.dp.toPx()
        val gWidth = gRight - gLeft
        val plotTop = graphTop + 10.dp.toPx()
        val plotBottom = h - 12.dp.toPx()
        val plotH = plotBottom - plotTop

        // Baseline (full brightness) reference line.
        drawLine(
            color = SurfaceCardLight,
            start = Offset(gLeft, plotTop),
            end = Offset(gRight, plotTop),
            strokeWidth = 1.dp.toPx(),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 6f), 0f)
        )
        // Axes.
        drawLine(
            color = SurfaceCardLight,
            start = Offset(gLeft, plotTop - 4.dp.toPx()),
            end = Offset(gLeft, plotBottom),
            strokeWidth = 1.5.dp.toPx()
        )
        drawLine(
            color = SurfaceCardLight,
            start = Offset(gLeft, plotBottom),
            end = Offset(gRight, plotBottom),
            strokeWidth = 1.5.dp.toPx()
        )

        // Curve: sample the blocked fraction across the full crossing and map
        // it to the plot height (normalised so the dip is clearly visible).
        val usableH = plotH * 0.82f
        val path = Path()
        val steps = gWidth.toInt().coerceAtLeast(2)
        for (i in 0..steps) {
            val frac = i.toFloat() / steps
            val fx = gLeft + frac * gWidth
            val blocked = blockedAt(startX + frac * span)
            val fy = plotTop + (blocked / depth) * usableH
            if (i == 0) path.moveTo(fx, fy) else path.lineTo(fx, fy)
        }
        drawPath(
            path = path,
            color = AuroraGreen,
            style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        )

        // Moving marker tracking the planet's current position on the curve.
        val headX = gLeft + progress * gWidth
        val headY = plotTop + (blockedAt(planetX) / depth) * usableH
        drawLine(
            color = AuroraGreen.copy(alpha = 0.25f),
            start = Offset(headX, plotTop),
            end = Offset(headX, plotBottom),
            strokeWidth = 1.dp.toPx()
        )
        drawCircle(color = AuroraGreen, radius = 3.5.dp.toPx(), center = Offset(headX, headY))
    }
}

@Preview
@Composable
private fun TransitMethodContentPreview() = PreviewSurface { TransitMethodContent() }
