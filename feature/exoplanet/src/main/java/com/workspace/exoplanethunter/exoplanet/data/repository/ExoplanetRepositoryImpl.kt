package com.workspace.exoplanethunter.exoplanet.data.repository

import com.workspace.exoplanethunter.exoplanet.data.local.csv.CsvParser
import com.workspace.exoplanethunter.exoplanet.data.local.db.ExoplanetDao
import com.workspace.exoplanethunter.exoplanet.data.local.db.ExoplanetEntity
import com.workspace.exoplanethunter.exoplanet.domain.model.Exoplanet
import com.workspace.exoplanethunter.exoplanet.domain.model.StarSystem
import com.workspace.exoplanethunter.exoplanet.domain.repository.ExoplanetRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class ExoplanetRepositoryImpl(
    private val dao: ExoplanetDao,
    private val csvParser: CsvParser
) : ExoplanetRepository {

    override fun getAllPlanets(): Flow<List<Exoplanet>> {
        return dao.getAllPlanets().map { entities -> entities.map { it.toDomain() } }
    }

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

    override fun getAllStarSystems(): Flow<List<String>> {
        return dao.getAllStarSystems()
    }

    override suspend fun getStarSystem(hostName: String): StarSystem? {
        val entities = dao.getPlanetsForSystem(hostName)
        if (entities.isEmpty()) return null
        val first = entities.first()
        return StarSystem(
            hostName = hostName,
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

    override fun searchStarSystems(query: String): Flow<List<String>> {
        return dao.searchStarSystems(query)
    }

    override fun getMultiPlanetSystems(): Flow<List<String>> {
        return dao.getMultiPlanetSystems()
    }

    override fun getStarSystemsByStarCount(starCount: Int): Flow<List<String>> {
        return dao.getStarSystemsByStarCount(starCount)
    }

    override suspend fun loadDataIfNeeded() {
        withContext(Dispatchers.IO) {
            if (dao.getCount() == 0) {
                val planets = csvParser.parseExoplanets()
                planets.chunked(500).forEach { chunk ->
                    dao.insertAll(chunk)
                }
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
