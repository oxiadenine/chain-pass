package io.github.oxiadenine.chainpass

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import java.io.File

class Settings(dataDirPath: String) {
    private val settingsFile = File("$dataDirPath/settings.json")

    init {
        if (!settingsFile.exists()) {
            settingsFile.createNewFile()
        }
    }

    fun load() = settingsFile.readText().let { json ->
        if (json.isNotEmpty()) {
            Json.decodeFromString<JsonObject>(json)
        } else null
    }

    fun save(settings: JsonObject) {
        settingsFile.writeText(Json.encodeToString(settings))
    }
}