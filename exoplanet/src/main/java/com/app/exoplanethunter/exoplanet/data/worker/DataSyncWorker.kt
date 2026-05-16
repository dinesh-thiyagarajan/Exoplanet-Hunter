package com.app.exoplanethunter.exoplanet.data.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.app.exoplanethunter.exoplanet.data.local.SyncPreferences
import com.app.exoplanethunter.exoplanet.data.local.db.ExoplanetDao
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

class DataSyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    private val dao = ExoplanetDatabase.getInstance(context).exoplanetDao()

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val retrofit = Retrofit.Builder()
                .baseUrl("https://exoplanetarchive.ipac.caltech.edu/")
                .addConverterFactory(ScalarsConverterFactory.create())
                .client(OkHttpClient.Builder().build())
                .build()

            val api = retrofit.create(NasaExoplanetApi::class.java)
            
            setProgress(workDataOf("progress" to 10))

            val csvData = api.getExoplanets()
            if (csvData.isNullOrEmpty()) {
                return@withContext Result.failure(workDataOf("error" to "API returned no data"))
            }
            
            setProgress(workDataOf("progress" to 40))

            val lines = csvData.lines()
            if (lines.size < 2) {
                return@withContext Result.failure(workDataOf("error" to "Invalid data format"))
            }

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

            if (planets.isEmpty()) {
                return@withContext Result.failure(workDataOf("error" to "No valid records found"))
            }

            dao.deleteAllPlanets()
            dao.deleteAllStarSystems()

            val systemEntities = systems.map { StarSystemEntity(hostName = it) }
            val systemIds = dao.insertStarSystemsAndGetIds(systemEntities)
            val systemMap = systems.zip(systemIds).toMap()

            val planetsWithIds = planets.map { it.copy(systemId = systemMap[it.hostName] ?: 0) }
            dao.insertPlanets(planetsWithIds)

            SyncPreferences(applicationContext).saveLastSyncTime(System.currentTimeMillis())
            setProgress(workDataOf("progress" to 100))
            Result.success()
        } catch (e: Exception) {
            Result.failure(workDataOf("error" to (e.message ?: "Unknown error")))
        }
    }

    private fun splitCsv(line: String): List<String> {
        val result = mutableListOf<String>()
        val sb = StringBuilder()
        var inQuotes = false
        for (char in line) {
            if (char == '\"') inQuotes = !inQuotes
            else if (char == ',' && !inQuotes) {
                result.add(sb.toString().trim())
                sb.clear()
            } else sb.append(char)
        }
        result.add(sb.toString().trim())
        return result
    }

    private fun mapToEntity(parts: List<String>): ExoplanetEntity {
        return ExoplanetEntity(
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
    }
}
