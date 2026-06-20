package com.app.exoplanethunter.presentation.screens.about

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.widget.Toast
import com.app.exoplanethunter.BuildConfig
import com.app.exoplanethunter.R
import com.app.exoplanethunter.exoplanet.domain.repository.SyncStatus
import com.app.exoplanethunter.presentation.components.StarField
import com.app.exoplanethunter.presentation.theme.*
import com.app.exoplanethunter.spacefacts.SpaceFactScheduler
import com.app.exoplanethunter.widget.WidgetPinHelper
import org.koin.androidx.compose.koinViewModel
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun AboutScreen(
    viewModel: AboutViewModel = koinViewModel()
) {
    val syncStatus by viewModel.syncStatus.collectAsState()
    val planetCount by viewModel.planetCount.collectAsState()
    val systemCount by viewModel.systemCount.collectAsState()
    val lastSyncTime by viewModel.lastSyncTime.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        containerColor = SpaceBlack,
        // Hosted inside MainScreen's Scaffold, which already applies system-bar/bottom-nav
        // insets. Zero these so the bottom inset isn't padded twice (which left a black gap).
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            StarField(starCount = 100)
            
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
            ) {
                Spacer(modifier = Modifier.height(32.dp))
                
                // Animated Logo Header
                AboutHeader(planetCount, systemCount)

                Spacer(modifier = Modifier.height(32.dp))

                // Sync Control
                AnimatedSection(delay = 100) {
                    SyncControl(
                        status = syncStatus,
                        lastSyncTime = lastSyncTime,
                        onSyncClick = { viewModel.syncData() }
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                // One-tap "add widget" promo (only on launchers that support pinning)
                val widgetContext = LocalContext.current
                if (WidgetPinHelper.isSupported(widgetContext)) {
                    AnimatedSection(delay = 150) {
                        OutlinedButton(
                            onClick = { WidgetPinHelper.requestPin(widgetContext) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Widgets,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(stringResource(R.string.widget_add_to_home))
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))
                }

                SectionHeader(stringResource(R.string.about_section_scientific_framework))

                // Information Sections
                AnimatedSection(delay = 200) {
                    AboutSection(
                        icon = Icons.Default.Dataset,
                        iconColor = CosmicCyan,
                        title = stringResource(R.string.about_data_source_title),
                        description = stringResource(R.string.about_data_source_description)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                AnimatedSection(delay = 300) {
                    AboutSection(
                        icon = Icons.Default.AutoAwesome,
                        iconColor = StarGold,
                        title = stringResource(R.string.about_verification_title),
                        description = stringResource(R.string.about_verification_description)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                AnimatedSection(delay = 400) {
                    AboutSection(
                        icon = Icons.Default.Psychology,
                        iconColor = NebulaPink,
                        title = stringResource(R.string.about_ml_title),
                        description = stringResource(R.string.about_ml_description)
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Debug-only: fire a space-fact notification immediately for testing.
                if (BuildConfig.DEBUG) {
                    val context = LocalContext.current
                    OutlinedButton(
                        onClick = {
                            SpaceFactScheduler.triggerNow(context)
                            Toast.makeText(
                                context,
                                "Triggering space-fact notification…",
                                Toast.LENGTH_SHORT
                            ).show()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("DEBUG: Show space-fact notification")
                    }

                    Spacer(modifier = Modifier.height(32.dp))
                }

                // Footer
                AboutFooter()

                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}

@Composable
private fun AboutHeader(planetCount: Int, systemCount: Int) {
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
                    .size(120.dp)
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
                modifier = Modifier.size(80.dp),
                shape = CircleShape,
                color = SurfaceCard,
                tonalElevation = 8.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Public,
                        contentDescription = null,
                        tint = CosmicCyan,
                        modifier = Modifier.size(40.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = stringResource(R.string.about_title),
            style = MaterialTheme.typography.headlineLarge,
            color = Color.White,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 1.sp
        )
        
        Text(
            text = stringResource(R.string.about_nasa_archive_dataset),
            style = MaterialTheme.typography.labelMedium,
            color = CosmicCyan,
            fontWeight = FontWeight.Bold,
            letterSpacing = 4.sp,
            modifier = Modifier.padding(top = 4.dp)
        )

        Text(
            text = stringResource(R.string.about_version),
            style = MaterialTheme.typography.bodySmall,
            color = TextMuted,
            modifier = Modifier.padding(top = 8.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Improved Stats Bar
        Surface(
            color = SurfaceCard.copy(alpha = 0.5f),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.widthIn(max = 320.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                CountBadge(
                    count = planetCount,
                    label = stringResource(R.string.about_planets),
                    icon = Icons.Default.Public,
                    modifier = Modifier.weight(1f)
                )

                Box(
                    modifier = Modifier
                        .height(32.dp)
                        .width(1.dp)
                        .background(SurfaceCardLight)
                )

                CountBadge(
                    count = systemCount,
                    label = stringResource(R.string.about_systems),
                    icon = Icons.Default.Star,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun CountBadge(
    count: Int, 
    label: String, 
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = CosmicCyan,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = String.format("%,d", count),
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = TextMuted,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

@Composable
private fun SectionHeader(title: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp, start = 4.dp)
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
fun SyncControl(
    status: SyncStatus,
    lastSyncTime: Long,
    onSyncClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(CosmicCyan.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.CloudDownload,
                        contentDescription = null,
                        tint = CosmicCyan,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.about_sync_title),
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = stringResource(R.string.about_sync_subtitle),
                        style = MaterialTheme.typography.labelSmall,
                        color = CosmicCyan,
                        letterSpacing = 0.5.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            
            // Last Updated Info Row
            Surface(
                color = SpaceBlack.copy(alpha = 0.3f),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.History,
                        contentDescription = null,
                        tint = TextMuted,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.about_sync_last_updated),
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (lastSyncTime == 0L) stringResource(R.string.about_sync_never) else formatLastSyncTime(lastSyncTime),
                        style = MaterialTheme.typography.bodySmall,
                        color = StarWhite,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

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
                        contentPadding = PaddingValues(16.dp),
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
fun AboutSection(
    icon: ImageVector,
    iconColor: Color,
    title: String,
    description: String
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(iconColor.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    lineHeight = 22.sp
                )
            }
        }
    }
}

@Composable
private fun AboutFooter() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceCard.copy(alpha = 0.5f))
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.about_acknowledgement_label),
            style = MaterialTheme.typography.labelSmall,
            color = CosmicCyan,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = stringResource(R.string.about_acknowledgement_text),
            style = MaterialTheme.typography.bodySmall,
            color = TextMuted,
            textAlign = TextAlign.Center,
            lineHeight = 18.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = stringResource(R.string.about_acknowledgement_citation),
            style = MaterialTheme.typography.labelSmall,
            color = TextMuted,
            textAlign = TextAlign.Center,
            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
            lineHeight = 16.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stringResource(R.string.about_acknowledgement_attribution),
            style = MaterialTheme.typography.labelSmall,
            color = TextMuted,
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.sp
        )
    }
}

@Composable
private fun AnimatedSection(delay: Int, content: @Composable () -> Unit) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(delay.toLong())
        visible = true
    }
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(600)) + slideInVertically(
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioLowBouncy,
                stiffness = Spring.StiffnessLow
            ),
            initialOffsetY = { it / 3 }
        )
    ) {
        content()
    }
}
