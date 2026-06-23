package com.app.exoplanethunter.presentation.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.exoplanethunter.R
import com.app.exoplanethunter.presentation.preview.PreviewSurface
import com.app.exoplanethunter.presentation.theme.CosmicCyan
import com.app.exoplanethunter.presentation.theme.SurfaceCard
import com.app.exoplanethunter.presentation.theme.SurfaceCardLight
import com.app.exoplanethunter.presentation.theme.TextMuted
import com.app.exoplanethunter.presentation.theme.TextSecondary
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.roundToInt
import kotlin.math.sin

/**
 * "Find it in the night sky" — plots a host star on a celestial (RA/Dec) chart
 * with a handful of famous reference stars for orientation, plus a plain-language
 * read-out of where and when it's visible. Renders nothing when coordinates are
 * missing. A future iteration can add a sensor-driven AR "point your phone" mode.
 */
@Composable
fun SkyChartCard(
    ra: Double?,
    dec: Double?,
    hostName: String,
    modifier: Modifier = Modifier
) {
    if (ra == null || dec == null) return

    val textMeasurer = rememberTextMeasurer()
    val transition = rememberInfiniteTransition(label = "sky")
    val twinkle by transition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "twinkle"
    )
    val pulse by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(SurfaceCard)
            .padding(20.dp)
    ) {
        Text(
            text = stringResource(R.string.sky_chart_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = Color.White
        )
        Text(
            text = stringResource(R.string.sky_chart_subtitle, hostName),
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary,
            modifier = Modifier.padding(top = 2.dp)
        )

        Spacer(modifier = Modifier.height(14.dp))

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF0A0E1A))
        ) {
            val w = size.width
            val h = size.height

            fun xOf(raDeg: Double) = ((raDeg / 360.0) * w).toFloat()
            fun yOf(decDeg: Double) = (((90.0 - decDeg) / 180.0) * h).toFloat()

            // RA grid lines every 6h (90°) with hour labels.
            for (raH in 0..24 step 6) {
                val gx = xOf(raH * 15.0)
                drawLine(
                    color = SurfaceCardLight.copy(alpha = 0.6f),
                    start = Offset(gx, 0f),
                    end = Offset(gx, h),
                    strokeWidth = 1f
                )
            }
            // Dec grid lines every 30°.
            for (d in -60..60 step 30) {
                val gy = yOf(d.toDouble())
                drawLine(
                    color = SurfaceCardLight.copy(alpha = 0.6f),
                    start = Offset(0f, gy),
                    end = Offset(w, gy),
                    strokeWidth = 1f
                )
            }
            // Celestial equator, emphasised.
            drawLine(
                color = CosmicCyan.copy(alpha = 0.35f),
                start = Offset(0f, yOf(0.0)),
                end = Offset(w, yOf(0.0)),
                strokeWidth = 1.5f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
            )

            // Reference stars (twinkling), with short labels on the brightest.
            referenceStars.forEachIndexed { i, star ->
                val cxp = xOf(star.ra)
                val cyp = yOf(star.dec)
                val a = 0.55f + 0.35f * sin(twinkle + i).let { abs(it) }
                drawCircle(color = Color.White.copy(alpha = a), radius = 2.2f, center = Offset(cxp, cyp))
                if (star.label) {
                    val layout = textMeasurer.measure(
                        star.name,
                        style = TextStyle(color = TextMuted, fontSize = 8.sp)
                    )
                    drawText(layout, topLeft = Offset(cxp + 4f, cyp - layout.size.height - 1f))
                }
            }

            // The target star — pulsing cyan marker drawn on top.
            val tx = xOf(ra)
            val ty = yOf(dec)
            drawCircle(
                color = CosmicCyan.copy(alpha = 0.25f),
                radius = 8f + pulse * 6f,
                center = Offset(tx, ty)
            )
            drawCircle(
                color = CosmicCyan,
                radius = 6f,
                center = Offset(tx, ty),
                style = Stroke(width = 2f)
            )
            drawCircle(color = CosmicCyan, radius = 2.5f, center = Offset(tx, ty))
            val nameLayout = textMeasurer.measure(
                hostName,
                style = TextStyle(color = CosmicCyan, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
            )
            // Keep the label on-screen near right edge.
            val labelX = if (tx + 10f + nameLayout.size.width > w) tx - 10f - nameLayout.size.width else tx + 10f
            drawText(nameLayout, topLeft = Offset(labelX, ty - nameLayout.size.height - 4f))

            // X-axis tick caps.
            val leftCap = textMeasurer.measure("0h", style = TextStyle(color = TextMuted, fontSize = 8.sp))
            drawText(leftCap, topLeft = Offset(3f, h - leftCap.size.height - 2f))
            val rightCap = textMeasurer.measure("24h", style = TextStyle(color = TextMuted, fontSize = 8.sp))
            drawText(rightCap, topLeft = Offset(w - rightCap.size.width - 3f, h - rightCap.size.height - 2f))
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = stringResource(R.string.sky_chart_coords, formatRa(ra), formatDec(dec)),
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = stringResource(hemisphereRes(dec)),
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary,
            modifier = Modifier.padding(top = 4.dp)
        )
        Text(
            text = stringResource(R.string.sky_chart_best_month, bestMonth(ra)),
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary,
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}

private data class RefStar(val name: String, val ra: Double, val dec: Double, val label: Boolean)

// A spread of famous naked-eye stars to orient the chart (J2000, degrees).
private val referenceStars = listOf(
    RefStar("Sirius", 101.287, -16.716, true),
    RefStar("Canopus", 95.99, -52.70, false),
    RefStar("Rigel", 78.63, -8.20, false),
    RefStar("Betelgeuse", 88.79, 7.41, true),
    RefStar("Aldebaran", 68.98, 16.51, false),
    RefStar("Polaris", 37.95, 89.26, true),
    RefStar("Vega", 279.23, 38.78, true),
    RefStar("Altair", 297.70, 8.87, false),
    RefStar("Deneb", 310.36, 45.28, false),
    RefStar("Antares", 247.35, -26.43, true),
    RefStar("Spica", 201.30, -11.16, true),
    RefStar("Arcturus", 213.92, 19.18, true),
    RefStar("Regulus", 152.09, 11.97, false),
    RefStar("Pollux", 116.33, 28.03, false),
    RefStar("Fomalhaut", 344.41, -29.62, false)
)

private val monthNames = arrayOf(
    "January", "February", "March", "April", "May", "June",
    "July", "August", "September", "October", "November", "December"
)

private fun formatRa(raDeg: Double): String {
    val raHours = raDeg / 15.0
    val h = floor(raHours).toInt()
    val m = ((raHours - h) * 60).roundToInt()
    return if (m == 60) "${h + 1}h 0m" else "${h}h ${m}m"
}

private fun formatDec(decDeg: Double): String {
    val sign = if (decDeg >= 0) "+" else "−"
    return "$sign${abs(decDeg).roundToInt()}°"
}

private fun hemisphereRes(decDeg: Double): Int = when {
    decDeg >= 30 -> R.string.sky_chart_hemi_north
    decDeg <= -30 -> R.string.sky_chart_hemi_south
    else -> R.string.sky_chart_hemi_both
}

// Roughly the month a star is highest near midnight (opposite the Sun).
private fun bestMonth(raDeg: Double): String {
    val raHours = raDeg / 15.0
    val monthsAfterMarch = (raHours - 12.0) / 2.0
    val idx = (((2 + monthsAfterMarch).roundToInt() % 12) + 12) % 12
    return monthNames[idx]
}

@Preview
@Composable
private fun SkyChartCardPreview() = PreviewSurface {
    SkyChartCard(ra = 285.0, dec = 42.0, hostName = "Kepler-22")
}
