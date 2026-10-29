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
    val maxDist = remember(stars) { stars.maxOfOrNull { it.distanceParsec }?.coerceAtLeast(1.0) ?: 1.0 }

    var yaw by remember { mutableFloatStateOf(0f) }
    var pitch by remember { mutableFloatStateOf(15f) }
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
            animation = tween(durationMillis = 90_000, easing = LinearEasing),
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
                        val hit = screenPositions.entries.minByOrNull { (_, p) ->
                            val dx = tap.x - p.x; val dy = tap.y - p.y
                            dx * dx + dy * dy
                        }
                        val newId = if (hit != null) {
                            val dx = tap.x - hit.value.x; val dy = tap.y - hit.value.y
                            if (sqrt(dx * dx + dy * dy) < 48f) {
                                autoRotate = false
                                hit.key
                            } else null
                        } else null
                        if (newId != null && newId != selectedId) {
                            onStarSelected()
                        }
                        selectedId = newId
                    }
                }
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, gestureZoom, _ ->
                        autoRotate = false
                        yaw += pan.x * 0.3f
                        pitch = (pitch + pan.y * 0.3f).coerceIn(-85f, 85f)
                        zoom = (zoom * gestureZoom).coerceIn(0.5f, 4f)
                    }
                }
        ) {
            val cx = size.width / 2f
            val cy = size.height / 2f
            val base = min(cx, cy)

            val effYaw = (yaw + if (autoRotate) autoSpin else 0f) * (PI / 180.0)
            val pitchR = pitch * (PI / 180.0)
            val cosYaw = cos(effYaw); val sinYaw = sin(effYaw)
            val cosPitch = cos(pitchR); val sinPitch = sin(pitchR)
            val norm = 1.0 / maxDist
            val cam = 2.6f
            val spread = 1.7f

            screenPositions.clear()

            fun project(wx: Double, wy: Double, wz: Double): Offset {
                val x1 = wx * cosYaw + wz * sinYaw
                val z1 = -wx * sinYaw + wz * cosYaw
                val y2 = wy * cosPitch - z1 * sinPitch
                val z2 = wy * sinPitch + z1 * cosPitch
                val depth = (cam + z2).toFloat().coerceAtLeast(0.05f)
                val k = base * spread / depth * zoom
                return Offset(cx + (x1 * k).toFloat(), cy - (y2 * k).toFloat())
            }

            // Three scale rings at round distances, in the plane of the celestial equator.
            scaleRingsLy(maxDist * PARSEC_TO_LY).forEach { ly ->
                val rPc = ly / PARSEC_TO_LY
                if (rPc > maxDist * 1.15) return@forEach
                val ring = androidx.compose.ui.graphics.Path()
                for (i in 0..96) {
                    val t = i / 96.0 * 2 * PI
                    val o = project(rPc * cos(t) * norm, 0.0, rPc * sin(t) * norm)
                    if (i == 0) ring.moveTo(o.x, o.y) else ring.lineTo(o.x, o.y)
                }
                drawPath(ring, Line, style = Stroke(width = 1.2.dp.toPx()))
                val tag = project(rPc * norm, 0.0, 0.0)
                val layout = textMeasurer.measure("$ly ly", TextStyle(color = InkTextFaint, fontSize = 10.sp))
                drawText(layout, topLeft = Offset(
                    (tag.x + 4f).coerceIn(0f, (size.width - layout.size.width).coerceAtLeast(0f)),
                    (tag.y - layout.size.height).coerceIn(0f, (size.height - layout.size.height).coerceAtLeast(0f)),
                ))
            }

            // Which way the centre of the Milky Way lies (Sagittarius A*: RA 266.4°, Dec −28.9°).
            run {
                val raR = 266.4 * (PI / 180.0); val decR = -28.94 * (PI / 180.0)
                val d = maxDist * 1.05 * norm
                val o = project(d * cos(decR) * cos(raR), d * sin(decR), d * cos(decR) * sin(raR))
                val toward = o - Offset(cx, cy)
                val arrow = if (toward.x < 0) "\u2190 " else ""
                val trail = if (toward.x >= 0) " \u2192" else ""
                val layout = textMeasurer.measure(
                    arrow + galacticCentreLabel + trail,
                    TextStyle(color = InkTextDim, fontSize = 11.sp, fontWeight = FontWeight.SemiBold),
                )
                drawText(layout, topLeft = Offset(
                    (o.x - layout.size.width / 2f).coerceIn(8f, (size.width - layout.size.width - 8f).coerceAtLeast(8f)),
                    (o.y - layout.size.height / 2f).coerceIn(8f, (size.height - layout.size.height - 8f).coerceAtLeast(8f)),
                ))
            }

            // Project every star, then paint far-to-near (painter's algorithm).
            val projected = stars.map { s ->
                val raR = s.ra * (PI / 180.0)
                val decR = s.dec * (PI / 180.0)
                val wx = s.distanceParsec * cos(decR) * cos(raR) * norm
                val wy = s.distanceParsec * sin(decR) * norm
                val wz = s.distanceParsec * cos(decR) * sin(raR) * norm

                val x1 = wx * cosYaw + wz * sinYaw
                val z1 = -wx * sinYaw + wz * cosYaw
                val y2 = wy * cosPitch - z1 * sinPitch
                val z2 = wy * sinPitch + z1 * cosPitch

                val depth = (cam + z2).toFloat().coerceAtLeast(0.05f)
                val k = base * spread / depth * zoom
                val sx = cx + (x1 * k).toFloat()
                val sy = cy - (y2 * k).toFloat()
                val r = (base * 0.016f / depth * zoom * (1f + min(s.numPlanets, 6) * 0.12f))
                    .coerceIn(1.2f, base * 0.06f)
                ProjectedStar(s, Offset(sx, sy), depth, r)
            }.sortedByDescending { it.depth }

            projected.forEach { p ->
                screenPositions[p.star.id] = p.center
                val color = spectralColor(p.star.spectralType)
                val alpha = (1.6f / p.depth).coerceIn(0.35f, 1f)
                // soft halo
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(color.copy(alpha = 0.5f * alpha), Color.Transparent),
                        center = p.center,
                        radius = p.radius * 3f
                    ),
                    radius = p.radius * 3f,
                    center = p.center
                )
                drawCircle(color = color.copy(alpha = alpha), radius = p.radius, center = p.center)

                if (p.star.id == selectedId) {
                    drawCircle(
                        color = Matcha,
                        radius = p.radius + 8.dp.toPx(),
                        center = p.center,
                        style = Stroke(width = 2.5.dp.toPx())
                    )
                    val label = p.star.hostName
                    val layout = textMeasurer.measure(
                        label,
                        style = TextStyle(color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    )
                    drawText(layout, topLeft = Offset(p.center.x + p.radius + 8f, p.center.y - layout.size.height / 2f))
                }
            }

            // The Sun at the origin — always projects to the center.
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(StarGold.copy(alpha = 0.6f), Color.Transparent),
                    center = Offset(cx, cy),
                    radius = 26f
                ),
                radius = 26f,
                center = Offset(cx, cy)
            )
            drawCircle(color = StarClassG, radius = 6.dp.toPx(), center = Offset(cx, cy))
            val sunLayout = textMeasurer.measure(
                sunLabel,
                style = TextStyle(color = InkText, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            )
            drawText(sunLayout, topLeft = Offset(cx + 10f, cy - sunLayout.size.height - 4f))
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

/** Three evenly spaced, round ring distances that fit inside [maxLy], e.g. 25 / 50 / 75 ly. */
internal fun scaleRingsLy(maxLy: Double): List<Int> {
    val target = maxLy / 4
    val step = listOf(5, 10, 25, 50, 100, 250, 500, 1000, 2500).lastOrNull { it <= target } ?: 5
    return listOf(step, step * 2, step * 3)
}
