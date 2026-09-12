package com.app.exoplanethunter.discovery

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

/** Schedules (and reschedules) the periodic [DiscoveryWorker]. */
object DiscoveryScheduler {

    private const val WORK_NAME = "new_discovery_check"

    /**
     * Ensure the periodic check is scheduled. Safe to call on every app start:
     * [ExistingPeriodicWorkPolicy.UPDATE] keeps an existing schedule running while picking up
     * any interval change.
     */
    fun schedule(context: Context) {
        val prefs = DiscoveryPreferences(context)
        if (!prefs.discoveryEnabled) {
            cancel(context)
            return
        }

        // The check is a network fetch of the whole catalog — only worth doing while connected.
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val request = PeriodicWorkRequestBuilder<DiscoveryWorker>(
            prefs.intervalHours, TimeUnit.HOURS
        )
            .setConstraints(constraints)
            .setInitialDelay(DiscoveryPreferences.FIRST_RUN_DELAY_HOURS, TimeUnit.HOURS)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            request
        )
    }

    fun cancel(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
    }

    /** Run one check immediately — for debug/testing so the schedule needn't be waited out. */
    fun triggerNow(context: Context) {
        WorkManager.getInstance(context).enqueue(
            OneTimeWorkRequestBuilder<DiscoveryWorker>().build()
        )
    }
}
