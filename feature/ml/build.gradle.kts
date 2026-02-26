plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.workspace.exoplanethunter.ml"
    compileSdk = 34

    defaultConfig {
        minSdk = 29
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }

    aaptOptions {
        noCompress("tflite")
    }
}

dependencies {
    implementation(project(":feature:exoplanet"))
    implementation(libs.androidx.core.ktx)
    implementation(libs.tensorflow.lite)
    implementation(libs.tensorflow.lite.support)
    implementation(libs.kotlinx.coroutines.android)
}
