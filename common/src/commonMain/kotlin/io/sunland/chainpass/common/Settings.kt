package io.sunland.chainpass.common

expect class SettingsFactory(dirPath: String) {
    val dirPath: String

    fun save(settings: Settings)
    fun load(settings: Settings): Settings?
    fun delete(settings: Settings)
}

interface Settings {
    val fileName: String

    fun toMap(): Map<String, String>
    fun fromMap(data: Map<String, String>): Settings
}