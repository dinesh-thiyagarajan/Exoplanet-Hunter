package com.app.exoplanethunter.presentation.screens.splash

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.exoplanethunter.exoplanet.domain.usecase.LoadDataUseCase
import kotlinx.coroutines.launch

class SplashViewModel(
    private val loadDataUseCase: LoadDataUseCase
) : ViewModel() {

    var isLoaded by mutableStateOf(false)
        private set

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            try {
                loadDataUseCase()
                isLoaded = true
            } catch (e: Exception) {
                e.printStackTrace()
                isLoaded = true
            }
        }
    }
}
