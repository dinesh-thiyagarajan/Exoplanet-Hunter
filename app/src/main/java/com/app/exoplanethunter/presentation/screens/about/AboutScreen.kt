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
    val notificationsEnabled by viewModel.notificationsEnabled.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        containerColor = SpaceBlack,
        // Hosted inside MainScreen's Scaffold, which already applies system-bar/bottom-nav
        // insets. Zero these so the bottom inset isn't padded twice (which left a black gap).
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            AboutHeader(planetCount, systemCount)

            Spacer(modifier = Modifier.height(28.dp))

            // Preferences
            AnimatedSection(delay = 100) {
                Column {
                    SectionHeader(stringResource(R.string.settings_section_preferences))

                    SettingsToggleRow(
                        icon = Icons.Default.Notifications,
                        iconColor = StarGold,
                        title = stringResource(R.string.settings_notifications_title),
                        subtitle = notificationIntervalLabel(viewModel.notificationIntervalHours),
                        checked = notificationsEnabled,
                        onCheckedChange = viewModel::setNotificationsEnabled
                    )

                    val widgetContext = LocalContext.current
                    if (WidgetPinHelper.isSupported(widgetContext)) {
                        Spacer(modifier = Modifier.height(12.dp))
                        SettingsActionRow(
                            icon = Icons.Default.Widgets,
                            iconColor = CosmicCyan,
                            title = stringResource(R.string.settings_widget_title),
                            subtitle = stringResource(R.string.settings_widget_subtitle),
                            onClick = { WidgetPinHelper.requestPin(widgetContext) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Data
            AnimatedSection(delay = 200) {
                Column {
                    SectionHeader(stringResource(R.string.settings_section_data))
                    SyncControl(
                        status = syncStatus,
                        lastSyncTime = lastSyncTime,
                        onSyncClick = { viewModel.syncData() }
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // About — collapsed rows; tap to read
            AnimatedSection(delay = 300) {
                Column {
                    SectionHeader(stringResource(R.string.settings_section_about))

                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        ExpandableInfoRow(
                            icon = Icons.Default.Dataset,
                            iconColor = CosmicCyan,
                            title = stringResource(R.string.about_data_source_title),
                            description = stringResource(R.string.about_data_source_description)
                        )
                        ExpandableInfoRow(
                            icon = Icons.Default.AutoAwesome,
                            iconColor = StarGold,
                            title = stringResource(R.string.about_verification_title),
                            description = stringResource(R.string.about_verification_description)
                        )
                        ExpandableInfoRow(
                            icon = Icons.Default.Psychology,
                            iconColor = NebulaPink,
                            title = stringResource(R.string.about_ml_title),
                            description = stringResource(R.string.about_ml_description)
                        )
                        ExpandableInfoRow(
                            icon = Icons.Default.WorkspacePremium,
                            iconColor = HabitableGreen,
                            title = stringResource(R.string.about_acknowledgement_row_title),
                            description = stringResource(R.string.about_acknowledgement_text) + "\n\n" +
                                stringResource(R.string.about_acknowledgement_citation)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

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

                Spacer(modifier = Modifier.height(28.dp))
            }

            AttributionLine()

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun notificationIntervalLabel(hours: Long): String {
    val days = hours / 24
    return when {
        hours >= 24 && hours % 24 == 0L ->
            if (days == 1L) stringResource(R.string.settings_notifications_every_day)
            else stringResource(R.string.settings_notifications_every_days, days)
        else -> stringResource(R.string.settings_notifications_every_hours, hours)
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
