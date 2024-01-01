plugins {
    id("com.android.application")
    kotlin("android")
    id("org.jetbrains.compose")
}

dependencies {
    implementation(project(":common"))

    implementation(androidxDependency("core-ktx"))
    implementation(androidxDependency("appcompat"))
    implementation(androidxDependency("activity-compose"))

    testImplementation(kotlin("test", kotlinVersion()))
}

android {
    compileSdk = 31

    defaultConfig {
        minSdk = 24
        targetSdk = 31

        applicationId = "${rootProject.group}.chainpass.${project.name}"
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        setProperty("archivesBaseName", "${rootProject.name}-${project.name}")
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    dexOptions {
        javaMaxHeapSize = "4G"
    }

    buildTypes {
        named("debug") {
            isDebuggable = true
        }
        named("release") {
            isMinifyEnabled = false
        }
    }
}
