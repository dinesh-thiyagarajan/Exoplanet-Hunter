package com.app.exoplanethunter.presentation.screens.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.exoplanethunter.analytics.domain.model.AnalyticsEvent
import com.app.exoplanethunter.analytics.domain.usecase.TrackEventUseCase
import com.app.exoplanethunter.exoplanet.data.local.SyncPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class SplashViewModel(
    private val trackEvent: TrackEventUseCase,
    private val syncPreferences: SyncPreferences
) : ViewModel() {

    private val _loadingState = MutableStateFlow(false)
    val loadingState = _loadingState.asStateFlow()

    init {
        trackEvent(AnalyticsEvent.AboutScreenViewed)

        viewModelScope.launch {
            val isInitialAssetCopied = syncPreferences.isInitialAssetCopied.first()
            if (!isInitialAssetCopied) {
                syncPreferences.setInitialAssetCopied(true)
            }
            _loadingState.value = true
        }
    }
}
