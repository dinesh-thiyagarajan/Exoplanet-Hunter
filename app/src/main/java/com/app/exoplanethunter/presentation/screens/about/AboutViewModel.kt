package com.app.exoplanethunter.presentation.screens.about

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.exoplanethunter.analytics.domain.model.AnalyticsEvent
import com.app.exoplanethunter.analytics.domain.usecase.TrackEventUseCase
import com.app.exoplanethunter.exoplanet.domain.repository.ExoplanetRepository
import com.app.exoplanethunter.exoplanet.domain.repository.SyncStatus
import com.app.exoplanethunter.exoplanet.domain.usecase.SyncExoplanetsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AboutViewModel(
    private val repository: ExoplanetRepository,
    private val syncExoplanetsUseCase: SyncExoplanetsUseCase,
    private val trackEvent: TrackEventUseCase
) : ViewModel() {

    val planetCount = repository.getPlanetCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val systemCount = repository.getStarSystemCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val lastSyncTime = repository.getLastSyncTime()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    init {
        trackEvent(AnalyticsEvent.AboutScreenViewed)
    }

    private val _syncStatus = MutableStateFlow<SyncStatus>(SyncStatus.Idle)
    val syncStatus = _syncStatus.asStateFlow()

    fun syncData() {
        trackEvent(AnalyticsEvent.ManualSyncInitiated)
        viewModelScope.launch {
            syncExoplanetsUseCase().collect {
                _syncStatus.value = it
            }
        }
    }
}
