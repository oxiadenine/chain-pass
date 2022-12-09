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

                api(ktorDependency("client-core"))
                api(ktorDependency("client-websockets"))
                api(ktorDependency("client-cio"))
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

                api(ktorDependency("client-core"))
                api(ktorDependency("client-websockets"))
                api(ktorDependency("client-cio"))
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