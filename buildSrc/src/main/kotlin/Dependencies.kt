import org.gradle.api.Project

fun Project.kotlinxDependency(name: String) =
    "org.jetbrains.kotlinx:kotlinx-$name:${kotlinxVersion(name.substringBefore("-"))}"

fun Project.ktorDependency(name: String) = "io.ktor:ktor-$name:${version("ktor")}"
fun Project.rsocketDependency(name: String) = "io.rsocket.kotlin:rsocket-$name:${version("rsocket")}"
fun Project.exposedDependency(name: String) = "org.jetbrains.exposed:exposed-$name:${version("exposed")}"
fun Project.h2databaseDependency() = "com.h2database:h2:${version("h2database")}"

fun Project.androidxDependency(name: String) =
    "androidx.${name.substringBefore("-")}:$name:${androidxVersion(name)}"

fun Project.junitDependency(name: String) = "org.junit.jupiter:junit-$name:${version("junit")}"