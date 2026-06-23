package com.app.exoplanethunter.presentation.preview

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.app.exoplanethunter.presentation.theme.ExoplanetHunterTheme
import com.app.exoplanethunter.presentation.theme.SpaceBlack

/** Wraps preview content in the app theme on the space-black background. Preview-only. */
@Composable
internal fun PreviewSurface(content: @Composable () -> Unit) {
    ExoplanetHunterTheme {
        Box(modifier = Modifier.background(SpaceBlack).padding(16.dp)) {
            content()
        }
    }
}
