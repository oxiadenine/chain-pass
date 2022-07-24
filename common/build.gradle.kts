import org.jetbrains.compose.compose

plugins {
    id("com.android.library")
    kotlin("multiplatform")
    id("org.jetbrains.compose")
    kotlin("plugin.serialization")
}

kotlin {
    jvm("desktop") {
        compilations.all {
            kotlinOptions {
                jvmTarget = "11"
            }
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
                api(compose.materialIconsExtended)

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

        named("androidMain") {
            dependencies {
                api(androidxDependency("core-ktx"))
                api(androidxDependency("appcompat"))
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
    compileSdk = 31

    defaultConfig {
        minSdk = 24
        targetSdk = 31
    }

    compileOptions {
        sourceCompatibility(JavaVersion.VERSION_11)
        targetCompatibility(JavaVersion.VERSION_11)
    }

    sourceSets {
        named("main") {
            manifest.srcFile("src/androidMain/AndroidManifest.xml")
            res.srcDirs("src/androidMain/res")
        }
    }
}