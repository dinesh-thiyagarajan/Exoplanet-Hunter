package com.app.exoplanethunter.presentation.components

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * The area content must stay clear of: status/navigation bars plus any display cutout.
 * Deliberately excludes the IME — these screens scroll rather than resize.
 */
val SafeAreaInsets: WindowInsets
    @Composable get() = WindowInsets.systemBars.union(WindowInsets.displayCutout)

/**
 * Insets for a pinned top bar: keeps it clear of the status bar and of side cutouts in
 * landscape. Apply *after* `.background(...)` so the bar's colour still paints behind the
 * status bar — that is the point of drawing edge to edge.
 */
@Composable
fun Modifier.topBarInsets(): Modifier = windowInsetsPadding(
    SafeAreaInsets.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal)
)

/**
 * Insets for the scrolling body of a full-screen (non-tab) screen: sides and the
 * navigation bar / gesture pill. Apply *before* `.verticalScroll(...)` so it pads the
 * viewport rather than the scrolled content.
 */
@Composable
fun Modifier.screenContentInsets(): Modifier = windowInsetsPadding(
    SafeAreaInsets.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom)
)
