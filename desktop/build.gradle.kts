import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("multiplatform")
    kotlin("plugin.compose")
    id("org.jetbrains.compose")
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
            val exposedVersion = properties["exposed.version"] as String
            val h2databaseVersion = properties["h2database.version"] as String

            dependencies {
                implementation(project(":common"))
                implementation(compose.desktop.currentOs)
                implementation("org.jetbrains.exposed:exposed-jdbc:$exposedVersion")
                implementation("com.h2database:h2:$h2databaseVersion")
            }
        }
        jvmTest {
            val junitVersion = properties["junit.version"] as String

            dependencies {
                implementation(kotlin("test"))
                implementation("org.junit.jupiter:junit-jupiter-api:$junitVersion")
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