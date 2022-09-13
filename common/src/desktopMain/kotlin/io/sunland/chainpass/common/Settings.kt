package io.sunland.chainpass.common

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.writeText

actual class SettingsManager actual constructor(actual val dirPath: String) {
    private val filePath = Path.of("$dirPath/settings.json")

    actual fun save(settings: Settings) {
        if (!Files.exists(Path.of(dirPath))) {
            Files.createDirectory(Path.of(dirPath))
        }

        if (!Files.exists(filePath)) {
            Files.createFile(filePath)
        }

        filePath.writeText(Json.encodeToString(settings))
    }

    actual fun load(settings: Settings): Settings? {
        return if (Files.exists(filePath)) {
            Json.decodeFromString(Files.readString(filePath))
        } else null
    }

    actual fun delete(settings: Settings) {
        Files.deleteIfExists(filePath)
    }
}