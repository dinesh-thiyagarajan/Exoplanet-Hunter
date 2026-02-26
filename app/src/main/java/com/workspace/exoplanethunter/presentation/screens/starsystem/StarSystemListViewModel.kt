package com.workspace.exoplanethunter.presentation.screens.starsystem

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
import com.workspace.exoplanethunter.ExoplanetApp
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class StarSystemListViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as ExoplanetApp
    private val getAllStarSystemsUseCase = app.getAllStarSystemsUseCase
    private val searchStarSystemsUseCase = app.searchStarSystemsUseCase
    private val getMultiPlanetSystemsUseCase = app.getMultiPlanetSystemsUseCase

    var starSystems by mutableStateOf<List<String>>(emptyList())
        private set

    var isLoading by mutableStateOf(true)
        private set

    var searchQuery by mutableStateOf("")
        private set

    var showMultiPlanetOnly by mutableStateOf(false)
        private set

    private var searchJob: Job? = null

    init {
        loadSystems()
    }

    private fun loadSystems() {
        viewModelScope.launch {
            isLoading = true
            if (showMultiPlanetOnly) {
                getMultiPlanetSystemsUseCase().collectLatest { list ->
                    starSystems = list
                    isLoading = false
                }
            } else {
                getAllStarSystemsUseCase().collectLatest { list ->
                    starSystems = list
                    isLoading = false
                }
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        searchQuery = query
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(300) // debounce
            if (query.isBlank()) {
                loadSystems()
            } else {
                searchStarSystemsUseCase(query).collectLatest { list ->
                    starSystems = list
                    isLoading = false
                }
            }
        }
    }

    fun onToggleMultiPlanet() {
        showMultiPlanetOnly = !showMultiPlanetOnly
        searchQuery = ""
        loadSystems()
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as ExoplanetApp
                StarSystemListViewModel(app)
            }
        }
    }
}
