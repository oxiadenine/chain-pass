import org.jetbrains.compose.compose

plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
    id("com.android.library")
    id("org.jetbrains.compose")
}

kotlin {
    jvm("desktop") {
        testRuns["test"].executionTask {
            useJUnitPlatform()
        }
    }
    android()

    sourceSets {
        commonMain {
            dependencies {
                api(kotlinxDependency("coroutines-core"))
                api(kotlinxDependency("serialization-json"))

                api(compose.runtime)
                api(compose.foundation)
                api(compose.material)

                api(ktorDependency("client-core"))
                api(ktorDependency("client-websockets"))
                api(ktorDependency("client-cio"))
                api(ktorDependency("client-logging"))

                api(typesafeDependency("config"))
                api(logbackDependency("classic"))
            }
        }

        commonTest {
            dependencies {
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
            }
        }

        named("desktopMain")
        named("desktopTest") {
            dependencies {
                implementation(kotlin("test"))
            }
        }

        named("androidMain")
        named("androidTest") {
            dependencies {
                implementation(kotlin("test"))
            }
        }
    }
}

android {
    compileSdk = 31

    defaultConfig {
        minSdk = 24
        targetSdk = 31
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
}
