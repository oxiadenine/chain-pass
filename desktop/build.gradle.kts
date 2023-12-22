import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("multiplatform")
    id("org.jetbrains.compose")
}

kotlin {
    jvmToolchain(20)

    jvm {
        withJava()

        testRuns["test"].executionTask.configure {
            useJUnitPlatform()
        }
    }

    sourceSets {
        named("jvmMain") {
            dependencies {
                implementation(project(":common"))

                implementation(compose.desktop.currentOs)

                implementation(exposedDependency("jdbc"))
                implementation(h2databaseDependency())
            }
        }
        named("jvmTest") {
            dependencies {
                implementation(kotlin("test"))
                implementation(junitDependency("jupiter-api"))
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
            vendor = "SunLand"

            val resourcesDir = "${project.layout.buildDirectory}/processedResources/jvm/main"

            linux {
                packageName = "${rootProject.name}-${project.name}"
                menuGroup = "Security"
                appCategory = "Security"

                iconFile.set(project.file("$resourcesDir/icon.png"))
            }

            windows {
                menuGroup = "Security"
                perUserInstall = true
                menu = true
                shortcut = true

                iconFile.set(project.file("$resourcesDir/icon.ico"))
            }

            modules("java.naming", "java.sql")
        }
    }
}