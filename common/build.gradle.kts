plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
    kotlin("plugin.compose")
    id("com.android.library")
    id("org.jetbrains.compose")
}

kotlin {
    jvmToolchain(17)

    androidTarget()
    jvm("desktop") {
        testRuns["test"].executionTask.configure {
            useJUnitPlatform()
        }
    }

    sourceSets {
        all {
            languageSettings {
                optIn("org.jetbrains.compose.resources.ExperimentalResourceApi")
            }
        }

        commonMain {
            val kotlinxCoroutinesVersion = properties["kotlinx-coroutines.version"] as String
            val kotlinxSerializationVersion = properties["kotlinx-serialization.version"] as String
            val ktorVersion = properties["ktor.version"] as String
            val rsocketVersion = properties["rsocket.version"] as String
            val exposedVersion = properties["exposed.version"] as String

            dependencies {
                api(compose.runtime)
                api(compose.foundation)
                api(compose.material3)
                api(compose.materialIconsExtended)
                api(compose.components.resources)

                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlinxCoroutinesVersion")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$kotlinxSerializationVersion")
                implementation("io.ktor:ktor-client-cio:$ktorVersion")
                implementation("io.ktor:ktor-client-websockets:$ktorVersion")
                implementation("io.ktor:ktor-server-cio:$ktorVersion")
                implementation("io.ktor:ktor-server-websockets:$ktorVersion")
                implementation("io.rsocket.kotlin:rsocket-transport-ktor-websocket-client:$rsocketVersion")
                implementation("io.rsocket.kotlin:rsocket-transport-ktor-websocket-server:$rsocketVersion")
                implementation("org.jetbrains.exposed:exposed-core:$exposedVersion")
            }
        }
        commonTest {
            dependencies {
                implementation(kotlin("test"))
            }
        }

        androidMain {
            val androidxCoreVersion = properties["androidx-core.version"] as String
            val androidxAppcompatVersion = properties["androidx-appcompat.version"] as String
            val androidxActivityVersion = properties["androidx-activity.version"] as String

            dependencies {
                api("androidx.core:core-ktx:$androidxCoreVersion")
                api("androidx.appcompat:appcompat:$androidxAppcompatVersion")
                api("androidx.activity:activity-compose:$androidxActivityVersion")
            }
        }
        val androidUnitTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }

        val desktopMain by getting {
            dependencies {
                implementation(compose.desktop.common)
            }
        }
        val desktopTest by getting {
            val junitVersion = properties["junit.version"] as String

            dependencies {
                implementation(compose.desktop.currentOs)
                implementation(kotlin("test"))
                implementation("org.junit.jupiter:junit-jupiter-api:$junitVersion")
            }
        }
    }
}

android {
    namespace = "${project.group}.chainpass.${project.name}"
    compileSdk = 35

    defaultConfig {
        minSdk = 26
    }

    sourceSets["main"].apply {
        manifest.srcFile("src/androidMain/AndroidManifest.xml")
        res.srcDirs("src/androidMain/res")
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}