package com.app.exoplanethunter.presentation.screens.splash

import android.provider.Settings
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
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
import com.app.exoplanethunter.presentation.theme.BodyFamily
import com.app.exoplanethunter.presentation.theme.Corners
import com.app.exoplanethunter.presentation.theme.HeadlineFamily
import com.app.exoplanethunter.presentation.theme.InkText
import com.app.exoplanethunter.presentation.theme.InkTextDim
import com.app.exoplanethunter.presentation.theme.Matcha
import com.app.exoplanethunter.presentation.theme.MatchaMid
import com.app.exoplanethunter.presentation.theme.OnMatchaDeep
import com.app.exoplanethunter.presentation.theme.StarCheek
import com.app.exoplanethunter.presentation.theme.StarGlow
import com.app.exoplanethunter.presentation.theme.StarOrange
import com.app.exoplanethunter.presentation.theme.Surface
import org.koin.androidx.compose.koinViewModel
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sin

// ===========================================================================
// Splash v2 ("Peekaboo"): the logo comes to life. The star pops on, the planet peeks up from
// the bottom of the screen and looks at it, then blinks. Geometry is the handoff's 412 × 915
// phone frame, scaled by width; the planet stays anchored to the bottom edge.
// ===========================================================================

/** The logo's ground, shared with the system splash so the hand-off is seamless. */
val SplashGround = Color(0xFF272E1B)
private val Ink = Color(0xFF201E1D)
private val Cream = Color(0xFFF5EAD8)
private val PlanetCheek = Color(0xFFFFC6A5)
private val StarDim = Color(0xFFB2622D)
private val Track = Color(0xFF353D2A)

private fun ease(x: Float): Float = 1f - (1f - x.coerceIn(0f, 1f)).pow(3)
private fun back(x: Float): Float {
    val t = x.coerceIn(0f, 1f) - 1f
    val c = 1.70158f * 1.2f
    return 1f + (c + 1f) * t * t * t + c * t * t
}

// Background stars in the 412-wide frame: x, y, size, twinkle phase.
private val Twinkles = listOf(
    floatArrayOf(40f, 70f, 1.6f, 0f), floatArrayOf(180f, 40f, 1.2f, 1.3f), floatArrayOf(370f, 60f, 1.4f, 2.1f),
    floatArrayOf(30f, 420f, 1.2f, 0.7f), floatArrayOf(390f, 470f, 1.6f, 2.8f), floatArrayOf(120f, 300f, 1f, 1.9f),
    floatArrayOf(220f, 560f, 1.2f, 0.4f), floatArrayOf(360f, 230f, 1f, 3.1f), floatArrayOf(60f, 560f, 1.3f, 1.1f),
)

private const val PEEK_DISTANCE = 420f
private const val FIRST_MIN_LOADING = 2.6f   // progress fills over at least this long
private const val READY_HOLD = 1.3f          // after "✓ Ready", before fading out
private const val FIRST_FADE = 0.8f
private const val RETURNING_EXIT_AT = 1.2f

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
    LaunchedEffect(Unit) {
        var start = 0L
        while (true) withFrameNanos { now ->
            if (start == 0L) start = now
            t = (now - start) / 1e9f
        }
    }

    val first = isFirstLaunch == true
    LaunchedEffect(isDataLoaded, isFirstLaunch) {
        if (!isDataLoaded || isFirstLaunch == null) return@LaunchedEffect
        if (isFirstLaunch == true) {
            // Let the progress bar finish, show "✓ Ready" with the happy hops, then fade.
            val readyTime = maxOf(t, 1.2f + FIRST_MIN_LOADING)
            readyAt = readyTime
            val leave = readyTime + READY_HOLD + FIRST_FADE
            kotlinx.coroutines.delay(((leave - t) * 1000).toLong().coerceAtLeast(0))
        } else {
            val leave = if (reducedMotion) 0.4f else RETURNING_EXIT_AT
            kotlinx.coroutines.delay(((leave - t) * 1000).toLong().coerceAtLeast(0))
        }
        onDataLoaded()
    }

    val c = t
    val ready = readyAt >= 0f && c >= readyAt
    val out = if (first && readyAt >= 0f) ease((c - readyAt - READY_HOLD) / FIRST_FADE) else 0f

    // Animation tracks (reduced motion: the final pose, no peek, hops or wander).
    val starScale = when {
        reducedMotion -> 1f
        first -> back((c - 0.15f) / 0.6f)
        else -> back(c / 0.4f)
    }
    val peek = when {
        reducedMotion -> 0f
        first -> (1f - back((c - 0.6f) / 0.9f)) * PEEK_DISTANCE
        else -> (1f - back((c - 0.1f) / 0.55f)) * PEEK_DISTANCE
    }
    val hop = if (ready && !reducedMotion) {
        val since = c - readyAt
        -abs(sin(since * 7f)) * 10f * (1f - since / 0.9f).coerceIn(0f, 1f)
    } else 0f
    val look = when {
        !first || reducedMotion -> Offset(6f, -8f)
        else -> {
            val k = ease((c - 1.4f) / 0.5f)
            val wander = if (c > 3.5f) sin((c - 3.5f) * 1.1f) * 8f else 0f
            Offset(6f * k + wander * k - 2f * (1 - k), -8f * k + 2f * (1 - k))
        }
    }
    val blink = !reducedMotion && if (first) {
        (c > 2.2f && c < 2.34f) || (c > 5.6f && c < 5.74f) || (c > 7.7f && c < 7.84f)
    } else c > 0.8f && c < 0.92f
    val twinkle = if (first && !reducedMotion) ease(c / 0.6f) else 1f
    val titleIn = when {
        reducedMotion -> 1f
        first -> ease((c - 0.4f) / 0.6f)
        else -> ease((c - 0.2f) / 0.3f)
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(SplashGround)
            .graphicsLayer { alpha = 1f - out }
    ) {
        val unit = maxWidth / 412f
        Canvas(Modifier.fillMaxSize()) {
            drawPeekabooScene(
                t = if (reducedMotion) 0f else c,
                starScale = starScale,
                peek = peek + hop,
                look = look,
                blink = blink,
                twinkle = twinkle,
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(top = unit * 64f)
        ) {
            // "Exoplanet / Hunter", Caprasimo 54, with "Hunter" in matcha.
            Text(
                buildAnnotatedString {
                    append(stringResource(R.string.splash_title_line1))
                    append("\n")
                    withStyle(SpanStyle(color = Matcha)) { append(stringResource(R.string.splash_title_line2)) }
                },
                fontFamily = HeadlineFamily,
                fontSize = 54.sp,
                lineHeight = 51.sp,
                color = InkText,
                modifier = Modifier
                    .padding(start = 32.dp, end = 32.dp)
                    .graphicsLayer {
                        alpha = titleIn
                        translationY = (1f - titleIn) * 18.dp.toPx()
                    }
            )

            if (first) {
                FirstLaunchExtras(c = c, ready = ready, readyAt = readyAt, unit = unit.value, reducedMotion = reducedMotion)
            }
        }
    }
}

/** Progress bar and status (clear of the star), then the "While you wait" fact card. */
@Composable
private fun FirstLaunchExtras(c: Float, ready: Boolean, readyAt: Float, unit: Float, reducedMotion: Boolean) {
    val barIn = if (reducedMotion) 1f else ease((c - 1f) / 0.4f)
    val progress = when {
        ready -> 1f
        readyAt >= 0f -> ((c - 1.2f) / (readyAt - 1.2f)).coerceIn(0f, 1f)
        else -> (((c - 1.2f) / FIRST_MIN_LOADING) * 0.9f).coerceIn(0f, 0.9f)
    }
    Column(
        modifier = Modifier
            .padding(start = 32.dp, end = (150 * unit).dp, top = 24.dp)
            .graphicsLayer { alpha = barIn }
    ) {
        Box(Modifier.fillMaxWidth().height(10.dp).clip(Corners.Pill).background(Track)) {
            Box(Modifier.fillMaxWidth(progress.coerceAtLeast(0.02f)).height(10.dp).clip(Corners.Pill).background(Matcha))
        }
        Spacer(Modifier.height(10.dp))
        Text(
            if (ready) "✓ " + stringResource(R.string.splash_ready) else stringResource(R.string.splash_loading),
            fontFamily = BodyFamily,
            fontSize = 14.sp,
            fontWeight = if (ready) FontWeight.Bold else FontWeight.Medium,
            color = if (ready) OnMatchaDeep else InkTextDim,
        )
    }

    val facts = stringArrayResource(R.array.splash_facts)
    val cardIn = if (reducedMotion) 1f else ease((c - 1.5f) / 0.5f)
    val since = maxOf(0f, c - 1.5f)
    val index = (since / 4f).toInt() % facts.size
    val phase = since % 4f
    val factAlpha = if (reducedMotion) 1f else minOf(1f, minOf(phase, 4f - phase) * 2.5f) * cardIn
    Column(
        modifier = Modifier
            .padding(start = 20.dp, end = 20.dp, top = (150 * unit).dp)
            .fillMaxWidth()
            .heightIn(min = 100.dp)
            .graphicsLayer {
                alpha = cardIn
                translationY = (1f - cardIn) * 14.dp.toPx()
            }
            .clip(Corners.Card)
            .background(Surface.copy(alpha = 0.92f))
            .padding(horizontal = 20.dp, vertical = 18.dp)
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

/**
 * The whole Peekaboo scene in one pass: halo, twinkling stars, the star with its face, and the
 * planet rising from the bottom edge with banded body, big eyes looking at [look], and an open
 * smile. [peek] pushes the planet down (in frame units) for the peek-up.
 */
private fun DrawScope.drawPeekabooScene(
    t: Float,
    starScale: Float,
    peek: Float,
    look: Offset,
    blink: Boolean,
    twinkle: Float,
    worried: Boolean = false,
    dim: Boolean = false,
) {
    val u = size.width / 412f
    // Star and halo sit relative to the top; the planet to the bottom (frame y 840 of 915).
    fun top(x: Float, y: Float) = Offset(x * u, y * u)
    val planetBase = size.height - 75f * u
    val py = (peek + sin(t * 1.5f) * 4f) * u
    fun pl(x: Float, y: Float) = Offset(x * u, planetBase + (y - 840f) * u + py)

    drawCircle(StarGlow.copy(alpha = if (dim) 0.06f else 0.16f), 260f * u, top(330f, 340f))
    Twinkles.forEach { (x, y, r, ph) ->
        drawCircle(Cream.copy(alpha = twinkle * (0.3f + 0.7f * abs(sin(t * 1.2f + ph)))), r * 1.5f * u, top(x, y))
    }

    // --- The star ---
    if (starScale > 0.01f) {
        val sc = top(318f, 360f)
        fun s(x: Float, y: Float) = Offset(sc.x + (x - 318f) * u * starScale, sc.y + (y - 360f) * u * starScale)
        val k = u * starScale
        drawCircle(StarGlow.copy(alpha = if (dim) 0.2f else 0.5f), (96f + 6f * sin(t * 2.2f)) * k, sc)
        drawCircle(if (dim) StarDim else StarOrange, 62f * k, sc)
        if (!dim) {
            drawArc(
                Cream.copy(alpha = 0.5f), startAngle = -54f, sweepAngle = 39f, useCenter = false,
                topLeft = s(274f, 316f), size = Size(88f * k, 88f * k),
                style = Stroke(6f * k, cap = StrokeCap.Round),
            )
        }
        drawCircle(Ink, 7f * k, s(300f, 354f))
        drawCircle(Ink, 7f * k, s(336f, 354f))
        val cheek = StarCheek.copy(alpha = if (dim) 0.4f else 1f)
        drawOval(cheek, s(277f, 368.5f), Size(18f * k, 11f * k))
        drawOval(cheek, s(341f, 368.5f), Size(18f * k, 11f * k))
        val mouth = Path().apply {
            if (worried) { moveTo(s(306f, 382f).x, s(306f, 382f).y); quadraticBezierTo(s(318f, 374f).x, s(318f, 374f).y, s(330f, 382f).x, s(330f, 382f).y) }
            else { moveTo(s(306f, 372f).x, s(306f, 372f).y); quadraticBezierTo(s(318f, 384f).x, s(318f, 384f).y, s(330f, 372f).x, s(330f, 372f).y) }
        }
        drawPath(mouth, Ink, style = Stroke(5f * k, cap = StrokeCap.Round))
    }

    // --- The planet ---
    val center = pl(170f, 840f)
    val radius = 250f * u
    drawCircle(Matcha, radius, center)
    val body = Path().apply { addOval(androidx.compose.ui.geometry.Rect(center, radius)) }
    clipPath(body) {
        // Bands from the artwork: start, control and end of the top edge, then back along the bottom.
        fun band(cx: Float, y0: Float, yc: Float, y1: Float, y1b: Float, ycb: Float, y0b: Float, color: Color) {
            val p = Path().apply {
                moveTo(pl(-100f, y0).x, pl(-100f, y0).y)
                quadraticBezierTo(pl(cx, yc).x, pl(cx, yc).y, pl(460f, y1).x, pl(460f, y1).y)
                lineTo(pl(460f, y1b).x, pl(460f, y1b).y)
                quadraticBezierTo(pl(cx, ycb).x, pl(cx, ycb).y, pl(-100f, y0b).x, pl(-100f, y0b).y)
                close()
            }
            drawPath(p, color)
        }
        band(120f, 640f, 600f, 660f, 690f, 630f, 676f, OnMatchaDeep)
        band(150f, 880f, 840f, 900f, 940f, 876f, 920f, MatchaMid)
        drawCircle(MatchaMid, 14f * u, pl(340f, 820f))
        drawCircle(MatchaMid, 10f * u, pl(20f, 700f))
    }
    listOf(pl(118f, 740f), pl(236f, 720f)).forEachIndexed { i, eye ->
        val eyeH = if (blink) 4f else 36f
        drawOval(Cream, Offset(eye.x - 36f * u, eye.y - eyeH * u), Size(72f * u, eyeH * 2f * u))
        if (!blink) {
            val pupil = Offset(eye.x + look.x * u, eye.y + look.y * u)
            drawCircle(Ink, 18f * u, pupil)
            drawCircle(Cream, 5.5f * u, Offset(pupil.x + 6f * u, pupil.y - 6f * u))
        }
        if (worried) {
            val a = if (i == 0) Offset(86f, -50f) to Offset(134f, -60f) else Offset(268f, -50f) to Offset(220f, -60f)
            drawLine(Ink, Offset(a.first.x * u, eye.y + a.first.y * u), Offset(a.second.x * u, eye.y + a.second.y * u), 6f * u, StrokeCap.Round)
        }
    }
    drawOval(PlanetCheek, Offset(pl(42f, 792f).x, pl(42f, 792f).y), Size(48f * u, 28f * u))
    drawOval(PlanetCheek, Offset(pl(266f, 770f).x, pl(266f, 770f).y), Size(48f * u, 28f * u))
    if (worried) {
        val m = Path().apply { moveTo(pl(150f, 812f).x, pl(150f, 812f).y); quadraticBezierTo(pl(178f, 800f).x, pl(178f, 800f).y, pl(206f, 808f).x, pl(206f, 808f).y) }
        drawPath(m, Ink, style = Stroke(7f * u, cap = StrokeCap.Round))
    } else {
        val mouth = Path().apply {
            moveTo(pl(136f, 800f).x, pl(136f, 800f).y)
            quadraticBezierTo(pl(186f, 793f).x, pl(186f, 793f).y, pl(236f, 782f).x, pl(236f, 782f).y)
            quadraticBezierTo(pl(226f, 846f).x, pl(226f, 846f).y, pl(186f, 848f).x, pl(186f, 848f).y)
            quadraticBezierTo(pl(146f, 844f).x, pl(146f, 844f).y, pl(136f, 800f).x, pl(136f, 800f).y)
            close()
        }
        drawPath(mouth, SplashGround)
        val tongue = Path().apply {
            moveTo(pl(156f, 834f).x, pl(156f, 834f).y)
            quadraticBezierTo(pl(184f, 822f).x, pl(184f, 822f).y, pl(214f, 828f).x, pl(214f, 828f).y)
            quadraticBezierTo(pl(200f, 850f).x, pl(200f, 850f).y, pl(178f, 849f).x, pl(178f, 849f).y)
            close()
        }
        drawPath(tongue, PlanetCheek)
    }
}
