package com.workspace.exoplanethunter.domain.usecase

import com.workspace.exoplanethunter.data.local.ml.HabitabilityClassifier
import com.workspace.exoplanethunter.domain.model.Exoplanet
import com.workspace.exoplanethunter.domain.model.HabitabilityInsight

class GetHabitabilityInsightUseCase(private val classifier: HabitabilityClassifier) {
    operator fun invoke(planet: Exoplanet): HabitabilityInsight = classifier.classify(planet)
}
