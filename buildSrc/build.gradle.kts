import java.util.*

plugins {
    `kotlin-dsl`
}

repositories {
    gradlePluginPortal()
    google()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
}

val props = Properties().apply {
    file("../gradle.properties").inputStream().use { fileStream -> load(fileStream) }
}

fun version(target: String) = props.getProperty("${target}.version")

dependencies {
    implementation(kotlin("gradle-plugin", version("kotlin")))
    implementation(kotlin("serialization", version("kotlin")))
    implementation("org.jetbrains.compose:compose-gradle-plugin:${version("compose")}")
    implementation("com.android.tools.build:gradle:${version("android")}")
    implementation("com.squareup.sqldelight:gradle-plugin:${version("sqldelight")}")
}