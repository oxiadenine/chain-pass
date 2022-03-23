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
        sourceCompatibility(JavaVersion.VERSION_11)
        targetCompatibility(JavaVersion.VERSION_11)
    }

    kotlinOptions {
        jvmTarget = "11"
    }

    signingConfigs {
        register("release") {
            keyAlias = "${rootProject.name}-${project.name}-key"
            keyPassword = System.getenv("SIGNING_KEY_PASSWORD")
            storeFile = file("${project.projectDir}/keystore.jks")
            storePassword = System.getenv("SIGNING_STORE_PASSWORD")
        }
    }

    buildTypes {
        named("debug") {
            isDebuggable = true
        }
        named("release") {
            isMinifyEnabled = false
            signingConfig = signingConfigs["release"]
        }
    }
}
