package com.app.exoplanethunter.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.app.exoplanethunter.R
import com.app.exoplanethunter.presentation.theme.Brass
import com.app.exoplanethunter.presentation.theme.Surface
import kotlinx.coroutines.launch

/** Rows scrolled past before the button appears. */
private const val SHOW_AFTER_INDEX = 10

/**
 * Brass "back to top" button for long lists. Appears once the list is scrolled past
 * [SHOW_AFTER_INDEX] rows; tapping it returns to the top and brings the collapsing header back.
 */
@Composable
fun BackToTopButton(
    listState: LazyListState,
    headerState: CollapsingHeaderState,
    modifier: Modifier = Modifier,
) {
    val scope = rememberCoroutineScope()
    val visible by remember { derivedStateOf { listState.firstVisibleItemIndex > SHOW_AFTER_INDEX } }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + scaleIn(initialScale = 0.8f),
        exit = fadeOut() + scaleOut(targetScale = 0.8f),
        modifier = modifier,
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .shadow(8.dp, CircleShape)
                .clip(CircleShape)
                .background(Surface)
                .border(0.5.dp, Brass, CircleShape)
                .clickable {
                    scope.launch { headerState.expand() }
                    scope.launch {
                        // Jump most of the way first: animating across thousands of rows is slow.
                        if (listState.firstVisibleItemIndex > SHOW_AFTER_INDEX) {
                            listState.scrollToItem(SHOW_AFTER_INDEX)
                        }
                        listState.animateScrollToItem(0)
                    }
                },
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Icons.Default.KeyboardArrowUp,
                contentDescription = stringResource(R.string.cd_back_to_top),
                tint = Brass,
            )
        }
    }
}
