package io.sunland.chainpass.common

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

actual class SettingsManager actual constructor(actual val dirPath: String) {
    private val filePath = "$dirPath/settings.json"

    actual fun save(settings: Settings) {
        if (!File(dirPath).exists()) {
            File(dirPath).mkdir()
        }

        val file = File(filePath)

        if (!file.exists()) {
            file.createNewFile()
        }

        file.writeText(Json.encodeToString(settings))
    }

    actual fun load(): Settings? {
        val file = File(filePath)

        return if (file.exists()) {
            Json.decodeFromString(file.readText())
        } else null
    }

    actual fun delete() {
        val file = File(filePath)

        if (file.exists()) {
            file.delete()
        }
    }
}