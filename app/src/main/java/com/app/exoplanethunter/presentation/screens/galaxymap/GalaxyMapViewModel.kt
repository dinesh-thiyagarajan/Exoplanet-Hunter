package com.app.exoplanethunter.presentation.screens.galaxymap

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.exoplanethunter.exoplanet.domain.model.StarPosition
import com.app.exoplanethunter.exoplanet.domain.usecase.GetNearbyStarPositionsUseCase
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class GalaxyMapViewModel(
    private val getNearbyStarPositions: GetNearbyStarPositionsUseCase
) : ViewModel() {

    var stars by mutableStateOf<List<StarPosition>>(emptyList())
        private set

    var isLoading by mutableStateOf(true)
        private set

    init {
        viewModelScope.launch {
            getNearbyStarPositions().collectLatest { list ->
                stars = list
                isLoading = false
            }
        }
    }
}
