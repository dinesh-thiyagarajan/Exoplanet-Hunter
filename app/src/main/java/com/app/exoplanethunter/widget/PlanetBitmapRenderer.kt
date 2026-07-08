package com.app.exoplanethunter.widget

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RadialGradient
import android.graphics.RectF
import android.graphics.Shader
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.ColorUtils
import com.app.exoplanethunter.presentation.components.planetAccentColor
import com.app.exoplanethunter.presentation.components.planetTypeLabel
import kotlin.random.Random

/**
 * Draws a small shaded planet sphere as a [Bitmap] for the home-screen widget, mirroring the
 * accent colour used by the in-app renderers. RemoteViews can't host Compose, so we rasterise
 * with plain Android Canvas.
 *
 * Beyond the lit sphere, the art adapts to the planet's type: an atmosphere glow for everything,
 * cloud bands (and a ring for true giants) on gaseous planets, and surface speckles on rocky
 * ones. [seed] keeps those details stable per planet across re-renders.
 */
object PlanetBitmapRenderer {

    fun render(
        sizePx: Int,
        equilibriumTempK: Double?,
        radiusEarth: Double?,
        massEarth: Double?,
        seed: Int = 0
    ): Bitmap {
        val accent = planetAccentColor(equilibriumTempK, radiusEarth, massEarth).toArgb()
        val type = planetTypeLabel(radiusEarth, massEarth)
        val isGaseous = type == "Gas Giant" || type == "Neptune-like" || type == "Sub-Neptune"
        val hasRing = type == "Gas Giant"
        val isRocky = type == "Rocky" || type == "Super-Earth"

        val bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val center = sizePx / 2f
        // Leave room inside the bitmap for the glow (and ring, which is wider than the sphere).
        val radius = sizePx / 2f * if (hasRing) 0.58f else 0.72f

        // --- Atmosphere glow ---
        val glowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = RadialGradient(
                center, center, radius * 1.38f,
                intArrayOf(
                    ColorUtils.setAlphaComponent(accent, 110),
                    ColorUtils.setAlphaComponent(accent, 40),
                    ColorUtils.setAlphaComponent(accent, 0)
                ),
                floatArrayOf(0.6f, 0.8f, 1f),
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawCircle(center, center, radius * 1.38f, glowPaint)

        // --- Ring (back half, drawn behind the sphere) ---
        val ringRect = RectF(
            center - radius * 1.72f, center - radius * 0.52f,
            center + radius * 1.72f, center + radius * 0.52f
        )
        val ringPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = radius * 0.16f
            color = ColorUtils.setAlphaComponent(ColorUtils.blendARGB(accent, Color.WHITE, 0.35f), 200)
        }
        if (hasRing) {
            canvas.save()
            canvas.rotate(RING_TILT_DEG, center, center)
            canvas.drawArc(ringRect, 180f, 180f, false, ringPaint)
            canvas.restore()
        }

        // --- Lit sphere (light source toward the upper-left, like the detail-screen sphere) ---
        val highlight = ColorUtils.blendARGB(accent, Color.WHITE, 0.45f)
        val shadow = ColorUtils.blendARGB(accent, Color.BLACK, 0.55f)
        val spherePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = RadialGradient(
                center - radius * 0.3f,
                center - radius * 0.3f,
                radius * 1.35f,
                intArrayOf(highlight, accent, shadow),
                floatArrayOf(0f, 0.5f, 1f),
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawCircle(center, center, radius, spherePaint)

        // --- Type-specific surface detail, clipped to the sphere ---
        val random = Random(seed)
        canvas.save()
        canvas.clipPath(Path().apply { addCircle(center, center, radius, Path.Direction.CW) })

        if (isGaseous) {
            // Horizontal cloud bands at deterministic offsets.
            val bandPaint = Paint(Paint.ANTI_ALIAS_FLAG)
            repeat(3) { i ->
                val y = center + radius * (-0.55f + 0.45f * i + random.nextFloat() * 0.18f)
                val bandHeight = radius * (0.10f + random.nextFloat() * 0.08f)
                bandPaint.color = ColorUtils.setAlphaComponent(
                    if (i % 2 == 0) shadow else highlight,
                    (28 + random.nextInt(22))
                )
                canvas.drawRect(center - radius, y, center + radius, y + bandHeight, bandPaint)
            }
        } else if (isRocky) {
            // A few darker surface speckles/maria.
            val speckPaint = Paint(Paint.ANTI_ALIAS_FLAG)
            repeat(5) {
                val angle = random.nextFloat() * (2 * Math.PI).toFloat()
                val dist = random.nextFloat() * radius * 0.75f
                val x = center + dist * kotlin.math.cos(angle)
                val y = center + dist * kotlin.math.sin(angle)
                speckPaint.color = ColorUtils.setAlphaComponent(shadow, 60 + random.nextInt(50))
                canvas.drawCircle(x, y, radius * (0.08f + random.nextFloat() * 0.10f), speckPaint)
            }
        }
        canvas.restore()

        // --- Ring (front half, drawn over the sphere) ---
        if (hasRing) {
            canvas.save()
            canvas.rotate(RING_TILT_DEG, center, center)
            canvas.drawArc(ringRect, 0f, 180f, false, ringPaint)
            canvas.restore()
        }

        return bitmap
    }

    private const val RING_TILT_DEG = -18f
}
