package io.sunland.chainpass.common

import kotlinx.serialization.Serializable

expect class SettingsManager(dirPath: String) {
    val dirPath: String

    fun save(settings: Settings)
    fun load(settings: Settings): Settings?
    fun delete(settings: Settings)
}

@Serializable
data class Settings(val serverHost: String = "", val serverPort: Int = 0)