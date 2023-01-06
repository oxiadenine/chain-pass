plugins {
    id("com.android.application")
    kotlin("android")
    id("org.jetbrains.compose")
}

dependencies {
    implementation(project(":common"))

    testImplementation(kotlin("test"))
}

android {
    compileSdk = 33

    defaultConfig {
        minSdk = 26
        targetSdk = 33

        applicationId = "${project.group}.chainpass.${project.name}"
        versionCode = 1
        versionName = project.version as String

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        setProperty("archivesBaseName", "${rootProject.name}-${project.name}-${project.version}")
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
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
            isMinifyEnabled = true
            isShrinkResources = true
            signingConfig = signingConfigs["release"]

            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"),"proguard-rules.pro")
        }
    }

    packagingOptions {
        resources.excludes.add("META-INF/INDEX.LIST")
    }
}