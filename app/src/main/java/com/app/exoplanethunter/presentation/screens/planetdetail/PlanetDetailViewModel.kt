package com.app.exoplanethunter.presentation.screens.planetdetail

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.app.exoplanethunter.ExoplanetApp
import com.app.exoplanethunter.exoplanet.domain.model.Exoplanet
import com.app.exoplanethunter.exoplanet.domain.model.HabitabilityInsight
import kotlinx.coroutines.launch

class PlanetDetailViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as ExoplanetApp
    private val getPlanetByIdUseCase = app.getPlanetByIdUseCase
    private val getHabitabilityInsightUseCase = app.getHabitabilityInsightUseCase

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

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as ExoplanetApp
                PlanetDetailViewModel(app)
            }
        }
    }
}
