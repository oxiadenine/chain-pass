package io.sunland.chainpass.common

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.writeText

actual class Settings actual constructor(actual val dirPath: String) {
    actual fun save(data: Map<String, String>, fileName: String) {
        if (!Files.exists(Path.of(dirPath))) {
            Files.createDirectory(Path.of(dirPath))
        }

        val file = Files.createFile(Path.of("$dirPath/$fileName.json"))

        file.writeText(Json.encodeToString(data))
    }

    actual fun load(fileName: String): Map<String, String>? {
        return if (Files.exists(Path.of("$dirPath/$fileName.json"))) {
            Json.decodeFromString(Files.readString(Path.of("$dirPath/$fileName.json")))
        } else null
    }

    actual fun delete(fileName: String) {
        Files.deleteIfExists(Path.of("$dirPath/$fileName.json"))
    }
}
