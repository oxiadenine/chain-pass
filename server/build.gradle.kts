import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("multiplatform")
    id("org.jetbrains.compose")
}

kotlin {
    jvm {
        withJava()

        compilations.all {
            kotlinOptions {
                jvmTarget = JavaVersion.VERSION_11.toString()
            }
        }
        testRuns["test"].executionTask.configure {
            useJUnitPlatform()
        }
    }

    sourceSets {
        named("jvmMain") {
            dependencies {
                implementation(project(":common"))

                implementation(compose.desktop.currentOs)

                implementation(ktorDependency("server-cio"))
                implementation(ktorDependency("server-websockets"))

                implementation(typesafeDependency("config"))
                implementation(logbackDependency("classic"))

                implementation(exposedDependency("core"))
                implementation(exposedDependency("dao"))
                implementation(exposedDependency("jdbc"))

                implementation(hikaricpDependency())
                implementation(h2Dependency())
            }
        }
        named("jvmTest") {
            dependencies {
                implementation(kotlin("test"))

                implementation(junitDependency("jupiter-api"))
                runtimeOnly(junitDependency("jupiter-engine"))

                implementation(ktorDependency("server-test-host"))
            }
        }
    }
}

tasks {
    register<Jar>("fatJar") {
        manifest {
            attributes["Main-Class"] = "${rootProject.group}.chainpass.${project.name}.MainKt"
        }

        archiveBaseName.set("${rootProject.name}-${project.name}")
        archiveVersion.set("")

        from(configurations["runtimeClasspath"].map { file: File ->
            if (file.isDirectory) file else zipTree(file)
        })
        with(getByName<Jar>("jvmJar"))

        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    }
}

compose.desktop {
    application {
        javaHome = System.getenv("JAVA_HOME") ?: ""

        mainClass = "${rootProject.group}.chainpass.${project.name}.MainKt"

        setProperty("archivesBaseName", "${rootProject.name}-${project.name}")

        args += System.getenv("JAVA_ENV")?.let { env -> listOf(env) } ?: listOf()

        nativeDistributions {
            targetFormats(TargetFormat.Deb, TargetFormat.Exe)

            packageName = "Chain Pass Server"
            packageVersion = rootProject.version as String
            vendor = "SunLand"

            val resourcesDir = "${project.buildDir}/processedResources/jvm/main"

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