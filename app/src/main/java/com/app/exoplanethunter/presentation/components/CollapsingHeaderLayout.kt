package com.app.exoplanethunter.presentation.components

import androidx.compose.animation.core.animate
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
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

/** Scroll position of a [CollapsingHeaderLayout]'s header. */
@Stable
class CollapsingHeaderState {
    internal var heightPx by mutableIntStateOf(0)

    /** 0 = fully shown, -[heightPx] = fully hidden. */
    internal var offsetPx by mutableFloatStateOf(0f)

    /**
     * Slides the header fully back into view. Call this after a programmatic jump to the top
     * of the list (`scrollToItem`), which bypasses nested scrolling and so wouldn't reveal it.
     */
    suspend fun expand() {
        if (offsetPx < 0f) animate(offsetPx, 0f) { value, _ -> offsetPx = value }
    }
}

@Composable
fun rememberCollapsingHeaderState(): CollapsingHeaderState = remember { CollapsingHeaderState() }

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
    state: CollapsingHeaderState = rememberCollapsingHeaderState(),
    content: @Composable (headerHeight: Dp) -> Unit,
) {
    val connection = remember(state) {
        object : NestedScrollConnection {
            override fun onPostScroll(consumed: Offset, available: Offset, source: NestedScrollSource): Offset {
                // Follow only what the list actually scrolled, so a short list that can't scroll
                // never hides the header and reaching the top of the list always fully reveals it.
                state.offsetPx = (state.offsetPx + consumed.y).coerceIn(-state.heightPx.toFloat(), 0f)
                return Offset.Zero
            }

            override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
                // Never leave the header half-shown: finish revealing it. Snapping to shown (not
                // hidden) is always safe — near the top of the list, hiding would leave a gap.
                if (state.offsetPx > -state.heightPx) state.expand()
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
        content(with(LocalDensity.current) { state.heightPx.toDp() })

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .onSizeChanged { state.heightPx = it.height }
                .graphicsLayer { translationY = state.offsetPx },
        ) {
            header()
        }
    }
}
