plugins {
    kotlin("multiplatform")
    id("com.android.library")
    id("org.jetbrains.compose")
    kotlin("plugin.serialization")
}

kotlin {
    jvmToolchain(20)

    androidTarget()
    jvm("desktop") {
        testRuns["test"].executionTask.configure {
            useJUnitPlatform()
        }
    }

    sourceSets {
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

        named("androidMain") {
            val androidxCoreVersion = properties["androidx-core.version"] as String
            val androidxAppcompatVersion = properties["androidx-appcompat.version"] as String
            val androidxActivityVersion = properties["androidx-activity.version"] as String

            dependencies {
                api("androidx.core:core-ktx:$androidxCoreVersion")
                api("androidx.appcompat:appcompat:$androidxAppcompatVersion")
                api("androidx.activity:activity-compose:$androidxActivityVersion")
            }
        }
        named("androidUnitTest") {
            dependencies {
                implementation(kotlin("test"))
            }
        }

        named("desktopMain") {
            dependencies {
                api(compose.desktop.common)
            }
        }
        named("desktopTest") {
            val junitVersion = properties["junit.version"] as String

            dependencies {
                implementation(kotlin("test"))

                implementation("org.junit.jupiter:junit-jupiter-api:$junitVersion")
            }
        }
    }
}

android {
    namespace = "${project.group}.chainpass.${project.name}"
    compileSdk = 34

    defaultConfig {
        minSdk = 26
    }

    sourceSets {
        named("main") {
            manifest.srcFile("src/androidMain/AndroidManifest.xml")
            res.srcDirs("src/androidMain/res")
            resources.srcDirs("src/commonMain/resources")
        }
    }
}