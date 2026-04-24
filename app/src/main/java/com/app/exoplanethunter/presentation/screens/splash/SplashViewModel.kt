package com.app.exoplanethunter.presentation.screens.splash

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class SplashViewModel : ViewModel() {

    var isLoaded by mutableStateOf(false)
        private set

    init {
        isLoaded = true
    }
}
