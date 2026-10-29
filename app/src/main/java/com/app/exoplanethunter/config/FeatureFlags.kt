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

    /** In-app review prompt: whether it's allowed. */
    @Volatile
    var reviewEnabled: Boolean = true


    /** Days of use before the first review request, and between requests after that. */
    @Volatile
    var reviewIntervalDays: Int = 60

    /** In-app update prompt: whether it's allowed at all. */
    @Volatile
    var updateEnabled: Boolean = true

    /** Installed versions below this get the full-screen, must-update flow (0 = never). */
    @Volatile
    var updateMinVersionCode: Long = 0L
}
