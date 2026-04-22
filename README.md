# Exoplanet Hunter

An Android app for browsing confirmed exoplanets and the star systems that host them, with on-device ML analysis that estimates each planet's habitability and type.

---

## Data Source

All planetary data is sourced from the **[NASA Exoplanet Archive](https://exoplanetarchive.ipac.caltech.edu/)**, maintained by Caltech/IPAC under contract with NASA.

- **Format**: CSV snapshot shipped in the APK at `exoplanet/src/main/assets/exoplanets.csv`
- **Snapshot date**: 25 Feb 2026
- **Load flow**: on first launch, [CsvParser.kt](exoplanet/src/main/java/com/app/exoplanethunter/exoplanet/data/local/csv/CsvParser.kt) parses the CSV into Room entities via [ExoplanetRepositoryImpl.kt](exoplanet/src/main/java/com/app/exoplanethunter/exoplanet/data/repository/ExoplanetRepositoryImpl.kt). Subsequent launches read directly from the local Room database.

---

## Architecture

The project follows **Clean Architecture** with strict layer separation, organised into five top-level Gradle modules:

```
:app            → UI, navigation, ViewModels, DI wiring (entry point)
:exoplanet      → Data layer (CSV, Room) + Domain layer (use cases, models)
:ml             → TensorFlow Lite inference for habitability + planet type
:ads            → AdMob banner integration
:analytics      → Firebase Analytics event tracking
```

### Layers

| Layer | Responsibility | Key pieces |
|-------|---------------|------------|
| **Data** | CSV parsing, Room persistence, repository implementation | `CsvParser`, `ExoplanetDao`, `ExoplanetRepositoryImpl` |
| **Domain** | Pure Kotlin models and use cases, no Android deps | `Exoplanet`, `GetHabitabilityInsightUseCase`, `ExoplanetRepository` |
| **Presentation** | Compose UI, ViewModels with StateFlow | `PlanetListScreen`, `PlanetDetailViewModel` |

### Tech stack

- **UI**: Jetpack Compose (BOM `2024.06.00`) + Material 3 + Navigation Compose
- **State**: `ViewModel` + `StateFlow` + Kotlin Coroutines
- **DI**: Koin `3.5.6`
- **Persistence**: Room `2.6.1`
- **ML runtime**: TensorFlow Lite `2.17.0`
- **Analytics & Crashlytics**: Firebase BOM `33.7.0`
- **Ads**: Google Mobile Ads SDK `23.6.0`
- **Language / build**: Kotlin `1.9.24`, AGP `8.7.3`, JDK 17, `minSdk 29`, `targetSdk 36`

---

## Machine Learning

The app ships **two TensorFlow Lite models** in [ml/src/main/assets/](ml/src/main/assets/), executed on-device through [ExoplanetClassifier.kt](ml/src/main/java/com/app/exoplanethunter/ml/ExoplanetClassifier.kt). No data ever leaves the device.

### Models

| Model | Type | Output |
|-------|------|--------|
| `habitable_model.tflite` | Binary classifier | Single logit → sigmoid → habitability probability `[0, 1]`. Planets with score ≥ 0.62 are flagged **potentially habitable**. |
| `planet_type_model.tflite` | 6-class classifier | Softmax over `{Gas Giant, Neptune-like, Rocky, Sub-Neptune, Super-Earth, Unknown}` |

### Features

Each model takes the same **20-dimensional feature vector**, Z-score normalised using per-model hardcoded mean/scale arrays:

- **Base features (13)**: orbital period, semi-major axis, planet radius (Earth & Jupiter units), planet mass (Earth & Jupiter units), eccentricity, equilibrium temperature, insolation flux, stellar effective temperature, stellar radius, stellar mass, stellar surface gravity, stellar metallicity, distance to system
- **Derived features (7)**: radius-to-mass ratio, planet-to-star radius ratio, flux-to-temperature ratio, plus log-transforms of orbital period, mass, radius, and distance

### Outputs shown in the UI

For each planet the classifier returns a `HabitabilityInsight` containing:

- **Overall habitability score** (from `habitable_model`)
- **Planet classification** (from `planet_type_model`)
- **Five categorical sub-scores**: Habitability, Temperature Zone, Size Compatibility, Atmospheric Potential, Stellar Stability — derived from the raw features and model outputs
- **Insight bullets**: human-readable explanations surfaced on the planet detail screen

### Training

Both `.tflite` models were trained offline on a dataset derived from the NASA Exoplanet Archive, with the Kepler/K2 *Confirmed Planets* and *Planetary Systems Composite Data* tables as the primary sources of labels. Feature engineering (the 7 derived features above) matches the runtime normalisation arrays exactly so the training and inference pipelines stay in sync. Training notebooks are not part of this repository.

---

## Building

```bash
# Debug (all ABIs, for emulator + device testing)
./gradlew assembleDebug

# Release AAB (arm64-v8a + armeabi-v7a only — passes Play Store 16 KB page-size check)
./gradlew clean bundleRelease
```

### Ad configuration

Ads are gated by `local.properties` entries (kept out of source control):

```properties
ADS_ENABLED=true
ADMOB_APP_ID=ca-app-pub-xxxxx~yyyyy
ADMOB_AD_UNIT_ID=ca-app-pub-xxxxx/zzzzz
```

When `ADS_ENABLED=false` (the default), no ad SDK calls are made.

---

## License & Attribution

Planetary data © NASA Exoplanet Archive (public domain, with the [standard acknowledgement](https://exoplanetarchive.ipac.caltech.edu/docs/acknowledge.html)).
