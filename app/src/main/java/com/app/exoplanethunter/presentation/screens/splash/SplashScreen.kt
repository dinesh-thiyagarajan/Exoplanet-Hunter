package com.app.exoplanethunter.presentation.screens.splash

import android.provider.Settings
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.exoplanethunter.R
import com.app.exoplanethunter.presentation.components.drawPlanetFace
import com.app.exoplanethunter.presentation.components.drawStarFace
import com.app.exoplanethunter.presentation.theme.BodyFamily
import com.app.exoplanethunter.presentation.theme.Corners
import com.app.exoplanethunter.presentation.theme.HeadlineFamily
import com.app.exoplanethunter.presentation.theme.Ink
import com.app.exoplanethunter.presentation.theme.InkText
import com.app.exoplanethunter.presentation.theme.InkTextDim
import com.app.exoplanethunter.presentation.theme.Matcha
import com.app.exoplanethunter.presentation.theme.Raised
import com.app.exoplanethunter.presentation.theme.StarOrange
import com.app.exoplanethunter.presentation.theme.Surface
import org.koin.androidx.compose.koinViewModel
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin

// Easing from the handoff's splash: cubic ease-out, and a back-out "pop" with overshoot.
private fun ease(x: Float): Float = 1f - (1f - x.coerceIn(0f, 1f)).pow(3)
private fun pop(x: Float): Float {
    val t = x.coerceIn(0f, 1f) - 1f
    val c = 1.70158f * 1.3f
    return 1f + (c + 1f) * t * t * t + c * t * t
}

// Background stars in the 300-unit scene: x, y, size, twinkle phase.
private val Twinkles = listOf(
    floatArrayOf(30f, 40f, 1.4f, 0f), floatArrayOf(260f, 30f, 1.2f, 1.3f), floatArrayOf(280f, 210f, 1.6f, 2.1f),
    floatArrayOf(20f, 230f, 1.1f, 0.7f), floatArrayOf(150f, 16f, 1f, 2.8f), floatArrayOf(60f, 150f, 1.2f, 1.9f),
    floatArrayOf(240f, 120f, 1f, 0.4f), floatArrayOf(110f, 270f, 1.3f, 3.1f),
)

private const val FIRST_LAUNCH_MIN_SECONDS = 3.2f
private const val RETURNING_EXIT_AT = 1.1f
private const val FADE_OUT_SECONDS = 0.6f

/**
 * The "Star hug" splash. Returning launches are a ≤1 s hello: the star pops in and the planet
 * swoops round once. The first launch stays a little longer, with a progress bar and a fact
 * card while the catalogue is set up. With animations turned off, it shows a still frame.
 */
@Composable
fun SplashScreen(
    onDataLoaded: () -> Unit,
    viewModel: SplashViewModel = koinViewModel()
) {
    val isDataLoaded by viewModel.loadingState.collectAsState()
    val isFirstLaunch by viewModel.isFirstLaunch.collectAsState()
    val context = LocalContext.current
    val reducedMotion = remember {
        Settings.Global.getFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) == 0f
    }

    var t by remember { mutableFloatStateOf(0f) }
    var readyAt by remember { mutableFloatStateOf(-1f) }
    var exitAt by remember { mutableFloatStateOf(-1f) }

    LaunchedEffect(Unit) {
        var start = 0L
        while (true) {
            withFrameNanos { now ->
                if (start == 0L) start = now
                t = (now - start) / 1e9f
            }
        }
    }

    // Leave once the data is ready and the intro has had its moment.
    LaunchedEffect(isDataLoaded, isFirstLaunch) {
        if (!isDataLoaded || isFirstLaunch == null) return@LaunchedEffect
        readyAt = t
        val minimum = if (reducedMotion) 0.4f else if (isFirstLaunch == true) FIRST_LAUNCH_MIN_SECONDS else RETURNING_EXIT_AT
        val leave = maxOf(minimum, t + if (isFirstLaunch == true) 0.6f else 0f)
        exitAt = leave
        kotlinx.coroutines.delay(((leave - t) * 1000).toLong().coerceAtLeast(0))
        if (isFirstLaunch == true && !reducedMotion) kotlinx.coroutines.delay((FADE_OUT_SECONDS * 1000).toLong())
        onDataLoaded()
    }

    val c = if (reducedMotion) 10f else t
    val first = isFirstLaunch == true
    val out = if (first && exitAt > 0f) ease((c - exitAt) / FADE_OUT_SECONDS) else 0f

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Ink)
            .graphicsLayer { alpha = 1f - out }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .padding(start = 32.dp, end = 32.dp, bottom = 40.dp)
        ) {
            Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                StarScene(t = c, first = first, reducedMotion = reducedMotion, modifier = Modifier.size(300.dp))
            }

            // Title: "Exoplanet" over a matcha "Hunter".
            val titleIn = if (first) ease((c - 0.5f) / 0.6f) else ease((c - 0.25f) / 0.35f)
            Text(
                buildAnnotatedString {
                    append(stringResource(R.string.splash_title_line1))
                    append("\n")
                    withStyle(SpanStyle(color = Matcha)) { append(stringResource(R.string.splash_title_line2)) }
                },
                fontFamily = HeadlineFamily,
                fontSize = 52.sp,
                lineHeight = 50.sp,
                color = InkText,
                modifier = Modifier
                    .padding(bottom = 28.dp)
                    .graphicsLayer {
                        alpha = titleIn
                        translationY = (1f - titleIn) * 16.dp.toPx()
                    }
            )

            if (first) FirstLaunchExtras(c = c, ready = isDataLoaded && readyAt >= 0f, readyAt = readyAt)
        }
    }
}

/** The star with its face and the little planet orbiting (first launch) or swooping in. */
@Composable
private fun StarScene(t: Float, first: Boolean, reducedMotion: Boolean, modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val u = size.width / 300f
        fun p(x: Float, y: Float) = Offset(x * u, y * u)
        val twinkle = if (first) ease(t / 0.7f) else 1f
        Twinkles.forEach { (x, y, r, ph) ->
            drawCircle(InkText.copy(alpha = twinkle * (0.35f + 0.65f * abs(sin(t * 1.3f + ph)))), r * 1.4f * u, p(x, y))
        }

        val starScale = if (reducedMotion) 1f else if (first) pop(t / 0.7f) else pop(t / 0.45f)
        val bob = if (reducedMotion) 0f else sin(t * 1.6f) * 3f
        val glow = 64f + 5f * sin(t * 2.2f)

        // Planet position: an orbit on a tilted ellipse (first launch) or one swoop (returning).
        val planet: Triple<Offset, Float, Boolean> = when {
            reducedMotion -> Triple(p(232f, 176f), 1f, false)
            first -> {
                val ang = -PI.toFloat() / 2 + maxOf(0f, t - 0.5f) * 1.1f
                val rIn = ease((t - 0.4f) / 0.8f)
                val behind = sin(ang) < 0
                Triple(p(150f + 118f * rIn * cos(ang), 150f + 34f * rIn * sin(ang)), (if (behind) 0.8f else 1f) * pop((t - 0.4f) / 0.6f), behind)
            }
            else -> {
                val a = -PI.toFloat() * 0.9f + ease((t - 0.15f) / 0.65f) * PI.toFloat() * 1.65f
                val r = 118f - ease((t - 0.15f) / 0.65f) * 58f
                Triple(p(150f + r * cos(a), 150f + 40f * sin(a) + ease((t - 0.6f) / 0.3f) * 24f), pop((t - 0.15f) / 0.4f), false)
            }
        }

        if (first && !reducedMotion) {
            // The orbit, dashed, eased out with the planet.
            val rIn = ease((t - 0.4f) / 0.8f)
            drawOval(
                InkTextDim.copy(alpha = 0.35f * rIn),
                topLeft = p(150f - 118f * rIn, 150f - 34f * rIn),
                size = androidx.compose.ui.geometry.Size(236f * rIn * u, 68f * rIn * u),
                style = Stroke(1.5f * u, pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(5f * u, 6f * u))),
            )
        }
        if (planet.third) drawPlanetFace(planet.first, 17f * u * planet.second)

        val center = Offset(150f * u, (150f + bob) * u)
        val r = 50f * u * starScale
        if (r > 0.5f) {
            drawStarFace(
                center = center,
                r = r,
                glowRadius = glow * u * starScale,
                glowAlpha = 0.4f + 0.05f * sin(t * 2.2f),
                blink = !reducedMotion && (t % 3.4f) < 0.14f,
            )
        }
        if (!planet.third && planet.second > 0.05f) drawPlanetFace(planet.first, 17f * u * planet.second)
    }
}

/** First launch only: the progress bar and a "While you wait" fact that changes every 4 s. */
@Composable
private fun FirstLaunchExtras(c: Float, ready: Boolean, readyAt: Float) {
    val barIn = ease((c - 0.9f) / 0.4f)
    // Fills steadily while setting up, then completes once the catalogue is ready.
    val progress = if (ready) minOf(1f, 0.7f + ease((c - readyAt) / 0.5f) * 0.3f) else minOf(0.7f, maxOf(0f, (c - 1f) / 3f) * 0.7f)
    Column(Modifier.graphicsLayer { alpha = barIn }) {
        Box(Modifier.fillMaxWidth().height(10.dp).clip(Corners.Pill).background(Raised)) {
            Box(Modifier.fillMaxWidth(progress.coerceAtLeast(0.02f)).height(10.dp).clip(Corners.Pill).background(Matcha))
        }
        Row(Modifier.fillMaxWidth().padding(top = 10.dp)) {
            val done = ready && progress >= 1f
            Text(
                if (done) "✓ " + stringResource(R.string.splash_ready) else stringResource(R.string.splash_loading),
                fontFamily = BodyFamily,
                fontSize = 14.sp,
                fontWeight = if (done) FontWeight.Bold else FontWeight.Normal,
                color = if (done) Matcha else InkTextDim,
            )
        }
    }

    val facts = stringArrayResource(R.array.splash_facts)
    val cardIn = ease((c - 1.2f) / 0.5f)
    val since = maxOf(0f, c - 1.2f)
    val index = (since / 4f).toInt() % facts.size
    val phase = since % 4f
    val factAlpha = minOf(1f, minOf(phase, 4f - phase) * 2.5f) * cardIn
    Column(
        modifier = Modifier
            .padding(top = 24.dp)
            .fillMaxWidth()
            .heightIn(min = 100.dp)
            .graphicsLayer {
                alpha = cardIn
                translationY = (1f - cardIn) * 14.dp.toPx()
            }
            .clip(Corners.Card)
            .background(Surface)
            .padding(20.dp)
    ) {
        Text(stringResource(R.string.splash_while_you_wait), fontFamily = BodyFamily, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = StarOrange)
        Spacer(Modifier.height(6.dp))
        Text(
            facts[index],
            fontFamily = BodyFamily,
            fontSize = 16.sp,
            lineHeight = 23.sp,
            color = InkText,
            modifier = Modifier.graphicsLayer { alpha = factAlpha },
        )
    }
}
