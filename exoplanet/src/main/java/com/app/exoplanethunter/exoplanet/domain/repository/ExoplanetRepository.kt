package com.app.exoplanethunter.exoplanet.domain.repository

import com.app.exoplanethunter.exoplanet.domain.model.Exoplanet
import com.app.exoplanethunter.exoplanet.domain.model.LabelCount
import com.app.exoplanethunter.exoplanet.domain.model.StarPosition
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
    fun getPlanetsSortedByLatest(): Flow<List<Exoplanet>>
    fun getPlanetsByMinDiscoveryYear(minYear: Int): Flow<List<Exoplanet>>
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
    fun getNearbyStarPositions(limit: Int): Flow<List<StarPosition>>

    fun getSyncStatus(): Flow<SyncStatus>
    suspend fun syncExoplanets(): Flow<SyncStatus>

    // Statistics aggregates
    fun getDiscoveryMethodCounts(): Flow<List<LabelCount>>
    fun getDiscoveryYearCounts(): Flow<List<LabelCount>>
    fun getSizeDistribution(): Flow<List<LabelCount>>

    // Favorites — keyed by planetName so they survive catalog syncs (which reassign ids)
    fun getFavoriteNames(): Flow<Set<String>>
    fun getFavoritePlanets(): Flow<List<Exoplanet>>
    suspend fun toggleFavorite(planetName: String)
}
