package com.app.exoplanethunter.presentation.screens.about

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.exoplanethunter.analytics.domain.model.AnalyticsEvent
import com.app.exoplanethunter.analytics.domain.usecase.TrackEventUseCase
import com.app.exoplanethunter.exoplanet.domain.repository.ExoplanetRepository
import com.app.exoplanethunter.exoplanet.domain.repository.SyncStatus
import com.app.exoplanethunter.exoplanet.domain.usecase.SyncExoplanetsUseCase
import com.app.exoplanethunter.spacefacts.SpaceFactPreferences
import com.app.exoplanethunter.spacefacts.SpaceFactScheduler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AboutViewModel(
    private val repository: ExoplanetRepository,
    private val syncExoplanetsUseCase: SyncExoplanetsUseCase,
    private val trackEvent: TrackEventUseCase,
    private val appContext: Context
) : ViewModel() {

    private val spaceFactPrefs = SpaceFactPreferences(appContext)

    /** The user's in-app notification toggle (independent of the Remote Config kill switch). */
    private val _notificationsEnabled = MutableStateFlow(spaceFactPrefs.userNotificationsEnabled)
    val notificationsEnabled = _notificationsEnabled.asStateFlow()

    /** Cadence shown under the toggle; driven by Remote Config, applied at launch. */
    val notificationIntervalHours: Long = spaceFactPrefs.intervalHours

    fun setNotificationsEnabled(enabled: Boolean) {
        spaceFactPrefs.userNotificationsEnabled = enabled
        _notificationsEnabled.value = enabled
        // schedule() enqueues or cancels the periodic worker based on the effective state.
        SpaceFactScheduler.schedule(appContext)
    }

    val planetCount = repository.getPlanetCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val systemCount = repository.getStarSystemCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val lastSyncTime = repository.getLastSyncTime()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    private val _syncStatus = MutableStateFlow<SyncStatus>(SyncStatus.Idle)
    val syncStatus = _syncStatus.asStateFlow()

    init {
        trackEvent(AnalyticsEvent.AboutScreenViewed)
        observeSyncStatus()
    }

    private fun observeSyncStatus() {
        viewModelScope.launch {
            repository.getSyncStatus().collect { status ->
                if (_syncStatus.value != status) { // Only track changes
                    when (status) {
                        SyncStatus.Success -> trackEvent(AnalyticsEvent.ManualSyncSuccess)
                        is SyncStatus.Error -> trackEvent(AnalyticsEvent.ManualSyncFailure(status.message))
                        else -> Unit
                    }
                }
                _syncStatus.value = status
            }
        }
    }

    fun syncData() {
        trackEvent(AnalyticsEvent.ManualSyncInitiated)
        viewModelScope.launch {
            syncExoplanetsUseCase() // Just trigger, the observer will pick up progress
        }
    }
}
