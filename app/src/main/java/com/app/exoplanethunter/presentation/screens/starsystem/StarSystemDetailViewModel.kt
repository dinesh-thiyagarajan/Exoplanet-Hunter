package com.app.exoplanethunter.presentation.screens.starsystem

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
import com.app.exoplanethunter.exoplanet.domain.model.StarSystem
import kotlinx.coroutines.launch

class StarSystemDetailViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as ExoplanetApp
    private val getStarSystemUseCase = app.getStarSystemUseCase

    var starSystem by mutableStateOf<StarSystem?>(null)
        private set

    var isLoading by mutableStateOf(true)
        private set

    fun loadSystem(hostName: String) {
        viewModelScope.launch {
            isLoading = true
            starSystem = getStarSystemUseCase(hostName)
            isLoading = false
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as ExoplanetApp
                StarSystemDetailViewModel(app)
            }
        }
    }
}
