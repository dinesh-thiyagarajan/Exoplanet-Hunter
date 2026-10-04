package com.app.exoplanethunter.presentation.components

import android.graphics.Bitmap
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.app.exoplanethunter.exoplanet.domain.model.Exoplanet
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.PI
import kotlin.math.asin
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

/** One full turn of the auto-spin. Purely for display — not the planet's real day. */
private const val SPIN_PERIOD_SECONDS = 48f

/** Largest bitmap edge rendered per frame; the sphere is drawn scaled up from this. */
private const val MAX_RENDER_PX = 420

/** Axis lean on screen and how far above the equator we look from, in radians. */
private const val AXIS_TILT = 0.30f
private const val VIEW_LATITUDE = 0.26f

/**
 * A planet rendered as a lit sphere: a procedurally painted surface (see [PlanetSurfaceTexture])
 * wrapped on a globe, lit from the upper left in its own star's colour, with an atmospheric
 * rim, a soft terminator and, for cold giants, rings. Drag sideways to spin it.
 *
 * Tidally locked planets don't auto-spin, and their light turns with the surface — the same
 * face always points at the star, so spinning reveals the permanent night side.
 */
@Composable
fun RealisticPlanet(
    planet: Exoplanet,
    size: Dp,
    modifier: Modifier = Modifier,
    tidallyLocked: Boolean = false,
    autoRotate: Boolean = true,
) {
    val kind = remember(planet) { planet.surfaceKind() }
    val rings = remember(planet) { kind.hasRings(planet) }
    val light = remember(planet) { starLightColor(planet.stellarEffectiveTempK) }
    val atmosphere = remember(kind) { kind.atmosphere() }

    val texture by produceState<PlanetTexture?>(null, planet) {
        value = withContext(Dispatchers.Default) {
            generatePlanetTexture(kind, planet.id, planet.estimatedTempK())
        }
    }

    val sizePx = with(LocalDensity.current) { size.toPx() }
    // Leave room around the globe for the rings or the atmospheric halo.
    val radiusPx = sizePx * if (rings) 0.29f else 0.40f
    val renderPx = min((radiusPx * 2).roundToInt(), MAX_RENDER_PX).coerceAtLeast(8)

    // Start a locked world with its day side toward the light, as an unlocked one looks.
    var spin by remember(planet) { mutableFloatStateOf(if (tidallyLocked) -0.6f else 0f) }
    var frame by remember { mutableIntStateOf(0) }
    var dragging by remember { mutableFloatStateOf(0f) }
    val fadeIn = remember(planet) { Animatable(0f) }

    val renderer = remember(texture, renderPx, tidallyLocked) {
        texture?.let { SphereRenderer(renderPx, it, light.toArgb(), atmosphere, tidallyLocked) }
    }
    val bitmap = remember(renderPx) { Bitmap.createBitmap(renderPx, renderPx, Bitmap.Config.ARGB_8888) }
    val image = remember(bitmap) { bitmap.asImageBitmap() }

    LaunchedEffect(renderer) {
        val r = renderer ?: return@LaunchedEffect
        val pixels = IntArray(renderPx * renderPx)
        var renderedSpin = Float.NaN
        var last = 0L
        launch { fadeIn.animateTo(1f, tween(500)) }
        while (true) {
            val now = withFrameNanos { it }
            val dt = if (last == 0L) 0f else (now - last) / 1e9f
            last = now
            if (autoRotate && !tidallyLocked && dragging == 0f) {
                spin += dt * 2f * PI.toFloat() / SPIN_PERIOD_SECONDS
            }
            if (spin != renderedSpin) {
                val s = spin
                withContext(Dispatchers.Default) { r.render(s, pixels) }
                bitmap.setPixels(pixels, 0, renderPx, 0, 0, renderPx, renderPx)
                renderedSpin = s
                frame++
            }
        }
    }

    Canvas(
        modifier = modifier
            .size(size)
            .pointerInput(radiusPx) {
                detectHorizontalDragGestures(
                    onDragStart = { dragging = 1f },
                    onDragEnd = { dragging = 0f },
                    onDragCancel = { dragging = 0f },
                ) { change, dx ->
                    change.consume()
                    spin += dx / radiusPx
                }
            },
    ) {
        frame // read so each rendered frame redraws
        val c = center
        drawHalo(c, radiusPx, atmosphere)
        if (rings) drawRings(c, radiusPx, light, back = true)
        if (renderer != null) {
            val half = radiusPx.roundToInt()
            drawImage(
                image = image,
                srcSize = IntSize(renderPx, renderPx),
                dstOffset = IntOffset((c.x - half).roundToInt(), (c.y - half).roundToInt()),
                dstSize = IntSize(half * 2, half * 2),
                alpha = fadeIn.value,
                filterQuality = FilterQuality.Medium,
            )
        }
        if (rings) drawRings(c, radiusPx, light, back = false)
    }
}

// --- Atmosphere ---------------------------------------------------------------

/** Colour and strength of the scattering rim; zero strength for airless worlds. */
internal class Atmosphere(val color: Int, val strength: Float)

private fun SurfaceKind.atmosphere(): Atmosphere = when (this) {
    SurfaceKind.Temperate, SurfaceKind.Ocean -> Atmosphere(0xFF6FA8FF.toInt(), 0.9f)
    SurfaceKind.Desert -> Atmosphere(0xFFE8C88A.toInt(), 0.8f)
    SurfaceKind.Haze -> Atmosphere(0xFFC6E6EE.toInt(), 0.9f)
    SurfaceKind.IceGiant -> Atmosphere(0xFF8CC8FF.toInt(), 0.8f)
    SurfaceKind.CoolGiant -> Atmosphere(0xFFF0DDB8.toInt(), 0.5f)
    SurfaceKind.WarmGiant -> Atmosphere(0xFF9DB8E8.toInt(), 0.7f)
    SurfaceKind.HotJupiter -> Atmosphere(0xFFFF8A50.toInt(), 0.6f)
    SurfaceKind.Lava -> Atmosphere(0xFFFF7A3A.toInt(), 0.3f)
    SurfaceKind.Frozen -> Atmosphere(0xFFCFE3FF.toInt(), 0.25f)
    SurfaceKind.Barren -> Atmosphere(0, 0f)
}

private fun DrawScope.drawHalo(c: Offset, r: Float, atm: Atmosphere) {
    if (atm.strength <= 0f) return
    val color = Color(atm.color)
    val outer = r * 1.14f
    drawCircle(
        brush = Brush.radialGradient(
            0f to Color.Transparent,
            0.86f to Color.Transparent,
            0.88f to color.copy(alpha = 0.35f * atm.strength),
            1f to Color.Transparent,
            center = c,
            radius = outer,
        ),
        radius = outer,
        center = c,
    )
}

// --- Rings --------------------------------------------------------------------

private fun DrawScope.drawRings(c: Offset, r: Float, light: Color, back: Boolean) {
    // The ring plane is the equator, so it leans with the axis and is seen nearly edge-on.
    val squash = sin(VIEW_LATITUDE)
    rotate(degrees = -AXIS_TILT * 180f / PI.toFloat(), pivot = c) {
        // The far half of the rings passes behind the globe (above centre on screen).
        val clip = if (back) Rect(0f, 0f, size.width, c.y) else Rect(0f, c.y, size.width, size.height)
        clipRect(clip.left - size.width, clip.top, clip.right + size.width, clip.bottom) {
            val bands = listOf(
                1.30f to 0.18f, 1.42f to 0.35f, 1.56f to 0.55f, 1.70f to 0.62f,
                1.84f to 0.5f, 2.02f to 0.42f, 2.14f to 0.28f,
            )
            val base = Color(0xFFDCCDAE)
            for ((k, alpha) in bands) {
                val rx = r * k
                val ry = rx * squash
                drawOval(
                    color = Color(
                        red = base.red * light.red, green = base.green * light.green,
                        blue = base.blue * light.blue, alpha = alpha * if (back) 0.8f else 1f,
                    ),
                    topLeft = Offset(c.x - rx, c.y - ry),
                    size = Size(rx * 2, ry * 2),
                    style = Stroke(width = r * 0.11f),
                )
            }
        }
    }
}

// --- Per-pixel sphere renderer -------------------------------------------------------

/**
 * Maps each pixel of the disc to a latitude/longitude once, so a frame is just a texture
 * lookup and a few multiplies per pixel. Spinning only shifts longitude.
 */
private class SphereRenderer(
    private val n: Int,
    private val tex: PlanetTexture,
    lightColor: Int,
    atmosphere: Atmosphere,
    private val tidallyLocked: Boolean,
) {
    private val count: Int
    private val index: IntArray
    private val rowOffset: IntArray
    private val lon: FloatArray
    private val cosLat: FloatArray
    private val diffuse: FloatArray
    private val glint: FloatArray
    private val rim: FloatArray
    private val cover: FloatArray

    private val lr = ((lightColor shr 16) and 0xFF) / 255f
    private val lg = ((lightColor shr 8) and 0xFF) / 255f
    private val lb = (lightColor and 0xFF) / 255f
    private val ar = ((atmosphere.color shr 16) and 0xFF) * atmosphere.strength
    private val ag = ((atmosphere.color shr 8) and 0xFF) * atmosphere.strength
    private val ab = (atmosphere.color and 0xFF) * atmosphere.strength

    init {
        // Star light from the upper left, mostly in front: a gibbous phase that shows the surface.
        val lx = -0.5f; val ly = 0.36f; val lz = 0.79f
        val ll = sqrt(lx * lx + ly * ly + lz * lz)
        val lX = lx / ll; val lY = ly / ll; val lZ = lz / ll
        // Half-vector between light and viewer, for the ocean glint.
        val hx = lX; val hy = lY; val hz = lZ + 1f
        val hl = sqrt(hx * hx + hy * hy + hz * hz)

        val ct = cos(AXIS_TILT); val st = sin(AXIS_TILT)
        val cv = cos(VIEW_LATITUDE); val sv = sin(VIEW_LATITUDE)
        val half = n / 2f
        val edge = 1f + 1.5f / half

        val idx = ArrayList<Int>(n * n)
        val rows = ArrayList<Int>(); val lons = ArrayList<Float>(); val cls = ArrayList<Float>()
        val dif = ArrayList<Float>(); val gl = ArrayList<Float>(); val rm = ArrayList<Float>(); val cov = ArrayList<Float>()

        for (j in 0 until n) for (i in 0 until n) {
            var x = (i + 0.5f - half) / half
            var y = (half - j - 0.5f) / half
            val r2 = x * x + y * y
            if (r2 > edge * edge) continue
            val dist = sqrt(r2)
            val coverage = ((1f - dist) * half + 0.5f).coerceIn(0f, 1f)
            if (coverage <= 0f) continue
            if (dist > 1f) { x /= dist; y /= dist }
            val z = sqrt(max(0f, 1f - x * x - y * y))

            // View → body: undo the on-screen axis lean, then the viewing latitude.
            val x1 = x * ct + y * st
            val y1 = -x * st + y * ct
            val by = y1 * cv + z * sv
            val bz = -y1 * sv + z * cv
            val lat = asin(by.coerceIn(-1f, 1f))
            val row = ((0.5f - lat / PI.toFloat()) * tex.height).toInt().coerceIn(0, tex.height - 1)

            idx += j * n + i
            rows += row * tex.width
            lons += atan2(x1, bz)
            cls += cos(lat)
            // Slightly wrapped Lambert: a soft terminator, as an atmosphere would give.
            val d = x * lX + y * lY + z * lZ
            dif += ((d + 0.06f) / 1.06f).coerceAtLeast(0f)
            gl += ((x * hx + y * hy + z * hz) / hl).coerceAtLeast(0f).pow(160)
            rm += (1f - z).pow(3) * (0.15f + 0.85f * ((d + 0.35f) / 1.35f).coerceIn(0f, 1f))
            cov += coverage
        }
        count = idx.size
        index = idx.toIntArray(); rowOffset = rows.toIntArray(); lon = lons.toFloatArray()
        cosLat = cls.toFloatArray(); diffuse = dif.toFloatArray(); glint = gl.toFloatArray()
        rim = rm.toFloatArray(); cover = cov.toFloatArray()
    }

    fun render(spin: Float, out: IntArray) {
        java.util.Arrays.fill(out, 0)
        val w = tex.width
        val albedo = tex.albedo
        val spec = tex.specular
        val emissive = tex.emissive
        val inv2Pi = 1f / (2f * PI.toFloat())
        val ambient = 0.025f
        for (k in 0 until count) {
            val surfaceLon = lon[k] - spin
            var uf = surfaceLon * inv2Pi + 0.5f
            uf -= floor(uf)
            val ti = rowOffset[k] + min((uf * w).toInt(), w - 1)

            // A locked world's light is fixed to its surface: noon is always at longitude 0.
            val d = if (tidallyLocked) {
                ((cosLat[k] * cos(surfaceLon) + 0.06f) / 1.06f).coerceAtLeast(0f)
            } else diffuse[k]

            val a = albedo[ti]
            val lit = ambient + d
            var r = ((a shr 16) and 0xFF) * lit * lr
            var g = ((a shr 8) and 0xFF) * lit * lg
            var b = (a and 0xFF) * lit * lb

            if (spec != null && !tidallyLocked) {
                val s = (spec[ti].toInt() and 0xFF) / 255f * glint[k] * 170f
                r += s * lr; g += s * lg; b += s * lb
            }
            if (emissive != null) {
                // Self-glow shows through everywhere but stands out on the night side.
                val e = emissive[ti]
                val f = 0.45f + 0.55f * (1f - d.coerceAtMost(1f))
                r += ((e shr 16) and 0xFF) * f
                g += ((e shr 8) and 0xFF) * f
                b += (e and 0xFF) * f
            }
            val rimK = if (tidallyLocked) rim[k] * d.coerceIn(0.2f, 1f) else rim[k]
            r += ar * rimK; g += ag * rimK; b += ab * rimK

            val alpha = (cover[k] * 255).toInt()
            out[index[k]] = (alpha shl 24) or
                (r.toInt().coerceIn(0, 255) shl 16) or
                (g.toInt().coerceIn(0, 255) shl 8) or
                b.toInt().coerceIn(0, 255)
        }
    }
}
