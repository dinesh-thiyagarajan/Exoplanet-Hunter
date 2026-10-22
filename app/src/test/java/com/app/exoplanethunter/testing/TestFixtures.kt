package com.app.exoplanethunter.testing

import com.app.exoplanethunter.analytics.domain.model.AnalyticsEvent
import com.app.exoplanethunter.analytics.domain.repository.AnalyticsRepository
import com.app.exoplanethunter.exoplanet.domain.model.Exoplanet
import com.app.exoplanethunter.exoplanet.domain.model.LabelCount
import com.app.exoplanethunter.exoplanet.domain.model.StarPosition
import com.app.exoplanethunter.exoplanet.domain.model.StarSystem
import com.app.exoplanethunter.exoplanet.domain.model.StarSystemSummary
import com.app.exoplanethunter.exoplanet.domain.repository.ExoplanetRepository
import com.app.exoplanethunter.exoplanet.domain.repository.SyncStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf

/** An [Exoplanet] with every field defaulted to "unknown", so tests set only what they need. */
fun planet(
    id: Long = 1,
    name: String = "Test b",
    host: String = "Test",
    radiusEarth: Double? = null,
    massEarth: Double? = null,
    eqTempK: Double? = null,
    insolation: Double? = null,
    periodDays: Double? = null,
    semiMajorAxisAu: Double? = null,
    eccentricity: Double? = null,
    starTempK: Double? = null,
    starRadiusSolar: Double? = null,
    starMassSolar: Double? = null,
    distanceParsec: Double? = null,
    discoveryYear: Int = 2020,
) = Exoplanet(
    id = id, planetName = name, hostName = host, numStars = 1, numPlanets = 1,
    discoveryMethod = "Transit", discoveryYear = discoveryYear, discoveryFacility = "Test",
    orbitalPeriodDays = periodDays, orbitSemiMajorAxisAu = semiMajorAxisAu,
    planetRadiusEarth = radiusEarth, planetRadiusJupiter = null,
    planetMassEarth = massEarth, planetMassJupiter = null,
    eccentricity = eccentricity, insolationFlux = insolation, equilibriumTempK = eqTempK,
    stellarEffectiveTempK = starTempK, stellarRadiusSolar = starRadiusSolar,
    stellarMassSolar = starMassSolar, stellarMetallicity = null, stellarSurfaceGravity = null,
    spectralType = null, distanceParsec = distanceParsec, ra = null, dec = null,
)

fun starSystem(
    host: String = "Test",
    tempK: Double? = null,
    radiusSolar: Double? = null,
    massSolar: Double? = null,
    numStars: Int = 1,
    planets: List<Exoplanet> = emptyList(),
) = StarSystem(
    id = 1, hostName = host, numStars = numStars, numPlanets = planets.size,
    stellarEffectiveTempK = tempK, stellarRadiusSolar = radiusSolar, stellarMassSolar = massSolar,
    stellarMetallicity = null, stellarSurfaceGravity = null, spectralType = null,
    distanceParsec = null, ra = null, dec = null, planets = planets,
)

fun system(id: Long, host: String = "Star $id", stars: Int = 1) =
    StarSystemSummary(id = id, hostName = host, numPlanets = 1, numStars = stars)

class FakeAnalyticsRepository : AnalyticsRepository {
    val events = mutableListOf<AnalyticsEvent>()
    override fun track(event: AnalyticsEvent) { events += event }
}

/**
 * In-memory repository. Each query is backed by a [MutableStateFlow] the test can push new
 * values into — the same way Room re-emits when the database changes.
 */
class FakeExoplanetRepository : ExoplanetRepository {
    val planetsAll = MutableStateFlow<List<Exoplanet>>(emptyList())
    val latestPlanets = MutableStateFlow<List<Exoplanet>>(emptyList())
    val habitablePlanets = MutableStateFlow<List<Exoplanet>>(emptyList())
    val planetsByMethod = mutableMapOf<String, MutableStateFlow<List<Exoplanet>>>()
    val planetSearchResults = mutableMapOf<String, List<Exoplanet>>()
    val planetTotal = MutableStateFlow(0)

    val allSystems = MutableStateFlow<List<StarSystemSummary>>(emptyList())
    val systemsMultiPlanet = MutableStateFlow<List<StarSystemSummary>>(emptyList())
    val systemsByStarCount = mutableMapOf<Int, MutableStateFlow<List<StarSystemSummary>>>()
    val systemSearchResults = mutableMapOf<String, List<StarSystemSummary>>()
    val systemCount = MutableStateFlow(0)

    val favorites = MutableStateFlow<Set<String>>(emptySet())

    /** Override to make a query never emit, to observe the loading state in between. */
    var habitableOverride: Flow<List<Exoplanet>>? = null

    override fun getAllPlanets() = planetsAll
    override fun getPlanetsSortedByLatest() = latestPlanets
    override fun getPlanetsByMinDiscoveryYear(minYear: Int): Flow<List<Exoplanet>> =
        flowOf(planetsAll.value.filter { it.discoveryYear >= minYear })
    override fun getPlanetCount() = planetTotal
    override fun getStarSystemCount() = systemCount
    override fun getLastSyncTime(): Flow<Long> = flowOf(0L)
    override fun searchPlanets(query: String): Flow<List<Exoplanet>> = flowOf(planetSearchResults[query].orEmpty())
    override fun getPlanetsByDiscoveryMethod(method: String) =
        planetsByMethod.getOrPut(method) { MutableStateFlow(emptyList()) }
    override fun getMostHabitablePlanets(limit: Int): Flow<List<Exoplanet>> = habitableOverride ?: habitablePlanets
    override suspend fun getPlanetById(id: Long) = planetsAll.value.firstOrNull { it.id == id }
    override suspend fun getDiscoveryMethods() = listOf("Transit", "Radial Velocity")

    override fun getAllStarSystems() = allSystems
    override suspend fun getStarSystem(systemId: Long): StarSystem? = null
    override fun searchStarSystems(query: String): Flow<List<StarSystemSummary>> = flowOf(systemSearchResults[query].orEmpty())
    override fun getMultiPlanetSystems() = systemsMultiPlanet
    override fun getStarSystemsByStarCount(starCount: Int) =
        systemsByStarCount.getOrPut(starCount) { MutableStateFlow(emptyList()) }
    override fun getNearbyStarPositions(limit: Int): Flow<List<StarPosition>> = emptyFlow()

    override fun getSyncStatus(): Flow<SyncStatus> = flowOf(SyncStatus.Idle)
    override suspend fun syncExoplanets(): Flow<SyncStatus> = flowOf(SyncStatus.Success)

    override fun getDiscoveryMethodCounts(): Flow<List<LabelCount>> = emptyFlow()
    override fun getDiscoveryYearCounts(): Flow<List<LabelCount>> = emptyFlow()
    override fun getSizeDistribution(): Flow<List<LabelCount>> = emptyFlow()

    override fun getFavoriteNames() = favorites
    override fun getFavoritePlanets(): Flow<List<Exoplanet>> = flowOf(emptyList())
    override suspend fun toggleFavorite(planetName: String): Boolean {
        val now = planetName !in favorites.value
        favorites.value = if (now) favorites.value + planetName else favorites.value - planetName
        return now
    }
}
