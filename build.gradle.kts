plugins {
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.compose) apply false
    alias(libs.plugins.android.application) apply false
}

allprojects {
    repositories {
        mavenCentral()
        google()
    }
}