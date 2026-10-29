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
import com.app.exoplanethunter.exoplanet.domain.usecase.SearchPlanetsUseCase
import com.app.exoplanethunter.ml.GetHabitabilityInsightUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class CompareViewModel(
    private val getPlanetByIdUseCase: GetPlanetByIdUseCase,
    private val getHabitabilityInsightUseCase: GetHabitabilityInsightUseCase,
    private val trackEvent: TrackEventUseCase,
    private val searchPlanets: SearchPlanetsUseCase,
) : ViewModel() {

    /** Picker for "Change": which side is being replaced (0 = left, 1 = right), or null. */
    var changingSlot by mutableStateOf<Int?>(null)
        private set
    var pickerQuery by mutableStateOf("")
        private set
    var pickerResults by mutableStateOf<List<Exoplanet>>(emptyList())
        private set
    private var pickerJob: Job? = null

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

    /** Swaps the two planets left to right. */
    fun swap() {
        val a = planetA; val ia = insightA
        planetA = planetB; insightA = insightB
        planetB = a; insightB = ia
    }

    fun startChange(slot: Int) {
        changingSlot = slot
        onPickerQueryChanged("")
    }

    fun cancelChange() {
        changingSlot = null
        pickerJob?.cancel()
    }

    fun onPickerQueryChanged(query: String) {
        pickerQuery = query
        pickerJob?.cancel()
        if (query.isBlank()) {
            pickerResults = emptyList()
            return
        }
        pickerJob = viewModelScope.launch {
            delay(200)
            pickerResults = searchPlanets(query.trim()).first().take(30)
        }
    }

    /** Puts [planet] in the slot being changed. */
    fun pick(planet: Exoplanet) {
        val slot = changingSlot ?: return
        changingSlot = null
        viewModelScope.launch {
            val insight = getHabitabilityInsightUseCase(planet)
            if (slot == 0) { planetA = planet; insightA = insight } else { planetB = planet; insightB = insight }
            val a = planetA; val b = planetB
            if (a != null && b != null) {
                trackEvent(AnalyticsEvent.PlanetsCompared(a.id, a.planetName, b.id, b.planetName))
            }
        }
    }
}
