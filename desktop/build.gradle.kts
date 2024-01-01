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

compose.desktop {
    application {
        javaHome = System.getenv("JAVA_HOME")

        mainClass = "${rootProject.group}.chainpass.${project.name}.MainKt"

        setProperty("archivesBaseName", "${rootProject.name}-${project.name}")

        args += System.getenv("APP_ENV")?.let { appEnv -> listOf(appEnv) } ?: listOf()

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
