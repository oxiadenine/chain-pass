plugins {
    id("com.android.library")
    kotlin("multiplatform")
    id("org.jetbrains.compose")
    kotlin("plugin.serialization")
    id("com.squareup.sqldelight")
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
                api(compose.material)
                api(compose.materialIconsExtended)

                api(kotlinxDependency("coroutines-core"))
                api(kotlinxDependency("serialization-json"))

                api(ktorDependency("client-cio"))
                api(ktorDependency("client-websockets"))
                api(ktorDependency("server-cio"))
                api(ktorDependency("server-websockets"))

                api(rsocketDependency("transport-ktor-websocket-client"))
                api(rsocketDependency("transport-ktor-websocket-server"))
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

                api(sqldelightDependency("sqlite-driver"))
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
                
                api(sqldelightDependency("android-driver"))
            }
        }
        named("androidTest") {
            dependencies {
                implementation(kotlin("test"))
            }
        }
    }
}

android {
    compileSdk = 32

    defaultConfig {
        minSdk = 26
        targetSdk = 32
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    sourceSets {
        named("main") {
            manifest.srcFile("src/androidMain/AndroidManifest.xml")
            res.srcDirs("src/androidMain/res")
        }
    }
}

sqldelight {
    database("Database") {
        packageName = "io.sunland.chainpass.sqldelight"
    }
}