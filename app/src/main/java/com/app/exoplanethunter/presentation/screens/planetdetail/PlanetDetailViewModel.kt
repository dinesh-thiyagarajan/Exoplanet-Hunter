package com.app.exoplanethunter.presentation.screens.planetdetail

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.exoplanethunter.exoplanet.domain.model.Exoplanet
import com.app.exoplanethunter.exoplanet.domain.model.HabitabilityInsight
import com.app.exoplanethunter.exoplanet.domain.usecase.GetPlanetByIdUseCase
import com.app.exoplanethunter.ml.GetHabitabilityInsightUseCase
import kotlinx.coroutines.launch

class PlanetDetailViewModel(
    private val getPlanetByIdUseCase: GetPlanetByIdUseCase,
    private val getHabitabilityInsightUseCase: GetHabitabilityInsightUseCase
) : ViewModel() {

    var planet by mutableStateOf<Exoplanet?>(null)
        private set

    var insight by mutableStateOf<HabitabilityInsight?>(null)
        private set

    var isLoading by mutableStateOf(true)
        private set

    fun loadPlanet(id: Long) {
        viewModelScope.launch {
            isLoading = true
            val loadedPlanet = getPlanetByIdUseCase(id)
            planet = loadedPlanet
            loadedPlanet?.let {
                insight = getHabitabilityInsightUseCase(it)
            }
            isLoading = false
        }
    }
}
