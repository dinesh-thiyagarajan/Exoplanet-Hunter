package com.app.exoplanethunter.presentation.screens.compare

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.exoplanethunter.analytics.domain.model.AnalyticsEvent
import com.app.exoplanethunter.analytics.domain.usecase.TrackEventUseCase
import com.app.exoplanethunter.exoplanet.domain.model.Exoplanet
import com.app.exoplanethunter.exoplanet.domain.model.HabitabilityInsight
import com.app.exoplanethunter.exoplanet.domain.usecase.GetPlanetByIdUseCase
import com.app.exoplanethunter.ml.GetHabitabilityInsightUseCase
import kotlinx.coroutines.launch

class CompareViewModel(
    private val getPlanetByIdUseCase: GetPlanetByIdUseCase,
    private val getHabitabilityInsightUseCase: GetHabitabilityInsightUseCase,
    private val trackEvent: TrackEventUseCase
) : ViewModel() {

    var planetA by mutableStateOf<Exoplanet?>(null)
        private set

    var planetB by mutableStateOf<Exoplanet?>(null)
        private set

    var insightA by mutableStateOf<HabitabilityInsight?>(null)
        private set

    var insightB by mutableStateOf<HabitabilityInsight?>(null)
        private set

    var isLoading by mutableStateOf(true)
        private set

    fun load(planetAId: Long, planetBId: Long) {
        viewModelScope.launch {
            isLoading = true
            val a = getPlanetByIdUseCase(planetAId)
            val b = getPlanetByIdUseCase(planetBId)
            planetA = a
            planetB = b
            insightA = a?.let { getHabitabilityInsightUseCase(it) }
            insightB = b?.let { getHabitabilityInsightUseCase(it) }
            isLoading = false
            trackEvent(
                AnalyticsEvent.CompareScreenViewed(
                    planetAId = planetAId,
                    planetBId = planetBId
                )
            )
        }
    }
}
