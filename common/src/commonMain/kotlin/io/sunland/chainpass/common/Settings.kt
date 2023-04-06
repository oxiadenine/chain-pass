package io.sunland.chainpass.common

import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

class SettingsManager(val dirPath: String) {
    private val filePath = "$dirPath/settings.json"

    fun save(settings: Settings) {
        if (!File(dirPath).exists()) {
            File(dirPath).mkdir()
        }

        val file = File(filePath)

        if (!file.exists()) {
            file.createNewFile()
        }

        file.writeText(Json.encodeToString(settings))
    }

    fun load(): Settings? {
        val file = File(filePath)

        return if (file.exists()) {
            Json.decodeFromString(file.readText())
        } else null
    }
}

@Serializable
data class Settings(
    val deviceAddress: String,
    val passwordLength: Int,
    val passwordIsAlphanumeric: Boolean,
    val language: String
)