package io.sunland.chainpass.common

import kotlinx.serialization.Serializable

expect class SettingsManager(dirPath: String) {
    val dirPath: String

    fun save(settings: Settings)
    fun load(): Settings?
    fun delete()
}

@Serializable
data class Settings(val hostAddress: String, val deviceAddress: String, val passwordLength: Int, val passwordIsAlphanumeric: Boolean)