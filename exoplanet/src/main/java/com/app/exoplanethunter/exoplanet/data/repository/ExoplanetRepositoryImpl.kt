package com.app.exoplanethunter.exoplanet.data.repository

import android.content.Context
import android.util.Log
import androidx.work.*
import com.app.exoplanethunter.exoplanet.data.local.SyncPreferences
import com.app.exoplanethunter.exoplanet.data.local.db.ExoplanetDao
import com.app.exoplanethunter.exoplanet.data.local.db.ExoplanetEntity
import com.app.exoplanethunter.exoplanet.data.worker.DataSyncWorker
import com.app.exoplanethunter.exoplanet.domain.model.Exoplanet
import com.app.exoplanethunter.exoplanet.domain.model.StarSystem
import com.app.exoplanethunter.exoplanet.domain.model.StarSystemSummary
import com.app.exoplanethunter.exoplanet.domain.repository.ExoplanetRepository
import com.app.exoplanethunter.exoplanet.domain.repository.SyncStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import java.util.UUID

class ExoplanetRepositoryImpl(
    private val context: Context,
    private val dao: ExoplanetDao,
    private val syncPreferences: SyncPreferences
) : ExoplanetRepository {

    private val workManager = WorkManager.getInstance(context)

    override fun getAllPlanets(): Flow<List<Exoplanet>> {
        return dao.getAllPlanets().map { entities -> entities.map { it.toDomain() } }
    }

    override fun getPlanetsSortedByLatest(): Flow<List<Exoplanet>> {
        return dao.getPlanetsSortedByLatest().map { entities -> entities.map { it.toDomain() } }
    }

    override fun getPlanetsByMinDiscoveryYear(minYear: Int): Flow<List<Exoplanet>> {
        return dao.getPlanetsByMinDiscoveryYear(minYear).map { entities -> entities.map { it.toDomain() } }
    }

    override fun getPlanetCount(): Flow<Int> = dao.getPlanetCount().onEach {
        Log.d("DTD", "Planet count: $it")
    }

    override fun getStarSystemCount(): Flow<Int> = dao.getStarSystemCount()

    override fun getLastSyncTime(): Flow<Long> = syncPreferences.lastSyncTime

    override fun searchPlanets(query: String): Flow<List<Exoplanet>> {
        return dao.searchPlanets(query).map { entities -> entities.map { it.toDomain() } }
    }

    override fun getPlanetsByDiscoveryMethod(method: String): Flow<List<Exoplanet>> {
        return dao.getPlanetsByDiscoveryMethod(method).map { entities -> entities.map { it.toDomain() } }
    }

    override fun getMostHabitablePlanets(limit: Int): Flow<List<Exoplanet>> {
        return dao.getMostHabitablePlanets(limit).map { entities -> entities.map { it.toDomain() } }
    }

    override suspend fun getPlanetById(id: Long): Exoplanet? {
        return dao.getPlanetById(id)?.toDomain()
    }

    override suspend fun getDiscoveryMethods(): List<String> {
        return dao.getDiscoveryMethods()
    }

    override fun getAllStarSystems(): Flow<List<StarSystemSummary>> = dao.getAllStarSystems()

    override suspend fun getStarSystem(systemId: Long): StarSystem? {
        val entities = dao.getPlanetsForSystem(systemId)
        if (entities.isEmpty()) return null
        val first = entities.first()
        return StarSystem(
            id = systemId,
            hostName = first.hostName,
            numStars = first.numStars,
            numPlanets = first.numPlanets,
            stellarEffectiveTempK = first.stellarEffectiveTempK,
            stellarRadiusSolar = first.stellarRadiusSolar,
            stellarMassSolar = first.stellarMassSolar,
            stellarMetallicity = first.stellarMetallicity,
            stellarSurfaceGravity = first.stellarSurfaceGravity,
            spectralType = first.spectralType,
            distanceParsec = first.distanceParsec,
            ra = first.ra,
            dec = first.dec,
            planets = entities.map { it.toDomain() }
        )
    }

    override fun searchStarSystems(query: String): Flow<List<StarSystemSummary>> = dao.searchStarSystems(query)

    override fun getMultiPlanetSystems(): Flow<List<StarSystemSummary>> = dao.getMultiPlanetSystems()

    override fun getStarSystemsByStarCount(starCount: Int): Flow<List<StarSystemSummary>> =
        dao.getStarSystemsByStarCount(starCount)

    override suspend fun syncExoplanets(): Flow<SyncStatus> {
        val syncRequest = OneTimeWorkRequestBuilder<DataSyncWorker>()
            .addTag("data_sync")
            .build()
        
        workManager.enqueueUniqueWork(
            "exoplanet_sync",
            ExistingWorkPolicy.REPLACE,
            syncRequest
        )

        return workManager.getWorkInfoByIdFlow(syncRequest.id).map { workInfo ->
            when (workInfo?.state) {
                WorkInfo.State.RUNNING -> {
                    val progress = workInfo.progress.getInt("progress", 0)
                    SyncStatus.Progress(progress)
                }
                WorkInfo.State.SUCCEEDED -> SyncStatus.Success
                WorkInfo.State.FAILED -> {
                    val error = workInfo.outputData.getString("error") ?: "Sync failed"
                    SyncStatus.Error(error)
                }
                else -> SyncStatus.Idle
            }
        }
    }

    private fun ExoplanetEntity.toDomain(): Exoplanet {
        return Exoplanet(
            id = id, planetName = planetName, hostName = hostName,
            numStars = numStars, numPlanets = numPlanets,
            discoveryMethod = discoveryMethod, discoveryYear = discoveryYear,
            discoveryFacility = discoveryFacility,
            orbitalPeriodDays = orbitalPeriodDays,
            orbitSemiMajorAxisAu = orbitSemiMajorAxisAu,
            planetRadiusEarth = planetRadiusEarth,
            planetRadiusJupiter = planetRadiusJupiter,
            planetMassEarth = planetMassEarth,
            planetMassJupiter = planetMassJupiter,
            eccentricity = eccentricity,
            insolationFlux = insolationFlux,
            equilibriumTempK = equilibriumTempK,
            stellarEffectiveTempK = stellarEffectiveTempK,
            stellarRadiusSolar = stellarRadiusSolar,
            stellarMassSolar = stellarMassSolar,
            stellarMetallicity = stellarMetallicity,
            stellarSurfaceGravity = stellarSurfaceGravity,
            spectralType = spectralType,
            distanceParsec = distanceParsec,
            ra = ra, dec = dec,
        )
    }
}
