package com.app.exoplanethunter.spacefacts

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

/**
 * Posts one space-fact notification per run, advancing through [SpaceFacts] in order so
 * the user sees a fresh fact each time before the list eventually repeats.
 */
class SpaceFactWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val facts = SpaceFacts.all
        if (facts.isEmpty()) return Result.success()

        val prefs = SpaceFactPreferences(applicationContext)
        if (!prefs.notificationsEnabled) return Result.success()

        val nextIndex = (prefs.lastShownIndex + 1).mod(facts.size)
        val fact = facts[nextIndex]

        SpaceFactNotifier.show(applicationContext, fact)
        prefs.lastShownIndex = nextIndex

        return Result.success()
    }
}
