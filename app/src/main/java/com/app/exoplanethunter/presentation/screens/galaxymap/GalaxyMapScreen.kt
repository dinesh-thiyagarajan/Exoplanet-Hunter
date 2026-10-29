package com.app.exoplanethunter.presentation.screens.galaxymap

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import com.app.exoplanethunter.presentation.theme.Ink
import com.app.exoplanethunter.presentation.theme.InkText
import com.app.exoplanethunter.presentation.theme.StarOrange
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.draw.shadow
import com.app.exoplanethunter.presentation.components.BackTopBar
import com.app.exoplanethunter.presentation.components.PrimaryButton
import com.app.exoplanethunter.presentation.components.starClassColor
import com.app.exoplanethunter.presentation.theme.BodyFamily
import com.app.exoplanethunter.presentation.theme.Corners
import com.app.exoplanethunter.presentation.theme.InkTextDim
import com.app.exoplanethunter.presentation.theme.InkTextFaint
import com.app.exoplanethunter.presentation.theme.Line
import com.app.exoplanethunter.presentation.theme.Matcha
import com.app.exoplanethunter.presentation.theme.SectionTitle
import com.app.exoplanethunter.presentation.theme.StarClassG
import com.app.exoplanethunter.presentation.theme.Surface
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.exoplanethunter.R
import com.app.exoplanethunter.exoplanet.domain.model.StarPosition
import com.app.exoplanethunter.presentation.components.graticule
import com.app.exoplanethunter.presentation.components.screenContentInsets
import com.app.exoplanethunter.presentation.components.topBarInsets
import com.app.exoplanethunter.presentation.theme.AlmanacEyebrow
import com.app.exoplanethunter.presentation.theme.InkText
import com.app.exoplanethunter.presentation.theme.CosmicCyan
import com.app.exoplanethunter.presentation.theme.SpaceBlack
import com.app.exoplanethunter.presentation.theme.StarGold
import com.app.exoplanethunter.presentation.theme.SurfaceCard
import com.app.exoplanethunter.presentation.theme.TextMuted
import com.app.exoplanethunter.presentation.theme.TextSecondary
import org.koin.androidx.compose.koinViewModel
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

private const val PARSEC_TO_LY = 3.26156

@Composable
fun GalaxyMapScreen(
    onSystemClick: (Long) -> Unit,
    onBack: () -> Unit,
    viewModel: GalaxyMapViewModel = koinViewModel()
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SpaceBlack)
    ) {
        if (viewModel.isLoading) {
            CircularProgressIndicator(
                color = Matcha,
                modifier = Modifier.align(Alignment.Center)
            )
        } else if (viewModel.stars.isEmpty()) {
            Text(
                text = stringResource(R.string.galaxy_map_empty),
                color = TextSecondary,
                modifier = Modifier.align(Alignment.Center).padding(32.dp)
            )
        } else {
            GalaxyMapContent(
                stars = viewModel.stars,
                onStarSelected = viewModel::onStarSelected,
                onSystemClick = onSystemClick
            )
        }

        // Top bar
        BackTopBar(
            onBack = onBack,
            backDescription = stringResource(R.string.cd_back),
            title = stringResource(R.string.galaxy_map_title),
            subtitle = if (viewModel.stars.isNotEmpty()) stringResource(R.string.galaxy_map_hint, viewModel.stars.size) else null,
            modifier = Modifier.topBarInsets(),
        )
    }
}

@Composable
private fun GalaxyMapContent(
    stars: List<StarPosition>,
    onStarSelected: () -> Unit,
    onSystemClick: (Long) -> Unit
) {
    val textMeasurer = rememberTextMeasurer()
    val galacticCentreLabel = stringResource(R.string.galaxy_map_galactic_centre)
    val sunLabel = stringResource(R.string.galaxy_map_sun)
    // Scale to where most stars are, not the farthest outlier: the field fills the screen and
    // the few distant stars simply sit past its edge until you zoom out.
    val reach = remember(stars) {
        val d = stars.map { it.distanceParsec }.sorted()
        (if (d.isEmpty()) 1.0 else d[((d.size - 1) * 0.8).toInt()]).coerceAtLeast(1.0)
    }
    val backdrop = remember { backgroundStars() }

    var yaw by remember { mutableFloatStateOf(0f) }
    var pitch by remember { mutableFloatStateOf(DEFAULT_PITCH) }
    var zoom by remember { mutableFloatStateOf(1f) }
    var autoRotate by remember { mutableStateOf(true) }
    var selectedId by remember { mutableStateOf<Long?>(null) }

    // Mutable, non-state map of on-screen star centers, refreshed every frame for hit-testing.
    val screenPositions = remember { HashMap<Long, Offset>() }

    val infinite = rememberInfiniteTransition(label = "galaxy_spin")
    val autoSpin by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 140_000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "auto_spin"
    )

    val selectedStar = remember(selectedId, stars) { stars.firstOrNull { it.id == selectedId } }

    Box(modifier = Modifier.fillMaxSize()) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(stars) {
                    detectTapGestures { tap ->
                        val hit = screenPositions.entries.minByOrNull { (_, p) -> (p - tap).getDistance() }
                        val newId = if (hit != null && (hit.value - tap).getDistance() < 24.dp.toPx()) {
                            autoRotate = false
                            hit.key
                        } else null
                        if (newId != null && newId != selectedId) onStarSelected()
                        selectedId = newId
                    }
                }
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, gestureZoom, _ ->
                        autoRotate = false
                        yaw += pan.x * 0.3f
                        pitch = (pitch + pan.y * 0.2f).coerceIn(6f, 85f)
                        zoom = (zoom * gestureZoom).coerceIn(0.4f, 5f)
                    }
                }
        ) {
            val cx = size.width / 2f
            // A little above centre, leaving room for the card at the bottom.
            val cy = size.height * 0.46f

            // A still, faint field of background stars for depth.
            backdrop.forEach { (x, y, a) ->
                drawCircle(InkText.copy(alpha = a), 0.9.dp.toPx(), Offset(x * size.width, y * size.height))
            }

            val effYaw = (yaw + if (autoRotate) autoSpin else 0f) * (PI / 180.0)
            val pitchR = pitch * (PI / 180.0)
            val cosYaw = cos(effYaw); val sinYaw = sin(effYaw)
            val cosPitch = cos(pitchR); val sinPitch = sin(pitchR)
            val norm = 1.0 / reach
            val cam = 3.2
            // The 80th-percentile star sits at ~90% of the half-width at zoom 1.
            val scale = size.width * 0.45f * zoom

            screenPositions.clear()

            /** World (units of [reach]) → screen, with a gentle perspective; returns depth too. */
            fun project(wx: Double, wy: Double, wz: Double): Pair<Offset, Float> {
                val x1 = wx * cosYaw + wz * sinYaw
                val z1 = -wx * sinYaw + wz * cosYaw
                val y2 = wy * cosPitch - z1 * sinPitch
                val z2 = wy * sinPitch + z1 * cosPitch
                val depth = ((cam + z2) / cam).toFloat().coerceAtLeast(0.2f)
                val k = scale / depth
                return Offset(cx + (x1 * k).toFloat(), cy - (y2 * k).toFloat()) to depth
            }

            // Three scale rings at round distances around the Sun, labelled at their right end.
            val ringStyle = TextStyle(color = InkTextFaint, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            scaleRingsLy(reach * PARSEC_TO_LY * 1.1).forEach { ly ->
                val r = ly / PARSEC_TO_LY * norm
                val ring = androidx.compose.ui.graphics.Path()
                var right = Offset(-1f, 0f)
                for (i in 0..120) {
                    val t = i / 120.0 * 2 * PI
                    val (o, _) = project(r * cos(t), 0.0, r * sin(t))
                    if (i == 0) ring.moveTo(o.x, o.y) else ring.lineTo(o.x, o.y)
                    if (o.x > right.x) right = o
                }
                drawPath(ring, InkTextFaint.copy(alpha = 0.32f), style = Stroke(width = 1.dp.toPx()))
                val layout = textMeasurer.measure("$ly ly", ringStyle)
                val x = (right.x + 6.dp.toPx()).coerceAtMost(size.width - layout.size.width - 8.dp.toPx())
                if (x > 0f) drawText(layout, topLeft = Offset(x, (right.y - layout.size.height / 2f).coerceIn(0f, (size.height - layout.size.height).coerceAtLeast(0f))))
            }

            // Project every star, then paint far-to-near (painter's algorithm).
            val projected = stars.map { s ->
                val raR = s.ra * (PI / 180.0)
                val decR = s.dec * (PI / 180.0)
                val (o, depth) = project(
                    s.distanceParsec * cos(decR) * cos(raR) * norm,
                    s.distanceParsec * sin(decR) * norm,
                    s.distanceParsec * cos(decR) * sin(raR) * norm,
                )
                val r = ((1.6f + min(s.numPlanets, 6) * 0.35f) / depth * zoom.coerceAtMost(2f).let { 0.8f + it * 0.2f })
                    .coerceIn(1.2f, 5f).dp.toPx()
                ProjectedStar(s, o, depth, r)
            }.sortedByDescending { it.depth }

            projected.forEach { p ->
                if (p.center.x < -20f || p.center.x > size.width + 20f || p.center.y < -20f || p.center.y > size.height + 20f) return@forEach
                screenPositions[p.star.id] = p.center
                val color = spectralColor(p.star.spectralType)
                val alpha = (1.25f / p.depth).coerceIn(0.4f, 1f)
                if (p.radius > 2.5.dp.toPx()) {
                    drawCircle(color.copy(alpha = 0.12f * alpha), p.radius * 2.2f, p.center)
                }
                drawCircle(color.copy(alpha = alpha), p.radius, p.center)
                if (p.star.id == selectedId) {
                    drawCircle(Matcha, p.radius + 9.dp.toPx(), p.center, style = Stroke(width = 2.dp.toPx()))
                }
            }

            // The Sun at the origin.
            val sun = Offset(cx, cy)
            drawCircle(StarOrange.copy(alpha = 0.28f), 14.dp.toPx(), sun)
            drawCircle(StarClassG, 6.dp.toPx(), sun)
            val sunLayout = textMeasurer.measure(sunLabel, TextStyle(color = InkText, fontSize = 12.sp, fontWeight = FontWeight.Bold))
            drawRoundRect(
                Ink.copy(alpha = 0.7f),
                topLeft = Offset(cx + 4.dp.toPx(), cy + 2.dp.toPx()),
                size = androidx.compose.ui.geometry.Size(sunLayout.size.width + 8.dp.toPx(), sunLayout.size.height + 4.dp.toPx()),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(6.dp.toPx()),
            )
            drawText(sunLayout, topLeft = Offset(cx + 8.dp.toPx(), cy + 4.dp.toPx()))

            // Which way the centre of the Milky Way lies (Sagittarius A*: RA 266.4°, Dec −28.9°),
            // pinned to the side of the screen it's on.
            run {
                val raR = 266.4 * (PI / 180.0); val decR = -28.94 * (PI / 180.0)
                val (o, _) = project(cos(decR) * cos(raR), sin(decR), cos(decR) * sin(raR))
                val left = o.x < cx
                val layout = textMeasurer.measure(
                    if (left) "\u2190 $galacticCentreLabel" else "$galacticCentreLabel \u2192",
                    TextStyle(color = InkTextDim, fontSize = 12.sp, fontWeight = FontWeight.SemiBold),
                )
                val margin = 16.dp.toPx()
                val x = if (left) margin else size.width - layout.size.width - margin
                val y = (o.y - layout.size.height / 2f).coerceIn(cy + 40.dp.toPx(), (size.height * 0.72f).coerceAtLeast(cy + 40.dp.toPx()))
                drawRoundRect(
                    Ink.copy(alpha = 0.75f),
                    topLeft = Offset(x - 6.dp.toPx(), y - 2.dp.toPx()),
                    size = androidx.compose.ui.geometry.Size(layout.size.width + 12.dp.toPx(), layout.size.height + 4.dp.toPx()),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(8.dp.toPx()),
                )
                drawText(layout, topLeft = Offset(x, y))
            }
        }

        // Selected star info card
        AnimatedVisibility(
            visible = selectedStar != null,
            enter = slideInVertically { it } + fadeIn(),
            exit = slideOutVertically { it } + fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .screenContentInsets()
        ) {
            selectedStar?.let { star ->
                StarInfoCard(star = star, onView = { onSystemClick(star.id) })
            }
        }
    }
}

@Composable
private fun StarInfoCard(star: StarPosition, onView: () -> Unit) {
    val ly = (star.distanceParsec * PARSEC_TO_LY).roundToInt()
    val pc = star.distanceParsec.roundToInt()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .shadow(20.dp, Corners.Card)
            .clip(Corners.Card)
            .background(Surface)
            .padding(20.dp)
    ) {
        Text(star.hostName, style = SectionTitle.copy(fontSize = 26.sp))
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = stringResource(R.string.galaxy_map_card_distance, pc, ly),
            fontFamily = BodyFamily, fontSize = 15.sp, color = InkTextDim
        )
        Text(
            text = pluralStringResource(
                R.plurals.galaxy_map_card_meta,
                star.numPlanets,
                star.numPlanets,
                star.spectralType?.takeIf { it.isNotBlank() } ?: "—"
            ),
            fontFamily = BodyFamily, fontSize = 15.sp, color = InkTextDim,
            modifier = Modifier.padding(top = 2.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        PrimaryButton(stringResource(R.string.galaxy_map_card_view), onView, modifier = Modifier.fillMaxWidth())
    }
}

private data class ProjectedStar(
    val star: StarPosition,
    val center: Offset,
    val depth: Float,
    val radius: Float
)

/** Approximate visible colour of a star from its spectral class (O→M). */
private fun spectralColor(type: String?): Color = starClassColor(type)

private const val DEFAULT_PITCH = 24f

/** A fixed scatter of faint background stars: x, y (0–1) and alpha. */
private fun backgroundStars(): List<Triple<Float, Float, Float>> {
    val rnd = kotlin.random.Random(7)
    return List(140) { Triple(rnd.nextFloat(), rnd.nextFloat(), 0.08f + rnd.nextFloat() * 0.22f) }
}

/** Three evenly spaced, round ring distances that fit inside [maxLy], e.g. 25 / 50 / 75 ly. */
internal fun scaleRingsLy(maxLy: Double): List<Int> {
    val target = maxLy / 4
    val step = listOf(5, 10, 25, 50, 100, 250, 500, 1000, 2500).lastOrNull { it <= target } ?: 5
    return listOf(step, step * 2, step * 3)
}
