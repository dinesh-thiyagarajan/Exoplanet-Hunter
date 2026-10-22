package com.app.exoplanethunter.presentation.components

import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned

/**
 * Reports whether any part of this element is visible — false once it's scrolled out of its
 * scroll container or off the window. Used to pause per-frame animations nobody can see.
 */
fun Modifier.onScreenChanged(onChange: (Boolean) -> Unit): Modifier = onGloballyPositioned { coords ->
    // boundsInWindow() is clipped by every ancestor, so it collapses to empty off screen.
    val bounds = coords.boundsInWindow()
    onChange(coords.isAttached && bounds.width > 0f && bounds.height > 0f)
}
