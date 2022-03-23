package io.sunland.chainpass.common

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.writeText

actual class SettingsFactory actual constructor(actual val dirPath: String) {
    actual fun save(settings: Settings) {
        if (!Files.exists(Path.of(dirPath))) {
            Files.createDirectory(Path.of(dirPath))
        }

        val file = Files.createFile(Path.of("$dirPath/${settings.fileName}.json"))

        file.writeText(Json.encodeToString(settings.toMap()))
    }

    actual fun load(settings: Settings): Settings? {
        return if (Files.exists(Path.of("$dirPath/${settings.fileName}.json"))) {
            settings.fromMap(Json.decodeFromString(Files.readString(Path.of("$dirPath/${settings.fileName}.json"))))
        } else null
    }

    actual fun delete(settings: Settings) {
        Files.deleteIfExists(Path.of("$dirPath/${settings.fileName}.json"))
    }
}
