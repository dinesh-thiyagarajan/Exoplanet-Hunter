package com.workspace.exoplanethunter.domain.repository

import com.workspace.exoplanethunter.domain.model.Exoplanet
import kotlinx.coroutines.flow.Flow

interface ExoplanetRepository {
    fun getAllPlanets(): Flow<List<Exoplanet>>
    fun searchPlanets(query: String): Flow<List<Exoplanet>>
    fun getPlanetsByDiscoveryMethod(method: String): Flow<List<Exoplanet>>
    fun getMostHabitablePlanets(limit: Int = 20): Flow<List<Exoplanet>>
    suspend fun getPlanetById(id: Long): Exoplanet?
    suspend fun getDiscoveryMethods(): List<String>
    suspend fun loadDataIfNeeded()
}
