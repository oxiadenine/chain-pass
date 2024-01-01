package io.sunland.chainpass.common

import kotlinx.serialization.Serializable

expect class SettingsManager(dirPath: String) {
    val dirPath: String

    fun save(settings: Settings)
    fun load(): Settings?
    fun delete()
}

@Serializable
data class Settings(val serverHost: String = "", val serverPort: Int = 0, val isServerConnected: Boolean = false)