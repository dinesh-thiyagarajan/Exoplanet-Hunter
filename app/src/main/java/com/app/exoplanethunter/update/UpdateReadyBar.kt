package com.app.exoplanethunter.update

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.exoplanethunter.R
import com.app.exoplanethunter.presentation.components.PrimaryButton
import com.app.exoplanethunter.presentation.theme.BodyFamily
import com.app.exoplanethunter.presentation.theme.Corners
import com.app.exoplanethunter.presentation.theme.InkText
import com.app.exoplanethunter.presentation.theme.MatchaDeep
import com.app.exoplanethunter.presentation.theme.OnMatchaDeep

/** "A new version is ready" pill, shown once a flexible update has downloaded. */
@Composable
fun UpdateReadyBar(
    visible: Boolean,
    onRestart: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically { it },
        exit = slideOutVertically { it } + fadeOut(),
        modifier = modifier,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(12.dp, Corners.Pill)
                .clip(Corners.Pill)
                .background(MatchaDeep)
                .padding(start = 20.dp, end = 6.dp, top = 6.dp, bottom = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                stringResource(R.string.update_ready),
                fontFamily = BodyFamily,
                fontSize = 14.sp,
                color = InkText,
                modifier = Modifier.weight(1f),
            )
            Text(
                stringResource(R.string.update_later),
                fontFamily = BodyFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = OnMatchaDeep,
                modifier = Modifier.clip(Corners.Pill).clickable(onClick = onDismiss).padding(horizontal = 12.dp, vertical = 12.dp),
            )
            Spacer(Modifier.width(4.dp))
            PrimaryButton(stringResource(R.string.update_restart), onRestart)
        }
    }
}
