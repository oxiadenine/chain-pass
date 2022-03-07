package io.sunland.chainpass.common

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

actual class SettingsFactory actual constructor(actual val dirPath: String) {
    actual fun save(settings: Settings) {
        if (!File(dirPath).exists()) {
            File(dirPath).mkdir()
        }

        val file = File("$dirPath/${settings.fileName}.json")

        file.writeText(Json.encodeToString(settings.toMap()))
    }

    actual fun load(settings: Settings): Settings? {
        return if (File("$dirPath/${settings.fileName}.json").exists()) {
            settings.fromMap(Json.decodeFromString(File("$dirPath/${settings.fileName}.json").readText()))
        } else null
    }

    actual fun delete(settings: Settings) {
        val file = File("$dirPath/${settings.fileName}.json")

        if (file.exists()) {
            file.delete()
        }
    }
}
