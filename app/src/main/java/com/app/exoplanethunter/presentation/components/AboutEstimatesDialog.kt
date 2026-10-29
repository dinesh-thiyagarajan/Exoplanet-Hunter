package com.app.exoplanethunter.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.app.exoplanethunter.R
import com.app.exoplanethunter.presentation.theme.BodyFamily
import com.app.exoplanethunter.presentation.theme.CardTitle
import com.app.exoplanethunter.presentation.theme.Corners
import com.app.exoplanethunter.presentation.theme.InkTextDim
import com.app.exoplanethunter.presentation.theme.Raised

/** "About these estimates": what the AI values are, and that they're rough. */
@Composable
fun AboutEstimatesDialog(onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(24.dp, Corners.Dialog)
                .clip(Corners.Dialog)
                .background(Raised)
                .padding(24.dp),
        ) {
            AiChip()
            Spacer(Modifier.height(12.dp))
            Text(stringResource(R.string.planet_detail_disclaimer_title), style = CardTitle.copy(fontSize = 22.sp))
            Spacer(Modifier.height(10.dp))
            Text(
                stringResource(R.string.planet_detail_disclaimer_body),
                fontFamily = BodyFamily,
                fontSize = 15.sp,
                lineHeight = 23.sp,
                color = InkTextDim,
            )
            Spacer(Modifier.height(20.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                PrimaryButton(stringResource(R.string.planet_detail_disclaimer_dismiss), onDismiss)
            }
        }
    }
}
