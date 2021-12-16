plugins {
    kotlin("jvm")
    application
}

dependencies {
    implementation(project(":common"))

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
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

application {
    applicationName = "${rootProject.name}-${project.name}"

    mainClass.set("${rootProject.group}.chainpass.${project.name}.MainKt")
}

tasks {
    named<Jar>("jar") {
        manifest {
            attributes["Main-Class"] = application.mainClass.get()
        }

        archiveBaseName.set("${rootProject.name}-${project.name}")
        archiveVersion.set("")

        from(configurations["runtimeClasspath"].map { file: File ->
            if (file.isDirectory) file else zipTree(file)
        })

        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    }

    named<CreateStartScripts>("startScripts") {
        applicationName = "${rootProject.name}-${project.name}"
    }

    withType<Test> {
        useJUnitPlatform()
    }
}
