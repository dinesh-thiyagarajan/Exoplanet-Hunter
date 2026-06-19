package com.app.exoplanethunter.config

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * In-memory, observable feature flags driven by Remote Config (see [RemoteConfigManager]).
 *
 * UI collects these as state so a remote toggle takes effect as soon as config is fetched,
 * without a restart. Defaults are permissive (feature on) until a server value arrives.
 */
object FeatureFlags {

    private val _compareEnabled = MutableStateFlow(true)
    val compareEnabled: StateFlow<Boolean> = _compareEnabled.asStateFlow()

    fun setCompareEnabled(enabled: Boolean) {
        _compareEnabled.value = enabled
    }
}
