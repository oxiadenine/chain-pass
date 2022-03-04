package io.sunland.chainpass.common

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

actual class Settings actual constructor(actual val dirPath: String) {
    actual fun save(data: Map<String, String>, fileName: String) {
        if (!File(dirPath).exists()) {
            File(dirPath).mkdir()
        }

        val file = File("$dirPath/$fileName.json")

        file.writeText(Json.encodeToString(data))
    }

    actual fun load(fileName: String): Map<String, String>? {
        return if (File("$dirPath/$fileName.json").exists()) {
            Json.decodeFromString(File("$dirPath/$fileName.json").readText())
        } else null
    }

    actual fun delete(fileName: String) {
        val file = File("$dirPath/$fileName.json")

        if (file.exists()) {
            file.delete()
        }
    }
}
