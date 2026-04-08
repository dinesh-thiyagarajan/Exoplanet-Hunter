package com.app.exoplanethunter.exoplanet.domain.repository

import com.app.exoplanethunter.exoplanet.domain.model.Exoplanet
import com.app.exoplanethunter.exoplanet.domain.model.StarSystem
import kotlinx.coroutines.flow.Flow

interface ExoplanetRepository {
    fun getAllPlanets(): Flow<List<Exoplanet>>
    fun searchPlanets(query: String): Flow<List<Exoplanet>>
    fun getPlanetsByDiscoveryMethod(method: String): Flow<List<Exoplanet>>
    fun getMostHabitablePlanets(limit: Int = 20): Flow<List<Exoplanet>>
    suspend fun getPlanetById(id: Long): Exoplanet?
    suspend fun getDiscoveryMethods(): List<String>
    fun getAllStarSystems(): Flow<List<String>>
    suspend fun getStarSystem(hostName: String): StarSystem?
    fun searchStarSystems(query: String): Flow<List<String>>
    fun getMultiPlanetSystems(): Flow<List<String>>
    fun getStarSystemsByStarCount(starCount: Int): Flow<List<String>>
    suspend fun loadDataIfNeeded()
}
