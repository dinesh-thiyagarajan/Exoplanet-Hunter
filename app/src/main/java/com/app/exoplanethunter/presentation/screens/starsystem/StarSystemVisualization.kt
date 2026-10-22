package com.app.exoplanethunter.presentation.screens.starsystem

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.exoplanethunter.R
import com.app.exoplanethunter.exoplanet.domain.model.Exoplanet
import com.app.exoplanethunter.exoplanet.domain.model.StarSystem
import com.app.exoplanethunter.presentation.components.composition
import com.app.exoplanethunter.presentation.components.onScreenChanged
import com.app.exoplanethunter.presentation.components.estimatedTempK
import com.app.exoplanethunter.presentation.components.formatOrbitalPeriod
import com.app.exoplanethunter.presentation.theme.AlmanacData
import com.app.exoplanethunter.presentation.theme.AlmanacMeta
import com.app.exoplanethunter.presentation.theme.Brass
import com.app.exoplanethunter.presentation.theme.Hairline
import com.app.exoplanethunter.presentation.theme.InkText
import com.app.exoplanethunter.presentation.theme.InkTextFaint
import com.app.exoplanethunter.presentation.theme.Surface as SurfaceColor
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.min
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.ln
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

// ===========================================================================
// Orrery — the system's planets moving on their real orbits around their star.
//
// Physics that's real: orbit sizes (or ones derived from the period by Kepler's
// third law), eccentric orbits with the star at a focus, speeds from the actual
// periods (solved with Kepler's equation), and the habitable zone from the star's
// luminosity. What isn't known is chosen deterministically per planet: where
// each planet starts on its orbit and which way its ellipse points.
//
// Distances are square-root scaled so wide systems still fit; sizes are
// exaggerated so planets are visible at all.
// ===========================================================================

/** Speed options: multiples of the base rate (innermost orbit in ~8 s at 1×). */
private val SPEEDS = listOf(1f, 10f, 100f)
private const val BASE_SECONDS_PER_INNER_ORBIT = 8.0

/** Default viewing angle: 0 = top-down, larger = closer to edge-on. */
private const val DEFAULT_TILT = 0.9f // ~52°
private const val MAX_TILT = 1.35f // ~77°

private data class ReferenceOrbit(val labelRes: Int, val au: Double)

/** Our Solar System's planets, for the "how big is this system?" overlay. */
private val SOLAR_SYSTEM = listOf(
    ReferenceOrbit(R.string.orrery_mercury, 0.387),
    ReferenceOrbit(R.string.orrery_venus, 0.723),
    ReferenceOrbit(R.string.size_scale_earth, 1.0),
    ReferenceOrbit(R.string.orrery_mars, 1.524),
    ReferenceOrbit(R.string.size_scale_jupiter, 5.203),
)

@Composable
internal fun SolarSystemVisualization(
    system: StarSystem,
    onPlanetClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val bodies = remember(system) { buildBodies(system) }
    val hz = remember(system) { habitableZone(system) }
    val circumbinary = remember(system) { isCircumbinary(system) }
    val companions = remember(system) { companionLabels(system) }
    // A close pair circles faster than any planet that can stably orbit both stars.
    val pairPeriodDays = remember(bodies) { (bodies.minOfOrNull { it.periodDays } ?: 30.0) / 6.0 }
    val missing = system.planets.size - bodies.size
    val starColor = getStarColor(system.stellarEffectiveTempK)
    val textMeasurer = rememberTextMeasurer()
    val scope = rememberCoroutineScope()

    // --- Time --------------------------------------------------------------------
    // At 1× the innermost planet takes ~8 s per orbit; outer ones follow at their real
    // relative speeds, so resonant chains like TRAPPIST-1's are visible.
    val baseDaysPerSecond = remember(bodies) {
        (bodies.minOfOrNull { it.periodDays } ?: 365.0) / BASE_SECONDS_PER_INNER_ORBIT
    }
    var days by remember { mutableDoubleStateOf(0.0) }
    var playing by remember { mutableStateOf(true) }
    var speedIndex by remember { mutableIntStateOf(0) }
    val daysPerSecond = baseDaysPerSecond * SPEEDS[speedIndex]

    var onScreen by remember { mutableStateOf(true) }
    LaunchedEffect(Unit) {
        var last = 0L
        while (true) {
            if (!onScreen) {
                // Paused while scrolled away — no redraws nobody can see.
                snapshotFlow { onScreen }.first { it }
                last = 0L
            }
            withFrameNanos { now ->
                if (last != 0L && playing) days += (now - last) / 1e9 * baseDaysPerSecond * SPEEDS[speedIndex]
                last = now
            }
        }
    }

    // --- View --------------------------------------------------------------------
    val tilt = remember { Animatable(DEFAULT_TILT) }
    var zoom by remember { mutableFloatStateOf(1f) }
    var showHz by remember { mutableStateOf(hz != null) }
    var showSolar by remember { mutableStateOf(false) }
    var selectedId by remember { mutableStateOf<Long?>(null) }
    var hitTargets by remember { mutableStateOf<List<Pair<Long, Offset>>>(emptyList()) }

    // How far out the view reaches; widens smoothly to fit the Solar System overlay.
    val systemReach = remember(bodies, hz) {
        max(bodies.maxOfOrNull { it.aAu * (1 + it.e) } ?: 1.0, hz?.endInclusive?.times(0.6) ?: 0.0)
    }
    val solarShown = SOLAR_SYSTEM.filter { it.au <= max(systemReach * 1.25, 1.6) }
    val reach by animateFloatAsState(
        targetValue = (if (showSolar) max(systemReach, solarShown.maxOf { it.au }) else systemReach).toFloat(),
        animationSpec = tween(700, easing = FastOutSlowInEasing),
        label = "reach",
    )

    val transition = rememberInfiniteTransition(label = "star")
    val glowPulse by transition.animateFloat(
        0.85f, 1f, infiniteRepeatable(tween(2500, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "glow"
    )
    val corona by transition.animateFloat(
        0f, 360f, infiniteRepeatable(tween(30_000, easing = LinearEasing)), label = "corona"
    )

    val labelStyle = AlmanacMeta.copy(fontSize = 10.sp)
    val ghostStyle = AlmanacMeta.copy(fontSize = 9.sp, color = InkTextFaint)
    val hzLabel = stringResource(R.string.orrery_hz)
    val offScaleLabel = stringResource(R.string.orrery_companion_off_scale)
    // Companions' temperatures aren't catalogued; most are cooler than the planet host.
    val companionColor = getStarColor(system.stellarEffectiveTempK?.times(0.75) ?: 4500.0)
    val solarLabels = solarShown.map { stringResource(it.labelRes) }

    Box(modifier = modifier.onScreenChanged { onScreen = it }) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures { tap ->
                        val hit = hitTargets.minByOrNull { (_, p) -> (p - tap).getDistance() }
                        selectedId = if (hit != null && (hit.second - tap).getDistance() < 28.dp.toPx()) {
                            if (selectedId == hit.first) {
                                onPlanetClick(hit.first); hit.first
                            } else hit.first
                        } else null
                    }
                }
                // Two fingers: pinch to zoom, drag up/down to tilt. One finger is left
                // alone so the page still scrolls over the orrery.
                .pointerInput(Unit) {
                    awaitEachGesture {
                        awaitFirstDown(requireUnconsumed = false)
                        do {
                            val event = awaitPointerEvent()
                            if (event.changes.count { it.pressed } >= 2) {
                                zoom = (zoom * event.calculateZoom()).coerceIn(0.6f, 6f)
                                val dy = event.calculatePan().y
                                scope.launch { tilt.snapTo((tilt.value + dy * 0.004f).coerceIn(0f, MAX_TILT)) }
                                event.changes.forEach { if (it.positionChanged()) it.consume() }
                            }
                        } while (event.changes.any { it.pressed })
                    }
                }
        ) {
            val c = center
            val t = tilt.value
            val squash = cos(t)
            val starPx = (8f + 5f * ln(1f + (system.stellarRadiusSolar ?: 1.0).toFloat())).coerceIn(8f, 20f).dp.toPx()
            // A central pair needs room inside the innermost orbit.
            val innerPx = if (circumbinary) starPx * 2.6f + 12.dp.toPx() else starPx + 12.dp.toPx()
            val outerPx = size.width / 2f - 18.dp.toPx()
            val k = (outerPx - innerPx) / sqrt(reach.toDouble()).toFloat() * zoom

            // AU in the orbital plane → screen, with square-root distance scaling.
            fun project(x: Double, y: Double): Offset {
                val r = hypot(x, y)
                if (r == 0.0) return c
                val s = innerPx + k * sqrt(r).toFloat()
                val th = atan2(y, x)
                return Offset(c.x + s * cos(th).toFloat(), c.y + s * sin(th).toFloat() * squash)
            }
            fun ringRadius(au: Double) = innerPx + k * sqrt(au).toFloat()

            // Habitable zone: a soft green band between its inner and outer edge.
            if (showHz && hz != null) {
                val rIn = ringRadius(hz.start)
                val rOut = ringRadius(hz.endInclusive)
                val band = Path().apply {
                    fillType = PathFillType.EvenOdd
                    addOval(Rect(c.x - rOut, c.y - rOut * squash, c.x + rOut, c.y + rOut * squash))
                    addOval(Rect(c.x - rIn, c.y - rIn * squash, c.x + rIn, c.y + rIn * squash))
                }
                drawPath(band, HZ_GREEN.copy(alpha = 0.13f))
                drawText(
                    textMeasurer, hzLabel,
                    topLeft = Offset(c.x - rOut * 0.71f, c.y + rOut * squash * 0.71f),
                    style = ghostStyle.copy(color = HZ_GREEN.copy(alpha = 0.8f)),
                )
            }

            // Our Solar System's orbits, ghosted, at the same scale.
            if (showSolar) {
                solarShown.forEachIndexed { i, ref ->
                    val r = ringRadius(ref.au)
                    drawOval(
                        color = Color.White.copy(alpha = 0.22f),
                        topLeft = Offset(c.x - r, c.y - r * squash),
                        size = Size(r * 2, r * 2 * squash),
                        style = Stroke(1.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(2f, 7f))),
                    )
                    drawText(textMeasurer, solarLabels[i], topLeft = Offset(c.x + r + 3f, c.y - 6f), style = ghostStyle)
                }
            }

            // Orbits
            bodies.forEach { b ->
                val path = Path()
                for (step in 0..120) {
                    val p = b.orbitPoint(step / 120.0 * 2 * PI)
                    val o = project(p.first, p.second)
                    if (step == 0) path.moveTo(o.x, o.y) else path.lineTo(o.x, o.y)
                }
                val selected = b.planet.id == selectedId
                drawPath(
                    path,
                    color = if (selected) Brass.copy(alpha = 0.7f) else Color.White.copy(alpha = 0.14f),
                    style = Stroke(
                        width = (if (selected) 1.5f else 1f).dp.toPx(),
                        // A dashed orbit marks a size derived from the period, not measured.
                        pathEffect = if (b.aEstimated) PathEffect.dashPathEffect(floatArrayOf(6f, 6f)) else null,
                    ),
                )
            }

            // Planets on the far side pass behind the star; near-side ones in front of it.
            val placed = bodies.map { b ->
                val (x, y) = b.positionAt(days)
                Triple(b, project(x, y), y < 0)
            }
            val targets = ArrayList<Pair<Long, Offset>>(placed.size)
            fun drawBodies(far: Boolean) = placed.filter { it.third == far }.forEach { (b, pos, _) ->
                drawPlanet(b, pos, c, zoom, selected = b.planet.id == selectedId, textMeasurer, labelStyle)
                targets += b.planet.id to pos
            }
            // Distant companions: far beyond these orbits, so marked at the edge of the view.
            companions.forEachIndexed { i, letter ->
                drawCompanionMarker(c, innerPx + k * sqrt(reach.toDouble()).toFloat(), i, letter, companionColor, textMeasurer, ghostStyle, offScaleLabel)
            }
            drawBodies(far = true)
            if (circumbinary) {
                // Two stars circling their common centre, the planets orbiting both.
                val phase = (2 * PI * days / pairPeriodDays).toFloat()
                val sep = starPx * 1.9f
                val a = Offset(c.x + sep * 0.42f * cos(phase), c.y + sep * 0.42f * sin(phase) * squash)
                val b = Offset(c.x - sep * 0.58f * cos(phase), c.y - sep * 0.58f * sin(phase) * squash)
                // The one further back is drawn first.
                val order = if (sin(phase) > 0) listOf(false, true) else listOf(true, false)
                order.forEach { primary ->
                    if (primary) drawStar(a.x, a.y, starPx * 0.8f, starColor, glowPulse, corona, isPrimary = true)
                    else drawStar(b.x, b.y, starPx * 0.6f, companionColor, glowPulse, -corona * 0.7f, isPrimary = false)
                }
            } else {
                drawStar(c.x, c.y, starPx, starColor, glowPulse, corona, isPrimary = true)
            }
            drawBodies(far = false)
            hitTargets = targets
        }

        // --- Controls --------------------------------------------------------------
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopStart)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(stringResource(R.string.orrery_day, "%,d".format(days.toLong())), style = AlmanacData.copy(fontSize = 15.sp))
                Text(
                    stringResource(R.string.orrery_rate, formatOrbitalPeriod(daysPerSecond)),
                    style = AlmanacMeta.copy(fontSize = 10.sp),
                )
            }
            ControlButton(onClick = { playing = !playing }) {
                Icon(
                    if (playing) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = stringResource(if (playing) R.string.orrery_pause else R.string.orrery_play),
                    tint = Brass,
                    modifier = Modifier.size(18.dp),
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            ControlButton(onClick = { speedIndex = (speedIndex + 1) % SPEEDS.size }) {
                Text("%d×".format(SPEEDS[speedIndex].toInt()), style = AlmanacMeta.copy(color = Brass))
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomStart)
                .padding(horizontal = 12.dp, vertical = 8.dp),
        ) {
            val selected = bodies.firstOrNull { it.planet.id == selectedId }
            if (selected != null) {
                SelectedPlanetCard(selected, onOpen = { onPlanetClick(selected.planet.id) })
            } else {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (hz != null) ToggleChip(stringResource(R.string.orrery_hz), showHz) { showHz = !showHz }
                    ToggleChip(stringResource(R.string.orrery_solar_system), showSolar) { showSolar = !showSolar }
                    ToggleChip(stringResource(R.string.orrery_3d), tilt.targetValue > 0.1f) {
                        scope.launch { tilt.animateTo(if (tilt.targetValue > 0.1f) 0f else DEFAULT_TILT, tween(600)) }
                    }
                }
                val notes = listOfNotNull(
                    stringResource(R.string.orrery_scale_note),
                    when {
                        circumbinary -> stringResource(R.string.orrery_circumbinary_note)
                        companions.isNotEmpty() -> pluralStringResource(R.plurals.orrery_companions_note, companions.size, companions.size)
                        else -> null
                    },
                    if (missing > 0) stringResource(R.string.orrery_missing_note, missing) else null,
                )
                Text(
                    notes.joinToString(" · "),
                    style = AlmanacMeta.copy(fontSize = 10.sp),
                    modifier = Modifier.padding(top = 6.dp),
                )
            }
        }
    }
}

private val HZ_GREEN = Color(0xFF7FB069)

/**
 * A distant companion star, drawn at the edge of the view — real separations are typically
 * tens to thousands of AU, far outside the frame — with a dashed line pointing back to it.
 */
private fun DrawScope.drawCompanionMarker(
    c: Offset,
    systemEdgePx: Float,
    index: Int,
    letter: String,
    color: Color,
    textMeasurer: TextMeasurer,
    style: TextStyle,
    offScaleLabel: String,
) {
    // Upper right first, then lower left: clear of the time controls and the toggles.
    val angle = if (index % 2 == 0) -0.6 else PI - 0.55
    val dir = Offset(cos(angle).toFloat(), sin(angle).toFloat())
    val margin = 24.dp.toPx()
    val reachX = (size.width / 2f - margin) / abs(dir.x).coerceAtLeast(0.01f)
    val reachY = (size.height / 2f - margin - 48.dp.toPx()) / abs(dir.y).coerceAtLeast(0.01f)
    val pos = c + dir * min(reachX, reachY)
    val start = c + dir * min(systemEdgePx, (pos - c).getDistance() - 16.dp.toPx())
    drawLine(
        color = color.copy(alpha = 0.35f),
        start = start,
        end = pos - dir * 10.dp.toPx(),
        strokeWidth = 1.dp.toPx(),
        pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 8f)),
    )
    drawCircle(
        brush = Brush.radialGradient(listOf(color.copy(alpha = 0.5f), Color.Transparent), center = pos, radius = 14.dp.toPx()),
        radius = 14.dp.toPx(),
        center = pos,
    )
    drawCircle(color, radius = 4.dp.toPx(), center = pos)
    val title = textMeasurer.measure(letter, style.copy(color = InkText))
    val note = textMeasurer.measure(offScaleLabel, style)
    val textX = if (dir.x > 0) pos.x - 10.dp.toPx() - note.size.width else pos.x + 10.dp.toPx()
    drawText(title, topLeft = Offset(if (dir.x > 0) pos.x - 10.dp.toPx() - title.size.width else textX, pos.y - title.size.height))
    drawText(note, topLeft = Offset(textX, pos.y))
}

/** A compact on/off chip for the orrery's overlays — smaller than the list filter chips. */
@Composable
private fun ToggleChip(label: String, on: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(if (on) Brass.copy(alpha = 0.18f) else SurfaceColor.copy(alpha = 0.85f))
            .border(0.5.dp, if (on) Brass else Hairline, RoundedCornerShape(6.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp),
    ) {
        Text(label.uppercase(), style = AlmanacMeta.copy(fontSize = 10.sp, color = if (on) Brass else InkTextFaint))
    }
}

@Composable
private fun ControlButton(onClick: () -> Unit, content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .size(34.dp)
            .clip(CircleShape)
            .background(SurfaceColor.copy(alpha = 0.85f))
            .border(0.5.dp, Brass, CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) { content() }
}

@Composable
private fun SelectedPlanetCard(body: OrbitBody, onOpen: () -> Unit) {
    val planet = body.planet
    val composition = remember(planet) { planet.composition().first }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(SurfaceColor.copy(alpha = 0.95f))
            .border(0.5.dp, Hairline, RoundedCornerShape(8.dp))
            .clickable(onClick = onOpen)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(body.color))
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                planet.planetName,
                style = MaterialTheme.typography.titleMedium,
                color = InkText,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            val details = listOfNotNull(
                stringResource(composition.labelRes),
                stringResource(R.string.orrery_year, formatOrbitalPeriod(body.periodDays)),
                planet.estimatedTempK()?.let { "%,d°C".format((it - 273.15).toInt()) },
            )
            Text(details.joinToString(" · "), style = AlmanacMeta, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Text(stringResource(R.string.star_system_view_arrow), style = AlmanacMeta.copy(color = Brass))
    }
}

/**
 * A planet lit by its star: the bright side faces the star, so planets show phases as they
 * go round. Sizes grow with log radius — exaggerated, but ordered.
 */
private fun DrawScope.drawPlanet(
    b: OrbitBody,
    pos: Offset,
    star: Offset,
    zoom: Float,
    selected: Boolean,
    textMeasurer: TextMeasurer,
    labelStyle: TextStyle,
) {
    val r = (2.5f + 3.2f * ln(1f + (b.radiusEarth ?: 1.0).toFloat())).dp.toPx() * zoom.pow(0.3f)
    val toStar = (star - pos).let { d -> val len = d.getDistance(); if (len < 1f) Offset(-1f, 0f) else d / len }

    if (b.rings) {
        drawOval(
            color = Color(0xFFDCCDAE).copy(alpha = 0.55f),
            topLeft = Offset(pos.x - r * 2.1f, pos.y - r * 0.55f),
            size = Size(r * 4.2f, r * 1.1f),
            style = Stroke(r * 0.28f),
        )
    }
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(lighten(b.color), b.color, b.color.copy(red = b.color.red * 0.18f, green = b.color.green * 0.18f, blue = b.color.blue * 0.2f)),
            center = pos + toStar * (r * 0.55f),
            radius = r * 1.6f,
        ),
        radius = r,
        center = pos,
    )
    if (selected) {
        drawCircle(Brass, radius = r + 4.dp.toPx(), center = pos, style = Stroke(1.5.dp.toPx()))
    }
    val layout = textMeasurer.measure(b.label, if (selected) labelStyle.copy(color = Brass) else labelStyle)
    drawText(layout, topLeft = Offset(pos.x + r + 3.dp.toPx(), pos.y - r - layout.size.height * 0.6f))
}

private fun lighten(c: Color) = Color(
    red = c.red + (1 - c.red) * 0.35f,
    green = c.green + (1 - c.green) * 0.35f,
    blue = c.blue + (1 - c.blue) * 0.35f,
)
