import org.jetbrains.compose.compose
import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("jvm")
    id("org.jetbrains.compose")
}

dependencies {
    implementation(project(":common"))

    implementation(compose.desktop.currentOs)

    implementation(ktorDependency("server-netty"))
    implementation(ktorDependency("websockets"))

    implementation(exposedDependency("core"))
    implementation(exposedDependency("dao"))
    implementation(exposedDependency("jdbc"))

    implementation(hikaricpDependency())
    implementation(h2Dependency())

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

        args += System.getenv("APP_ENV")?.let { env -> listOf(env) } ?: listOf()

        nativeDistributions {
            targetFormats(TargetFormat.Deb, TargetFormat.Exe)

            packageName = "Chain Pass Service"
            packageVersion = rootProject.version as String
            description = "Chain Pass Service"
            vendor = "SunLand"

            val iconsDir = "${project.buildDir}/resources/main"

            linux {
                packageName = "${rootProject.name}-${project.name}"

                iconFile.set(project.file("$iconsDir/icon.png"))
            }

            windows {
                perUserInstall = true
                menu = true

                iconFile.set(project.file("$iconsDir/icon.ico"))
            }

            modules("java.naming", "java.sql")
        }
    }
}
