package com.workspace.exoplanethunter.ml

import android.content.Context
import com.workspace.exoplanethunter.exoplanet.domain.model.Exoplanet
import com.workspace.exoplanethunter.exoplanet.domain.model.HabitabilityInsight
import com.workspace.exoplanethunter.exoplanet.domain.model.PlanetClassification
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.max
import kotlin.math.min

class ExoplanetClassifier(private val context: Context) {

    private var habitableInterpreter: Interpreter? = null
    private var planetTypeInterpreter: Interpreter? = null

    // --- Habitable model scaler values (20 features) ---
    private val habitableMean = doubleArrayOf(
        25.560833292970155,
        0.13460360990145173,
        6.4986823561924885,
        25.79798072630463,
        0.004804193102388817,
        860.1479806436745,
        379.86174009524547,
        5482.1013948844,
        1.0138716154850602,
        0.9225480027651894,
        4.444814037176434,
        -0.07453644673169983,
        718.4176214993471,
        0.2080609381835047,
        0.06723511712452174,
        0.25520673162768975,
        2.5096081607032654,
        2.8122041565472364,
        1.3067653308078833,
        6.282567205820584
    )

    private val habitableScale = doubleArrayOf(
        65.35558673089939,
        0.14958115452650433,
        95.7136878735263,
        149.5999970781158,
        0.03670958665306548,
        406.04043215477435,
        1523.7997346151017,
        790.6255730076459,
        0.6007659751230484,
        0.21431383648388236,
        0.23393895377752685,
        0.22750864733327406,
        468.1251943857583,
        0.1035242334515346,
        1.1110805296756134,
        0.5096713872352066,
        1.1106401895153042,
        0.4850417180975384,
        0.6177089533843413,
        0.9186321931600263
    )

    private val habitableThreshold = 0.62f

    // --- Planet type model scaler values (20 features) ---
    private val planetTypeMean = doubleArrayOf(
        11108.13950634216,
        2.7014787152319224,
        4.47275712511527,
        289.9823757578617,
        0.022389933047275677,
        833.3962628852892,
        206.07078596455594,
        5474.107105570508,
        1.134566321814858,
        0.9455765836083887,
        4.423302329076321,
        -0.014755197278220689,
        722.4019331174528,
        0.10555481603702221,
        0.04488199122729885,
        0.15925262787217356,
        2.6901382198034174,
        5.1699936676080265,
        1.294391114165062,
        6.098611492058574
    )

    private val planetTypeScale = doubleArrayOf(
        2026758.0343796052,
        135.1906867724341,
        59.563570513224754,
        698.1905549437545,
        0.09047068281060498,
        286.7075398073097,
        922.4816974479531,
        949.590798231595,
        1.7037517200314074,
        0.2977623847044273,
        0.26503863279641726,
        0.18046065470692144,
        814.6651146558402,
        0.14403687635270496,
        0.698184349118896,
        0.30118938273052537,
        1.4314269552905081,
        0.9688837764669395,
        0.5180293461706119,
        1.1474022814476705
    )

    private val planetTypeLabels = arrayOf(
        "Gas Giant",
        "Neptune-like",
        "Rocky",
        "Sub-Neptune",
        "Super-Earth",
        "Unknown"
    )

    init {
        try {
            habitableInterpreter = Interpreter(loadModelFile("habitable_model.tflite"))
            planetTypeInterpreter = Interpreter(loadModelFile("planet_type_model.tflite"))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun loadModelFile(filename: String): MappedByteBuffer {
        val assetFileDescriptor = context.assets.openFd(filename)
        val inputStream = FileInputStream(assetFileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = assetFileDescriptor.startOffset
        val declaredLength = assetFileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    /**
     * Run full classification pipeline on an exoplanet.
     *
     * Returns a [HabitabilityInsight] containing the habitability probability,
     * category scores, educational insights, and planet classification.
     */
    fun classify(planet: Exoplanet): HabitabilityInsight {
        val rawFeatures = extractFeatures(planet)

        // Run habitability model
        val habitableProb = runHabitableModel(rawFeatures)

        // Run planet type model
        val planetTypeProbs = runPlanetTypeModel(rawFeatures)

        // Determine the best planet type label
        val planetTypeIndex = planetTypeProbs.indices.maxByOrNull { planetTypeProbs[it] } ?: 5
        val planetTypeLabel = planetTypeLabels[planetTypeIndex]
        val planetTypeConfidence = planetTypeProbs[planetTypeIndex]

        val classification = mapToClassification(planetTypeLabel, habitableProb)

        val scores = linkedMapOf<String, Double>()
        scores["Habitability"] = habitableProb.toDouble()
        scores["Temperature Zone"] = calculateTempScore(planet)
        scores["Size Compatibility"] = calculateSizeScore(planet)
        scores["Atmospheric Potential"] = calculateAtmosphereScore(planet)
        scores["Stellar Stability"] = calculateStellarScore(planet)

        val insights = generateInsights(
            planet, habitableProb, planetTypeLabel, planetTypeConfidence, planetTypeProbs
        )

        return HabitabilityInsight(
            overallScore = habitableProb.toDouble(),
            scores = scores,
            insights = insights,
            classification = classification
        )
    }

    // -----------------------------------------------------------------------
    // Feature extraction
    // -----------------------------------------------------------------------

    /**
     * Compute the 20-element raw feature vector from the [Exoplanet] domain model.
     *
     * Feature order (indices 0-19):
     *  0  pl_orbper              orbitalPeriodDays
     *  1  pl_orbsmax             orbitSemiMajorAxisAu
     *  2  pl_rade                planetRadiusEarth
     *  3  pl_bmasse              planetMassEarth
     *  4  pl_orbeccen            eccentricity
     *  5  pl_eqt                 equilibriumTempK
     *  6  pl_insol               insolationFlux
     *  7  st_teff                stellarEffectiveTempK
     *  8  st_rad                 stellarRadiusSolar
     *  9  st_mass                stellarMassSolar
     * 10  st_logg                stellarSurfaceGravity
     * 11  st_met                 stellarMetallicity
     * 12  sy_dist                distanceParsec
     * 13  radius_mass_ratio      pl_rade / pl_bmasse
     * 14  planet_star_radius_ratio  pl_rade / (st_rad * 109.076)
     * 15  flux_temp_ratio        pl_insol / pl_eqt
     * 16  log_pl_orbper          ln(pl_orbper)
     * 17  log_pl_bmasse          ln(pl_bmasse)
     * 18  log_pl_rade            ln(pl_rade)
     * 19  log_sy_dist            ln(sy_dist)
     */
    private fun extractFeatures(planet: Exoplanet): DoubleArray {
        val plOrbper = planet.orbitalPeriodDays ?: 0.0
        val plOrbsmax = planet.orbitSemiMajorAxisAu ?: 0.0
        val plRade = planet.planetRadiusEarth ?: 0.0
        val plBmasse = planet.planetMassEarth ?: 0.0
        val plOrbeccen = planet.eccentricity ?: 0.0
        val plEqt = planet.equilibriumTempK ?: 0.0
        val plInsol = planet.insolationFlux ?: 0.0
        val stTeff = planet.stellarEffectiveTempK ?: 0.0
        val stRad = planet.stellarRadiusSolar ?: 0.0
        val stMass = planet.stellarMassSolar ?: 0.0
        val stLogg = planet.stellarSurfaceGravity ?: 0.0
        val stMet = planet.stellarMetallicity ?: 0.0
        val syDist = planet.distanceParsec ?: 0.0

        // Derived features
        val radiusMassRatio = if (plBmasse > 0.0) plRade / plBmasse else 0.0
        val planetStarRadiusRatio = if (stRad > 0.0) plRade / (stRad * 109.076) else 0.0
        val fluxTempRatio = if (plEqt > 0.0) plInsol / plEqt else 0.0
        val logPlOrbper = safeLog(plOrbper)
        val logPlBmasse = safeLog(plBmasse)
        val logPlRade = safeLog(plRade)
        val logSyDist = safeLog(syDist)

        return doubleArrayOf(
            plOrbper,               // 0
            plOrbsmax,              // 1
            plRade,                 // 2
            plBmasse,               // 3
            plOrbeccen,             // 4
            plEqt,                  // 5
            plInsol,                // 6
            stTeff,                 // 7
            stRad,                  // 8
            stMass,                 // 9
            stLogg,                 // 10
            stMet,                  // 11
            syDist,                 // 12
            radiusMassRatio,        // 13
            planetStarRadiusRatio,  // 14
            fluxTempRatio,          // 15
            logPlOrbper,            // 16
            logPlBmasse,            // 17
            logPlRade,              // 18
            logSyDist               // 19
        )
    }

    /** Natural log that guards against non-positive values. */
    private fun safeLog(value: Double): Double {
        return if (value > 0.0) ln(value) else 0.0
    }

    // -----------------------------------------------------------------------
    // Normalization
    // -----------------------------------------------------------------------

    /**
     * Apply Z-score normalization: (x - mean) / scale for each feature.
     */
    private fun normalizeFeatures(
        raw: DoubleArray,
        mean: DoubleArray,
        scale: DoubleArray
    ): FloatArray {
        require(raw.size == mean.size && raw.size == scale.size) {
            "Feature array size (${raw.size}) must match mean (${mean.size}) and scale (${scale.size})"
        }
        return FloatArray(raw.size) { i ->
            ((raw[i] - mean[i]) / scale[i]).toFloat()
        }
    }

    // -----------------------------------------------------------------------
    // Model inference
    // -----------------------------------------------------------------------

    /**
     * Run the habitable model and return a sigmoid probability in [0, 1].
     */
    private fun runHabitableModel(rawFeatures: DoubleArray): Float {
        val interpreter = habitableInterpreter ?: return 0f
        val normalized = normalizeFeatures(rawFeatures, habitableMean, habitableScale)

        // Prepare input buffer: 1 sample x 20 features, float32
        val inputBuffer = ByteBuffer.allocateDirect(4 * normalized.size)
            .order(ByteOrder.nativeOrder())
        for (value in normalized) {
            inputBuffer.putFloat(value)
        }

        // Output: single float (logit or probability depending on training)
        val outputBuffer = ByteBuffer.allocateDirect(4)
            .order(ByteOrder.nativeOrder())

        interpreter.run(inputBuffer, outputBuffer)
        outputBuffer.rewind()
        val rawOutput = outputBuffer.float

        // Apply sigmoid to convert logit to probability
        return sigmoid(rawOutput)
    }

    /**
     * Run the planet-type model and return a softmax probability array of size 6.
     */
    private fun runPlanetTypeModel(rawFeatures: DoubleArray): FloatArray {
        val interpreter = planetTypeInterpreter ?: return FloatArray(6) { 0f }
        val normalized = normalizeFeatures(rawFeatures, planetTypeMean, planetTypeScale)

        // Prepare input buffer: 1 sample x 20 features, float32
        val inputBuffer = ByteBuffer.allocateDirect(4 * normalized.size)
            .order(ByteOrder.nativeOrder())
        for (value in normalized) {
            inputBuffer.putFloat(value)
        }

        // Output: 6 floats (one per class)
        val outputBuffer = ByteBuffer.allocateDirect(4 * planetTypeLabels.size)
            .order(ByteOrder.nativeOrder())

        interpreter.run(inputBuffer, outputBuffer)
        outputBuffer.rewind()

        val logits = FloatArray(planetTypeLabels.size) { outputBuffer.float }
        return softmax(logits)
    }

    private fun sigmoid(x: Float): Float {
        return (1.0f / (1.0f + exp(-x.toDouble()))).toFloat()
    }

    private fun softmax(logits: FloatArray): FloatArray {
        val maxLogit = logits.max()
        val exps = FloatArray(logits.size) { exp((logits[it] - maxLogit).toDouble()).toFloat() }
        val sumExps = exps.sum()
        return FloatArray(exps.size) { exps[it] / sumExps }
    }

    // -----------------------------------------------------------------------
    // Classification mapping
    // -----------------------------------------------------------------------

    /**
     * Map the TFLite planet-type label and habitability probability to the
     * app-level [PlanetClassification] enum.
     */
    private fun mapToClassification(
        planetTypeLabel: String,
        habitableProb: Float
    ): PlanetClassification {
        // If the habitability probability exceeds the threshold, mark potentially habitable
        if (habitableProb >= habitableThreshold) {
            return PlanetClassification.POTENTIALLY_HABITABLE
        }

        return when (planetTypeLabel) {
            "Gas Giant" -> PlanetClassification.GAS_GIANT
            "Neptune-like" -> PlanetClassification.NEPTUNE_LIKE
            "Rocky" -> PlanetClassification.ROCKY
            "Sub-Neptune" -> PlanetClassification.SUB_EARTH
            "Super-Earth" -> PlanetClassification.SUPER_EARTH
            "Unknown" -> PlanetClassification.UNKNOWN
            else -> PlanetClassification.UNKNOWN
        }
    }

    // -----------------------------------------------------------------------
    // Score helpers
    // -----------------------------------------------------------------------

    /**
     * Score based on equilibrium temperature proximity to the habitable zone
     * (roughly 200 K - 350 K). Returns a value in [0, 1].
     */
    private fun calculateTempScore(planet: Exoplanet): Double {
        val eqTemp = planet.equilibriumTempK ?: return 0.0
        if (eqTemp <= 0.0) return 0.0

        val idealCenter = 275.0 // midpoint of 200-350 K range
        val halfWidth = 75.0    // half-width of ideal zone
        val distance = abs(eqTemp - idealCenter)

        return when {
            distance <= halfWidth -> 1.0  // inside the habitable zone
            distance <= halfWidth * 2.0 -> {
                // Linear decay outside the zone up to 2x the half-width
                1.0 - (distance - halfWidth) / halfWidth
            }
            distance <= halfWidth * 4.0 -> {
                // Slower decay further out
                max(0.0, 0.25 * (1.0 - (distance - halfWidth * 2.0) / (halfWidth * 2.0)))
            }
            else -> 0.0
        }
    }

    /**
     * Score based on planet radius and mass being Earth-like.
     * Ideal range: 0.5 - 2.0 Earth radii.
     */
    private fun calculateSizeScore(planet: Exoplanet): Double {
        val radius = planet.planetRadiusEarth ?: return 0.0
        if (radius <= 0.0) return 0.0

        return when {
            radius in 0.5..2.0 -> {
                // Peak score at 1.0 Earth radius, linearly decreasing towards edges
                val distFromIdeal = abs(radius - 1.0)
                1.0 - distFromIdeal * 0.5
            }
            radius in 2.0..4.0 -> {
                // Super-Earth range: moderate score
                max(0.0, 0.5 - (radius - 2.0) * 0.15)
            }
            radius < 0.5 -> {
                // Too small: likely cannot retain atmosphere
                radius / 0.5 * 0.4
            }
            else -> {
                // Large gas/ice worlds: low score
                max(0.0, 0.2 - (radius - 4.0) * 0.01)
            }
        }
    }

    /**
     * Score based on whether the planet has sufficient mass to retain an atmosphere.
     * Ideal mass range: 0.5 - 10 Earth masses for rocky / super-Earth worlds.
     */
    private fun calculateAtmosphereScore(planet: Exoplanet): Double {
        val mass = planet.planetMassEarth ?: return 0.0
        if (mass <= 0.0) return 0.0

        return when {
            mass in 0.1..0.5 -> {
                // Marginal: atmosphere may be thin
                0.3 + (mass - 0.1) / 0.4 * 0.3
            }
            mass in 0.5..5.0 -> {
                // Ideal range for retaining a moderate atmosphere
                1.0 - abs(mass - 2.0) / 5.0 * 0.2
            }
            mass in 5.0..10.0 -> {
                // Super-Earth range: thick atmosphere likely
                0.7 - (mass - 5.0) / 5.0 * 0.2
            }
            mass in 10.0..50.0 -> {
                // Transitioning to gas envelope
                max(0.1, 0.5 - (mass - 10.0) / 40.0 * 0.4)
            }
            mass < 0.1 -> {
                // Too small to hold atmosphere
                mass / 0.1 * 0.2
            }
            else -> {
                // Massive gas giant
                0.05
            }
        }
    }

    /**
     * Score based on stellar properties being conducive to life.
     * K- and G-type main-sequence stars (Teff ~3900-6000 K, log(g) ~4.0-4.8)
     * are considered most stable.
     */
    private fun calculateStellarScore(planet: Exoplanet): Double {
        val teff = planet.stellarEffectiveTempK ?: return 0.0
        val logg = planet.stellarSurfaceGravity ?: 0.0

        var score = 0.0

        // Temperature component (K/G type stars: 3900-6000 K)
        val tempScore = when {
            teff in 3900.0..6000.0 -> 1.0
            teff in 3500.0..3900.0 -> 0.5 + (teff - 3500.0) / 400.0 * 0.5
            teff in 6000.0..7500.0 -> max(0.0, 1.0 - (teff - 6000.0) / 1500.0)
            teff in 2500.0..3500.0 -> max(0.0, 0.5 - (3500.0 - teff) / 1000.0 * 0.5)
            else -> 0.0
        }
        score += tempScore * 0.6

        // Surface gravity component (main-sequence: log(g) ~4.0-4.8)
        if (logg > 0.0) {
            val loggScore = when {
                logg in 4.0..4.8 -> 1.0
                logg in 3.5..4.0 -> (logg - 3.5) / 0.5
                logg in 4.8..5.5 -> max(0.0, 1.0 - (logg - 4.8) / 0.7)
                else -> 0.0
            }
            score += loggScore * 0.4
        } else {
            // No gravity data: assume moderate score contribution
            score += 0.2
        }

        return min(1.0, score)
    }

    // -----------------------------------------------------------------------
    // Insight generation
    // -----------------------------------------------------------------------

    /**
     * Produce a list of educational, human-readable insights about the planet
     * based on its properties and the model predictions.
     */
    private fun generateInsights(
        planet: Exoplanet,
        habitableProb: Float,
        planetTypeLabel: String,
        planetTypeConfidence: Float,
        planetTypeProbs: FloatArray
    ): List<String> {
        val insights = mutableListOf<String>()

        // --- Planet type classification ---
        val confidencePct = "%.1f".format(planetTypeConfidence * 100)
        insights.add(
            "This planet is classified as a $planetTypeLabel with $confidencePct% confidence."
        )

        // Second-best type if notable
        val sortedIndices = planetTypeProbs.indices.sortedByDescending { planetTypeProbs[it] }
        if (sortedIndices.size >= 2) {
            val secondIdx = sortedIndices[1]
            val secondProb = planetTypeProbs[secondIdx]
            if (secondProb > 0.15f) {
                val secondPct = "%.1f".format(secondProb * 100)
                insights.add(
                    "There is also a $secondPct% probability of being ${planetTypeLabels[secondIdx]}."
                )
            }
        }

        // --- Habitability ---
        val habitPct = "%.1f".format(habitableProb * 100)
        if (habitableProb >= habitableThreshold) {
            insights.add(
                "The ML model gives a habitability probability of $habitPct%, which exceeds " +
                        "the ${(habitableThreshold * 100).toInt()}% threshold. This planet may " +
                        "have conditions compatible with life as we know it."
            )
        } else if (habitableProb >= 0.3f) {
            insights.add(
                "The habitability probability is $habitPct%. While below the " +
                        "${(habitableThreshold * 100).toInt()}% threshold, this planet shows " +
                        "some promising characteristics worth further study."
            )
        } else {
            insights.add(
                "The habitability probability is $habitPct%, indicating conditions are " +
                        "unlikely to support Earth-like life."
            )
        }

        // --- Temperature insights ---
        val eqTemp = planet.equilibriumTempK
        if (eqTemp != null && eqTemp > 0.0) {
            val tempStr = "%.0f".format(eqTemp)
            when {
                eqTemp in 200.0..350.0 -> insights.add(
                    "The equilibrium temperature of ${tempStr}K falls within the habitable " +
                            "zone where liquid water could exist on the surface."
                )
                eqTemp in 150.0..200.0 -> insights.add(
                    "At ${tempStr}K, the planet is cold but could potentially support " +
                            "sub-surface liquid water with sufficient greenhouse warming."
                )
                eqTemp in 350.0..500.0 -> insights.add(
                    "The equilibrium temperature of ${tempStr}K is above the habitable zone. " +
                            "Surface water would likely evaporate under these conditions."
                )
                eqTemp > 500.0 -> insights.add(
                    "At ${tempStr}K, this planet is extremely hot. Any water would exist " +
                            "only as vapour, and the surface environment is likely inhospitable."
                )
                eqTemp < 150.0 -> insights.add(
                    "At ${tempStr}K, this planet is very cold. Surface water would be " +
                            "frozen, though tidal heating or a thick atmosphere could provide warmth."
                )
            }
        } else {
            insights.add(
                "No equilibrium temperature data is available, limiting our ability to " +
                        "assess surface conditions."
            )
        }

        // --- Size and mass insights ---
        val radius = planet.planetRadiusEarth
        val mass = planet.planetMassEarth
        if (radius != null && radius > 0.0) {
            val radStr = "%.2f".format(radius)
            when {
                radius < 0.5 -> insights.add(
                    "With a radius of ${radStr} Earth radii, this is a very small world " +
                            "that likely cannot retain a significant atmosphere."
                )
                radius in 0.5..1.5 -> insights.add(
                    "At ${radStr} Earth radii, this planet is comparable in size to Earth " +
                            "and could have a rocky surface with a moderate atmosphere."
                )
                radius in 1.5..2.0 -> insights.add(
                    "At ${radStr} Earth radii, this is a Super-Earth with likely stronger " +
                            "surface gravity and a denser atmosphere than our planet."
                )
                radius in 2.0..4.0 -> insights.add(
                    "With a radius of ${radStr} Earth radii, this planet falls in the " +
                            "Sub-Neptune category, likely possessing a significant gaseous envelope."
                )
                radius in 4.0..10.0 -> insights.add(
                    "At ${radStr} Earth radii, this is a Neptune-sized world likely " +
                            "dominated by a thick atmosphere of hydrogen and helium."
                )
                else -> insights.add(
                    "With a radius of ${radStr} Earth radii, this is a gas giant with no " +
                            "solid surface, similar in scale to Jupiter or Saturn."
                )
            }
        }

        if (mass != null && mass > 0.0 && radius != null && radius > 0.0) {
            val density = mass / (radius * radius * radius) // relative to Earth's density
            val densityStr = "%.2f".format(density)
            when {
                density > 1.5 -> insights.add(
                    "The estimated relative density of $densityStr (compared to Earth) " +
                            "suggests a predominantly rocky or iron-rich composition."
                )
                density in 0.5..1.5 -> insights.add(
                    "The estimated relative density of $densityStr (compared to Earth) is " +
                            "consistent with a rocky world, possibly with a water-rich mantle."
                )
                density < 0.5 -> insights.add(
                    "The low relative density of $densityStr (compared to Earth) indicates " +
                            "a gaseous composition with a small or absent rocky core."
                )
            }
        }

        // --- Orbital insights ---
        val period = planet.orbitalPeriodDays
        val sma = planet.orbitSemiMajorAxisAu
        if (period != null && period > 0.0) {
            val periodStr = "%.2f".format(period)
            when {
                period < 1.0 -> insights.add(
                    "The extremely short orbital period of $periodStr days suggests this " +
                            "planet orbits very close to its star, likely tidally locked."
                )
                period in 1.0..10.0 -> insights.add(
                    "With an orbital period of $periodStr days, this planet is in a tight " +
                            "orbit and receives intense stellar radiation."
                )
                period in 200.0..500.0 -> insights.add(
                    "An orbital period of $periodStr days places this planet at a moderate " +
                            "distance from its star, within the realm of habitable-zone orbits " +
                            "for Sun-like stars."
                )
                period > 500.0 -> insights.add(
                    "The long orbital period of $periodStr days indicates this planet " +
                            "orbits far from its host star."
                )
            }
        }

        val ecc = planet.eccentricity
        if (ecc != null && ecc > 0.2) {
            val eccStr = "%.3f".format(ecc)
            insights.add(
                "The orbital eccentricity of $eccStr indicates a noticeably elliptical orbit, " +
                        "which could cause significant seasonal temperature variations."
            )
        }

        // --- Stellar host insights ---
        val stTeff = planet.stellarEffectiveTempK
        if (stTeff != null && stTeff > 0.0) {
            val spectralDescription = when {
                stTeff >= 7500.0 -> "an F-type or hotter star, which may have a shorter main-sequence lifetime"
                stTeff in 6000.0..7500.0 -> "an F/G-type star similar to our Sun"
                stTeff in 5200.0..6000.0 -> "a G-type star closely resembling our Sun"
                stTeff in 3900.0..5200.0 -> "a K-type star, which is cooler and longer-lived than the Sun"
                stTeff in 2500.0..3900.0 -> "an M-type red dwarf, the most common type of star in the galaxy"
                else -> "a very cool stellar object"
            }
            insights.add(
                "The host star has an effective temperature of ${"%.0f".format(stTeff)}K, " +
                        "consistent with $spectralDescription."
            )
        }

        // --- Distance ---
        val dist = planet.distanceParsec
        if (dist != null && dist > 0.0) {
            val lightYears = dist * 3.26156
            val lyStr = "%.1f".format(lightYears)
            insights.add(
                "This system is approximately $lyStr light-years from Earth " +
                        "(${"%.1f".format(dist)} parsecs)."
            )
        }

        return insights
    }

    /**
     * Release TFLite interpreter resources. Call when the classifier is no
     * longer needed (e.g. when the ViewModel is cleared).
     */
    fun close() {
        habitableInterpreter?.close()
        habitableInterpreter = null
        planetTypeInterpreter?.close()
        planetTypeInterpreter = null
    }
}
