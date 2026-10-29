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
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.CornerRadius
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
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.exoplanethunter.R
import com.app.exoplanethunter.analytics.domain.model.OrreryToggle
import com.app.exoplanethunter.exoplanet.domain.model.Exoplanet
import com.app.exoplanethunter.exoplanet.domain.model.StarSystem
import com.app.exoplanethunter.presentation.components.composition
import com.app.exoplanethunter.presentation.components.onScreenChanged
import com.app.exoplanethunter.presentation.components.estimatedTempK
import com.app.exoplanethunter.presentation.components.formatOrbitalPeriod
import com.app.exoplanethunter.presentation.theme.AlmanacData
import com.app.exoplanethunter.presentation.theme.AlmanacMeta
import com.app.exoplanethunter.presentation.theme.AlmanacCaption
import com.app.exoplanethunter.presentation.theme.BodyFamily
import com.app.exoplanethunter.presentation.theme.Corners
import com.app.exoplanethunter.presentation.theme.InkTextDim
import com.app.exoplanethunter.presentation.theme.Line
import com.app.exoplanethunter.presentation.theme.Matcha
import com.app.exoplanethunter.presentation.theme.MatchaDeep
import com.app.exoplanethunter.presentation.theme.OnMatcha
import com.app.exoplanethunter.presentation.theme.OnMatchaDeep
import com.app.exoplanethunter.presentation.theme.RowName
import com.app.exoplanethunter.presentation.theme.Surface
import com.app.exoplanethunter.presentation.theme.TempTemperate
import com.app.exoplanethunter.presentation.theme.Well
import com.app.exoplanethunter.presentation.components.AppIcons
import com.app.exoplanethunter.presentation.components.CircleIconButton
import com.app.exoplanethunter.presentation.components.PlanetDisc
import com.app.exoplanethunter.presentation.components.ToggleChip
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.height
import androidx.compose.ui.text.font.FontWeight
import com.app.exoplanethunter.presentation.theme.Hairline
import com.app.exoplanethunter.presentation.theme.InkText
import com.app.exoplanethunter.presentation.theme.InkTextFaint
import com.app.exoplanethunter.presentation.theme.SpaceBlack
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
    modifier: Modifier = Modifier,
    onToggle: (OrreryToggle, Boolean) -> Unit = { _, _ -> },
) {
    val bodies = remember(system) { buildBodies(system) }
    val hz = remember(system) { habitableZone(system) }
    val circumbinary = remember(system) { isCircumbinary(system) }
    val companions = remember(system) { companionsOf(system) }
    val companionAngles = remember(system) { companionAngles(system.hostName, companions.size) }
    val knownPairPeriod = KNOWN_STARS[system.hostName]?.binaryPeriodDays
    val pairPeriodDays = remember(system, bodies) { pairPeriodDays(system, bodies.minOfOrNull { it.periodDays }) }
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
    // Off by default: the band can dominate the view; one tap shows it.
    var showHz by remember { mutableStateOf(false) }
    var showSolar by remember { mutableStateOf(false) }
    var selectedId by remember { mutableStateOf<Long?>(null) }
    var hitTargets by remember { mutableStateOf<List<Pair<Long, Offset>>>(emptyList()) }

    // How far out the view reaches; widens smoothly to fit the Solar System overlay.
    val systemReach = remember(bodies, hz) {
        max(bodies.maxOfOrNull { it.aAu * (1 + it.e) } ?: 1.0, hz?.endInclusive?.times(0.6) ?: 0.0)
    }
    val solarShown = SOLAR_SYSTEM.filter { it.au <= max(systemReach * 1.25, 1.6) }
    val reach by animateFloatAsState(
        targetValue = listOfNotNull(
            systemReach,
            // The whole habitable zone, so its band ends inside the view rather than at the edge.
            if (showHz) hz?.endInclusive else null,
            if (showSolar) solarShown.maxOf { it.au } else null,
        ).max().toFloat(),
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
    val starLabel = stringResource(R.string.orrery_star_letter)
    val distanceLabel = stringResource(R.string.orrery_companion_distance)
    // Companions' temperatures aren't catalogued; most are cooler than the planet host.
    val companionColor = getStarColor(system.stellarEffectiveTempK?.times(0.75) ?: 4500.0)
    val solarLabels = solarShown.map { stringResource(it.labelRes) }

    Column(modifier = modifier.onScreenChanged { onScreen = it }) {
      Box(
          modifier = Modifier
              .fillMaxWidth()
              .height(ORRERY_HEIGHT)
              .clip(Corners.Card)
              .background(Well)
      ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                // Keep everything inside the view, whatever the zoom.
                .clipToBounds()
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
            val t = tilt.value
            val squash = cos(t)
            // Draw in the clear area between the time controls and the toggles, with a margin
            // at the sides, so orbits and bands never run under the controls or to the edge.
            val top = 44.dp.toPx()
            val bottom = size.height - 16.dp.toPx()
            val c = Offset(size.width / 2f, (top + bottom) / 2f)
            val starPx = (8f + 5f * ln(1f + (system.stellarRadiusSolar ?: 1.0).toFloat())).coerceIn(8f, 20f).dp.toPx()
            // A central pair needs room inside the innermost orbit.
            val innerPx = if (circumbinary) starPx * 2.6f + 12.dp.toPx() else starPx + 12.dp.toPx()
            // Fit whichever is tighter. Tilted, orbits are squashed vertically, so they can use
            // more of the height; on tablets and in landscape the height is what limits them.
            val outerPx = min(size.width / 2f - 28.dp.toPx(), (bottom - top) / 2f / squash)
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
                drawPath(band, HZ_GREEN.copy(alpha = 0.16f))
                // At the band's left end, away from the Solar System labels in the middle.
                val hzLayout = textMeasurer.measure(hzLabel, ghostStyle.copy(color = HZ_GREEN))
                // Skip it when the band is too small to hold it (it'd cover the planets).
                if ((rIn + rOut) / 2f > hzLayout.size.width) {
                    drawLabel(hzLayout, Offset(c.x - (rIn + rOut) / 2f - hzLayout.size.width / 2f, c.y - hzLayout.size.height / 2f), backdrop = true)
                }
            }

            // Our Solar System's orbits, ghosted, at the same scale.
            if (showSolar) {
                val solarStyle = ghostStyle.copy(color = SOLAR_BLUE)
                solarShown.forEachIndexed { i, ref ->
                    val r = ringRadius(ref.au)
                    drawOval(
                        color = SOLAR_BLUE.copy(alpha = 0.45f),
                        topLeft = Offset(c.x - r, c.y - r * squash),
                        size = Size(r * 2, r * 2 * squash),
                        style = Stroke(1.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(3f, 6f))),
                    )
                    // Labels alternate below and above the star, so neighbouring rings'
                    // labels are two rings apart and never collide.
                    val below = i % 2 == 0
                    val anchor = Offset(c.x, c.y + (if (below) r else -r) * squash)
                    drawCircle(SOLAR_BLUE, radius = 2.dp.toPx(), center = anchor)
                    val layout = textMeasurer.measure(solarLabels[i], solarStyle)
                    drawLabel(
                        layout,
                        Offset(anchor.x - layout.size.width / 2f, if (below) anchor.y + 3.dp.toPx() else anchor.y - 3.dp.toPx() - layout.size.height),
                        backdrop = true,
                    )
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
                    color = if (selected) InkText.copy(alpha = 0.6f) else Line,
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
            // Distant companions: far beyond these orbits, so drawn at the edge of the view.
            companions.forEachIndexed { i, companion ->
                val note = companion.distanceAu?.let { distanceLabel.format("%,d".format(it)) } ?: offScaleLabel
                drawCompanionStar(
                    c, companionAngles[i], companion.letter, companionColor, starPx * 0.6f,
                    glowPulse, -corona * 0.7f, textMeasurer, ghostStyle, starLabel, note,
                )
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

        // Day counter, top left of the canvas.
        Column(modifier = Modifier.align(Alignment.TopStart).padding(start = 16.dp, top = 14.dp)) {
            Text(stringResource(R.string.orrery_day, "%,d".format(days.toLong())), style = AlmanacData.copy(fontSize = 16.sp))
            Text(stringResource(R.string.orrery_rate, formatOrbitalPeriod(daysPerSecond)), style = AlmanacCaption)
        }
      }

        // --- Controls, below the orbits ------------------------------------------------
        Spacer(Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            CircleIconButton(
                icon = if (playing) AppIcons.Pause else AppIcons.Play,
                contentDescription = stringResource(if (playing) R.string.orrery_pause else R.string.orrery_play),
                onClick = { playing = !playing },
                container = Matcha,
                tint = OnMatcha,
            )
            SpeedSegments(selected = speedIndex, onSelect = { speedIndex = it })
        }
        Spacer(Modifier.height(10.dp))
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (hz != null) ToggleChip(stringResource(R.string.orrery_hz), showHz) {
                showHz = !showHz
                onToggle(OrreryToggle.HABITABLE_ZONE, showHz)
            }
            ToggleChip(stringResource(R.string.orrery_solar_system), showSolar) {
                showSolar = !showSolar
                onToggle(OrreryToggle.SOLAR_SYSTEM, showSolar)
            }
            ToggleChip(stringResource(R.string.orrery_3d), tilt.targetValue > 0.1f) {
                val to3d = tilt.targetValue <= 0.1f
                onToggle(OrreryToggle.TILT_3D, to3d)
                scope.launch { tilt.animateTo(if (to3d) DEFAULT_TILT else 0f, tween(600)) }
            }
        }
        val selected = bodies.firstOrNull { it.planet.id == selectedId }
        if (selected != null) {
            Spacer(Modifier.height(12.dp))
            SelectedPlanetCard(selected, onOpen = { onPlanetClick(selected.planet.id) })
        }
        val notes = listOfNotNull(
            if (showSolar) stringResource(R.string.orrery_solar_legend) else null,
            stringResource(R.string.orrery_scale_note),
            if (showHz && hz != null) stringResource(R.string.orrery_hz_legend) else null,
            when {
                circumbinary && knownPairPeriod != null ->
                    stringResource(R.string.orrery_circumbinary_period_note, formatOrbitalPeriod(knownPairPeriod))
                circumbinary -> stringResource(R.string.orrery_circumbinary_note)
                companions.isNotEmpty() -> pluralStringResource(R.plurals.orrery_companions_note, companions.size, companions.size)
                else -> null
            },
            if (missing > 0) stringResource(R.string.orrery_missing_note, missing) else null,
        )
        Text(
            notes.joinToString(" · "),
            style = AlmanacCaption,
            modifier = Modifier.padding(top = 10.dp),
        )
    }
}

private val ORRERY_HEIGHT = 380.dp

/** The 1× / 10× / 100× segmented control. */
@Composable
private fun SpeedSegments(selected: Int, onSelect: (Int) -> Unit) {
    Row(
        modifier = Modifier.clip(Corners.Pill).background(Surface).padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        SPEEDS.forEachIndexed { i, speed ->
            val on = i == selected
            Box(
                modifier = Modifier
                    .size(width = 52.dp, height = 40.dp)
                    .clip(Corners.Pill)
                    .background(if (on) MatchaDeep else Color.Transparent)
                    .clickable { onSelect(i) },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    "%d×".format(speed.toInt()),
                    fontFamily = BodyFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = if (on) OnMatchaDeep else InkTextDim,
                )
            }
        }
    }
}

private val HZ_GREEN = TempTemperate
private val SOLAR_BLUE = Color(0xFF7FA8D8)

/**
 * Draws a distant companion star. Real separations are typically tens to thousands of AU —
 * far outside the frame — so the star sits at the edge of the view in its direction, with a
 * dashed line back towards the planets' star and a label saying how far it really is.
 */
private fun DrawScope.drawCompanionStar(
    c: Offset,
    angle: Float,
    letter: String,
    color: Color,
    radius: Float,
    glowPulse: Float,
    corona: Float,
    textMeasurer: TextMeasurer,
    style: TextStyle,
    starLabel: String,
    offScaleLabel: String,
) {
    val dir = Offset(cos(angle), sin(angle))
    // Keep clear of the time controls (top) and the toggles (bottom), with room for the glow.
    val glow = radius * 1.8f
    val marginX = 12.dp.toPx() + glow
    val marginTop = 48.dp.toPx() + glow
    val marginBottom = 16.dp.toPx() + glow
    val reachX = (size.width / 2f - marginX) / abs(dir.x).coerceAtLeast(0.01f)
    val reachY = (if (dir.y < 0) c.y - marginTop else size.height - c.y - marginBottom) / abs(dir.y).coerceAtLeast(0.01f)
    val star = c + dir * min(reachX, reachY)

    // Dashed: "much further than it looks".
    drawLine(
        color = color.copy(alpha = 0.45f),
        start = star - dir * (glow + 40.dp.toPx()),
        end = star - dir * (glow + 2.dp.toPx()),
        strokeWidth = 1.5.dp.toPx(),
        pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f)),
    )
    drawStar(star.x, star.y, radius, color, glowPulse, corona, isPrimary = false)

    val title = textMeasurer.measure(starLabel.format(letter), style.copy(color = InkText))
    val note = textMeasurer.measure(offScaleLabel, style)
    // Label above or below the star, aligned to the edge it sits against.
    val alignRight = dir.x > 0
    val w = max(title.size.width, note.size.width)
    val h = title.size.height + note.size.height
    val gap = glow + 2.dp.toPx()
    val x = if (alignRight) star.x + radius - w else star.x - radius
    val y = if (dir.y < 0) star.y - h - gap else star.y + gap
    drawLabel(title, Offset(if (alignRight) x + w - title.size.width else x, y), backdrop = true)
    drawLabel(note, Offset(if (alignRight) x + w - note.size.width else x, y + title.size.height), backdrop = true)
}

/**
 * Draws pre-measured text, nudged so it always stays fully inside the canvas. With
 * [backdrop], a dark plate behind it keeps it readable over orbits and bands.
 */
private fun DrawScope.drawLabel(layout: TextLayoutResult, topLeft: Offset, backdrop: Boolean = false) {
    val x = topLeft.x.coerceIn(0f, (size.width - layout.size.width).coerceAtLeast(0f))
    val y = topLeft.y.coerceIn(0f, (size.height - layout.size.height).coerceAtLeast(0f))
    if (backdrop) {
        val pad = 3.dp.toPx()
        drawRoundRect(
            color = SpaceBlack.copy(alpha = 0.8f),
            topLeft = Offset(x - pad, y),
            size = Size(layout.size.width + pad * 2, layout.size.height.toFloat()),
            cornerRadius = CornerRadius(pad),
        )
    }
    drawText(layout, topLeft = Offset(x, y))
}

@Composable
private fun SelectedPlanetCard(body: OrbitBody, onOpen: () -> Unit) {
    val planet = body.planet
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(Corners.SmallCard)
            .background(Surface)
            .clickable(onClick = onOpen)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        PlanetDisc(color = body.color, size = 36.dp)
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(planet.planetName, style = RowName, maxLines = 1, overflow = TextOverflow.Ellipsis)
            val details = listOfNotNull(
                planet.planetRadiusEarth?.let { "%.2f R\u2295".format(it) },
                stringResource(R.string.orrery_year, formatOrbitalPeriod(body.periodDays)),
                planet.estimatedTempK()?.let { (if (planet.equilibriumTempK == null) "\u2248 " else "") + "%,d K".format(it.toInt()) },
            )
            Text(details.joinToString(" · "), style = AlmanacMeta, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Text(stringResource(R.string.orrery_open), fontFamily = BodyFamily, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Matcha)
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
        drawCircle(InkText, radius = r + 4.dp.toPx(), center = pos, style = Stroke(2.dp.toPx()))
    }
    val layout = textMeasurer.measure(b.label, if (selected) labelStyle.copy(color = InkText) else labelStyle)
    drawLabel(layout, Offset(pos.x + r + 3.dp.toPx(), pos.y - r - layout.size.height * 0.6f))
}

private fun lighten(c: Color) = Color(
    red = c.red + (1 - c.red) * 0.35f,
    green = c.green + (1 - c.green) * 0.35f,
    blue = c.blue + (1 - c.blue) * 0.35f,
)
