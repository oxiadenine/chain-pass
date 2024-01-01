group = "io.sunland"
version = "1.0.0"

allprojects {
    repositories {
        mavenCentral()
        google()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }
}

tasks.wrapper {
    gradleVersion = "7.2"
}
