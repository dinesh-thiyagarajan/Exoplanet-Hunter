package com.app.exoplanethunter.spacefacts

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

/** Schedules (and reschedules) the periodic [SpaceFactWorker]. */
object SpaceFactScheduler {

    private const val WORK_NAME = "space_fact_notification"

    /**
     * Ensure the periodic worker is scheduled at the user's configured interval. Safe to call
     * on every app start: [ExistingPeriodicWorkPolicy.UPDATE] keeps the existing schedule but
     * picks up any interval change without resetting the timer unnecessarily.
     */
    fun schedule(context: Context) {
        val prefs = SpaceFactPreferences(context)
        if (!prefs.notificationsEnabled) {
            cancel(context)
            return
        }

        val request = PeriodicWorkRequestBuilder<SpaceFactWorker>(
            prefs.intervalHours, TimeUnit.HOURS
        ).build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            request
        )
    }

    /** Reschedule from scratch — use after the interval changes. */
    fun reschedule(context: Context) {
        val prefs = SpaceFactPreferences(context)
        if (!prefs.notificationsEnabled) {
            cancel(context)
            return
        }
        val request = PeriodicWorkRequestBuilder<SpaceFactWorker>(
            prefs.intervalHours, TimeUnit.HOURS
        ).build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WORK_NAME,
            ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE,
            request
        )
    }

    fun cancel(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
    }

    /**
     * Fire one fact notification immediately via a one-time worker. Intended for debug/testing
     * so the periodic schedule doesn't have to be waited out.
     */
    fun triggerNow(context: Context) {
        val request = OneTimeWorkRequestBuilder<SpaceFactWorker>().build()
        WorkManager.getInstance(context).enqueue(request)
    }
}
