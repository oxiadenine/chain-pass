import org.gradle.api.Project

fun Project.kotlinVersion() = version("kotlin")

fun Project.kotlinxVersion(name: String) = version("kotlinx-$name")
fun Project.androidxVersion(name: String) = version("androidx-${name.substringBefore("-")}")

fun Project.version(target: String) = property("${target}.version") as String