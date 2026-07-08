package com.app.exoplanethunter.presentation.screens.about
import androidx.compose.ui.tooling.preview.Preview
import com.app.exoplanethunter.presentation.preview.PreviewSurface

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.exoplanethunter.BuildConfig
import com.app.exoplanethunter.R
import com.app.exoplanethunter.exoplanet.domain.repository.SyncStatus
import com.app.exoplanethunter.presentation.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
internal fun AboutHeader(planetCount: Int, systemCount: Int) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        val infiniteTransition = rememberInfiniteTransition(label = "rotation")
        val rotation by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(10000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "logo_rotation"
        )

        Box(contentAlignment = Alignment.Center) {
            // Rotating ring
            Box(
                modifier = Modifier
                    .size(84.dp)
                    .rotate(rotation)
                    .border(
                        width = 2.dp,
                        brush = Brush.sweepGradient(
                            listOf(CosmicCyan, Color.Transparent, NebulaPink, Color.Transparent, CosmicCyan)
                        ),
                        shape = CircleShape
                    )
            )

            Surface(
                modifier = Modifier.size(58.dp),
                shape = CircleShape,
                color = SurfaceCard,
                tonalElevation = 8.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Public,
                        contentDescription = null,
                        tint = CosmicCyan,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stringResource(R.string.about_title),
            style = MaterialTheme.typography.headlineMedium,
            color = Color.White,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 1.sp
        )

        Text(
            text = stringResource(R.string.about_nasa_archive_dataset),
            style = MaterialTheme.typography.labelSmall,
            color = CosmicCyan,
            fontWeight = FontWeight.Bold,
            letterSpacing = 3.sp,
            modifier = Modifier.padding(top = 4.dp)
        )

        Text(
            text = "v${BuildConfig.VERSION_NAME}  •  " +
                "${"%,d".format(planetCount)} ${stringResource(R.string.about_planets).lowercase()}  •  " +
                "${"%,d".format(systemCount)} ${stringResource(R.string.about_systems).lowercase()}",
            style = MaterialTheme.typography.bodySmall,
            color = TextMuted,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

@Composable
internal fun SectionHeader(title: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp, start = 4.dp)
    ) {
        Text(
            text = title.uppercase(),
            style = MaterialTheme.typography.labelLarge,
            color = TextMuted,
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.sp
        )
        Spacer(modifier = Modifier.width(16.dp))
        Divider(
            modifier = Modifier.weight(1f),
            color = SurfaceCardLight,
            thickness = 1.dp
        )
    }
}

@Composable
private fun IconBadge(icon: ImageVector, color: Color) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(color.copy(alpha = 0.1f)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(22.dp)
        )
    }
}

/** A settings row with a trailing switch. */
@Composable
internal fun SettingsToggleRow(
    icon: ImageVector,
    iconColor: Color,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconBadge(icon, iconColor)
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedTrackColor = CosmicCyan,
                    checkedThumbColor = SpaceBlack
                )
            )
        }
    }
}

/** A tappable settings row with a trailing chevron. */
@Composable
internal fun SettingsActionRow(
    icon: ImageVector,
    iconColor: Color,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconBadge(icon, iconColor)
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = TextMuted,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

/** One entry of the About list: icon, title and always-visible description. */
internal data class InfoItem(
    val icon: ImageVector,
    val iconColor: Color,
    val title: String,
    val description: String
)

/** A single card presenting the About entries as a plain divided list. */
@Composable
internal fun InfoListCard(items: List<InfoItem>) {
    Card(
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(vertical = 4.dp)) {
            items.forEachIndexed { index, item ->
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    IconBadge(item.icon, item.iconColor)
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = item.title,
                            style = MaterialTheme.typography.titleSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = item.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            lineHeight = 18.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
                if (index != items.lastIndex) {
                    Divider(
                        color = SurfaceCardLight,
                        thickness = 1.dp,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun SyncControl(
    status: SyncStatus,
    lastSyncTime: Long,
    onSyncClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconBadge(Icons.Default.CloudDownload, CosmicCyan)
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.about_sync_title),
                        style = MaterialTheme.typography.titleSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = stringResource(R.string.about_sync_last_updated) + " " +
                            if (lastSyncTime == 0L) stringResource(R.string.about_sync_never)
                            else formatLastSyncTime(lastSyncTime),
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            when (status) {
                is SyncStatus.Idle, is SyncStatus.Success, is SyncStatus.Error -> {
                    Button(
                        onClick = onSyncClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CosmicCyan,
                            contentColor = SpaceBlack
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(12.dp),
                        enabled = status !is SyncStatus.Progress
                    ) {
                        Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            stringResource(R.string.about_sync_refresh),
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }

                    if (status is SyncStatus.Success) {
                        SyncMessage(
                            message = stringResource(R.string.about_sync_success),
                            color = HabitableGreen,
                            icon = Icons.Default.CheckCircle
                        )
                    }
                    if (status is SyncStatus.Error) {
                        SyncMessage(
                            message = status.message,
                            color = HostileRed,
                            icon = Icons.Default.Error
                        )
                    }
                }
                is SyncStatus.Progress -> {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        LinearProgressIndicator(
                            progress = { status.percentage / 100f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(CircleShape),
                            color = CosmicCyan,
                            trackColor = SpaceBlack
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = stringResource(R.string.about_sync_updating),
                                style = MaterialTheme.typography.labelMedium,
                                color = CosmicCyan
                            )
                            Text(
                                text = "${status.percentage}%",
                                style = MaterialTheme.typography.labelMedium,
                                color = CosmicCyan,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun formatLastSyncTime(timestamp: Long): String {
    val sdf = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

@Composable
private fun SyncMessage(message: String, color: Color, icon: ImageVector) {
    Row(
        modifier = Modifier.padding(top = 12.dp, start = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(14.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = message,
            color = color,
            style = MaterialTheme.typography.labelMedium
        )
    }
}

@Composable
internal fun AttributionLine() {
    Text(
        text = stringResource(R.string.about_acknowledgement_attribution),
        style = MaterialTheme.typography.labelSmall,
        color = TextMuted,
        fontWeight = FontWeight.Bold,
        letterSpacing = 2.sp,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth()
    )
}


@Preview
@Composable
private fun AboutHeaderPreview() = PreviewSurface { AboutHeader(planetCount = 5234, systemCount = 3891) }

@Preview
@Composable
private fun SectionHeaderPreview() = PreviewSurface { SectionHeader("Preferences") }

@Preview
@Composable
private fun SettingsToggleRowPreview() = PreviewSurface {
    SettingsToggleRow(
        icon = Icons.Default.Notifications,
        iconColor = StarGold,
        title = "Space fact notifications",
        subtitle = "A new fact every 2 days",
        checked = true,
        onCheckedChange = {}
    )
}

@Preview
@Composable
private fun InfoListCardPreview() = PreviewSurface {
    InfoListCard(
        items = listOf(
            InfoItem(
                icon = Icons.Default.Dataset,
                iconColor = CosmicCyan,
                title = "NASA Exoplanet Archive",
                description = "The global standard for confirmed exoplanet data."
            ),
            InfoItem(
                icon = Icons.Default.AutoAwesome,
                iconColor = StarGold,
                title = "Data Verification",
                description = "Every planet has undergone a rigorous peer-review process."
            )
        )
    )
}
