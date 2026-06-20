package com.app.exoplanethunter.widget

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RadialGradient
import android.graphics.Shader
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.ColorUtils
import com.app.exoplanethunter.presentation.components.planetAccentColor

/**
 * Draws a small shaded planet sphere as a [Bitmap] for the home-screen widget, mirroring the
 * accent colour used by the in-app renderers (a lit sphere with a soft highlight and dark rim).
 * RemoteViews can't host Compose, so we rasterise with plain Android Canvas.
 */
object PlanetBitmapRenderer {

    fun render(
        sizePx: Int,
        equilibriumTempK: Double?,
        radiusEarth: Double?,
        massEarth: Double?
    ): Bitmap {
        val accent = planetAccentColor(equilibriumTempK, radiusEarth, massEarth).toArgb()

        val bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val center = sizePx / 2f
        val radius = sizePx / 2f * 0.92f

        val highlight = ColorUtils.blendARGB(accent, Color.WHITE, 0.45f)
        val shadow = ColorUtils.blendARGB(accent, Color.BLACK, 0.55f)

        // Light source toward the upper-left, like the detail-screen sphere.
        val gradient = RadialGradient(
            center - radius * 0.3f,
            center - radius * 0.3f,
            radius * 1.35f,
            intArrayOf(highlight, accent, shadow),
            floatArrayOf(0f, 0.5f, 1f),
            Shader.TileMode.CLAMP
        )

        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { shader = gradient }
        canvas.drawCircle(center, center, radius, paint)
        return bitmap
    }
}
