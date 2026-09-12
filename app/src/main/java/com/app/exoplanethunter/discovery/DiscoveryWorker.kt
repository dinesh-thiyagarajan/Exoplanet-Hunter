package com.app.exoplanethunter.discovery

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.app.exoplanethunter.exoplanet.data.sync.ExoplanetSyncer

/**
 * Periodically refreshes the catalog from the NASA Exoplanet Archive in the background and, when
 * the refresh brings in planets the local catalog didn't have, posts a "new worlds" notification.
 *
 * This is what turns the existing manual "Refresh catalog" plumbing into a genuine reason to
 * reopen the app: previously the catalog only ever updated when a user went looking for the
 * button, so newly confirmed planets were invisible to almost everyone.
 */
class DiscoveryWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val prefs = DiscoveryPreferences(applicationContext)
        if (!prefs.discoveryEnabled) return Result.success()

        return try {
            val outcome = ExoplanetSyncer(applicationContext).sync()

            // On a first sync every planet looks "new" — that's a seeded catalog, not a discovery.
            if (!outcome.wasFirstSync && outcome.newPlanetNames.isNotEmpty()) {
                DiscoveryNotifier.show(applicationContext, outcome.newPlanetNames)
            }
            Result.success()
        } catch (e: Exception) {
            // Transient network/archive problems are expected; let WorkManager back off and retry.
            Result.retry()
        }
    }
}
