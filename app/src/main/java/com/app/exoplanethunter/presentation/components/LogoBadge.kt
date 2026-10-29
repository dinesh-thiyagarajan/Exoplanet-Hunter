package com.app.exoplanethunter.presentation.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.app.exoplanethunter.R

/**
 * The app logo ("Peekaboo") as a round badge, drawn from the launcher icon's own layers so it
 * always matches the icon.
 */
@Composable
fun LogoBadge(size: Dp, modifier: Modifier = Modifier) {
    Box(modifier.size(size).clip(CircleShape)) {
        // The adaptive-icon art sits in the central 66 of 108 units; scale it up to fill the badge.
        val fill = 108f / 80f
        Image(painterResource(R.drawable.ic_launcher_background), null, Modifier.fillMaxSize().scale(fill))
        Image(painterResource(R.drawable.ic_launcher_foreground), null, Modifier.fillMaxSize().scale(fill))
    }
}

/** Small default size for empty states. */
val EmptyArtSize: Dp = 120.dp
