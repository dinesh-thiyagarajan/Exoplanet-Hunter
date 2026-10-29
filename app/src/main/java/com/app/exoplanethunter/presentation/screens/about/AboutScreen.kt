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
import androidx.compose.ui.unit.sp
import android.widget.Toast
import com.app.exoplanethunter.BuildConfig
import com.app.exoplanethunter.R
import com.app.exoplanethunter.presentation.theme.*
import com.app.exoplanethunter.discovery.DiscoveryScheduler
import com.app.exoplanethunter.spacefacts.SpaceFactScheduler
import com.app.exoplanethunter.widget.WidgetPinHelper
import androidx.compose.foundation.background
import com.app.exoplanethunter.presentation.components.BackTopBar
import com.app.exoplanethunter.presentation.components.SecondaryButton
import com.app.exoplanethunter.presentation.components.SectionCard
import com.app.exoplanethunter.presentation.components.TabHeader
import com.app.exoplanethunter.presentation.components.screenContentInsets
import com.app.exoplanethunter.presentation.components.topBarInsets
import org.koin.androidx.compose.koinViewModel
import kotlinx.coroutines.delay
import java.util.*

/** The Settings tab: preferences, the NASA catalog, and links to the fact library and About. */
@Composable
fun AboutScreen(
    onOpenFactLibrary: () -> Unit = {},
    onOpenAbout: () -> Unit = {},
    viewModel: AboutViewModel = koinViewModel()
) {
    val syncStatus by viewModel.syncStatus.collectAsState()
    val lastSyncTime by viewModel.lastSyncTime.collectAsState()
    val notificationsEnabled by viewModel.notificationsEnabled.collectAsState()
    val discoveryEnabled by viewModel.discoveryEnabled.collectAsState()
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Ink)
            .verticalScroll(rememberScrollState())
    ) {
        TabHeader(title = stringResource(R.string.nav_about), subtitle = null)
        Spacer(modifier = Modifier.height(12.dp))

        Column(Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            // Preferences
            SectionCard(padding = PaddingValues(vertical = 8.dp)) {
                SettingsSwitchRow(
                    title = stringResource(R.string.settings_notifications_title),
                    subtitle = notificationIntervalLabel(viewModel.notificationIntervalHours),
                    checked = notificationsEnabled,
                    onCheckedChange = viewModel::setNotificationsEnabled
                )
                SettingsSwitchRow(
                    title = stringResource(R.string.settings_discovery_title),
                    subtitle = stringResource(R.string.settings_discovery_subtitle),
                    checked = discoveryEnabled,
                    onCheckedChange = viewModel::setDiscoveryEnabled
                )
                if (WidgetPinHelper.isSupported(context)) {
                    SettingsLinkRow(
                        title = stringResource(R.string.settings_widget_title),
                        subtitle = stringResource(R.string.settings_widget_subtitle),
                        onClick = { WidgetPinHelper.requestPin(context) }
                    )
                }
            }

            // Data
            SyncCard(status = syncStatus, lastSyncTime = lastSyncTime, onSyncClick = { viewModel.syncData() })

            // Links
            SectionCard(padding = PaddingValues(vertical = 4.dp)) {
                SettingsLinkRow(stringResource(R.string.settings_fact_library_title), onClick = onOpenFactLibrary)
                SettingsLinkRow(stringResource(R.string.settings_about_link), onClick = onOpenAbout)
            }

            Text(
                stringResource(R.string.settings_attribution),
                style = AlmanacCaption,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)
            )

            // Debug-only: fire a space-fact notification immediately for testing.
            if (BuildConfig.DEBUG) {
                SecondaryButton(
                    label = "DEBUG: space-fact notification",
                    onClick = {
                        SpaceFactScheduler.triggerNow(context)
                        Toast.makeText(context, "Triggering space-fact notification…", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                SecondaryButton(
                    label = "DEBUG: new-discovery check",
                    onClick = {
                        DiscoveryScheduler.triggerNow(context)
                        Toast.makeText(context, "Checking archive for new worlds…", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

/** About & data sources: where the data comes from, how planets are confirmed, the AI, credits. */
@Composable
fun AboutPageScreen(
    onBack: () -> Unit,
    viewModel: AboutViewModel = koinViewModel()
) {
    val planetCount by viewModel.planetCount.collectAsState()
    val systemCount by viewModel.systemCount.collectAsState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Ink)
            .screenContentInsets()
    ) {
        BackTopBar(
            onBack = onBack,
            backDescription = stringResource(R.string.cd_back),
            title = stringResource(R.string.settings_about_link),
            titleSize = 26,
            modifier = Modifier.topBarInsets(),
        )
        Column(
            Modifier
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                SectionCard(Modifier.weight(1f), small = true) {
                    Text("%,d".format(planetCount), style = AlmanacHeroFigure.copy(fontSize = 28.sp))
                    Text(stringResource(R.string.about_planets), style = AlmanacCaption.copy(color = InkTextDim, fontSize = 14.sp))
                }
                SectionCard(Modifier.weight(1f), small = true) {
                    Text("%,d".format(systemCount), style = AlmanacHeroFigure.copy(fontSize = 28.sp, color = StarOrange))
                    Text(stringResource(R.string.about_systems), style = AlmanacCaption.copy(color = InkTextDim, fontSize = 14.sp))
                }
            }
            Spacer(Modifier.height(12.dp))
            AboutSection(stringResource(R.string.about_data_source_title), stringResource(R.string.about_data_source_description))
            AboutSection(stringResource(R.string.about_verification_title), stringResource(R.string.about_verification_description))
            AboutSection(stringResource(R.string.about_ml_title), stringResource(R.string.about_ml_description))
            AboutSection(stringResource(R.string.about_affiliation_title), stringResource(R.string.about_affiliation_description))
            Spacer(Modifier.height(8.dp))
            SectionCard(small = true) {
                Text(stringResource(R.string.about_acknowledgement_row_title), style = CardTitle)
                Spacer(Modifier.height(6.dp))
                Text(stringResource(R.string.about_acknowledgement_text), style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(10.dp))
                Text(stringResource(R.string.about_acknowledgement_citation), style = AlmanacCaption.copy(color = InkTextDim))
                Text(stringResource(R.string.about_acknowledgement_attribution), style = AlmanacCaption)
            }
            Spacer(Modifier.height(32.dp))
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
