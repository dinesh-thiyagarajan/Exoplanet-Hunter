package com.app.exoplanethunter.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.app.exoplanethunter.presentation.theme.InkText
import com.app.exoplanethunter.presentation.theme.MatchaMid
import com.app.exoplanethunter.presentation.theme.StarCheek
import com.app.exoplanethunter.presentation.theme.StarGlow
import com.app.exoplanethunter.presentation.theme.StarOrange

private val Face = Color(0xFF201E1D)
private val IconGround = Color(0xFF2E2B25)

/**
 * The "Star hug" icon as a drawing: a smiling star with a little planet hugging it, on the
 * icon's dark ground, in a circle. Drawn from the 100-unit icon artwork.
 */
@Composable
fun StarHugBadge(size: Dp, modifier: Modifier = Modifier) {
    Canvas(modifier.size(size)) {
        val u = this.size.width / 100f
        drawCircle(IconGround)
        fun p(x: Float, y: Float) = Offset(x * u, y * u)
        drawCircle(InkText, 1.2f * u, p(22f, 24f))
        drawCircle(InkText, 1.2f * u, p(82f, 80f))
        drawCircle(InkText, 1.0f * u, p(80f, 20f))
        // The star, its glow, closed happy eyes, smile and cheeks.
        drawCircle(StarGlow.copy(alpha = 0.45f), 30f * u, p(58f, 44f))
        drawCircle(StarOrange, 24f * u, p(58f, 44f))
        val face = Stroke(2.4f * u, cap = StrokeCap.Round)
        drawPath(Path().apply { moveTo(50f * u, 40f * u); quadraticBezierTo(53f * u, 37f * u, 56f * u, 40f * u) }, Face, style = face)
        drawPath(Path().apply { moveTo(62f * u, 40f * u); quadraticBezierTo(65f * u, 37f * u, 68f * u, 40f * u) }, Face, style = face)
        drawPath(Path().apply { moveTo(55f * u, 47f * u); quadraticBezierTo(59f * u, 52f * u, 63f * u, 47f * u) }, Face, style = face)
        drawOval(StarCheek, p(44.5f, 45f), Size(7f * u, 4f * u))
        drawOval(StarCheek, p(68.5f, 45f), Size(7f * u, 4f * u))
        // The planet, its arms round the star, and its little face.
        drawCircle(MatchaMid, 16f * u, p(34f, 68f))
        val arm = Stroke(6f * u, cap = StrokeCap.Round)
        drawPath(Path().apply { moveTo(44f * u, 58f * u); quadraticBezierTo(52f * u, 56f * u, 56f * u, 62f * u) }, MatchaMid, style = arm)
        drawPath(Path().apply { moveTo(24f * u, 56f * u); quadraticBezierTo(26f * u, 46f * u, 34f * u, 44f * u) }, MatchaMid, style = arm)
        drawCircle(Face, 2f * u, p(30f, 68f))
        drawCircle(Face, 2f * u, p(39f, 66f))
        drawPath(
            Path().apply { moveTo(32f * u, 73f * u); quadraticBezierTo(35f * u, 75f * u, 38f * u, 72f * u) },
            Face,
            style = Stroke(1.8f * u, cap = StrokeCap.Round),
        )
    }
}

/**
 * The splash character: the star with a face, at [center] with radius [r]. [blink] closes the
 * eyes, [worried] lifts the brows and turns the mouth down, [dim] darkens it (offline).
 */
fun DrawScope.drawStarFace(
    center: Offset,
    r: Float,
    glowRadius: Float,
    glowAlpha: Float,
    blink: Boolean = false,
    worried: Boolean = false,
    dim: Boolean = false,
) {
    val u = r / 50f
    fun p(x: Float, y: Float) = Offset(center.x + (x - 150f) * u, center.y + (y - 150f) * u)
    drawCircle(StarGlow.copy(alpha = glowAlpha), glowRadius, center)
    drawCircle(if (dim) Color(0xFFC77E4E) else StarOrange, r, center)
    if (blink) {
        drawLine(Face, p(129f, 140f), p(139f, 140f), 3f * u, StrokeCap.Round)
        drawLine(Face, p(161f, 140f), p(171f, 140f), 3f * u, StrokeCap.Round)
    } else {
        drawCircle(Face, 4.5f * u, p(134f, 140f))
        drawCircle(Face, 4.5f * u, p(166f, 140f))
    }
    if (worried) {
        drawLine(Face, p(127f, 133f), p(139f, 129f), 3f * u, StrokeCap.Round)
        drawLine(Face, p(173f, 133f), p(161f, 129f), 3f * u, StrokeCap.Round)
    }
    val cheek = StarCheek.copy(alpha = if (dim) 0.5f else 1f)
    drawOval(cheek, p(115f, 152f), Size(14f * u, 8f * u))
    drawOval(cheek, p(171f, 152f), Size(14f * u, 8f * u))
    val mouth = Path().apply {
        if (worried) { moveTo(p(141f, 162f).x, p(141f, 162f).y); quadraticBezierTo(p(150f, 155f).x, p(150f, 155f).y, p(159f, 162f).x, p(159f, 162f).y) }
        else { moveTo(p(140f, 156f).x, p(140f, 156f).y); quadraticBezierTo(p(150f, 166f).x, p(150f, 166f).y, p(160f, 156f).x, p(160f, 156f).y) }
    }
    drawPath(mouth, Face, style = Stroke(4f * u, cap = StrokeCap.Round))
}

/** The little planet character, radius [r] at [center]. */
fun DrawScope.drawPlanetFace(center: Offset, r: Float, alpha: Float = 1f, worried: Boolean = false) {
    val u = r / 17f
    drawCircle(MatchaMid.copy(alpha = alpha), r, center)
    drawCircle(Face.copy(alpha = alpha), 2.2f * u, Offset(center.x - 5f * u, center.y - u))
    drawCircle(Face.copy(alpha = alpha), 2.2f * u, Offset(center.x + 5f * u, center.y - u))
    val mouth = Path().apply {
        if (worried) { moveTo(center.x - 3f * u, center.y + 7f * u); quadraticBezierTo(center.x, center.y + 4.5f * u, center.x + 3f * u, center.y + 7f * u) }
        else { moveTo(center.x - 3f * u, center.y + 5f * u); quadraticBezierTo(center.x, center.y + 7.5f * u, center.x + 3f * u, center.y + 5f * u) }
    }
    drawPath(mouth, Face.copy(alpha = alpha), style = Stroke(1.8f * u, cap = StrokeCap.Round))
}

/** Small default size for empty states. */
val EmptyArtSize: Dp = 120.dp
