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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.app.exoplanethunter.R
import com.app.exoplanethunter.presentation.preview.PreviewSurface
import com.app.exoplanethunter.presentation.theme.CosmicCyan
import com.app.exoplanethunter.presentation.theme.SpaceBlack
import com.app.exoplanethunter.presentation.theme.StarGold
import com.app.exoplanethunter.presentation.theme.SurfaceCardLight
import com.app.exoplanethunter.presentation.theme.TextMuted
import com.app.exoplanethunter.presentation.theme.TextSecondary
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

private val ShiftBlue = Color(0xFF6EC6FF)
private val ShiftRed = Color(0xFFFF6E6E)

/**
 * Explainer for the radial-velocity method, the companion to the transit
 * animation. The star circles a shared centre of mass; its light blueshifts as
 * it swings toward us and redshifts as it pulls away, revealing the unseen
 * planet. The wobble is greatly exaggerated for clarity.
 */
@Composable
internal fun RadialVelocityContent() {
    Column(modifier = Modifier.fillMaxWidth()) {
        RadialVelocityAnimation(
            modifier = Modifier
                .fillMaxWidth()
                .height(210.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            LegendDot(color = StarGold, label = stringResource(R.string.stats_transit_legend_star))
            LegendDot(color = SpaceBlack, label = stringResource(R.string.stats_transit_legend_planet), ring = CosmicCyan)
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = stringResource(R.string.stats_rv_caption),
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary
        )
    }
}

@Composable
private fun LegendDot(color: Color, label: String, ring: Color? = null) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(label, style = MaterialTheme.typography.labelSmall, color = TextMuted)
    }
}

@Composable
private fun RadialVelocityAnimation(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "rv")
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 5000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rv_phase"
    )

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val sceneH = h * 0.58f
        val cx = w / 2f
        val cy = sceneH / 2f
        val tilt = 0.45f

        val planetOrbit = sceneH * 0.34f
        val starOrbit = sceneH * 0.13f

        val theta = phase.toDouble()
        val starTheta = theta + PI

        val planet = Offset(
            cx + (planetOrbit * cos(theta)).toFloat(),
            cy + (planetOrbit * sin(theta) * tilt).toFloat()
        )
        val star = Offset(
            cx + (starOrbit * cos(starTheta)).toFloat(),
            cy + (starOrbit * sin(starTheta) * tilt).toFloat()
        )

        // Line-of-sight is vertical (observer below). +shift = approaching → blueshift.
        val shift = cos(starTheta).toFloat()

        // Orbits (faint dashed ellipses) and barycentre.
        val dash = PathEffect.dashPathEffect(floatArrayOf(4f, 6f), 0f)
        drawOval(
            color = SurfaceCardLight.copy(alpha = 0.7f),
            topLeft = Offset(cx - planetOrbit, cy - planetOrbit * tilt),
            size = Size(planetOrbit * 2, planetOrbit * 2 * tilt),
            style = Stroke(width = 1f, pathEffect = dash)
        )
        drawOval(
            color = SurfaceCardLight.copy(alpha = 0.7f),
            topLeft = Offset(cx - starOrbit, cy - starOrbit * tilt),
            size = Size(starOrbit * 2, starOrbit * 2 * tilt),
            style = Stroke(width = 1f, pathEffect = dash)
        )
        drawLine(SurfaceCardLight, Offset(cx - 5f, cy), Offset(cx + 5f, cy), strokeWidth = 1f)
        drawLine(SurfaceCardLight, Offset(cx, cy - 5f), Offset(cx, cy + 5f), strokeWidth = 1f)

        // Planet behind the star when on the far side (smaller sin), simple painter order.
        val planetFirst = sin(theta) < sin(starTheta)
        if (planetFirst) drawPlanet(planet) else drawStar(star, shift)
        if (planetFirst) drawStar(star, shift) else drawPlanet(planet)

        // Spectrum strip with a shifting absorption line.
        val stripLeft = 16.dp.toPx()
        val stripRight = w - 16.dp.toPx()
        val stripW = stripRight - stripLeft
        val stripTop = sceneH + 16.dp.toPx()
        val stripH = 16.dp.toPx()
        drawRoundRect(
            brush = Brush.horizontalGradient(
                colors = listOf(ShiftBlue, Color.White.copy(alpha = 0.85f), ShiftRed),
                startX = stripLeft,
                endX = stripRight
            ),
            topLeft = Offset(stripLeft, stripTop),
            size = Size(stripW, stripH),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f)
        )
        val midX = stripLeft + stripW / 2f
        val lineX = midX - shift * (stripW / 2f) * 0.72f
        drawLine(
            color = Color(0xFF101424),
            start = Offset(lineX, stripTop - 3f),
            end = Offset(lineX, stripTop + stripH + 3f),
            strokeWidth = 3f
        )
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawPlanet(pos: Offset) {
    drawCircle(color = SpaceBlack, radius = 7f, center = pos)
    drawCircle(color = CosmicCyan.copy(alpha = 0.5f), radius = 7f, center = pos, style = Stroke(width = 1.5f))
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawStar(pos: Offset, shift: Float) {
    val tint = if (shift >= 0f) lerp(StarGold, ShiftBlue, shift * 0.6f)
    else lerp(StarGold, ShiftRed, -shift * 0.6f)
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(tint.copy(alpha = 0.5f), Color.Transparent),
            center = pos,
            radius = 34f
        ),
        radius = 34f,
        center = pos
    )
    drawCircle(color = tint, radius = 15f, center = pos)
}

@Preview
@Composable
private fun RadialVelocityContentPreview() = PreviewSurface { RadialVelocityContent() }
