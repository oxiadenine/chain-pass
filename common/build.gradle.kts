plugins {
    id("com.android.library")
    kotlin("multiplatform")
    id("org.jetbrains.compose")
    kotlin("plugin.serialization")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(11))
}

kotlin {
    jvm("desktop") {
        compilations.all {
            kotlinOptions {
                jvmTarget = JavaVersion.VERSION_11.toString()
            }
        }
    }
    android()

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
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
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
    }
}

android {
    compileSdk = 33

    defaultConfig {
        minSdk = 26
        targetSdk = 33
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    sourceSets {
        named("main") {
            manifest.srcFile("src/androidMain/AndroidManifest.xml")
            res.srcDirs("src/androidMain/res")
            resources.setSrcDirs(listOf("src/androidMain/resources", "src/commonMain/resources"))
        }
    }
}