import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.compose)
}

kotlin {
    jvmToolchain(17)

    jvm {
        testRuns["test"].executionTask.configure {
            useJUnitPlatform()
        }
    }

    sourceSets {
        jvmMain {
            dependencies {
                implementation(project(":common"))

                implementation(compose.desktop.currentOs)
                implementation(libs.exposed.jdbc)
                implementation(libs.h2)
            }
        }
        jvmTest {
            dependencies {
                implementation(kotlin("test"))

                implementation(libs.junit.jupiter.api)
            }
        }
    }
}

compose.desktop {
    application {
        javaHome = System.getenv("JAVA_HOME") ?: ""
        mainClass = "${rootProject.group}.chainpass.MainKt"

        setProperty("archivesBaseName", "${rootProject.name}-${project.name}")

        nativeDistributions {
            targetFormats(TargetFormat.Deb, TargetFormat.Exe)

            packageName = "Chain Pass"
            packageVersion = rootProject.version as String

            val resourcesDir = project.file("src/jvmMain/resources")

            linux {
                packageName = "${rootProject.name}-${project.name}"
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