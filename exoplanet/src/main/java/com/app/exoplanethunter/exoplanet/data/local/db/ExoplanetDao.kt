package com.app.exoplanethunter.exoplanet.data.local.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.app.exoplanethunter.exoplanet.domain.model.StarSystemSummary
import kotlinx.coroutines.flow.Flow

@Dao
interface ExoplanetDao {

    @Query("SELECT * FROM exoplanets WHERE isDefault = 1 ORDER BY planetName ASC")
    fun getAllPlanets(): Flow<List<ExoplanetEntity>>

    @Query("SELECT * FROM exoplanets WHERE id = :id")
    suspend fun getPlanetById(id: Long): ExoplanetEntity?

    @Query(
        "SELECT * FROM exoplanets WHERE isDefault = 1 AND " +
            "(planetName LIKE '%' || :query || '%' OR hostName LIKE '%' || :query || '%') " +
            "ORDER BY planetName ASC"
    )
    fun searchPlanets(query: String): Flow<List<ExoplanetEntity>>

    @Query(
        "SELECT * FROM exoplanets WHERE isDefault = 1 AND discoveryMethod = :method " +
            "ORDER BY planetName ASC"
    )
    fun getPlanetsByDiscoveryMethod(method: String): Flow<List<ExoplanetEntity>>

    @Query("SELECT DISTINCT discoveryMethod FROM exoplanets WHERE isDefault = 1 ORDER BY discoveryMethod")
    suspend fun getDiscoveryMethods(): List<String>

    @Query(
        "SELECT * FROM exoplanets WHERE isDefault = 1 AND equilibriumTempK IS NOT NULL " +
            "AND planetRadiusEarth IS NOT NULL ORDER BY ABS(equilibriumTempK - 288) ASC LIMIT :limit"
    )
    fun getMostHabitablePlanets(limit: Int = 20): Flow<List<ExoplanetEntity>>

    @Query("SELECT id, hostName FROM star_systems ORDER BY hostName ASC")
    fun getAllStarSystems(): Flow<List<StarSystemSummary>>

    @Query(
        "SELECT * FROM exoplanets WHERE systemId = :systemId AND isDefault = 1 ORDER BY orbitSemiMajorAxisAu ASC"
    )
    suspend fun getPlanetsForSystem(systemId: Long): List<ExoplanetEntity>

    @Query(
        "SELECT id, hostName FROM star_systems WHERE hostName LIKE '%' || :query || '%' ORDER BY hostName ASC"
    )
    fun searchStarSystems(query: String): Flow<List<StarSystemSummary>>

    @Query(
        "SELECT ss.id, ss.hostName FROM star_systems ss " +
            "INNER JOIN exoplanets e ON e.systemId = ss.id AND e.isDefault = 1 " +
            "GROUP BY ss.id HAVING COUNT(*) > 1 ORDER BY COUNT(*) DESC"
    )
    fun getMultiPlanetSystems(): Flow<List<StarSystemSummary>>

    @Query(
        "SELECT DISTINCT ss.id, ss.hostName FROM star_systems ss " +
            "INNER JOIN exoplanets e ON e.systemId = ss.id AND e.isDefault = 1 AND e.numStars = :starCount " +
            "ORDER BY ss.hostName ASC"
    )
    fun getStarSystemsByStarCount(starCount: Int): Flow<List<StarSystemSummary>>
}
