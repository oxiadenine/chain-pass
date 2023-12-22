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
            dependencies {
                api(compose.runtime)
                api(compose.foundation)
                api(compose.material3)
                api(compose.materialIconsExtended)

                implementation(kotlinxDependency("coroutines-core"))
                implementation(kotlinxDependency("serialization-json"))

                implementation(ktorDependency("client-cio"))
                implementation(ktorDependency("client-websockets"))
                implementation(ktorDependency("server-cio"))
                implementation(ktorDependency("server-websockets"))

                implementation(rsocketDependency("transport-ktor-websocket-client"))
                implementation(rsocketDependency("transport-ktor-websocket-server"))

                implementation(exposedDependency("core"))
            }
        }
        commonTest {
            dependencies {
                implementation(kotlin("test"))
            }
        }

        named("androidMain") {
            dependencies {
                api(androidxDependency("core-ktx"))
                api(androidxDependency("appcompat"))
                api(androidxDependency("activity-compose"))
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
            dependencies {
                implementation(kotlin("test"))
                implementation(junitDependency("jupiter-api"))
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