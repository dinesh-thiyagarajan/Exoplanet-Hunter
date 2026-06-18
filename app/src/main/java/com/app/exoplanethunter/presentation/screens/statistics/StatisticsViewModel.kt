package com.app.exoplanethunter.presentation.screens.statistics

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.exoplanethunter.analytics.domain.model.AnalyticsEvent
import com.app.exoplanethunter.analytics.domain.usecase.TrackEventUseCase
import com.app.exoplanethunter.exoplanet.domain.model.Statistics
import com.app.exoplanethunter.exoplanet.domain.usecase.GetStatisticsUseCase
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class StatisticsViewModel(
    private val getStatisticsUseCase: GetStatisticsUseCase,
    private val trackEvent: TrackEventUseCase
) : ViewModel() {

    var statistics by mutableStateOf<Statistics?>(null)
        private set

    var isLoading by mutableStateOf(true)
        private set

    init {
        trackEvent(AnalyticsEvent.StatisticsScreenViewed)
        loadStatistics()
    }

    private fun loadStatistics() {
        viewModelScope.launch {
            getStatisticsUseCase().collectLatest {
                statistics = it
                isLoading = false
            }
        }
    }
}
