package com.app.exoplanethunter.presentation.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathBuilder
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

/**
 * Outline icons in the handoff's style: 24-unit grid, round caps and joins, a 2.4 stroke.
 * Tinted by the `Icon` that draws them, like the Material icons.
 */
object AppIcons {

    private fun icon(name: String, stroke: Float = 2.4f, block: PathBuilder.() -> Unit): ImageVector =
        ImageVector.Builder(name = name, defaultWidth = 24.dp, defaultHeight = 24.dp, viewportWidth = 24f, viewportHeight = 24f)
            .path(
                fill = null,
                stroke = SolidColor(Color.Black),
                strokeLineWidth = stroke,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
                pathBuilder = block,
            )
            .build()

    private fun filled(name: String, block: PathBuilder.() -> Unit): ImageVector =
        ImageVector.Builder(name = name, defaultWidth = 24.dp, defaultHeight = 24.dp, viewportWidth = 24f, viewportHeight = 24f)
            .path(
                fill = SolidColor(Color.Black),
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 2.4f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
                pathBuilder = block,
            )
            .build()

    private fun PathBuilder.circle(cx: Float, cy: Float, r: Float) {
        moveTo(cx + r, cy)
        arcTo(r, r, 0f, isMoreThanHalf = false, isPositiveArc = true, x1 = cx - r, y1 = cy)
        arcTo(r, r, 0f, isMoreThanHalf = false, isPositiveArc = true, x1 = cx + r, y1 = cy)
        close()
    }

    private val starPath: PathBuilder.() -> Unit = {
        moveTo(12f, 2.5f)
        lineTo(14.9f, 8.4f)
        lineTo(21.4f, 9.3f)
        lineTo(16.7f, 13.9f)
        lineTo(17.8f, 20.4f)
        lineTo(12f, 17.3f)
        lineTo(6.2f, 20.4f)
        lineTo(7.3f, 13.9f)
        lineTo(2.6f, 9.3f)
        lineTo(9.1f, 8.4f)
        close()
    }

    /** Ringed planet (Planets tab). */
    val Planet: ImageVector = icon("Planet") {
        circle(12f, 12f, 5.5f)
        // A tilted ring: an ellipse rotated -25°, passing through the planet.
        moveTo(21.1f, 7.8f)
        arcTo(10f, 3.6f, -25f, isMoreThanHalf = false, isPositiveArc = true, x1 = 2.9f, y1 = 16.2f)
        arcTo(10f, 3.6f, -25f, isMoreThanHalf = false, isPositiveArc = true, x1 = 21.1f, y1 = 7.8f)
    }

    /** Sun (Stars tab). */
    val Sun: ImageVector = icon("Sun") {
        circle(12f, 12f, 4f)
        moveTo(12f, 2f); verticalLineTo(4f)
        moveTo(12f, 20f); verticalLineTo(22f)
        moveTo(4.93f, 4.93f); lineTo(6.34f, 6.34f)
        moveTo(17.66f, 17.66f); lineTo(19.07f, 19.07f)
        moveTo(2f, 12f); horizontalLineTo(4f)
        moveTo(20f, 12f); horizontalLineTo(22f)
        moveTo(6.34f, 17.66f); lineTo(4.93f, 19.07f)
        moveTo(19.07f, 4.93f); lineTo(17.66f, 6.34f)
    }

    /** Star outline (Fav tab, favourite off). */
    val Star: ImageVector = icon("Star", block = starPath)

    /** Filled star (favourite on). */
    val StarFilled: ImageVector = filled("StarFilled", starPath)

    /** Bars (Stats tab). */
    val Bars: ImageVector = icon("Bars") {
        moveTo(3f, 3f); verticalLineTo(21f); horizontalLineTo(21f)
        moveTo(8f, 17f); verticalLineTo(13f)
        moveTo(13f, 17f); verticalLineTo(6f)
        moveTo(18f, 17f); verticalLineTo(10f)
    }

    /** Vertical sliders (Settings tab). */
    val Sliders: ImageVector = icon("Sliders") {
        moveTo(4f, 21f); verticalLineTo(14f)
        moveTo(4f, 10f); verticalLineTo(3f)
        moveTo(12f, 21f); verticalLineTo(12f)
        moveTo(12f, 8f); verticalLineTo(3f)
        moveTo(20f, 21f); verticalLineTo(16f)
        moveTo(20f, 12f); verticalLineTo(3f)
        moveTo(2f, 14f); horizontalLineTo(6f)
        moveTo(10f, 8f); horizontalLineTo(14f)
        moveTo(18f, 16f); horizontalLineTo(22f)
    }

    val Search: ImageVector = icon("Search", stroke = 2.6f) {
        circle(11f, 11f, 7f)
        moveTo(21f, 21f); lineTo(16.65f, 16.65f)
    }

    /** Filter / sort: three lines getting shorter. */
    val Filter: ImageVector = icon("Filter", stroke = 2.6f) {
        moveTo(4f, 6f); horizontalLineTo(20f)
        moveTo(7f, 12f); horizontalLineTo(17f)
        moveTo(10f, 18f); horizontalLineTo(14f)
    }

    val Back: ImageVector = icon("Back", stroke = 2.75f) {
        moveTo(15f, 18f); lineTo(9f, 12f); lineTo(15f, 6f)
    }

    val ChevronRight: ImageVector = icon("ChevronRight", stroke = 2.6f) {
        moveTo(9f, 18f); lineTo(15f, 12f); lineTo(9f, 6f)
    }

    val Close: ImageVector = icon("Close", stroke = 2.6f) {
        moveTo(18f, 6f); lineTo(6f, 18f)
        moveTo(6f, 6f); lineTo(18f, 18f)
    }

    val ArrowRight: ImageVector = icon("ArrowRight", stroke = 2.6f) {
        moveTo(5f, 12f); horizontalLineTo(19f)
        moveTo(12f, 5f); lineTo(19f, 12f); lineTo(12f, 19f)
    }

    val Swap: ImageVector = icon("Swap", stroke = 2.4f) {
        moveTo(16f, 3f); lineTo(20f, 7f); lineTo(16f, 11f)
        moveTo(20f, 7f); horizontalLineTo(4f)
        moveTo(8f, 21f); lineTo(4f, 17f); lineTo(8f, 13f)
        moveTo(4f, 17f); horizontalLineTo(20f)
    }

    val Copy: ImageVector = icon("Copy") {
        moveTo(10f, 8f); horizontalLineTo(19f)
        quadTo(20f, 8f, 20f, 9f); verticalLineTo(19f)
        quadTo(20f, 20f, 19f, 20f); horizontalLineTo(10f)
        quadTo(9f, 20f, 9f, 19f); verticalLineTo(9f)
        quadTo(9f, 8f, 10f, 8f); close()
        moveTo(5f, 15f); quadTo(4f, 15f, 4f, 14f); verticalLineTo(5f)
        quadTo(4f, 4f, 5f, 4f); horizontalLineTo(14f); quadTo(15f, 4f, 15f, 5f)
    }

    val Share: ImageVector = icon("Share") {
        moveTo(12f, 3f); verticalLineTo(15f)
        moveTo(8f, 7f); lineTo(12f, 3f); lineTo(16f, 7f)
        moveTo(4f, 13f); verticalLineTo(19f)
        quadTo(4f, 21f, 6f, 21f); horizontalLineTo(18f)
        quadTo(20f, 21f, 20f, 19f); verticalLineTo(13f)
    }

    val Check: ImageVector = icon("Check", stroke = 2.75f) {
        moveTo(20f, 6f); lineTo(9f, 17f); lineTo(4f, 12f)
    }

    val External: ImageVector = icon("External") {
        moveTo(15f, 3f); horizontalLineTo(21f); verticalLineTo(9f)
        moveTo(10f, 14f); lineTo(21f, 3f)
        moveTo(18f, 13f); verticalLineTo(19f)
        quadTo(18f, 21f, 16f, 21f); horizontalLineTo(5f)
        quadTo(3f, 21f, 3f, 19f); verticalLineTo(8f)
        quadTo(3f, 6f, 5f, 6f); horizontalLineTo(11f)
    }

    val Play: ImageVector = filled("Play") {
        moveTo(7f, 4.5f); lineTo(19f, 12f); lineTo(7f, 19.5f); close()
    }

    val Pause: ImageVector = filled("Pause") {
        moveTo(7f, 5f); horizontalLineTo(9.5f); verticalLineTo(19f); horizontalLineTo(7f); close()
        moveTo(14.5f, 5f); horizontalLineTo(17f); verticalLineTo(19f); horizontalLineTo(14.5f); close()
    }

    val ArrowUp: ImageVector = icon("ArrowUp", stroke = 2.6f) {
        moveTo(12f, 19f); verticalLineTo(5f)
        moveTo(5f, 12f); lineTo(12f, 5f); lineTo(19f, 12f)
    }

    val Sparkle: ImageVector = filled("Sparkle") {
        moveTo(12f, 2f)
        quadTo(13f, 11f, 22f, 12f)
        quadTo(13f, 13f, 12f, 22f)
        quadTo(11f, 13f, 2f, 12f)
        quadTo(11f, 11f, 12f, 2f)
        close()
    }
}
