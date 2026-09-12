package com.app.exoplanethunter.exoplanet.data.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.app.exoplanethunter.exoplanet.data.sync.ExoplanetSyncer

/**
 * User-triggered catalog refresh ("Refresh catalog" in Settings).
 *
 * The actual fetch/replace lives in [ExoplanetSyncer] so the background discovery check runs
 * identical logic. This worker only adapts it to WorkManager's progress/result contract.
 */
class DataSyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = try {
        val outcome = ExoplanetSyncer(applicationContext).sync { progress ->
            setProgressAsync(workDataOf(KEY_PROGRESS to progress))
        }
        Result.success(
            workDataOf(
                KEY_NEW_PLANET_COUNT to outcome.newPlanetNames.size,
                KEY_TOTAL_PLANETS to outcome.totalPlanets,
            )
        )
    } catch (e: Exception) {
        Result.failure(workDataOf(KEY_ERROR to (e.message ?: "Unknown error")))
    }

    companion object {
        const val KEY_PROGRESS = "progress"
        const val KEY_ERROR = "error"
        const val KEY_NEW_PLANET_COUNT = "new_planet_count"
        const val KEY_TOTAL_PLANETS = "total_planets"
    }
}
