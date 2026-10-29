package com.app.exoplanethunter.presentation.screens.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.exoplanethunter.exoplanet.data.local.SyncPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class SplashViewModel(
    private val syncPreferences: SyncPreferences
) : ViewModel() {

    private val _loadingState = MutableStateFlow(false)
    val loadingState = _loadingState.asStateFlow()

    /** Null until known; true on the very first launch (the longer splash with a fact card). */
    private val _isFirstLaunch = MutableStateFlow<Boolean?>(null)
    val isFirstLaunch = _isFirstLaunch.asStateFlow()

    init {
        viewModelScope.launch {
            val isInitialAssetCopied = syncPreferences.isInitialAssetCopied.first()
            _isFirstLaunch.value = !isInitialAssetCopied
            if (!isInitialAssetCopied) {
                syncPreferences.setInitialAssetCopied(true)
            }
            _loadingState.value = true
        }
    }
}
