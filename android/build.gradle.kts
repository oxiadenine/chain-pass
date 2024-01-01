plugins {
    kotlin("multiplatform")
    id("com.android.application")
    id("org.jetbrains.compose")
}

kotlin {
    jvmToolchain(20)

    androidTarget()

    sourceSets {
        named("androidMain") {
            val exposedVersion = properties["exposed.version"] as String
            val h2databaseVersion = properties["h2database.version"] as String

            dependencies {
                implementation(project(":common"))

                implementation("org.jetbrains.exposed:exposed-jdbc:$exposedVersion")
                implementation("com.h2database:h2:$h2databaseVersion")
            }
        }
        named("androidUnitTest") {
            dependencies {
                implementation(kotlin("test"))
            }
        }
    }
}

android {
    namespace = "${project.group}.chainpass"
    compileSdk = 34

    defaultConfig {
        minSdk = 26
        targetSdk = 34

        applicationId = "${project.group}.chainpass"
        versionCode = (project.version as String).replace(".", "").toInt()
        versionName = project.version as String

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        setProperty("archivesBaseName", "${rootProject.name}-${project.name}-${project.version}")
    }

    signingConfigs {
        register("release") {
            keyAlias = "${rootProject.name}-${project.name}-key"
            keyPassword = System.getenv("SIGNING_KEY_PASSWORD")
            storeFile = file("${project.projectDir}/keystore.jks")
            storePassword = System.getenv("SIGNING_STORE_PASSWORD")
        }
    }

    buildFeatures {
        buildConfig = true
    }

    buildTypes {
        named("debug") {
            isDebuggable = true
        }
        named("release") {
            signingConfig = signingConfigs["release"]
        }
    }
}
