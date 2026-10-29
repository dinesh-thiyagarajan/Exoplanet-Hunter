package com.app.exoplanethunter.presentation.screens.about

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.exoplanethunter.R
import com.app.exoplanethunter.exoplanet.domain.repository.SyncStatus
import com.app.exoplanethunter.presentation.components.AppIcons
import com.app.exoplanethunter.presentation.components.MatchaSwitch
import com.app.exoplanethunter.presentation.components.SecondaryButton
import com.app.exoplanethunter.presentation.components.SectionCard
import com.app.exoplanethunter.presentation.preview.PreviewSurface
import com.app.exoplanethunter.presentation.theme.AlmanacCaption
import com.app.exoplanethunter.presentation.theme.AlmanacData
import com.app.exoplanethunter.presentation.theme.BodyFamily
import com.app.exoplanethunter.presentation.theme.CardTitle
import com.app.exoplanethunter.presentation.theme.Corners
import com.app.exoplanethunter.presentation.theme.InkText
import com.app.exoplanethunter.presentation.theme.InkTextDim
import com.app.exoplanethunter.presentation.theme.InkTextFaint
import com.app.exoplanethunter.presentation.theme.Matcha
import com.app.exoplanethunter.presentation.theme.Raised
import com.app.exoplanethunter.presentation.theme.TempScorched
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/** A settings row with a title, a faint line under it, and a switch. */
@Composable
internal fun SettingsSwitchRow(title: String, subtitle: String?, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, fontFamily = BodyFamily, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = InkText)
            if (subtitle != null) Text(subtitle, fontFamily = BodyFamily, fontSize = 13.sp, color = InkTextFaint)
        }
        Spacer(Modifier.width(12.dp))
        MatchaSwitch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

/** A settings row that opens something, with a chevron. */
@Composable
internal fun SettingsLinkRow(title: String, subtitle: String? = null, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, fontFamily = BodyFamily, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = InkText)
            if (subtitle != null) Text(subtitle, fontFamily = BodyFamily, fontSize = 13.sp, color = InkTextFaint)
        }
        Icon(AppIcons.ChevronRight, null, tint = InkTextFaint, modifier = Modifier.size(18.dp))
    }
}

/** The NASA catalog card: sync status, when it was last updated, and Refresh. */
@Composable
internal fun SyncCard(status: SyncStatus, lastSyncTime: Long, onSyncClick: () -> Unit) {
    SectionCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(stringResource(R.string.about_sync_title), style = CardTitle, modifier = Modifier.weight(1f))
            val (label, color) = when (status) {
                is SyncStatus.Progress -> stringResource(R.string.sync_state_syncing) to Matcha
                is SyncStatus.Error -> stringResource(R.string.sync_state_failed) to TempScorched
                else -> (if (lastSyncTime == 0L) stringResource(R.string.sync_state_never) else stringResource(R.string.sync_state_synced)) to Matcha
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(8.dp).clip(CircleShape).background(color))
                Spacer(Modifier.width(6.dp))
                Text(label, fontFamily = BodyFamily, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = color)
            }
        }
        Spacer(Modifier.height(8.dp))
        Text(
            stringResource(R.string.about_sync_last_updated) + " " +
                (if (lastSyncTime == 0L) stringResource(R.string.about_sync_never) else formatLastSyncTime(lastSyncTime)) +
                " · " + stringResource(R.string.sync_works_offline),
            fontFamily = BodyFamily,
            fontSize = 14.sp,
            color = InkTextDim,
        )
        Spacer(Modifier.height(16.dp))
        when (status) {
            is SyncStatus.Progress -> {
                Box(Modifier.fillMaxWidth().height(10.dp).clip(Corners.Pill).background(Raised)) {
                    Box(
                        Modifier
                            .fillMaxWidth((status.percentage / 100f).coerceIn(0.02f, 1f))
                            .height(10.dp)
                            .clip(Corners.Pill)
                            .background(Matcha)
                    )
                }
                Row(Modifier.fillMaxWidth().padding(top = 10.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(stringResource(R.string.about_sync_updating), style = AlmanacCaption.copy(color = InkTextDim, fontSize = 13.sp))
                    Text("${status.percentage}%", style = AlmanacData.copy(fontSize = 13.sp))
                }
            }
            else -> {
                SecondaryButton(stringResource(R.string.about_sync_refresh), onSyncClick, modifier = Modifier.fillMaxWidth())
                if (status is SyncStatus.Error) {
                    Spacer(Modifier.height(10.dp))
                    Text(status.message, fontFamily = BodyFamily, fontSize = 13.sp, color = TempScorched)
                }
            }
        }
    }
}

private fun formatLastSyncTime(timestamp: Long): String {
    val then = Calendar.getInstance().apply { timeInMillis = timestamp }
    val now = Calendar.getInstance()
    val sameDay = then.get(Calendar.YEAR) == now.get(Calendar.YEAR) && then.get(Calendar.DAY_OF_YEAR) == now.get(Calendar.DAY_OF_YEAR)
    return if (sameDay) "Today, " + SimpleDateFormat("H:mm", Locale.getDefault()).format(Date(timestamp))
    else SimpleDateFormat("d MMM yyyy", Locale.getDefault()).format(Date(timestamp))
}

/** A titled paragraph on the About page. */
@Composable
internal fun AboutSection(title: String, body: String) {
    Column(Modifier.fillMaxWidth().padding(vertical = 10.dp)) {
        Text(title, style = CardTitle)
        Spacer(Modifier.height(6.dp))
        Text(body, fontFamily = BodyFamily, fontSize = 15.sp, lineHeight = 23.sp, color = InkTextDim)
    }
}

@Preview
@Composable
private fun SyncCardPreview() = PreviewSurface {
    Column(Modifier.padding(16.dp)) {
        SyncCard(SyncStatus.Idle, lastSyncTime = System.currentTimeMillis(), onSyncClick = {})
        Spacer(Modifier.height(12.dp))
        SyncCard(SyncStatus.Progress(42), lastSyncTime = 0L, onSyncClick = {})
    }
}
