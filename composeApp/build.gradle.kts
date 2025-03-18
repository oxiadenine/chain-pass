import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.compose)
    alias(libs.plugins.android.application)
}

kotlin {
    jvmToolchain(JvmTarget.JVM_17.target.toInt())

    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }

    jvm("desktop") {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }

        testRuns["test"].executionTask.configure {
            useJUnitPlatform()
        }
    }

    sourceSets {
        all {
            languageSettings {
                optIn("androidx.compose.material3.ExperimentalMaterial3Api")
                optIn("org.jetbrains.compose.resources.ExperimentalResourceApi")
                optIn("androidx.compose.ui.ExperimentalComposeUiApi")
            }
        }

        commonMain {
            dependencies {
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material3)
                implementation(compose.materialIconsExtended)
                implementation(compose.components.resources)

                implementation(libs.kotlinx.coroutines.core)
                implementation(libs.kotlinx.serialization.json)
                implementation(libs.ktor.network)
                implementation(libs.exposed.core)

                implementation(libs.bundles.compose.multiplatform)
                implementation(libs.bundles.androidx.multiplatform)
            }
        }
        commonTest {
            dependencies {
                implementation(kotlin("test"))
            }
        }

        androidMain {
            dependencies {
                implementation(libs.kotlinx.coroutines.android)
                implementation(libs.exposed.jdbc)
                implementation(libs.h2)

                implementation(libs.bundles.androidx.android)
            }
        }
        val androidUnitTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }

        val desktopMain by getting {
            dependencies {
                implementation(compose.desktop.currentOs)

                implementation(libs.kotlinx.coroutines.swing)
                implementation(libs.exposed.jdbc)
                implementation(libs.h2)
            }
        }
        val desktopTest by getting {
            dependencies {
                implementation(kotlin("test"))

                implementation(compose.desktop.currentOs)

                implementation(libs.junit.jupiter.api)
            }
        }
    }
}

android {
    namespace = "${project.group}.chainpass"
    compileSdk = 35

    defaultConfig {
        minSdk = 26
        targetSdk = 35
        applicationId = "${project.group}.chainpass"
        versionCode = (project.version as String).replace(".", "").toInt()
        versionName = project.version as String
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        setProperty("archivesBaseName", rootProject.name)
    }

    sourceSets["main"].apply {
        manifest.srcFile("src/androidMain/AndroidManifest.xml")
        res.srcDirs("src/androidMain/res")
        resources.srcDirs("src/commonMain/resources")
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    signingConfigs {
        register("release") {
            keyAlias = "${rootProject.name}-android-key"
            keyPassword = System.getenv("SIGNING_KEY_PASSWORD")
            storeFile = file("${project.projectDir}/src/androidMain/keystore.jks")
            storePassword = System.getenv("SIGNING_STORE_PASSWORD")
        }
    }

    buildFeatures {
        buildConfig = true
    }

    buildTypes {
        debug {
            isDebuggable = true
        }
        release {
            isMinifyEnabled = false
            signingConfig = signingConfigs["release"]
        }
    }
}

compose.desktop {
    application {
        javaHome = System.getenv("JAVA_HOME") ?: ""
        mainClass = "${project.group}.chainpass.MainKt"

        setProperty("archivesBaseName", rootProject.name)

        nativeDistributions {
            targetFormats(TargetFormat.Deb, TargetFormat.Msi)

            packageName = "Chain Pass"
            packageVersion = project.version as String

            val resourcesDir = project.file("src/desktopMain/resources")

            linux {
                packageName = rootProject.name
                menuGroup = "Security"
                appCategory = "Security"

                iconFile.set(resourcesDir.resolve("icon.png"))
            }

            windows {
                menuGroup = "Security"
                perUserInstall = true
                menu = true
                shortcut = true

                iconFile.set(resourcesDir.resolve("icon.ico"))
            }

            modules("java.naming", "java.sql")
        }
    }
}