package com.app.exoplanethunter.exoplanet.data.local.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.app.exoplanethunter.exoplanet.domain.model.LabelCount
import com.app.exoplanethunter.exoplanet.domain.model.StarSystemSummary
import kotlinx.coroutines.flow.Flow

@Dao
interface ExoplanetDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlanets(planets: List<ExoplanetEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStarSystems(systems: List<StarSystemEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStarSystemsAndGetIds(systems: List<StarSystemEntity>): List<Long>

    @Query("DELETE FROM exoplanets")
    suspend fun deleteAllPlanets()

    @Query("DELETE FROM star_systems")
    suspend fun deleteAllStarSystems()

    @Query("SELECT COUNT(*) FROM exoplanets WHERE isDefault = 1")
    fun getPlanetCount(): Flow<Int>

    @Query("SELECT COUNT(DISTINCT hostName) FROM exoplanets WHERE isDefault = 1")
    fun getStarSystemCount(): Flow<Int>

    @Query("SELECT * FROM exoplanets WHERE isDefault = 1 ORDER BY planetName ASC")
    fun getAllPlanets(): Flow<List<ExoplanetEntity>>

    @Query("SELECT * FROM exoplanets WHERE isDefault = 1 ORDER BY discoveryYear DESC, planetName ASC")
    fun getPlanetsSortedByLatest(): Flow<List<ExoplanetEntity>>

    @Query("SELECT * FROM exoplanets WHERE isDefault = 1 AND discoveryYear >= :minYear ORDER BY discoveryYear DESC, planetName ASC")
    fun getPlanetsByMinDiscoveryYear(minYear: Int): Flow<List<ExoplanetEntity>>

    @Query("SELECT * FROM exoplanets WHERE id = :id")
    suspend fun getPlanetById(id: Long): ExoplanetEntity?

    @Query("SELECT * FROM exoplanets WHERE isDefault = 1 AND planetName IN (:names) ORDER BY planetName ASC")
    fun getPlanetsByNames(names: Set<String>): Flow<List<ExoplanetEntity>>

    @Query(
        """
        SELECT * FROM exoplanets 
        WHERE isDefault = 1 AND (planetName LIKE '%' || :query || '%' OR hostName LIKE '%' || :query || '%') 
        ORDER BY planetName ASC
        """
    )
    fun searchPlanets(query: String): Flow<List<ExoplanetEntity>>

    @Query("SELECT * FROM exoplanets WHERE isDefault = 1 AND discoveryMethod = :method ORDER BY planetName ASC")
    fun getPlanetsByDiscoveryMethod(method: String): Flow<List<ExoplanetEntity>>

    @Query("SELECT DISTINCT discoveryMethod FROM exoplanets WHERE isDefault = 1 ORDER BY discoveryMethod")
    suspend fun getDiscoveryMethods(): List<String>

    // ── Statistics aggregates ────────────────────────────────────────────────

    @Query(
        """
        SELECT discoveryMethod AS label, COUNT(*) AS count FROM exoplanets
        WHERE isDefault = 1 GROUP BY discoveryMethod ORDER BY count DESC
        """
    )
    fun getDiscoveryMethodCounts(): Flow<List<LabelCount>>

    @Query(
        """
        SELECT CAST(discoveryYear AS TEXT) AS label, COUNT(*) AS count FROM exoplanets
        WHERE isDefault = 1 GROUP BY discoveryYear ORDER BY discoveryYear ASC
        """
    )
    fun getDiscoveryYearCounts(): Flow<List<LabelCount>>

    @Query(
        """
        SELECT
          CASE
            WHEN planetRadiusEarth < 1.25 THEN 'Earth-size'
            WHEN planetRadiusEarth < 2.0 THEN 'Super-Earth'
            WHEN planetRadiusEarth < 6.0 THEN 'Neptune-like'
            ELSE 'Jupiter-like'
          END AS label,
          COUNT(*) AS count
        FROM exoplanets
        WHERE isDefault = 1 AND planetRadiusEarth IS NOT NULL
        GROUP BY label
        """
    )
    fun getSizeDistribution(): Flow<List<LabelCount>>

    @Query(
        """
        SELECT * FROM exoplanets 
        WHERE isDefault = 1 AND equilibriumTempK IS NOT NULL AND planetRadiusEarth IS NOT NULL 
        ORDER BY ABS(equilibriumTempK - 288) ASC LIMIT :limit
        """
    )
    fun getMostHabitablePlanets(limit: Int = 20): Flow<List<ExoplanetEntity>>

    @Query(
        """
        SELECT ss.id AS id, ss.hostName AS hostName,
               COUNT(e.id) AS numPlanets,
               MAX(e.numStars) AS numStars,
               MAX(e.distanceParsec) AS distanceParsec,
               MAX(e.spectralType) AS spectralType
        FROM star_systems ss
        INNER JOIN exoplanets e ON e.systemId = ss.id AND e.isDefault = 1
        GROUP BY ss.id
        ORDER BY ss.hostName ASC
        """
    )
    fun getAllStarSystems(): Flow<List<StarSystemSummary>>

    @Query("SELECT * FROM exoplanets WHERE systemId = :systemId AND isDefault = 1 ORDER BY orbitSemiMajorAxisAu ASC")
    suspend fun getPlanetsForSystem(systemId: Long): List<ExoplanetEntity>

    @Query(
        """
        SELECT ss.id AS id, ss.hostName AS hostName,
               COUNT(e.id) AS numPlanets,
               MAX(e.numStars) AS numStars,
               MAX(e.distanceParsec) AS distanceParsec,
               MAX(e.spectralType) AS spectralType
        FROM star_systems ss
        INNER JOIN exoplanets e ON e.systemId = ss.id AND e.isDefault = 1
        WHERE ss.hostName LIKE '%' || :query || '%'
        GROUP BY ss.id
        ORDER BY ss.hostName ASC
        """
    )
    fun searchStarSystems(query: String): Flow<List<StarSystemSummary>>

    @Query(
        """
        SELECT ss.id AS id, ss.hostName AS hostName,
               COUNT(e.id) AS numPlanets,
               MAX(e.numStars) AS numStars,
               MAX(e.distanceParsec) AS distanceParsec,
               MAX(e.spectralType) AS spectralType
        FROM star_systems ss
        INNER JOIN exoplanets e ON e.systemId = ss.id AND e.isDefault = 1
        GROUP BY ss.id HAVING COUNT(e.id) > 1 ORDER BY COUNT(e.id) DESC
        """
    )
    fun getMultiPlanetSystems(): Flow<List<StarSystemSummary>>

    @Query(
        """
        SELECT ss.id AS id, ss.hostName AS hostName,
               COUNT(e.id) AS numPlanets,
               MAX(e.numStars) AS numStars,
               MAX(e.distanceParsec) AS distanceParsec,
               MAX(e.spectralType) AS spectralType
        FROM star_systems ss
        INNER JOIN exoplanets e ON e.systemId = ss.id AND e.isDefault = 1 AND e.numStars = :starCount
        GROUP BY ss.id
        ORDER BY ss.hostName ASC
        """
    )
    fun getStarSystemsByStarCount(starCount: Int): Flow<List<StarSystemSummary>>
}
