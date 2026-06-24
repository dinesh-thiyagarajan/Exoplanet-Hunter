package com.app.exoplanethunter.presentation.screens.about

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import android.widget.Toast
import com.app.exoplanethunter.BuildConfig
import com.app.exoplanethunter.R
import com.app.exoplanethunter.presentation.components.StarField
import com.app.exoplanethunter.presentation.theme.*
import com.app.exoplanethunter.spacefacts.SpaceFactScheduler
import com.app.exoplanethunter.widget.WidgetPinHelper
import org.koin.androidx.compose.koinViewModel
import kotlinx.coroutines.delay
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
