package com.workspace.exoplanethunter.ml

import com.workspace.exoplanethunter.exoplanet.domain.model.Exoplanet
import com.workspace.exoplanethunter.exoplanet.domain.model.HabitabilityInsight

/**
 * Use case that wraps [ExoplanetClassifier] to produce a [HabitabilityInsight]
 * for a given [Exoplanet].
 *
 * This keeps the presentation layer decoupled from the TFLite implementation
 * details and follows the clean-architecture convention used in the rest of the
 * project.
 */
class GetHabitabilityInsightUseCase(private val classifier: ExoplanetClassifier) {

    /**
     * Classify the given [planet] and return a complete [HabitabilityInsight]
     * containing the habitability probability, category scores, educational
     * insights, and planet classification.
     */
    operator fun invoke(planet: Exoplanet): HabitabilityInsight {
        return classifier.classify(planet)
    }
}
