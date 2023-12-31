buildscript {
    repositories {
        google()
    }

    dependencies {
        classpath("com.diffplug.spotless:spotless-plugin-gradle:6.19.0")
    }
}


// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.android.application") version "8.1.0-rc01" apply false
    id("org.jetbrains.kotlin.android") version "1.8.10" apply false
    id("com.diffplug.spotless") version "6.9.0" apply false
}

subprojects {
    afterEvaluate {
        project.apply("../spotless.gradle")
    }
}