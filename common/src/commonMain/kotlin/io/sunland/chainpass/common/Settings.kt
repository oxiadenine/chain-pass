package io.sunland.chainpass.common

expect class Settings(dirPath: String) {
    val dirPath: String

    fun save(data: Map<String, String>, fileName: String)
    fun load(fileName: String): Map<String, String>?
    fun delete(fileName: String)
}
