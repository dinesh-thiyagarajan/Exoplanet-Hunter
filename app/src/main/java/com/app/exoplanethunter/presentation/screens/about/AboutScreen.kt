package com.app.exoplanethunter.presentation.screens.about

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.app.exoplanethunter.R
import com.app.exoplanethunter.exoplanet.domain.repository.SyncStatus
import com.app.exoplanethunter.presentation.components.StarField
import com.app.exoplanethunter.presentation.theme.*
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
    val context = LocalContext.current

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.syncData()
        }
    }

    Scaffold(
        containerColor = SpaceBlack,
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
                        onSyncClick = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                val permissionCheck = ContextCompat.checkSelfPermission(
                                    context, Manifest.permission.POST_NOTIFICATIONS
                                )
                                if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
                                    viewModel.syncData()
                                } else {
                                    permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                }
                            } else {
                                viewModel.syncData()
                            }
                        }
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
                
                SectionHeader("Scientific Framework")

                // Information Sections
                AnimatedSection(delay = 200) {
                    AboutSection(
                        icon = Icons.Default.Dataset,
                        iconColor = CosmicCyan,
                        title = "NASA Exoplanet Archive",
                        description = "Operated by the California Institute of Technology (Caltech) under contract with NASA, this archive is the global standard for confirmed exoplanet data. It aggregates measurements from space missions like Kepler, K2, and TESS, as well as major ground-based surveys."
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                AnimatedSection(delay = 300) {
                    AboutSection(
                        icon = Icons.Default.AutoAwesome,
                        iconColor = StarGold,
                        title = "Data Verification",
                        description = "Every planet in this app has undergone a rigorous peer-review process. Confirmation typically requires multiple independent observations to verify the planet's existence and accurately measure its physical properties, such as mass, radius, and orbital period."
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                AnimatedSection(delay = 400) {
                    AboutSection(
                        icon = Icons.Default.Psychology,
                        iconColor = NebulaPink,
                        title = "Inference & ML",
                        description = "We use planetary and stellar features (orbital semi-major axis, stellar mass, temperature) to run on-device habitability estimates. These insights are intended for educational exploration based on current astrophysical models."
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

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
            
            // App Icon Placeholder / Icon
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
            text = "Exoplanet Hunter",
            style = MaterialTheme.typography.headlineLarge,
            color = Color.White,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 1.sp
        )
        
        Text(
            text = "NASA ARCHIVE DATASET",
            style = MaterialTheme.typography.labelMedium,
            color = CosmicCyan,
            fontWeight = FontWeight.Bold,
            letterSpacing = 4.sp,
            modifier = Modifier.padding(top = 4.dp)
        )
        
        Text(
            text = "Version 1.12.2",
            style = MaterialTheme.typography.bodySmall,
            color = TextMuted,
            modifier = Modifier.padding(top = 8.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CountBadge(count = planetCount, label = "Planets", icon = Icons.Default.Public)
            CountBadge(count = systemCount, label = "Systems", icon = Icons.Default.Star)
        }
    }
}

@Composable
private fun CountBadge(count: Int, label: String, icon: ImageVector) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = CosmicCyan,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
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
            letterSpacing = 1.sp
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
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(CosmicCyan.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.CloudDownload,
                        contentDescription = null,
                        tint = CosmicCyan,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = "NASA TAP Sync",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Last updated: ${formatLastSyncTime(lastSyncTime)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted
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
                        contentPadding = PaddingValues(16.dp)
                    ) {
                        Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "REFRESH CATALOG",
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }
                    
                    if (status is SyncStatus.Success) {
                        SyncMessage(
                            message = "Catalog successfully updated",
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
                                text = "Updating planetary records...",
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
    if (timestamp == 0L) return "Never"
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
            text = "DATA SOURCE ACKNOWLEDGEMENT",
            style = MaterialTheme.typography.labelSmall,
            color = CosmicCyan,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Text(
            text = "This application uses the NASA Exoplanet Archive, which is operated by the California Institute of Technology, under contract with the National Aeronautics and Space Administration under the Exoplanet Exploration Program.",
            style = MaterialTheme.typography.bodySmall,
            color = TextMuted,
            textAlign = TextAlign.Center,
            lineHeight = 18.sp
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "IPAC / CALTECH / NASA",
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
