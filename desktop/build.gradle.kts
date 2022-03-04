import org.jetbrains.compose.compose
import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("jvm")
    id("org.jetbrains.compose")
}

dependencies {
    implementation(project(":common"))

    implementation(compose.desktop.currentOs)

    testImplementation(kotlin("test", kotlinVersion()))
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

kotlin {
    target.compilations.all {
        kotlinOptions {
            jvmTarget = "11"
        }
    }
}

compose.desktop {
    application {
        mainClass = "${rootProject.group}.chainpass.${project.name}.MainKt"

        setProperty("archivesBaseName", "${rootProject.name}-${project.name}")

        nativeDistributions {
            targetFormats(TargetFormat.Deb, TargetFormat.Exe)

            packageName = "Chain Pass"
            packageVersion = rootProject.version as String
            description = "Save and manage passwords"
            vendor = rootProject.group as String

            val iconsDir = "${project.buildDir}/resources/main/icons"

            linux {
                packageName = "${rootProject.name}-${project.name}"

                iconFile.set(project.file("$iconsDir/icon_linux.png"))
            }

            windows {
                perUserInstall = true

                iconFile.set(project.file("$iconsDir/icon_windows.ico"))
            }
        }
    }
}
