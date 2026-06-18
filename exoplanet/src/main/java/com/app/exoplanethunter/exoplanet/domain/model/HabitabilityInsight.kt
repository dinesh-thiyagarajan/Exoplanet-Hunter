package com.app.exoplanethunter.exoplanet.domain.model

data class HabitabilityInsight(
    val overallScore: Double,
    val scores: Map<String, Double>,
    val insights: List<String>,
    val classification: PlanetClassification,
    /**
     * False when the planet is missing the inputs that define habitability
     * (equilibrium temperature, radius, insolation). In that case the ML
     * habitability probability is outside the model's trained domain and must
     * not be presented as a confident score.
     */
    val habitabilityReliable: Boolean = true
)

enum class PlanetClassification(val label: String, val description: String) {
    SUB_EARTH("Sub-Earth", "Smaller than Earth, may lack atmosphere"),
    ROCKY("Rocky World", "Earth-sized rocky planet"),
    POTENTIALLY_HABITABLE("Potentially Habitable", "Earth-like conditions possible"),
    SUPER_EARTH("Super-Earth", "Larger rocky planet with thick atmosphere"),
    NEPTUNE_LIKE("Neptune-like", "Ice giant with gaseous envelope"),
    GAS_GIANT("Gas Giant", "Massive gas planet like Jupiter"),
    UNKNOWN("Unknown", "Insufficient data for classification")
}
