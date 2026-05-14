package com.app.exoplanethunter.exoplanet.domain.repository

import com.app.exoplanethunter.exoplanet.domain.model.Exoplanet
import com.app.exoplanethunter.exoplanet.domain.model.StarSystem
import com.app.exoplanethunter.exoplanet.domain.model.StarSystemSummary
import kotlinx.coroutines.flow.Flow

sealed class SyncStatus {
    object Idle : SyncStatus()
    data class Progress(val percentage: Int) : SyncStatus()
    object Success : SyncStatus()
    data class Error(val message: String) : SyncStatus()
}

interface ExoplanetRepository {
    fun getAllPlanets(): Flow<List<Exoplanet>>
    fun getPlanetCount(): Flow<Int>
    fun getStarSystemCount(): Flow<Int>
    fun getLastSyncTime(): Flow<Long>
    fun searchPlanets(query: String): Flow<List<Exoplanet>>
    fun getPlanetsByDiscoveryMethod(method: String): Flow<List<Exoplanet>>
    fun getMostHabitablePlanets(limit: Int = 20): Flow<List<Exoplanet>>
    suspend fun getPlanetById(id: Long): Exoplanet?
    suspend fun getDiscoveryMethods(): List<String>
    
    fun getAllStarSystems(): Flow<List<StarSystemSummary>>
    suspend fun getStarSystem(systemId: Long): StarSystem?
    fun searchStarSystems(query: String): Flow<List<StarSystemSummary>>
    fun getMultiPlanetSystems(): Flow<List<StarSystemSummary>>
    fun getStarSystemsByStarCount(starCount: Int): Flow<List<StarSystemSummary>>

    suspend fun syncExoplanets(): Flow<SyncStatus>
}
