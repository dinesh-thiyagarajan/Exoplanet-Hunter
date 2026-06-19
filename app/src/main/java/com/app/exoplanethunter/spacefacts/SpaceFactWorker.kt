package com.app.exoplanethunter.spacefacts

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

/**
 * Posts one space-fact notification per run, advancing through the fact list in order so
 * the user sees a fresh fact each time before the list eventually repeats.
 */
class SpaceFactWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val facts = SpaceFactProvider(applicationContext).all()
        if (facts.isEmpty()) return Result.success()

        val prefs = SpaceFactPreferences(applicationContext)
        if (!prefs.notificationsEnabled) return Result.success()

        var unshown = facts.filterNot { it.id in prefs.shownFactIds }

        // Every fact has been shown — start a new cycle, but avoid immediately repeating the
        // most recently shown fact across the boundary.
        if (unshown.isEmpty()) {
            prefs.resetShown()
            unshown = facts.filterNot { it.id == prefs.lastShownId }
            if (unshown.isEmpty()) unshown = facts // safety net if only one fact exists
        }

        val fact = unshown.random()
        SpaceFactNotifier.show(applicationContext, fact)
        prefs.markShown(fact.id)

        return Result.success()
    }
}
