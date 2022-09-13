package io.sunland.chainpass.common

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

enum class StorageType { JSON, CSV, TXT }

expect class Storage(dirPath: String, type: StorageType) {
    val dirPath: String
    val type: StorageType

    fun store(storable: Storable): String
}

typealias Storable = Map<Map<String, String>, List<Map<String, String>>>

fun Storable.toString(storageType: StorageType) = when (storageType) {
    StorageType.JSON -> JsonArray(this.values.flatMap { value ->
        value.map {
            JsonObject(it.map { entry ->
                entry.key to JsonPrimitive(entry.value)
            }.toMap())
        }
    }).toString()
    StorageType.CSV -> buildString {
        val data = this@toString.values.flatten()

        append("${data.flatMap { it.keys }.toSet().joinToString(",")}\n")

        data.forEach { append("${it.values.joinToString(",")}\n") }
    }
    StorageType.TXT -> buildString { append(this@toString.values.flatten()) }
}