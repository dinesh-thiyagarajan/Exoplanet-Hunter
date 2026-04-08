package com.app.exoplanethunter.exoplanet.data.local.csv

import android.content.Context
import com.app.exoplanethunter.exoplanet.data.local.db.ExoplanetEntity
import java.io.BufferedReader
import java.io.InputStreamReader

class CsvParser(private val context: Context) {

    fun parseExoplanets(): List<ExoplanetEntity> {
        val planets = mutableListOf<ExoplanetEntity>()
        val inputStream = context.assets.open("exoplanets.csv")
        val reader = BufferedReader(InputStreamReader(inputStream))

        var headerLine: String? = null
        var headers: List<String> = emptyList()

        reader.useLines { lines ->
            for (line in lines) {
                if (line.startsWith("#")) continue
                if (headerLine == null) {
                    headerLine = line
                    headers = line.split(",").map { it.trim() }
                    continue
                }
                val entity = parseLine(line, headers)
                if (entity != null) {
                    planets.add(entity)
                }
            }
        }
        return planets
    }

    private fun parseLine(line: String, headers: List<String>): ExoplanetEntity? {
        val values = splitCsvLine(line)
        if (values.size < headers.size) return null

        val map = headers.zip(values).toMap()

        val planetName = map["pl_name"]?.takeIf { it.isNotBlank() } ?: return null
        val hostName = map["hostname"]?.takeIf { it.isNotBlank() } ?: return null

        return ExoplanetEntity(
            planetName = planetName,
            hostName = hostName,
            numStars = map["sy_snum"]?.toIntOrNull() ?: 1,
            numPlanets = map["sy_pnum"]?.toIntOrNull() ?: 1,
            discoveryMethod = map["discoverymethod"] ?: "Unknown",
            discoveryYear = map["disc_year"]?.toIntOrNull() ?: 0,
            discoveryFacility = map["disc_facility"] ?: "Unknown",
            orbitalPeriodDays = map["pl_orbper"]?.toDoubleOrNull(),
            orbitSemiMajorAxisAu = map["pl_orbsmax"]?.toDoubleOrNull(),
            planetRadiusEarth = map["pl_rade"]?.toDoubleOrNull(),
            planetRadiusJupiter = map["pl_radj"]?.toDoubleOrNull(),
            planetMassEarth = map["pl_bmasse"]?.toDoubleOrNull(),
            planetMassJupiter = map["pl_bmassj"]?.toDoubleOrNull(),
            eccentricity = map["pl_orbeccen"]?.toDoubleOrNull(),
            insolationFlux = map["pl_insol"]?.toDoubleOrNull(),
            equilibriumTempK = map["pl_eqt"]?.toDoubleOrNull(),
            stellarEffectiveTempK = map["st_teff"]?.toDoubleOrNull(),
            stellarRadiusSolar = map["st_rad"]?.toDoubleOrNull(),
            stellarMassSolar = map["st_mass"]?.toDoubleOrNull(),
            stellarMetallicity = map["st_met"]?.toDoubleOrNull(),
            stellarSurfaceGravity = map["st_logg"]?.toDoubleOrNull(),
            spectralType = map["st_spectype"]?.takeIf { it.isNotBlank() },
            distanceParsec = map["sy_dist"]?.toDoubleOrNull(),
            ra = map["ra"]?.toDoubleOrNull(),
            dec = map["dec"]?.toDoubleOrNull(),
            isDefault = map["default_flag"] == "1"
        )
    }

    private fun splitCsvLine(line: String): List<String> {
        val result = mutableListOf<String>()
        val sb = StringBuilder()
        var inQuotes = false
        var inTag = false

        for (ch in line) {
            when {
                ch == '<' -> { inTag = true; sb.append(ch) }
                ch == '>' -> { inTag = false; sb.append(ch) }
                ch == '"' && !inTag -> inQuotes = !inQuotes
                ch == ',' && !inQuotes && !inTag -> { result.add(sb.toString().trim()); sb.clear() }
                else -> sb.append(ch)
            }
        }
        result.add(sb.toString().trim())
        return result
    }
}
