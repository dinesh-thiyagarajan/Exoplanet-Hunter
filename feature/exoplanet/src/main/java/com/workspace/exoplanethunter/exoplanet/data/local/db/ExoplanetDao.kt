package com.workspace.exoplanethunter.exoplanet.data.local.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
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

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(planets: List<ExoplanetEntity>)

    @Query("SELECT COUNT(*) FROM exoplanets")
    suspend fun getCount(): Int

    @Query(
        "SELECT * FROM exoplanets WHERE isDefault = 1 AND equilibriumTempK IS NOT NULL " +
            "AND planetRadiusEarth IS NOT NULL ORDER BY ABS(equilibriumTempK - 288) ASC LIMIT :limit"
    )
    fun getMostHabitablePlanets(limit: Int = 20): Flow<List<ExoplanetEntity>>

    @Query("SELECT DISTINCT hostName FROM exoplanets WHERE isDefault = 1 ORDER BY hostName ASC")
    fun getAllStarSystems(): Flow<List<String>>

    @Query(
        "SELECT * FROM exoplanets WHERE isDefault = 1 AND hostName = :hostName ORDER BY orbitSemiMajorAxisAu ASC"
    )
    suspend fun getPlanetsForSystem(hostName: String): List<ExoplanetEntity>

    @Query(
        "SELECT DISTINCT hostName FROM exoplanets WHERE isDefault = 1 AND " +
            "hostName LIKE '%' || :query || '%' ORDER BY hostName ASC"
    )
    fun searchStarSystems(query: String): Flow<List<String>>

    @Query(
        "SELECT DISTINCT hostName FROM exoplanets WHERE isDefault = 1 GROUP BY hostName " +
            "HAVING COUNT(*) > 1 ORDER BY COUNT(*) DESC"
    )
    fun getMultiPlanetSystems(): Flow<List<String>>

    @Query(
        "SELECT DISTINCT hostName FROM exoplanets WHERE isDefault = 1 AND numStars = :starCount " +
            "ORDER BY hostName ASC"
    )
    fun getStarSystemsByStarCount(starCount: Int): Flow<List<String>>
}
