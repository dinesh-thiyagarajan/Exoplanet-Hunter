package com.app.exoplanethunter.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.exoplanethunter.presentation.theme.AlmanacEyebrow
import com.app.exoplanethunter.presentation.theme.Brass
import com.app.exoplanethunter.presentation.theme.Hairline
import com.app.exoplanethunter.presentation.theme.Ink
import com.app.exoplanethunter.presentation.theme.InkTextDim
import com.app.exoplanethunter.presentation.theme.SansFamily

/**
 * Rectangular almanac filter chip: brass fill when selected, otherwise a hairline
 * outline. The single-accent rule means no per-category colours.
 */
@Composable
fun AlmanacChip(
    label: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(if (selected) Brass else Color.Transparent)
            .border(
                width = 0.5.dp,
                color = if (selected) Brass else Hairline,
                shape = RoundedCornerShape(6.dp),
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp),
    ) {
        Text(
            text = label,
            fontFamily = SansFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 13.sp,
            color = if (selected) Ink else InkTextDim,
        )
    }
}

/**
 * Small outlined brass button with a mono small-caps label, e.g. "COMPARE".
 * Fills faintly with brass when [active].
 */
@Composable
fun AlmanacOutlinedButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    active: Boolean = false,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(if (active) Brass.copy(alpha = 0.16f) else Color.Transparent)
            .border(0.5.dp, Brass, RoundedCornerShape(6.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp),
    ) {
        Text(text = label, style = AlmanacEyebrow)
    }
}
