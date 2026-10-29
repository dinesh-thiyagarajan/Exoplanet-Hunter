package com.app.exoplanethunter.update

import android.app.Activity
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.app.exoplanethunter.BuildConfig
import com.app.exoplanethunter.analytics.domain.model.AnalyticsEvent
import com.app.exoplanethunter.analytics.domain.usecase.TrackEventUseCase
import com.app.exoplanethunter.config.FeatureFlags
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.ActivityResult
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability

/**
 * Google Play in-app updates: tells people a new version is out and installs it without
 * leaving the app. See [UpdatePolicy] for when each kind of prompt is used.
 *
 * Create it as a property of the activity (it registers an activity-result launcher), then
 * call [checkForUpdate] once per launch. Only works for installs from Google Play; anywhere
 * else (debug builds, sideloads) Play reports an error and nothing is shown.
 */
class AppUpdateController(
    private val activity: ComponentActivity,
    private val trackEvent: TrackEventUseCase,
) {
    private val manager = AppUpdateManagerFactory.create(activity)
    private val prefs = UpdatePreferences(activity)
    private var activeMode: UpdateMode? = null

    /** A flexible update finished downloading; show the "Restart to update" bar. */
    var updateReady by mutableStateOf(false)
        private set

    private val launcher = activity.registerForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        val mode = activeMode ?: return@registerForActivityResult
        val outcome = when (result.resultCode) {
            Activity.RESULT_OK -> "accepted"
            Activity.RESULT_CANCELED -> "declined".also { prefs.lastDeclinedMillis = System.currentTimeMillis() }
            ActivityResult.RESULT_IN_APP_UPDATE_FAILED -> "failed"
            else -> "failed"
        }
        trackEvent(AnalyticsEvent.AppUpdateResult(mode.analyticsName, outcome))
    }

    private val installListener = InstallStateUpdatedListener { state ->
        if (state.installStatus() == InstallStatus.DOWNLOADED) updateReady = true
    }

    init {
        manager.registerListener(installListener)
        activity.lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onResume(owner: LifecycleOwner) = onResumed()
            override fun onDestroy(owner: LifecycleOwner) = manager.unregisterListener(installListener)
        })
    }

    /**
     * Asks Play whether a newer version is out and, if [UpdatePolicy] says so, shows the
     * prompt. [onChecked] gets whether a prompt was shown, so other prompts (the review
     * request) can stay out of its way.
     */
    fun checkForUpdate(onChecked: (prompted: Boolean) -> Unit = {}) {
        if (!FeatureFlags.updateEnabled) return onChecked(false)
        manager.appUpdateInfo
            .addOnSuccessListener { info -> onChecked(maybePrompt(info)) }
            .addOnFailureListener { e ->
                // Expected outside Play installs (debug builds, sideloads).
                Log.d(TAG, "update check unavailable: ${e.message}")
                onChecked(false)
            }
    }

    /** Installs a downloaded flexible update; the app restarts into the new version. */
    fun completeUpdate() {
        trackEvent(AnalyticsEvent.AppUpdateResult(UpdateMode.FLEXIBLE.analyticsName, "installed"))
        manager.completeUpdate()
    }

    /** Hides the bar for this session; Play installs the update the next time the app is closed. */
    fun dismissReady() {
        updateReady = false
    }

    private fun maybePrompt(info: AppUpdateInfo): Boolean {
        if (info.installStatus() == InstallStatus.DOWNLOADED) {
            updateReady = true
            return false
        }
        if (info.updateAvailability() != UpdateAvailability.UPDATE_AVAILABLE) return false

        val mode = UpdatePolicy.choose(
            installedVersionCode = BuildConfig.VERSION_CODE.toLong(),
            minVersionCode = FeatureFlags.updateMinVersionCode,
            updatePriority = info.updatePriority(),
            immediateAllowed = info.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE),
            flexibleAllowed = info.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE),
            lastDeclinedMillis = prefs.lastDeclinedMillis,
            nowMillis = System.currentTimeMillis(),
        ) ?: return false

        return launch(info, mode).also { launched ->
            if (launched) trackEvent(AnalyticsEvent.AppUpdatePrompted(mode.analyticsName, info.availableVersionCode()))
        }
    }

    private fun launch(info: AppUpdateInfo, mode: UpdateMode): Boolean {
        activeMode = mode
        val type = if (mode == UpdateMode.IMMEDIATE) AppUpdateType.IMMEDIATE else AppUpdateType.FLEXIBLE
        return try {
            manager.startUpdateFlowForResult(info, launcher, AppUpdateOptions.newBuilder(type).build())
        } catch (e: Exception) {
            Log.w(TAG, "could not start update flow: ${e.message}")
            false
        }
    }

    private fun onResumed() {
        manager.appUpdateInfo.addOnSuccessListener { info ->
            when {
                // Downloaded while the app was in the background.
                info.installStatus() == InstallStatus.DOWNLOADED -> updateReady = true
                // An immediate update was interrupted (e.g. the user switched apps): carry on.
                info.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS ->
                    launch(info, UpdateMode.IMMEDIATE)
                // Too old to keep using: once asked, ask again each time the app comes back.
                activeMode != null &&
                    info.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE &&
                    BuildConfig.VERSION_CODE < FeatureFlags.updateMinVersionCode &&
                    info.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE) ->
                    launch(info, UpdateMode.IMMEDIATE)
            }
        }
    }

    private companion object {
        const val TAG = "AppUpdate"
    }
}
