package com.app.exoplanethunter.exoplanet.data.sync

import android.content.Context
import com.app.exoplanethunter.exoplanet.data.local.SyncPreferences
import com.app.exoplanethunter.exoplanet.data.local.db.ExoplanetDatabase
import com.app.exoplanethunter.exoplanet.data.local.db.ExoplanetEntity
import com.app.exoplanethunter.exoplanet.data.local.db.StarSystemEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface NasaExoplanetApi {
    @GET("TAP/sync")
    suspend fun getExoplanets(
        @Query("query") query: String = "select pl_name,hostname,sy_snum,sy_pnum,discoverymethod,disc_year,disc_facility,pl_orbper,pl_orbsmax,pl_rade,pl_radj,pl_bmasse,pl_bmassj,pl_orbeccen,pl_insol,pl_eqt,st_teff,st_rad,st_mass,st_met,st_logg,st_spectype,sy_dist,ra,dec from pscomppars",
        @Query("format") format: String = "csv"
    ): String
}

/**
 * Fetches the current catalog from the NASA Exoplanet Archive's public TAP service and replaces
 * the local Room copy.
 *
 * Shared by the user-triggered refresh ([com.app.exoplanethunter.exoplanet.data.worker.DataSyncWorker])
 * and the background discovery check, so both run identical logic. The syncer also diffs the
 * catalog across the replace and reports which planet names are newly present, which is what makes
 * "new worlds confirmed" notifications possible.
 */
class ExoplanetSyncer(private val context: Context) {

    /**
     * @param totalPlanets number of planets in the refreshed catalog.
     * @param newPlanetNames planets present after the sync that were absent before.
     * @param wasFirstSync true when the local catalog was empty beforehand — every planet then
     *   counts as "new", so callers must not treat this as a discovery event.
     */
    data class Outcome(
        val totalPlanets: Int,
        val newPlanetNames: List<String>,
        val wasFirstSync: Boolean,
    )

    class SyncException(message: String) : Exception(message)

    private val dao = ExoplanetDatabase.getInstance(context).exoplanetDao()

    suspend fun sync(onProgress: (Int) -> Unit = {}): Outcome = withContext(Dispatchers.IO) {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(ScalarsConverterFactory.create())
            .client(OkHttpClient.Builder().build())
            .build()

        val api = retrofit.create(NasaExoplanetApi::class.java)
        onProgress(10)

        val csvData = api.getExoplanets()
        if (csvData.isEmpty()) throw SyncException("API returned no data")

        onProgress(40)

        val lines = csvData.lines()
        if (lines.size < 2) throw SyncException("Invalid data format")

        val planets = mutableListOf<ExoplanetEntity>()
        val systems = mutableSetOf<String>()

        for (i in 1 until lines.size) {
            val line = lines[i]
            if (line.isBlank()) continue
            val parts = splitCsv(line)
            if (parts.size < 25) continue
            val entity = mapToEntity(parts)
            planets.add(entity)
            systems.add(entity.hostName)
        }

        if (planets.isEmpty()) throw SyncException("No valid records found")

        // Snapshot the existing catalog before it is replaced so we can spot genuinely new worlds.
        val existingNames = dao.getAllPlanetNames().toSet()

        dao.deleteAllPlanets()
        dao.deleteAllStarSystems()

        val systemEntities = systems.map { StarSystemEntity(hostName = it) }
        val systemIds = dao.insertStarSystemsAndGetIds(systemEntities)
        val systemMap = systems.zip(systemIds).toMap()

        val planetsWithIds = planets.map { it.copy(systemId = systemMap[it.hostName] ?: 0) }
        dao.insertPlanets(planetsWithIds)

        SyncPreferences(context).saveLastSyncTime(System.currentTimeMillis())
        onProgress(100)

        val newNames = planets.map { it.planetName }.filterNot { it in existingNames }
        Outcome(
            totalPlanets = planets.size,
            newPlanetNames = newNames,
            wasFirstSync = existingNames.isEmpty(),
        )
    }

    private fun splitCsv(line: String): List<String> {
        val result = mutableListOf<String>()
        val sb = StringBuilder()
        var inQuotes = false
        for (char in line) {
            if (char == '"') inQuotes = !inQuotes
            else if (char == ',' && !inQuotes) {
                result.add(sb.toString().trim())
                sb.clear()
            } else sb.append(char)
        }
        result.add(sb.toString().trim())
        return result
    }

    private fun mapToEntity(parts: List<String>): ExoplanetEntity = ExoplanetEntity(
        planetName = parts[0],
        hostName = parts[1],
        numStars = parts[2].toIntOrNull() ?: 0,
        numPlanets = parts[3].toIntOrNull() ?: 0,
        discoveryMethod = parts[4],
        discoveryYear = parts[5].toIntOrNull() ?: 0,
        discoveryFacility = parts[6],
        orbitalPeriodDays = parts[7].toDoubleOrNull(),
        orbitSemiMajorAxisAu = parts[8].toDoubleOrNull(),
        planetRadiusEarth = parts[9].toDoubleOrNull(),
        planetRadiusJupiter = parts[10].toDoubleOrNull(),
        planetMassEarth = parts[11].toDoubleOrNull(),
        planetMassJupiter = parts[12].toDoubleOrNull(),
        eccentricity = parts[13].toDoubleOrNull(),
        insolationFlux = parts[14].toDoubleOrNull(),
        equilibriumTempK = parts[15].toDoubleOrNull(),
        stellarEffectiveTempK = parts[16].toDoubleOrNull(),
        stellarRadiusSolar = parts[17].toDoubleOrNull(),
        stellarMassSolar = parts[18].toDoubleOrNull(),
        stellarMetallicity = parts[19].toDoubleOrNull(),
        stellarSurfaceGravity = parts[20].toDoubleOrNull(),
        spectralType = parts[21],
        distanceParsec = parts[22].toDoubleOrNull(),
        ra = parts[23].toDoubleOrNull(),
        dec = parts[24].toDoubleOrNull(),
        isDefault = true,
        systemId = 0
    )

    private companion object {
        const val BASE_URL = "https://exoplanetarchive.ipac.caltech.edu/"
    }
}
