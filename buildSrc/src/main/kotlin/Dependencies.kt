import org.gradle.api.Project

fun Project.kotlinxDependency(name: String) =
    "org.jetbrains.kotlinx:kotlinx-$name:${kotlinxVersion(name.substringBefore("-"))}"

fun Project.ktorDependency(name: String) = "io.ktor:ktor-$name:${version("ktor")}"
fun Project.exposedDependency(name: String) = "org.jetbrains.exposed:exposed-$name:${version("exposed")}"
fun Project.typesafeDependency(name: String) = "com.typesafe:$name:${version("typesafe-$name")}"
fun Project.logbackDependency(name: String) = "ch.qos.logback:logback-$name:${version("logback-$name")}"

fun Project.hikaricpDependency() = "com.zaxxer:HikariCP:${version("hikaricp")}"
fun Project.h2Dependency() = "com.h2database:h2:${version("h2")}"

fun Project.androidxDependency(name: String) =
    "androidx.${name.substringBefore("-")}:$name:${androidxVersion(name)}"

fun Project.junitDependency(name: String) = "org.junit.jupiter:junit-$name:${version("junit")}"
