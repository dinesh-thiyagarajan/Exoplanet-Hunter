package com.workspace.exoplanethunter.exoplanet.data.local.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "exoplanets")
data class ExoplanetEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val planetName: String,
    val hostName: String,
    val numStars: Int,
    val numPlanets: Int,
    val discoveryMethod: String,
    val discoveryYear: Int,
    val discoveryFacility: String,
    val orbitalPeriodDays: Double?,
    val orbitSemiMajorAxisAu: Double?,
    val planetRadiusEarth: Double?,
    val planetRadiusJupiter: Double?,
    val planetMassEarth: Double?,
    val planetMassJupiter: Double?,
    val eccentricity: Double?,
    val insolationFlux: Double?,
    val equilibriumTempK: Double?,
    val stellarEffectiveTempK: Double?,
    val stellarRadiusSolar: Double?,
    val stellarMassSolar: Double?,
    val stellarMetallicity: Double?,
    val stellarSurfaceGravity: Double?,
    val spectralType: String?,
    val distanceParsec: Double?,
    val ra: Double?,
    val dec: Double?,
    val isDefault: Boolean
)
