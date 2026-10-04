package com.app.exoplanethunter.presentation.components

import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.sin
import kotlin.random.Random

/**
 * A planet's surface as an equirectangular map (longitude across, latitude down).
 *
 * @property albedo    ARGB surface colour, lit by the star at render time.
 * @property specular  0–255 per texel; how mirror-like it is (open water glints).
 * @property emissive  ARGB self-glow (lava, a hot Jupiter's thermal night side), or null.
 */
internal class PlanetTexture(
    val width: Int,
    val height: Int,
    val albedo: IntArray,
    val specular: ByteArray?,
    val emissive: IntArray?,
)

/**
 * Procedurally paints a surface for [kind]. Noise is sampled on the unit sphere (not the flat
 * map), so there's no seam at the date line and no pinching at the poles. Deterministic for a
 * given [seed], so a planet always looks the same.
 */
internal fun generatePlanetTexture(
    kind: SurfaceKind,
    seed: Long,
    tempK: Double?,
    width: Int = 896,
    height: Int = 448,
): PlanetTexture {
    val rnd = Random(seed)
    val noise = SphereNoise(rnd.nextInt())
    val albedo = IntArray(width * height)
    val wantsSpecular = kind == SurfaceKind.Temperate || kind == SurfaceKind.Ocean
    val specular = if (wantsSpecular) ByteArray(width * height) else null
    val emissive = if (kind == SurfaceKind.Lava || kind == SurfaceKind.HotJupiter) IntArray(width * height) else null
    val painter = SurfacePainter(kind, rnd, noise, tempK ?: 0.0)

    val px = Pixel()
    for (v in 0 until height) {
        val lat = PI / 2 - (v + 0.5) * PI / height
        val cl = cos(lat)
        val sl = sin(lat)
        for (u in 0 until width) {
            val lon = (u + 0.5) * 2 * PI / width - PI
            px.reset()
            painter.paint(cl * cos(lon), sl, cl * sin(lon), lat, px)
            val i = v * width + u
            albedo[i] = px.color
            if (specular != null) specular[i] = (px.specular * 255).toInt().coerceIn(0, 255).toByte()
            if (emissive != null) emissive[i] = px.emissive
        }
    }
    return PlanetTexture(width, height, albedo, specular, emissive)
}

private class Pixel {
    var color = 0
    var specular = 0f
    var emissive = 0
    fun reset() { color = 0; specular = 0f; emissive = 0 }
}

private class SurfacePainter(
    private val kind: SurfaceKind,
    rnd: Random,
    private val n: SphereNoise,
    private val tempK: Double,
) {
    // Per-planet variation: where features sit, how much land, how many bands.
    private val ox = rnd.nextFloat() * 50f
    private val oy = rnd.nextFloat() * 50f
    private val oz = rnd.nextFloat() * 50f
    private val seaLevel = 0.47f + rnd.nextFloat() * 0.1f
    private val bandCount = 7f + rnd.nextInt(6)
    private val stormLon = rnd.nextFloat() * 2f * PI.toFloat()
    private val stormLat = (rnd.nextFloat() - 0.5f) * 0.9f
    private val ringed = kind == SurfaceKind.CoolGiant && tempK < 200

    fun paint(x: Double, y: Double, z: Double, lat: Double, out: Pixel) {
        val fx = x.toFloat(); val fy = y.toFloat(); val fz = z.toFloat()
        when (kind) {
            SurfaceKind.Temperate -> temperate(fx, fy, fz, lat, out, oceanWorld = false)
            SurfaceKind.Ocean -> temperate(fx, fy, fz, lat, out, oceanWorld = true)
            SurfaceKind.Frozen -> frozen(fx, fy, fz, out)
            SurfaceKind.Barren -> barren(fx, fy, fz, out)
            SurfaceKind.Desert -> desert(fx, fy, fz, lat, out)
            SurfaceKind.Lava -> lava(fx, fy, fz, out)
            SurfaceKind.Haze -> haze(fx, fy, fz, lat, out)
            SurfaceKind.IceGiant -> iceGiant(fx, fy, fz, lat, out)
            SurfaceKind.CoolGiant -> giant(fx, fy, fz, lat, out, if (ringed) SATURN else JUPITER)
            SurfaceKind.WarmGiant -> giant(fx, fy, fz, lat, out, AZURE)
            SurfaceKind.HotJupiter -> hotJupiter(fx, fy, fz, lat, out)
        }
    }

    private fun fbm(x: Float, y: Float, z: Float, scale: Float, octaves: Int, shift: Float = 0f) =
        n.fbm((x + ox + shift) * scale, (y + oy + shift) * scale, (z + oz + shift) * scale, octaves)

    // --- Rocky worlds -------------------------------------------------------

    private fun temperate(x: Float, y: Float, z: Float, lat: Double, out: Pixel, oceanWorld: Boolean) {
        val h = fbm(x, y, z, 1.7f, 6)
        val sea = if (oceanWorld) 1.1f else seaLevel
        val depth = ((sea - h) / 0.25f).coerceIn(0f, 1f)
        val water = lerpColor(0xFF3A88B8.toInt(), 0xFF153A68.toInt(), smooth(depth))
        // Blend across the shoreline rather than switching, so coasts aren't stair-stepped.
        val landness = smoothRange(h, sea - 0.006f, sea + 0.006f)
        var c: Int
        if (landness <= 0f) {
            c = water
            out.specular = 0.9f
        } else {
            val e = ((h - sea) / (1f - sea)).coerceIn(0f, 1f)
            val moisture = fbm(x, y, z, 3.1f, 4, 17f)
            val lowland = lerpColor(0xFFA08A5C.toInt(), 0xFF3E6B36.toInt(), smooth(((moisture - 0.38f) * 4f).coerceIn(0f, 1f)))
            c = when {
                e < 0.03f -> lerpColor(0xFFC8B98E.toInt(), lowland, e / 0.03f)
                e < 0.45f -> lerpColor(lowland, 0xFF7C6C4C.toInt(), (e - 0.03f) / 0.42f)
                e < 0.7f -> lerpColor(0xFF7C6C4C.toInt(), 0xFF8E877E.toInt(), (e - 0.45f) / 0.25f)
                else -> lerpColor(0xFF8E877E.toInt(), 0xFFF2F4F6.toInt(), ((e - 0.7f) / 0.3f).coerceIn(0f, 1f))
            }
            c = lerpColor(water, c, landness)
            out.specular = 0.9f * (1f - landness)
        }
        // Polar ice, with a ragged edge.
        val iceEdge = 1.18f + (fbm(x, y, z, 4f, 3, 5f) - 0.5f) * 0.25f
        if (abs(lat) > iceEdge) {
            c = lerpColor(c, 0xFFEAF1F6.toInt(), ((abs(lat).toFloat() - iceEdge) / 0.08f).coerceIn(0f, 1f))
            out.specular = 0f
        }
        // Clouds: stretched along latitude like real weather bands.
        val cloud = n.fbm((x + oz) * 2.4f, (y + ox) * 4.2f, (z + oy) * 2.4f, 5)
        val cover = smoothRange(cloud, if (oceanWorld) 0.46f else 0.52f, 0.72f) * 0.92f
        out.color = lerpColor(c, 0xFFF7F8FA.toInt(), cover)
        out.specular *= 1f - cover
    }

    private fun frozen(x: Float, y: Float, z: Float, out: Pixel) {
        val h = fbm(x, y, z, 2f, 5)
        var c = lerpColor(0xFFF1F5F9.toInt(), 0xFF9DB0C3.toInt(), smooth(((h - 0.45f) * 2.5f).coerceIn(0f, 1f)))
        // Long fractures, like Europa's lineae.
        val crack = n.ridged((x + ox) * 5f, (y + oy) * 5f, (z + oz) * 5f)
        if (crack > 0.9f) c = lerpColor(c, 0xFF7B6A5E.toInt(), (crack - 0.9f) / 0.1f * 0.7f)
        out.color = c
    }

    private fun barren(x: Float, y: Float, z: Float, out: Pixel) {
        val h = fbm(x, y, z, 2.2f, 6)
        val maria = smoothRange(fbm(x, y, z, 1.1f, 3, 9f), 0.5f, 0.62f)
        var c = lerpColor(0xFFB3ADA4.toInt(), 0xFF6E6862.toInt(), smooth(((h - 0.3f) * 2.2f).coerceIn(0f, 1f)))
        c = lerpColor(c, 0xFF56504B.toInt(), maria * 0.6f)
        out.color = c
    }

    private fun desert(x: Float, y: Float, z: Float, lat: Double, out: Pixel) {
        val h = fbm(x, y, z, 2f, 6)
        val ground = lerpColor(0xFFE2C28E.toInt(), 0xFF8A5E35.toInt(), smooth(((h - 0.35f) * 2f).coerceIn(0f, 1f)))
        // Thick sulfurous cloud deck swirling around the equator, Venus-style.
        val warp = fbm(x, y, z, 3f, 3, 31f)
        val swirl = n.fbm((x + oy) * 1.6f, (y * 3.5f + warp), (z + ox) * 1.6f, 5)
        val cover = smoothRange(swirl, 0.42f, 0.7f) * (0.55f + 0.35f * cos(lat).toFloat())
        out.color = lerpColor(ground, 0xFFEADBB0.toInt(), cover)
    }

    private fun lava(x: Float, y: Float, z: Float, out: Pixel) {
        val h = fbm(x, y, z, 2.4f, 5)
        out.color = lerpColor(0xFF4A3B33.toInt(), 0xFF1A1312.toInt(), smooth(((h - 0.35f) * 2f).coerceIn(0f, 1f)))
        val crack = n.ridged((x + ox) * 4f, (y + oy) * 4f, (z + oz) * 4f)
        val lake = 1f - smoothRange(h, 0.28f, 0.36f)
        val glow = maxOf(((crack - 0.82f) / 0.18f).coerceIn(0f, 1f), lake)
        if (glow > 0f) {
            out.emissive = lerpColor(0xFF000000.toInt(), lerpColor(0xFFE0440E.toInt(), 0xFFFFD27A.toInt(), glow), glow)
            out.color = lerpColor(out.color, 0xFF5A1A0A.toInt(), glow)
        }
    }

    // --- Gas-rich worlds ----------------------------------------------------

    /** Latitude bands distorted by turbulence; returns 0..palette.size. */
    private fun bandValue(x: Float, y: Float, z: Float, lat: Double, turbulence: Float): Float {
        // Stretched east–west: jet streams shear storms into long streaks.
        val warp = (n.fbm((x + ox) * 2.2f, (y + oy) * 9f, (z + oz) * 2.2f, 4) - 0.5f) * turbulence
        return ((lat.toFloat() / PI.toFloat() + 0.5f) * bandCount + warp)
    }

    private fun giant(x: Float, y: Float, z: Float, lat: Double, out: Pixel, palette: IntArray) {
        val b = bandValue(x, y, z, lat, 1.3f)
        var c = paletteAt(palette, b)
        val grain = n.fbm((x + oy) * 5f, (y + oz) * 22f, (z + ox) * 5f, 3)
        c = shade(c, 0.9f + grain * 0.2f)
        // A great storm, oval and stretched east–west.
        val dLat = (lat.toFloat() - stormLat) / 0.09f
        val dLon = angleDiff(kotlin.math.atan2(z, x), stormLon) / 0.22f
        val s = dLat * dLat + dLon * dLon
        if (s < 1f && palette !== SATURN) c = lerpColor(c, 0xFFC0623E.toInt(), (1f - s) * 0.85f)
        out.color = c
    }

    private fun iceGiant(x: Float, y: Float, z: Float, lat: Double, out: Pixel) {
        val warm = tempK > 400
        val b = bandValue(x, y, z, lat, 0.9f)
        var c = paletteAt(if (warm) PALE_ICE else NEPTUNE, b)
        // Bright high-altitude methane streaks.
        val streak = n.fbm((x + ox) * 2f, (y + oy) * 14f, (z + oz) * 2f, 3)
        c = lerpColor(c, 0xFFE8F4FF.toInt(), smoothRange(streak, 0.68f, 0.8f) * 0.7f)
        val dLat = (lat.toFloat() - stormLat * 0.6f) / 0.1f
        val dLon = angleDiff(kotlin.math.atan2(z, x), stormLon) / 0.2f
        val s = dLat * dLat + dLon * dLon
        if (s < 1f) c = lerpColor(c, 0xFF1C2E6A.toInt(), (1f - s) * 0.8f)
        out.color = c
    }

    private fun haze(x: Float, y: Float, z: Float, lat: Double, out: Pixel) {
        val hot = tempK > 700
        val b = bandValue(x, y, z, lat, 0.6f)
        val c = paletteAt(if (hot) STEAM else MINI_NEPTUNE, b * 0.6f)
        out.color = shade(c, 0.94f + fbm(x, y * 2f, z, 4f, 3, 3f) * 0.12f)
    }

    private fun hotJupiter(x: Float, y: Float, z: Float, lat: Double, out: Pixel) {
        val b = bandValue(x, y, z, lat, 1.4f)
        out.color = shade(paletteAt(HOT_JUPITER, b), 0.9f + fbm(x, y * 3f, z, 6f, 3, 7f) * 0.2f)
        // ~1,500 K atmospheres glow a dull red of their own, visible on the night side.
        val heat = 0.25f + 0.2f * fbm(x, y, z, 2.5f, 3, 13f)
        out.emissive = lerpColor(0xFF000000.toInt(), 0xFFB8401A.toInt(), heat)
    }

    private fun paletteAt(palette: IntArray, b: Float): Int {
        val i = floor(b).toInt()
        val f = smooth(b - i)
        val a = palette[Math.floorMod(i, palette.size)]
        val c = palette[Math.floorMod(i + 1, palette.size)]
        return lerpColor(a, c, f)
    }

    companion object {
        val JUPITER = intArrayOf(0xFFEFE3CC.toInt(), 0xFFC9A27A.toInt(), 0xFFE6D3B3.toInt(), 0xFF9E6B47.toInt(), 0xFFDCC4A0.toInt(), 0xFFB98B62.toInt())
        val SATURN = intArrayOf(0xFFEBDDB8.toInt(), 0xFFD8C190.toInt(), 0xFFE7D6AE.toInt(), 0xFFC8AE7E.toInt())
        val AZURE = intArrayOf(0xFF5873A8.toInt(), 0xFF7B97C6.toInt(), 0xFF46608F.toInt(), 0xFF96AED4.toInt())
        val HOT_JUPITER = intArrayOf(0xFF5A2A1C.toInt(), 0xFF3A1A14.toInt(), 0xFF74392A.toInt(), 0xFF4A2018.toInt())
        val NEPTUNE = intArrayOf(0xFF3F78C8.toInt(), 0xFF3366B4.toInt(), 0xFF4A86D2.toInt(), 0xFF2E5CA6.toInt())
        val PALE_ICE = intArrayOf(0xFF9CC8DE.toInt(), 0xFF86B6D0.toInt(), 0xFFB0D4E6.toInt())
        val MINI_NEPTUNE = intArrayOf(0xFFA6CFD8.toInt(), 0xFF8DBDCB.toInt(), 0xFFBBDCE2.toInt())
        val STEAM = intArrayOf(0xFFE3D6BC.toInt(), 0xFFCFBF9F.toInt(), 0xFFEDE4CF.toInt())
    }
}

// --- Noise --------------------------------------------------------------------

/** Seeded 3D value noise with fractal sums, sampled on the unit sphere. */
private class SphereNoise(private val seed: Int) {
    private fun hash(x: Int, y: Int, z: Int): Float {
        var h = seed + x * 374761393 + y * 668265263 + z * 1274126177
        h = (h xor (h ushr 13)) * 1103515245
        h = h xor (h ushr 16)
        return (h and 0xFFFFFF) / 16777215f
    }

    fun noise(x: Float, y: Float, z: Float): Float {
        val x0 = floor(x).toInt(); val y0 = floor(y).toInt(); val z0 = floor(z).toInt()
        val tx = smooth(x - x0); val ty = smooth(y - y0); val tz = smooth(z - z0)
        fun l(a: Float, b: Float, t: Float) = a + (b - a) * t
        val c00 = l(hash(x0, y0, z0), hash(x0 + 1, y0, z0), tx)
        val c10 = l(hash(x0, y0 + 1, z0), hash(x0 + 1, y0 + 1, z0), tx)
        val c01 = l(hash(x0, y0, z0 + 1), hash(x0 + 1, y0, z0 + 1), tx)
        val c11 = l(hash(x0, y0 + 1, z0 + 1), hash(x0 + 1, y0 + 1, z0 + 1), tx)
        return l(l(c00, c10, ty), l(c01, c11, ty), tz)
    }

    /** Fractal sum, normalised to roughly 0..1. */
    fun fbm(x: Float, y: Float, z: Float, octaves: Int): Float {
        var sum = 0f; var amp = 0.5f; var freq = 1f; var norm = 0f
        repeat(octaves) {
            sum += amp * noise(x * freq, y * freq, z * freq)
            norm += amp; amp *= 0.5f; freq *= 2.03f
        }
        return sum / norm
    }

    /** Sharp ridges where the noise crosses its midpoint — cracks and fault lines. */
    fun ridged(x: Float, y: Float, z: Float): Float = 1f - abs(fbm(x, y, z, 4) * 2f - 1f)
}

// --- Colour helpers -------------------------------------------------------------

private fun smooth(t: Float): Float = t * t * (3f - 2f * t)

private fun smoothRange(v: Float, lo: Float, hi: Float): Float = smooth(((v - lo) / (hi - lo)).coerceIn(0f, 1f))

private fun angleDiff(a: Float, b: Float): Float {
    var d = a - b
    while (d > PI) d -= (2 * PI).toFloat()
    while (d < -PI) d += (2 * PI).toFloat()
    return d
}

internal fun lerpColor(a: Int, b: Int, t: Float): Int {
    val tt = t.coerceIn(0f, 1f)
    fun ch(shift: Int): Int {
        val ca = (a shr shift) and 0xFF
        val cb = (b shr shift) and 0xFF
        return (ca + (cb - ca) * tt).toInt() and 0xFF
    }
    return (0xFF shl 24) or (ch(16) shl 16) or (ch(8) shl 8) or ch(0)
}

private fun shade(c: Int, f: Float): Int {
    fun ch(shift: Int) = (((c shr shift) and 0xFF) * f).toInt().coerceIn(0, 255)
    return (0xFF shl 24) or (ch(16) shl 16) or (ch(8) shl 8) or ch(0)
}
