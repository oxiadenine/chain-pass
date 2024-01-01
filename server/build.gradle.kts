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
    implementation(ktorDependency("server-websockets"))

    implementation(exposedDependency("core"))
    implementation(exposedDependency("dao"))
    implementation(exposedDependency("jdbc"))

    implementation(hikaricpDependency())
    implementation(h2Dependency())

    testImplementation(kotlin("test", kotlinVersion()))

    testImplementation(junitDependency("jupiter-api"))
    testRuntimeOnly(junitDependency("jupiter-engine"))

    testImplementation(ktorDependency("server-test-host"))
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

    tasks {
        jar {
            manifest {
                attributes["Main-Class"] = "${rootProject.group}.chainpass.${project.name}.MainKt"
            }

            archiveBaseName.set("${rootProject.name}-${project.name}")
            archiveVersion.set("")

            from(configurations["runtimeClasspath"].map { file: File ->
                if (file.isDirectory) file else zipTree(file)
            })

            duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        }

        test {
            useJUnitPlatform()
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

            packageName = "Chain Pass Server"
            packageVersion = rootProject.version as String
            vendor = "SunLand"

            val iconsDir = "${project.buildDir}/resources/main"

            linux {
                packageName = "${rootProject.name}-${project.name}"
                menuGroup = "Security"
                appCategory = "Security"

                iconFile.set(project.file("$iconsDir/icon.png"))
            }

            windows {
                menuGroup = "Security"
                perUserInstall = true
                menu = true
                shortcut = true

                iconFile.set(project.file("$iconsDir/icon.ico"))
            }

            modules("java.naming", "java.sql")
        }
    }
}