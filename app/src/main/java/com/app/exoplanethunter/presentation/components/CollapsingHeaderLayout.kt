package com.app.exoplanethunter.presentation.components

import androidx.compose.animation.core.animate
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.Velocity

/**
 * "Quick return" header: slides away as the list scrolls down and slides back in as soon as
 * the user scrolls up, from anywhere in the list.
 *
 * The header is drawn over the content, so [content] receives the header's height and must
 * add it as top content padding to its scrolling list — that way the first item starts
 * below the header while the list still uses the full screen once the header is hidden.
 */
@Composable
fun CollapsingHeaderLayout(
    header: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable (headerHeight: Dp) -> Unit,
) {
    var headerHeightPx by remember { mutableIntStateOf(0) }
    // 0 = fully shown, -headerHeightPx = fully hidden.
    var headerOffsetPx by remember { mutableFloatStateOf(0f) }

    val connection = remember {
        object : NestedScrollConnection {
            override fun onPostScroll(consumed: Offset, available: Offset, source: NestedScrollSource): Offset {
                // Follow only what the list actually scrolled, so a short list that can't scroll
                // never hides the header and reaching the top of the list always fully reveals it.
                headerOffsetPx = (headerOffsetPx + consumed.y).coerceIn(-headerHeightPx.toFloat(), 0f)
                return Offset.Zero
            }

            override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
                // Never leave the header half-shown: finish revealing it. Snapping to shown (not
                // hidden) is always safe — near the top of the list, hiding would leave a gap.
                if (headerOffsetPx < 0f && headerOffsetPx > -headerHeightPx) {
                    animate(headerOffsetPx, 0f) { value, _ -> headerOffsetPx = value }
                }
                return Velocity.Zero
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .clipToBounds()
            .nestedScroll(connection),
    ) {
        content(with(LocalDensity.current) { headerHeightPx.toDp() })

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .onSizeChanged { headerHeightPx = it.height }
                .graphicsLayer { translationY = headerOffsetPx },
        ) {
            header()
        }
    }
}
